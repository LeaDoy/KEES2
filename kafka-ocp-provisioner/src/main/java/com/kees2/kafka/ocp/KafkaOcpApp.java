package com.kees2.kafka.ocp;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Provisions Confluent for Kubernetes (CFK) resources on OpenShift / Kubernetes:
 * a {@code KRaftController} and a KRaft-backed {@code Kafka} cluster ({@code platform.confluent.io/v1beta1}).
 *
 * <p>Requires the Confluent for Kubernetes operator and CRDs. Align image tags with your CFK
 * version and Confluent Platform release.</p>
 */
@Command(
    name = "kafka-ocp-provisioner",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Create or replace a KRaft Kafka cluster using Confluent for Kubernetes (KRaftController + Kafka)."
)
public class KafkaOcpApp implements Callable<Integer> {

  private static final String CFK_API = "platform.confluent.io/v1beta1";

  @Option(names = "--namespace", required = true, description = "Target namespace (must exist).")
  String namespace;

  @Option(
      names = "--cluster-name",
      defaultValue = "kafka",
      description = "Name of the Kafka custom resource."
  )
  String clusterName;

  @Option(
      names = "--kraft-controller-name",
      description = "Name of the KRaftController CR. Default: <cluster-name>-kraft"
  )
  String kraftControllerName;

  @Option(
      names = "--size",
      required = true,
      description = "Kafka broker replicas (spec.replicas on the Kafka CR)."
  )
  int size;

  @Option(
      names = "--cpu",
      required = true,
      description = "CPU requests/limits per Kafka broker (Kubernetes quantity), e.g. 1, 500m, 2."
  )
  String cpu;

  @Option(
      names = "--memory",
      defaultValue = "4Gi",
      description = "Memory requests/limits per Kafka broker."
  )
  String memory;

  @Option(
      names = "--controller-replicas",
      description = "KRaft controller replicas (odd; CFK recommends ≥ 3). Default: 3."
  )
  Integer controllerReplicas;

  @Option(
      names = "--controller-cpu",
      defaultValue = "500m",
      description = "CPU requests/limits per KRaft controller pod."
  )
  String controllerCpu;

  @Option(
      names = "--controller-memory",
      defaultValue = "2Gi",
      description = "Memory requests/limits per KRaft controller pod."
  )
  String controllerMemory;

  @Option(
      names = "--cp-tag",
      defaultValue = "7.8.0",
      description = "Confluent Platform image tag for cp-server (e.g. 7.8.0)."
  )
  String cpTag;

  @Option(
      names = "--init-tag",
      defaultValue = "2.9.0",
      description = "confluent-init-container image tag (match your CFK / operator bundle)."
  )
  String initTag;

  @Option(
      names = "--image-registry",
      defaultValue = "docker.io",
      description = "Registry prefix for cp-server (e.g. docker.io or your mirror)."
  )
  String imageRegistry;

  @Option(
      names = "--cp-server-image",
      description = "Full cp-server image (overrides registry + cp-tag), e.g. docker.io/confluentinc/cp-server:7.8.0"
  )
  String cpServerImage;

  @Option(
      names = "--init-image",
      description = "Full init container image (overrides init-tag), e.g. confluentinc/confluent-init-container:2.9.0"
  )
  String initImage;

  @Option(
      names = "--kafka-data-volume",
      defaultValue = "10Gi",
      description = "Kafka data volume capacity (Kafka spec.dataVolumeCapacity)."
  )
  String kafkaDataVolume;

  @Option(
      names = "--controller-data-volume",
      defaultValue = "10Gi",
      description = "KRaft controller metadata volume capacity."
  )
  String controllerDataVolume;

  @Option(
      names = "--openshift-pod-security",
      defaultValue = "true",
      description = "Apply podSecurityContext (fsGroup/runAsUser) typical for OpenShift non-root SCC."
  )
  boolean openshiftPodSecurity;

  @Option(
      names = "--kubeconfig",
      description = "Path to kubeconfig. If omitted, uses KUBECONFIG env or ~/.kube/config."
  )
  String kubeconfig;

  @Option(
      names = "--master-url",
      description = "API server URL (optional; overrides kubeconfig cluster)."
  )
  String masterUrl;

  @Option(
      names = "--token",
      description = "Bearer token (optional; e.g. from oc whoami -t)."
  )
  String token;

  @Override
  public Integer call() {
    try {
      validateInputs();
      String kraftName = resolveKraftControllerName();
      String appImage = resolveCpServerImage();
      String initImg = resolveInitImage();

      Config config = buildConfig();
      try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
        GenericKubernetesResource kraft = buildKRaftController(kraftName, appImage, initImg);
        client.resource(kraft).inNamespace(namespace).createOrReplace();

        GenericKubernetesResource kafka = buildKafka(kraftName, appImage, initImg);
        client.resource(kafka).inNamespace(namespace).createOrReplace();

        String kafkaCrName = k8sName(clusterName);
        System.out.printf(
            "Confluent for Kubernetes: KRaftController '%s' + Kafka '%s' applied in '%s' (%d brokers, broker cpu=%s).%n",
            kraftName,
            kafkaCrName,
            namespace,
            size,
            cpu
        );
        System.out.println("Check: oc get kraftcontrollers,kafka,pods -n " + namespace);
      }
      return 0;
    } catch (IllegalArgumentException e) {
      System.err.println("Invalid input: " + e.getMessage());
      return 2;
    } catch (KubernetesClientException e) {
      System.err.println("Kubernetes API error: " + e.getMessage());
      if (e.getCode() == 403) {
        System.err.println("Hint: ensure you can create platform.confluent.io resources in this namespace.");
      }
      if (e.getCode() == 404) {
        System.err.println("Hint: install Confluent for Kubernetes operator and CRDs.");
      }
      return 1;
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
      e.printStackTrace(System.err);
      return 1;
    }
  }

  private String resolveKraftControllerName() {
    if (kraftControllerName != null && !kraftControllerName.isBlank()) {
      return k8sName(kraftControllerName.trim());
    }
    return k8sName(clusterName + "-kraft");
  }

  private static final int K8S_NAME_MAX = 63;

  private static String k8sName(String s) {
    String d =
        s.toLowerCase().replaceAll("[^a-z0-9-]+", "-").replaceAll("^-+", "").replaceAll("-+$", "");
    if (d.isEmpty()) {
      d = "kraft";
    }
    return d.length() <= K8S_NAME_MAX ? d : d.substring(0, K8S_NAME_MAX);
  }

  private void validateInputs() {
    if (size < 1) {
      throw new IllegalArgumentException("--size must be >= 1");
    }
    if (cpu == null || cpu.isBlank()) {
      throw new IllegalArgumentException("--cpu must not be empty");
    }
    if (memory == null || memory.isBlank()) {
      throw new IllegalArgumentException("--memory must not be empty");
    }
    int cr = controllerReplicas != null ? controllerReplicas : 3;
    if (cr < 1) {
      throw new IllegalArgumentException("--controller-replicas must be >= 1");
    }
    if (cr > 1 && cr % 2 == 0) {
      throw new IllegalArgumentException("--controller-replicas must be odd when > 1");
    }
  }

  private int controllerReplicaCount() {
    return controllerReplicas != null ? controllerReplicas : 3;
  }

  private String resolveCpServerImage() {
    if (cpServerImage != null && !cpServerImage.isBlank()) {
      return cpServerImage.trim();
    }
    String reg = imageRegistry.endsWith("/") ? imageRegistry.substring(0, imageRegistry.length() - 1) : imageRegistry;
    return reg + "/confluentinc/cp-server:" + cpTag;
  }

  private String resolveInitImage() {
    if (initImage != null && !initImage.isBlank()) {
      return initImage.trim();
    }
    return "confluentinc/confluent-init-container:" + initTag;
  }

  private Config buildConfig() {
    ConfigBuilder b = new ConfigBuilder();
    if (kubeconfig != null && !kubeconfig.isBlank()) {
      b.withFileKubeconfig(kubeconfig);
    }
    if (masterUrl != null && !masterUrl.isBlank()) {
      b.withMasterUrl(masterUrl);
    }
    if (token != null && !token.isBlank()) {
      b.withOauthToken(token);
    }
    return b.build();
  }

  private Map<String, Object> imageBlock(String application, String init) {
    Map<String, Object> img = new LinkedHashMap<>();
    img.put("application", application);
    img.put("init", init);
    return img;
  }

  private Map<String, Object> podTemplateWithResources(String cpuQty, String memQty) {
    Map<String, Object> pod = new LinkedHashMap<>();
    pod.put("resources", resourceMap(cpuQty, memQty));
    if (openshiftPodSecurity) {
      Map<String, Object> psc = new LinkedHashMap<>();
      psc.put("fsGroup", 1000);
      psc.put("runAsUser", 1000);
      psc.put("runAsNonRoot", true);
      pod.put("podSecurityContext", psc);
    }
    return pod;
  }

  /**
   * KRaftController — metadata quorum for CFK Kafka.
   */
  private GenericKubernetesResource buildKRaftController(String kraftName, String appImage, String initImg) {
    Map<String, Object> spec = new LinkedHashMap<>();
    spec.put("replicas", controllerReplicaCount());
    spec.put("dataVolumeCapacity", controllerDataVolume);
    spec.put("image", imageBlock(appImage, initImg));
    spec.put("podTemplate", podTemplateWithResources(controllerCpu, controllerMemory));

    Map<String, Object> configOverrides = new LinkedHashMap<>();
    configOverrides.put("server", List.of("default.replication.factor=" + size));
    spec.put("configOverrides", configOverrides);

    GenericKubernetesResource cr = new GenericKubernetesResource();
    cr.setApiVersion(CFK_API);
    cr.setKind("KRaftController");
    cr.setMetadata(
        new ObjectMetaBuilder().withNamespace(namespace).withName(k8sName(kraftName)).build()
    );
    cr.setAdditionalProperty("spec", spec);
    return cr;
  }

  /**
   * Kafka brokers referencing the KRaftController.
   */
  private GenericKubernetesResource buildKafka(String kraftName, String appImage, String initImg) {
    Map<String, Object> clusterRef = new LinkedHashMap<>();
    clusterRef.put("name", k8sName(kraftName));
    clusterRef.put("namespace", namespace);

    Map<String, Object> kRaft = new LinkedHashMap<>();
    kRaft.put("clusterRef", clusterRef);

    Map<String, Object> dependencies = new LinkedHashMap<>();
    dependencies.put("kRaftController", kRaft);

    Map<String, Object> spec = new LinkedHashMap<>();
    spec.put("replicas", size);
    spec.put("dataVolumeCapacity", kafkaDataVolume);
    spec.put("image", imageBlock(appImage, initImg));
    spec.put("dependencies", dependencies);
    spec.put("podTemplate", podTemplateWithResources(cpu, memory));

    GenericKubernetesResource cr = new GenericKubernetesResource();
    cr.setApiVersion(CFK_API);
    cr.setKind("Kafka");
    cr.setMetadata(
        new ObjectMetaBuilder().withNamespace(namespace).withName(k8sName(clusterName)).build()
    );
    cr.setAdditionalProperty("spec", spec);
    return cr;
  }

  private static Map<String, Object> resourceMap(String cpuQty, String memQty) {
    Map<String, Object> requests = new LinkedHashMap<>();
    requests.put("cpu", cpuQty);
    requests.put("memory", memQty);
    Map<String, Object> limits = new LinkedHashMap<>();
    limits.put("cpu", cpuQty);
    limits.put("memory", memQty);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("requests", requests);
    out.put("limits", limits);
    return out;
  }

  public static void main(String[] args) {
    int code = new CommandLine(new KafkaOcpApp()).execute(args);
    System.exit(code);
  }
}
