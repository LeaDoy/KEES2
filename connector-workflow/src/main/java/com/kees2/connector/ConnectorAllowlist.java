package com.kees2.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class ConnectorAllowlist {

  private ConnectorAllowlist() {}

  public static Set<String> load(Path yamlFile) throws Exception {
    var mapper = new ObjectMapper(new YAMLFactory());
    JsonNode root = mapper.readTree(yamlFile.toFile());
    Set<String> classes = new HashSet<>();
    JsonNode arr = root.path("allowedConnectorClasses");
    if (arr.isArray()) {
      for (JsonNode n : arr) {
        classes.add(n.asText());
      }
    }
    return classes;
  }

  public static boolean isPermitted(Set<String> allowlist, String connectorClass) {
    return allowlist.contains(connectorClass);
  }
}
