package com.kees2.connector;

/** WO-24 — Wire confluent-kafka-schema-registry-client for testCompatibility + register. */
public final class SchemaRegistrationService {

  public int registerSchema(String subject, String avroSchemaJson) {
    throw new UnsupportedOperationException(
        "Integrate Schema Registry client; enforce FULL_TRANSITIVE for subject=" + subject);
  }
}
