package com.kees2.topic;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;

@Command(name = "topic-provisioning-service", mixinStandardHelpOptions = true)
public class TopicProvisioningApp implements Callable<Integer> {

  @Option(names = "--bootstrap-servers", required = true)
  String bootstrap;

  @Option(names = "--neighborhood", required = true)
  String neighborhood;

  @Option(names = "--neighborhood-manifest", required = true, description = "YAML with maxPartitions and currentPartitionTotal.")
  Path neighborhoodManifest;

  @Option(names = "--name", required = true)
  String name;

  @Option(names = "--replication-factor", required = true)
  short replicationFactor;

  @Option(names = "--partitions", required = true)
  int partitions;

  @Option(names = "--min-insync-replicas", required = true)
  short minIsr;

  @Option(names = "--retention-ms", required = true)
  long retentionMs;

  @Option(names = "--cleanup-policy", required = true)
  String cleanupPolicy;

  @Override
  public Integer call() throws Exception {
    var params = NeighborhoodParams.load(neighborhoodManifest);
    var req =
        new TopicRequest(
            name, neighborhood, replicationFactor, partitions, minIsr, retentionMs, cleanupPolicy);
    Properties p = new Properties();
    p.put("bootstrap.servers", bootstrap);
    try (var svc = new TopicProvisioningService(p)) {
      svc.createTopic(req, params);
      return 0;
    } catch (TopicValidationException e) {
      System.err.println(e.getMessage());
      return 2;
    }
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new TopicProvisioningApp()).execute(args));
  }
}
