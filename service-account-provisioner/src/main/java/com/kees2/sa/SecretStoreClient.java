package com.kees2.sa;

/** Replace with Vault API or Kubernetes client patching ExternalSecret / PushSecret. */
final class SecretStoreClient {

  void writeCredentialsMetadata(String principal) {
    // TODO: persist API keys / certs to wf-secret-store paths (e.g. secret/data/kees2/sa/<principal>)
  }

  void deleteCredentials(String principal) {
    // TODO: revoke and remove secret versions
  }
}
