package com.kees2.topic;

public final class TopicNameValidator {

  private TopicNameValidator() {}

  /** Pattern: {@code <neighborhood>.<domain>.<entity>.<event-type>.v<version>} (all segments lowercase alnum/hyphen). */
  public static void validate(String topicName, String neighborhood) {
    String n = neighborhood.replace('_', '-');
    String[] parts = topicName.split("\\.");
    if (parts.length < 5) {
      throw new TopicValidationException(
          "Topic must be <neighborhood>.<domain>.<entity>.<event-type>.v<version>: " + topicName);
    }
    if (!parts[0].equals(n)) {
      throw new TopicValidationException("Topic must start with neighborhood '" + n + "': " + topicName);
    }
    String ver = parts[parts.length - 1];
    if (!ver.matches("v\\d+")) {
      throw new TopicValidationException("Topic must end with .v<version>: " + topicName);
    }
    for (String p : parts) {
      if (!p.matches("[a-z0-9][a-z0-9-]*")) {
        throw new TopicValidationException("Invalid segment in topic name: " + p);
      }
    }
  }
}
