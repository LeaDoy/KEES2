package com.kees2.topic;

public record TopicRequest(
    String name,
    String neighborhood,
    short replicationFactor,
    int partitionCount,
    short minInsyncReplicas,
    long retentionMs,
    String cleanupPolicy) {}
