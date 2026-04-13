package com.kees2.kraft;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "kraft-quorum-health-check",
    mixinStandardHelpOptions = true,
    description = "WO-3 — Wait for KRaftController CR to report ready state (CFK status schema)."
)
public class KraftQuorumHealthApp implements Callable<Integer> {

  private static final CustomResourceDefinitionContext KRAFT_CTX =
      new CustomResourceDefinitionContext.Builder()
          .withGroup("platform.confluent.io")
          .withVersion("v1beta2")
          .withPlural("kraftcontrollers")
          .withScope("Namespaced")
          .build();

  @Option(names = "--namespace", required = true)
  String namespace;

  @Option(names = "--controller-name", required = true)
  String controllerName;

  @Option(names = "--poll-interval-seconds", defaultValue = "15")
  int pollIntervalSeconds;

  @Option(names = "--timeout-seconds", defaultValue = "600")
  int timeoutSeconds;

  @Override
  public Integer call() throws Exception {
    var deadline = Instant.now().plus(Duration.ofSeconds(timeoutSeconds));
    try (var client = new KubernetesClientBuilder().build()) {
      var api = client.genericKubernetesResources(KRAFT_CTX).inNamespace(namespace);
      while (Instant.now().isBefore(deadline)) {
        GenericKubernetesResource cr = api.withName(controllerName).get();
        if (cr != null && isReady(cr)) {
          System.err.println("WO-3: KRaftController " + controllerName + " is ready.");
          return 0;
        }
        Thread.sleep(Duration.ofSeconds(pollIntervalSeconds).toMillis());
      }
      System.err.println("WO-3: Timeout waiting for KRaftController readiness.");
      return 1;
    }
  }

  @SuppressWarnings("unchecked")
  private static boolean isReady(GenericKubernetesResource cr) {
    Map<String, Object> additional = cr.getAdditionalProperties();
    if (additional == null) {
      return false;
    }
    Object status = additional.get("status");
    if (!(status instanceof Map)) {
      return false;
    }
    Map<String, Object> st = (Map<String, Object>) status;
    Object phase = st.get("phase");
    if (phase != null && "READY".equalsIgnoreCase(String.valueOf(phase))) {
      return true;
    }
    Object conditions = st.get("conditions");
    if (conditions instanceof List) {
      for (Object c : (List<?>) conditions) {
        if (c instanceof Map) {
          Map<String, Object> cond = (Map<String, Object>) c;
          if ("Ready".equals(String.valueOf(cond.get("type")))
              && "True".equalsIgnoreCase(String.valueOf(cond.get("status")))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new KraftQuorumHealthApp()).execute(args));
  }
}
