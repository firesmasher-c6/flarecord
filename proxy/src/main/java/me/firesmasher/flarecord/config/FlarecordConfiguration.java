package me.firesmasher.flarecord.config;

public final class FlarecordConfiguration {

  private final boolean enabled;
  private final boolean debug;
  private final boolean queueEnabled;
  private final int queueLimit;
  private final String priorityPermission;
  private final boolean antiBotEnabled;
  private final int maxConnectionsPerIp;
  private final int temporaryBlockDurationSeconds;
  private final boolean animationsEnabled;
  private final String defaultAnimation;

  FlarecordConfiguration(boolean enabled, boolean debug, boolean queueEnabled, int queueLimit,
      String priorityPermission, boolean antiBotEnabled, int maxConnectionsPerIp,
      int temporaryBlockDurationSeconds, boolean animationsEnabled, String defaultAnimation) {
    this.enabled = enabled;
    this.debug = debug;
    this.queueEnabled = queueEnabled;
    this.queueLimit = queueLimit;
    this.priorityPermission = priorityPermission;
    this.antiBotEnabled = antiBotEnabled;
    this.maxConnectionsPerIp = maxConnectionsPerIp;
    this.temporaryBlockDurationSeconds = temporaryBlockDurationSeconds;
    this.animationsEnabled = animationsEnabled;
    this.defaultAnimation = defaultAnimation;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isDebug() {
    return debug;
  }

  public boolean isQueueEnabled() {
    return queueEnabled;
  }

  public int getQueueLimit() {
    return queueLimit;
  }

  public String getPriorityPermission() {
    return priorityPermission;
  }

  public boolean isAntiBotEnabled() {
    return antiBotEnabled;
  }

  public int getMaxConnectionsPerIp() {
    return maxConnectionsPerIp;
  }

  public int getTemporaryBlockDurationSeconds() {
    return temporaryBlockDurationSeconds;
  }

  public boolean isAnimationsEnabled() {
    return animationsEnabled;
  }

  public String getDefaultAnimation() {
    return defaultAnimation;
  }
}
