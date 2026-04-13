package com.kees2.connector;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "connector-workflow",
    mixinStandardHelpOptions = true,
    subcommands = {ConnectorWorkflowApp.ValidateConnector.class}
)
public class ConnectorWorkflowApp {

  public static void main(String[] args) {
    System.exit(new CommandLine(new ConnectorWorkflowApp()).execute(args));
  }

  @Command(name = "validate-connector")
  static class ValidateConnector implements Callable<Integer> {
    @Option(names = "--allowlist", required = true)
    Path allowlist;

    @Parameters(index = "0")
    String connectorClass;

    @Override
    public Integer call() throws Exception {
      var list = ConnectorAllowlist.load(allowlist);
      var res = ConnectorValidator.validate(list, connectorClass, Map.of());
      if (!res.ok()) {
        System.err.println(res.message());
        return 1;
      }
      return 0;
    }
  }
}
