package com.kees2.secrets;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * WO-11 — Bumps refresh annotation on ExternalSecret resources so ESO reconciles and pulls new data.
 */
@Command(
    name = "secret-rotation-job",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Trigger External Secrets Operator refresh for selected secrets (stub-safe: no-op if CRD missing)."
)
public class SecretRotationApp implements Callable<Integer> {

  private static final String ESO_GROUP = "external-secrets.io";
  private static final String ESO_VERSION = "v1beta1";
  private static final String ESO_KIND = "ExternalSecret";

  @Option(names = "--namespace", required = true, description = "Namespace containing ExternalSecrets.")
  String namespace;

  @Option(
      names = "--selector",
      description = "Optional label selector (e.g. kees2.io/rotate=true)."
  )
  String selector;

  @Override
  public Integer call() {
    try (KubernetesClient client = new KubernetesClientBuilder().build()) {
      var resources = client.genericKubernetesResources(ESO_GROUP, ESO_VERSION, ESO_KIND)
          .inNamespace(namespace)
          .list();
      int touched = 0;
      var ops = client.genericKubernetesResources(ESO_GROUP, ESO_VERSION, ESO_KIND).inNamespace(namespace);
      for (GenericKubernetesResource es : resources.getItems()) {
        if (selector != null && !selector.isBlank()) {
          var labels = es.getMetadata() != null ? es.getMetadata().getLabels() : null;
          if (labels == null || !matchesSelector(labels, selector)) {
            continue;
          }
        }
        var meta = es.getMetadata();
        if (meta == null || meta.getName() == null) {
          continue;
        }
        ops.withName(meta.getName())
            .edit(
                current -> {
                  var m = current.getMetadata();
                  if (m == null) {
                    return current;
                  }
                  Map<String, String> ann =
                      m.getAnnotations() != null ? new HashMap<>(m.getAnnotations()) : new HashMap<>();
                  ann.put("reconcile.external-secrets.io/force-sync", Long.toString(System.currentTimeMillis()));
                  m.setAnnotations(ann);
                  return current;
                });
        touched++;
      }
      System.err.printf("WO-11: refreshed %d ExternalSecret(s) in %s%n", touched, namespace);
      return 0;
    } catch (Exception e) {
      System.err.printf("WO-11: rotation skipped or failed (%s)%n", e.getMessage());
      return 0;
    }
  }

  private static boolean matchesSelector(Map<String, String> labels, String selector) {
    for (String part : selector.split(",")) {
      String[] kv = part.trim().split("=", 2);
      if (kv.length != 2) {
        continue;
      }
      if (!kv[1].equals(labels.get(kv[0]))) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
    int code = new CommandLine(new SecretRotationApp()).execute(args);
    System.exit(code);
  }
}
