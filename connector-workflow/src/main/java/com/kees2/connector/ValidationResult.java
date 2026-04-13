package com.kees2.connector;

public record ValidationResult(boolean ok, String message) {
  public static ValidationResult passed() {
    return new ValidationResult(true, "ok");
  }

  public static ValidationResult rejected(String message) {
    return new ValidationResult(false, message);
  }
}
