package com.kees2.drift;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.common.config.ConfigResource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "drift-detection-job", mixinStandardHelpOptions = true)
public class DriftDetectionApp implements Callable<Integer> {

  @Option(names = "--baseline-dir", description = "Directory containing baseline YAML (e.g. baseline.yaml).")
  Path baselineDir;

  @Option(names = "--bootstrap-servers", description = "If set, fetch live broker configs and compare.")
  String bootstrap;

  @Option(names = "--cluster-id", defaultValue = "unknown")
  String clusterId;

  @Override
  public Integer call() throws Exception {
    var mapper = new ObjectMapper();
    Path baselineFile = baselineDir.resolve("baseline.yaml");
    var yamlMapper = new ObjectMapper(new YAMLFactory());
    JsonNode root = yamlMapper.readTree(Files.readString(baselineFile));
    JsonNode expected = root.path("brokerConfigs");
    Map<String, String> live = new HashMap<>();
    if (bootstrap != null && !bootstrap.isBlank()) {
      Properties p = new Properties();
      p.put("bootstrap.servers", bootstrap);
      try (var admin = AdminClient.create(p)) {
        var cr = new ConfigResource(ConfigResource.Type.BROKER, "");
        Config cfg = admin.describeConfigs(Set.of(cr)).values().get(cr).get();
        cfg.entries()
            .forEach(e -> live.put(e.name(), e.value() == null ? "" : e.value()));
      }
    }
    List<Map<String, Object>> entries = new ArrayList<>();
    if (!live.isEmpty()) {
      expected
          .fields()
          .forEachRemaining(
              e -> {
                String key = e.getKey();
                String want = e.getValue().asText();
                String got = live.get(key);
                if (got != null && !got.equals(want)) {
                  entries.add(
                      Map.of(
                          "configKey",
                          key,
                          "baselineValue",
                          want,
                          "liveValue",
                          got,
                          "remediationPolicy",
                          "manual",
                          "detectedAt",
                          Instant.now().toString()));
                }
              });
    }
    Map<String, Object> report = new HashMap<>();
    report.put("timestamp", Instant.now().toString());
    report.put("clusterId", clusterId);
    report.put("driftCount", entries.size());
    report.put("entries", entries);
    if (live.isEmpty()) {
      report.put("note", "No --bootstrap-servers; baseline-only render (no live comparison)");
    }
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));
    return entries.isEmpty() ? 0 : 3;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new DriftDetectionApp()).execute(args));
  }
}
