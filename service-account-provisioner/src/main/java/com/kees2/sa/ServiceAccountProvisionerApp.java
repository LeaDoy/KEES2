package com.kees2.sa;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * WO-7 — CLI entry points for service account lifecycle. Wire {@link MdsAdminClient} and
 * {@link SecretStoreClient} to your MDS REST API and Vault / ESO backend.
 */
@Command(
    name = "service-account-provisioner",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    subcommands = {ServiceAccountProvisionerApp.Provision.class, ServiceAccountProvisionerApp.Decommission.class}
)
public final class ServiceAccountProvisionerApp {

  private ServiceAccountProvisionerApp() {}

  public static void main(String[] args) {
    int code = new CommandLine(new ServiceAccountProvisionerApp()).execute(args);
    System.exit(code);
  }

  @Command(name = "provision", description = "Create or update a Confluent service account and optional AD group bindings.")
  static final class Provision implements Callable<Integer> {

    @Option(names = "--principal", required = true, description = "Service account principal / name.")
    String principal;

    @Option(names = "--groups", split = ",", description = "AD or RBAC group names to associate.")
    List<String> groups = List.of();

    @Override
    public Integer call() {
      var mds = new MdsAdminClient();
      var store = new SecretStoreClient();
      mds.ensureServiceAccount(principal, groups);
      store.writeCredentialsMetadata(principal);
      System.err.printf("WO-7 provision stub complete for %s groups=%s%n", principal, groups);
      return 0;
    }
  }

  @Command(name = "decommission", description = "Revoke credentials and remove the service account from MDS.")
  static final class Decommission implements Callable<Integer> {

    @Parameters(index = "0", description = "Principal to decommission.")
    String principal;

    @Override
    public Integer call() {
      var mds = new MdsAdminClient();
      var store = new SecretStoreClient();
      store.deleteCredentials(principal);
      mds.deleteServiceAccount(principal);
      System.err.printf("WO-7 decommission stub complete for %s%n", principal);
      return 0;
    }
  }
}
