package com.kees2.config;

public enum SizingProfile {
  SMALL(24, (short) 3, (short) 2, 6, "300Gi"),
  MEDIUM(72, (short) 3, (short) 2, 12, "1Ti"),
  LARGE(168, (short) 3, (short) 2, 24, "4Ti");

  public final int defaultRetentionHours;
  public final short replicationFactor;
  public final short minInSyncReplicas;
  public final int defaultPartitionCount;
  public final String logVolumeCapacity;

  SizingProfile(
      int defaultRetentionHours,
      short replicationFactor,
      short minInSyncReplicas,
      int defaultPartitionCount,
      String logVolumeCapacity) {
    this.defaultRetentionHours = defaultRetentionHours;
    this.replicationFactor = replicationFactor;
    this.minInSyncReplicas = minInSyncReplicas;
    this.defaultPartitionCount = defaultPartitionCount;
    this.logVolumeCapacity = logVolumeCapacity;
  }
}
