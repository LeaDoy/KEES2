package com.kees2.config;

import java.util.Map;

public enum Environment {
  DEV(Map.of("auto.create.topics.enable", "true", "log.cleanup.policy", "delete")),
  STAGING(Map.of("auto.create.topics.enable", "false", "log.cleanup.policy", "delete")),
  PROD(Map.of("auto.create.topics.enable", "false", "log.cleanup.policy", "delete"));

  public final Map<String, String> brokerOverrides;

  Environment(Map<String, String> brokerOverrides) {
    this.brokerOverrides = brokerOverrides;
  }
}
