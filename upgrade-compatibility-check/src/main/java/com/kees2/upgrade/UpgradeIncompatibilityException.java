package com.kees2.upgrade;

public class UpgradeIncompatibilityException extends RuntimeException {
  public UpgradeIncompatibilityException(String message) {
    super(message);
  }
}
