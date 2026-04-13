package com.kees2.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Path;

/** WO-9 / WO-20 — maxPartitions ceiling from Git manifest. */
public record NeighborhoodParams(int maxPartitions, int currentPartitionTotal) {

  public static NeighborhoodParams load(Path yamlPath) throws Exception {
    var mapper = new ObjectMapper(new YAMLFactory());
    var tree = mapper.readTree(yamlPath.toFile());
    int max = tree.path("maxPartitions").asInt(500);
    int cur = tree.path("currentPartitionTotal").asInt(0);
    return new NeighborhoodParams(max, cur);
  }
}
