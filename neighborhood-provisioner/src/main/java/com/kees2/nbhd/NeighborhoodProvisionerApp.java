package com.kees2.nbhd;

import com.kees2.onboarding.AcsValidator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "neighborhood-provisioner", mixinStandardHelpOptions = true)
public class NeighborhoodProvisionerApp implements Callable<Integer> {

  @Option(names = "--neighborhood-name", required = true)
  String neighborhoodName;

  @Option(names = "--data-center", required = true, description = "DC1 or DC2")
  String dataCenter;

  @Option(names = "--sizing-profile", required = true, description = "SMALL, MEDIUM, or LARGE")
  String sizingProfile;

  @Option(names = "--acs-file", required = true)
  Path acsFile;

  @Override
  public Integer call() throws Exception {
    if (AcsValidator.validate(acsFile) != 0) {
      return 1;
    }
    String runId = UUID.randomUUID().toString();
    long t0 = System.currentTimeMillis();
    String[] steps = {
      "ACS gate check",
      "Namespace",
      "ServiceAccount/RBAC",
      "ResourceQuota/NetworkPolicies",
      "TLS certificates",
      "CFK Kafka CRD",
      "Broker health Job",
      "ClusterLink",
      "Smoke test Job",
      "Audit NEIGHBORHOOD_PROVISIONED"
    };
    for (String s : steps) {
      System.err.println("WO-18 step: " + s + " (integrate kubectl/argocd/fabric8)");
    }
    Map<String, Object> audit = new LinkedHashMap<>();
    audit.put("eventType", "NEIGHBORHOOD_PROVISIONED");
    audit.put("timestamp", Instant.now().toString());
    audit.put("neighborhoodName", neighborhoodName);
    audit.put("dataCenter", dataCenter);
    audit.put("sizingProfile", sizingProfile);
    audit.put("pipelineRunId", runId);
    audit.put("stepsCompleted", steps.length);
    audit.put("outcome", "STUB");
    audit.put("durationSeconds", (System.currentTimeMillis() - t0) / 1000);
    System.out.println(audit);
    return 0;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new NeighborhoodProvisionerApp()).execute(args));
  }
}
