package com.kees2.link;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "clusterlink-smoke-test", mixinStandardHelpOptions = true)
public class ClusterLinkSmokeTestApp implements Callable<Integer> {

  @Option(names = "--neighborhood", env = "NEIGHBORHOOD_NAME", required = true)
  String neighborhood;

  @Option(names = "--source-bootstrap", env = "SOURCE_BOOTSTRAP")
  String source;

  @Option(names = "--dest-bootstrap", env = "DEST_BOOTSTRAP")
  String dest;

  @Override
  public Integer call() {
    if (source == null || source.isBlank() || dest == null || dest.isBlank()) {
      System.err.println("WO-22 stub: set SOURCE_BOOTSTRAP and DEST_BOOTSTRAP for live test");
      return 0;
    }
    System.err.println("WO-22: integrate kafkaTemplate produce/poll across clusters for " + neighborhood);
    return 0;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new ClusterLinkSmokeTestApp()).execute(args));
  }
}
