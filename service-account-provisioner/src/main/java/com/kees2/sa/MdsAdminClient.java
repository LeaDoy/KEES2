package com.kees2.sa;

import java.util.List;

/** Replace with HTTP calls to Confluent Metadata Service (MDS) admin API. */
final class MdsAdminClient {

  void ensureServiceAccount(String principal, List<String> groups) {
    // TODO: OAuth2 client credentials → MDS REST create/update principal + role bindings
    if (!groups.isEmpty()) {
      System.err.println("WO-7 MDS stub: would bind groups " + groups);
    }
  }

  void deleteServiceAccount(String principal) {
    // TODO: MDS REST delete / disable principal
  }
}
