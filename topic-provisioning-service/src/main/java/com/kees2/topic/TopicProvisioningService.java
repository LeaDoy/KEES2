package com.kees2.topic;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public final class TopicProvisioningService implements AutoCloseable {

  private final AdminClient admin;

  public TopicProvisioningService(Properties kafkaProps) {
    this.admin = AdminClient.create(kafkaProps);
  }

  public void createTopic(TopicRequest req, NeighborhoodParams neighborhoodParams)
      throws ExecutionException, InterruptedException {
    TopicNameValidator.validate(req.name(), req.neighborhood());
    if (req.replicationFactor() < 3) {
      throw new TopicValidationException("replicationFactor must be >= 3");
    }
    if (req.minInsyncReplicas() < 2) {
      throw new TopicValidationException("minInsyncReplicas must be >= 2");
    }
    if (req.cleanupPolicy() == null || req.cleanupPolicy().isBlank()) {
      throw new TopicValidationException("cleanupPolicy is required");
    }
    int newTotal = neighborhoodParams.currentPartitionTotal() + req.partitionCount();
    if (newTotal > neighborhoodParams.maxPartitions()) {
      throw new TopicValidationException(
          "Partition ceiling exceeded: "
              + newTotal
              + " > maxPartitions "
              + neighborhoodParams.maxPartitions());
    }
    var nt =
        new NewTopic(req.name(), req.partitionCount(), req.replicationFactor())
            .configs(
                java.util.Map.of(
                    "min.insync.replicas",
                    Short.toString(req.minInsyncReplicas()),
                    "retention.ms",
                    Long.toString(req.retentionMs()),
                    "cleanup.policy",
                    req.cleanupPolicy()));
    admin.createTopics(Collections.singleton(nt)).all().get();
  }

  @Override
  public void close() {
    admin.close();
  }
}
