package com.kees2.acl;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public final class AclProvisioningService implements AutoCloseable {

  private final AdminClient admin;

  public AclProvisioningService(Properties adminClientProps) {
    this.admin = AdminClient.create(adminClientProps);
  }

  public void applyProducerAcls(String principal, String topicPrefix)
      throws ExecutionException, InterruptedException {
    Collection<AclBinding> bindings = new ArrayList<>();
    var topicPat = new ResourcePattern(ResourceType.TOPIC, topicPrefix, PatternType.PREFIXED);
    bindings.add(new AclBinding(topicPat, new org.apache.kafka.common.acl.AccessControlEntry(principal, "*", AclOperation.WRITE, AclPermissionType.ALLOW)));
    bindings.add(new AclBinding(topicPat, new org.apache.kafka.common.acl.AccessControlEntry(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));
    var txnPat = new ResourcePattern(ResourceType.TRANSACTIONAL_ID, topicPrefix, PatternType.PREFIXED);
    bindings.add(new AclBinding(txnPat, new org.apache.kafka.common.acl.AccessControlEntry(principal, "*", AclOperation.WRITE, AclPermissionType.ALLOW)));
    admin.createAcls(bindings).all().get();
  }

  public void applyConsumerAcls(String principal, String topicPrefix, String consumerGroup)
      throws ExecutionException, InterruptedException {
    Collection<AclBinding> bindings = new ArrayList<>();
    var topicPat = new ResourcePattern(ResourceType.TOPIC, topicPrefix, PatternType.PREFIXED);
    bindings.add(new AclBinding(topicPat, new org.apache.kafka.common.acl.AccessControlEntry(principal, "*", AclOperation.READ, AclPermissionType.ALLOW)));
    bindings.add(new AclBinding(topicPat, new org.apache.kafka.common.acl.AccessControlEntry(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));
    var groupPat = new ResourcePattern(ResourceType.GROUP, consumerGroup, PatternType.LITERAL);
    bindings.add(new AclBinding(groupPat, new org.apache.kafka.common.acl.AccessControlEntry(principal, "*", AclOperation.READ, AclPermissionType.ALLOW)));
    admin.createAcls(bindings).all().get();
  }

  /** WO-26 — Remove all ACL bindings for principal (best-effort: filter describeAcls). */
  public void removeAllBindings(String principal)
      throws ExecutionException, InterruptedException {
    var filter =
        new org.apache.kafka.common.acl.AclBindingFilter(
            new org.apache.kafka.common.resource.ResourcePatternFilter(ResourceType.ANY, null, PatternType.ANY),
            new org.apache.kafka.common.acl.AccessControlEntryFilter(principal, null, AclOperation.ANY, AclPermissionType.ANY));
    Collection<AclBinding> found = admin.describeAcls(filter).values().get();
    if (!found.isEmpty()) {
      admin.deleteAcls(found.stream().map(AclBinding::toFilter).toList()).all().get();
    }
  }

  @Override
  public void close() {
    admin.close();
  }
}
