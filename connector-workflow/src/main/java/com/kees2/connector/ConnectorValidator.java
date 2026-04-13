package com.kees2.connector;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ConnectorValidator {

  private static final Pattern SECRET_KEY = Pattern.compile("(?i).*(password|secret|token).*");

  private ConnectorValidator() {}

  public static ValidationResult validate(
      Set<String> allowlist, String connectorClass, Map<String, String> config) {
    if (!ConnectorAllowlist.isPermitted(allowlist, connectorClass)) {
      return ValidationResult.rejected("Connector class not in allowlist: " + connectorClass);
    }
    for (var e : config.entrySet()) {
      if (SECRET_KEY.matcher(e.getKey()).matches()) {
        String v = e.getValue();
        if (v == null || !v.trim().startsWith("${file:")) {
          return ValidationResult.rejected(
              "Secret-like key must use ${file:...} reference: " + e.getKey());
        }
      }
    }
    return ValidationResult.passed();
  }
}
