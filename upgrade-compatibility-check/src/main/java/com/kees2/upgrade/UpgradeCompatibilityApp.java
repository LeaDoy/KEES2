package com.kees2.upgrade;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "upgrade-compatibility-check", mixinStandardHelpOptions = true)
public class UpgradeCompatibilityApp implements Callable<Integer> {

  @Option(names = "--current-version", required = true)
  String currentVersion;

  @Option(names = "--target-version", required = true)
  String targetVersion;

  @Option(names = "--protocol-version", defaultValue = "3.7")
  String protocolVersion;

  @Override
  public Integer call() {
    try {
      ConfluentVersionMatrix.check(currentVersion, targetVersion, protocolVersion);
      System.err.println("WO-25: compatibility check passed (stub matrix).");
      return 0;
    } catch (UpgradeIncompatibilityException e) {
      System.err.println(e.getMessage());
      return 1;
    }
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new UpgradeCompatibilityApp()).execute(args));
  }
}
