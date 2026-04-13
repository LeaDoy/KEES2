package com.kees2.quota;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Properties;
import java.util.concurrent.Callable;

@Command(name = "neighborhood-quota-service", mixinStandardHelpOptions = true)
public class NeighborhoodQuotaApp implements Callable<Integer> {

  @Option(names = "--bootstrap-servers", required = true)
  String bootstrap;

  @Option(names = "--principal", required = true)
  String principal;

  @Option(names = "--profile", required = true, description = "SMALL, MEDIUM, or LARGE")
  String profile;

  @Override
  public Integer call() throws Exception {
    double prod;
    double cons;
    double reqPct;
    switch (profile.toUpperCase()) {
      case "SMALL" -> {
        prod = 10 * 1024 * 1024;
        cons = 20 * 1024 * 1024;
        reqPct = 25;
      }
      case "MEDIUM" -> {
        prod = 50 * 1024 * 1024;
        cons = 100 * 1024 * 1024;
        reqPct = 50;
      }
      case "LARGE" -> {
        prod = 200 * 1024 * 1024;
        cons = 400 * 1024 * 1024;
        reqPct = 100;
      }
      default -> throw new IllegalArgumentException("Unknown profile: " + profile);
    }
    Properties p = new Properties();
    p.put("bootstrap.servers", bootstrap);
    try (var svc = new NeighborhoodQuotaService(p)) {
      svc.applyClientQuotas(principal, prod, cons, reqPct);
    }
    return 0;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new NeighborhoodQuotaApp()).execute(args));
  }
}
