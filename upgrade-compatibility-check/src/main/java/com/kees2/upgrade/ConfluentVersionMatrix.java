package com.kees2.upgrade;

public final class ConfluentVersionMatrix {

  private ConfluentVersionMatrix() {}

  public static void check(String currentVersion, String targetVersion, String protocolVersion) {
    if (currentVersion == null
        || targetVersion == null
        || currentVersion.isBlank()
        || targetVersion.isBlank()) {
      throw new UpgradeIncompatibilityException("Missing version inputs");
    }
    // Stub: reject obvious major skip (e.g. 6.x -> 8.x) — replace with real matrix.
    int c = major(currentVersion);
    int t = major(targetVersion);
    if (t - c > 1) {
      throw new UpgradeIncompatibilityException("Major version skip not allowed: " + c + " -> " + t);
    }
  }

  private static int major(String v) {
    String d = v.replaceFirst("^[^0-9]*", "");
    int i = 0;
    while (i < d.length() && Character.isDigit(d.charAt(i))) {
      i++;
    }
    if (i == 0) {
      return 0;
    }
    return Integer.parseInt(d.substring(0, i));
  }
}
