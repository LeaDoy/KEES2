package com.kees2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "configuration-generator",
    mixinStandardHelpOptions = true,
    description = "WO-4 — Emit Kafka configOverrides / topic defaults / quota / TLS snippets as YAML."
)
public class ConfigurationGeneratorApp implements Callable<Integer> {

  @Option(names = "--sizing-profile", required = true)
  SizingProfile sizingProfile;

  @Option(names = "--environment", required = true)
  Environment environment;

  @Option(names = "--datacenter", required = true)
  DataCenter dataCenter;

  @Option(names = "--output-dir", required = true, description = "Directory to write generated YAML files.")
  Path outputDir;

  @Override
  public Integer call() throws Exception {
    Files.createDirectories(outputDir);
    var yaml =
        new ObjectMapper(
            YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build());
    write(yaml, outputDir.resolve("broker-config.yaml"), wrap("configOverrides", ConfigurationGenerator.generateBrokerConfig(sizingProfile, environment, dataCenter)));
    write(yaml, outputDir.resolve("topic-defaults.yaml"), wrap("topicDefaults", ConfigurationGenerator.generateTopicDefaults(sizingProfile)));
    write(yaml, outputDir.resolve("quota-policy.yaml"), ConfigurationGenerator.generateQuotaPolicy(sizingProfile));
    write(yaml, outputDir.resolve("tls-hints.yaml"), ConfigurationGenerator.generateTlsConfig(dataCenter));
    return 0;
  }

  private static Map<String, Object> wrap(String key, Map<?, ?> inner) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put(key, inner);
    return m;
  }

  private static void write(ObjectMapper yaml, Path path, Object value) throws Exception {
    yaml.writeValue(path.toFile(), value);
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new ConfigurationGeneratorApp()).execute(args));
  }
}
