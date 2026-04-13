package com.kees2.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigurationGenerator {

  private ConfigurationGenerator() {}

  public static Map<String, Object> generateBrokerConfig(
      SizingProfile profile, Environment env, DataCenter dc) {
    Map<String, Object> config = new LinkedHashMap<>();
    config.put("log.retention.hours", profile.defaultRetentionHours);
    config.put("default.replication.factor", profile.replicationFactor);
    config.put("min.insync.replicas", profile.minInSyncReplicas);
    config.put("num.partitions", profile.defaultPartitionCount);
    config.put("kees2.datacenter.id", dc.id);
    config.put("kees2.internal.domain", dc.internalDomain);
    config.putAll(env.brokerOverrides);
    return config;
  }

  public static Map<String, Object> generateTopicDefaults(SizingProfile profile) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("replication.factor", profile.replicationFactor);
    m.put("min.insync.replicas", profile.minInSyncReplicas);
    m.put("retention.ms", profile.defaultRetentionHours * 3_600_000L);
    return m;
  }

  public static Map<String, Object> generateQuotaPolicy(SizingProfile profile) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("producer_byte_rate", profile == SizingProfile.SMALL ? 10_485_760 : profile == SizingProfile.MEDIUM ? 52_428_800 : 209_715_200);
    m.put("consumer_byte_rate", profile == SizingProfile.SMALL ? 20_971_520 : profile == SizingProfile.MEDIUM ? 104_857_600 : 419_430_400);
    m.put("request_percentage", profile == SizingProfile.SMALL ? 25 : profile == SizingProfile.MEDIUM ? 50 : 100);
    return m;
  }

  public static Map<String, String> generateTlsConfig(DataCenter dc) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("tls.cluster.domain", dc.internalDomain);
    m.put("tls.certificate.cluster.name", "tls-kafka");
    return m;
  }
}
