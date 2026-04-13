package com.kees2.acl;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Properties;
import java.util.concurrent.Callable;

@Command(
    name = "acl-provisioning-service",
    mixinStandardHelpOptions = true,
    subcommands = {
      AclProvisioningApp.ApplyProducer.class,
      AclProvisioningApp.ApplyConsumer.class,
      AclProvisioningApp.RemoveAll.class
    }
)
public class AclProvisioningApp {

  public static void main(String[] args) {
    System.exit(new CommandLine(new AclProvisioningApp()).execute(args));
  }

  static Properties kafkaProps(String bootstrap) {
    Properties p = new Properties();
    p.put("bootstrap.servers", bootstrap);
    p.put("request.timeout.ms", "30000");
    return p;
  }

  @Command(name = "apply-producer", description = "Apply producer ACL template (PREFIXED topic + transactional id).")
  static class ApplyProducer implements Callable<Integer> {
    @Option(names = "--bootstrap-servers", required = true)
    String bootstrap;

    @Option(names = "--principal", required = true)
    String principal;

    @Parameters(index = "0", description = "Topic prefix")
    String topicPrefix;

    @Override
    public Integer call() throws Exception {
      try (var svc = new AclProvisioningService(kafkaProps(bootstrap))) {
        svc.applyProducerAcls(principal, topicPrefix);
        return 0;
      }
    }
  }

  @Command(name = "apply-consumer", description = "Apply consumer ACL template.")
  static class ApplyConsumer implements Callable<Integer> {
    @Option(names = "--bootstrap-servers", required = true)
    String bootstrap;

    @Option(names = "--principal", required = true)
    String principal;

    @Option(names = "--topic-prefix", required = true)
    String topicPrefix;

    @Option(names = "--consumer-group", required = true)
    String consumerGroup;

    @Override
    public Integer call() throws Exception {
      try (var svc = new AclProvisioningService(kafkaProps(bootstrap))) {
        svc.applyConsumerAcls(principal, topicPrefix, consumerGroup);
        return 0;
      }
    }
  }

  @Command(name = "remove-all", description = "WO-26 — Delete ACLs for principal.")
  static class RemoveAll implements Callable<Integer> {
    @Option(names = "--bootstrap-servers", required = true)
    String bootstrap;

    @Parameters(index = "0")
    String principal;

    @Override
    public Integer call() throws Exception {
      try (var svc = new AclProvisioningService(kafkaProps(bootstrap))) {
        svc.removeAllBindings(principal);
        return 0;
      }
    }
  }
}
