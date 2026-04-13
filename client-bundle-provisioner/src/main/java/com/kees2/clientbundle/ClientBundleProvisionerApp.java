package com.kees2.clientbundle;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "client-bundle-provisioner", mixinStandardHelpOptions = true)
public class ClientBundleProvisionerApp implements Callable<Integer> {

  @Option(names = "--neighborhood", required = true)
  String neighborhood;

  @Option(names = "--app-name", required = true)
  String appName;

  @Option(names = "--target-namespace", required = true)
  String targetNamespace;

  @Option(names = "--topic-prefix", required = true)
  String topicPrefix;

  @Override
  public Integer call() {
    String sa = "svc-" + neighborhood + "-" + appName;
    Map<String, Object> audit = new LinkedHashMap<>();
    audit.put("principal", sa);
    audit.put("neighborhood", neighborhood);
    audit.put("appName", appName);
    audit.put("externalSecretPath", "secrets/kees2/" + neighborhood + "/" + sa);
    audit.put("esoNamespace", targetNamespace);
    audit.put("topicPrefix", topicPrefix);
    audit.put("note", "Integrate ServiceAccountProvisioner + AclProvisioningService + ESO creator");
    System.out.println(audit);
    return 0;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new ClientBundleProvisionerApp()).execute(args));
  }
}
