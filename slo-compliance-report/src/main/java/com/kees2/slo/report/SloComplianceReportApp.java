package com.kees2.slo.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "slo-compliance-report", mixinStandardHelpOptions = true)
public class SloComplianceReportApp implements Callable<Integer> {

  @Option(names = "--prometheus-url", env = "PROMETHEUS_URL", defaultValue = "")
  String prometheusUrl;

  @Option(names = "--neighborhood", defaultValue = "example-lob")
  String neighborhood;

  @Override
  public Integer call() throws Exception {
    var mapper = new ObjectMapper();
    Map<String, Object> report = new LinkedHashMap<>();
    report.put("reportPeriod", "weekly");
    report.put("generatedAt", Instant.now().toString());
    Map<String, Object> entry = new LinkedHashMap<>();
    entry.put("name", neighborhood);
    entry.put("dataCenter", "dc1-virginia");
    entry.put("sloTier", "tier2");
    entry.put("availability", "stub");
    entry.put("syntheticLoopSuccessRate", queryRateOrStub("slo.probe.result"));
    entry.put("avgRoundTripLatencyMs", queryRateOrStub("slo.probe.latency.ms"));
    entry.put("maxConsumerLagOffsets", queryRateOrStub("kafka_consumergroup_lag"));
    entry.put("replicationLagCompliance", "stub");
    entry.put("breaches", List.of());
    report.put("neighborhoods", List.of(entry));
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));
    return 0;
  }

  private String queryRateOrStub(String metric) throws Exception {
    if (prometheusUrl == null || prometheusUrl.isBlank()) {
      return "n/a (set PROMETHEUS_URL)";
    }
    String enc = URLEncoder.encode(metric, StandardCharsets.UTF_8);
    String q = prometheusUrl.replaceAll("/$", "") + "/api/v1/query?query=" + enc;
    var client = HttpClient.newHttpClient();
    var req = HttpRequest.newBuilder(URI.create(q)).GET().build();
    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
    return res.statusCode() == 200 ? "queried" : ("http_" + res.statusCode());
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new SloComplianceReportApp()).execute(args));
  }
}
