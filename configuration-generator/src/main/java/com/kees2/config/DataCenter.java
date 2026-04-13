package com.kees2.config;

public enum DataCenter {
  DC1_VIRGINIA("dc1-virginia", "virginia.kees2.internal"),
  DC2_TEXAS("dc2-texas", "texas.kees2.internal");

  public final String id;
  public final String internalDomain;

  DataCenter(String id, String internalDomain) {
    this.id = id;
    this.internalDomain = internalDomain;
  }
}
