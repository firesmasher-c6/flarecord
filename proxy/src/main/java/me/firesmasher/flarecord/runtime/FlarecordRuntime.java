package me.firesmasher.flarecord.runtime;

import java.io.IOException;
import java.nio.file.Path;
import me.firesmasher.flarecord.animation.AnimationCatalog;
import me.firesmasher.flarecord.config.FlarecordConfigLoader;
import me.firesmasher.flarecord.config.FlarecordConfiguration;
import me.firesmasher.flarecord.queue.PlayerQueue;
import me.firesmasher.flarecord.security.IpConnectionGuard;

public final class FlarecordRuntime {

  private final FlarecordConfiguration configuration;
  private final AnimationCatalog animations;
  private final PlayerQueue playerQueue;
  private final IpConnectionGuard connectionGuard;

  private FlarecordRuntime(FlarecordConfiguration configuration, AnimationCatalog animations) {
    this.configuration = configuration;
    this.animations = animations;
    this.playerQueue = new PlayerQueue(configuration.getQueueLimit());
    this.connectionGuard = new IpConnectionGuard(
        configuration.isEnabled() && configuration.isAntiBotEnabled(),
        configuration.getMaxConnectionsPerIp(),
        configuration.getTemporaryBlockDurationSeconds());
  }

  public static FlarecordRuntime load(Path directory) throws IOException {
    final FlarecordConfiguration configuration =
        FlarecordConfigLoader.load(directory.resolve("flarecord.yml"));
    final AnimationCatalog animations = AnimationCatalog.load(directory.resolve("animations.yml"));
    if (configuration.isAnimationsEnabled()
        && animations.get(configuration.getDefaultAnimation()) == null) {
      throw new IOException("Default animation '" + configuration.getDefaultAnimation()
          + "' is not defined in animations.yml");
    }
    return new FlarecordRuntime(configuration, animations);
  }

  public FlarecordConfiguration getConfiguration() {
    return configuration;
  }

  public AnimationCatalog getAnimations() {
    return animations;
  }

  public PlayerQueue getPlayerQueue() {
    return playerQueue;
  }

  public IpConnectionGuard getConnectionGuard() {
    return connectionGuard;
  }
}
