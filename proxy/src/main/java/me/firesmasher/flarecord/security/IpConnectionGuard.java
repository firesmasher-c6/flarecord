package me.firesmasher.flarecord.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class IpConnectionGuard {

  private static final long MAX_BLOCKED_ADDRESSES = 16_384;

  private final boolean enabled;
  private final int maximumConnectionsPerAddress;
  private final Cache<InetAddress, Boolean> blockedAddresses;
  private final ConcurrentHashMap<InetAddress, Integer> activeConnections =
      new ConcurrentHashMap<>();
  private final AtomicLong rejectedConnections = new AtomicLong();

  public IpConnectionGuard(boolean enabled, int maximumConnectionsPerAddress,
      int blockDurationSeconds) {
    if (maximumConnectionsPerAddress < 1 || blockDurationSeconds < 1) {
      throw new IllegalArgumentException("Connection guard limits must be positive");
    }
    this.enabled = enabled;
    this.maximumConnectionsPerAddress = maximumConnectionsPerAddress;
    this.blockedAddresses = Caffeine.newBuilder()
        .expireAfterWrite(blockDurationSeconds, TimeUnit.SECONDS)
        .maximumSize(MAX_BLOCKED_ADDRESSES)
        .build();
  }

  public boolean tryAcquire(InetAddress address) {
    if (!enabled) {
      return true;
    }
    if (blockedAddresses.getIfPresent(address) != null) {
      rejectedConnections.incrementAndGet();
      return false;
    }

    final boolean[] acquired = {false};
    activeConnections.compute(address, (key, active) -> {
      if (blockedAddresses.getIfPresent(key) != null) {
        return active;
      }
      final int nextCount = active == null ? 1 : active + 1;
      if (nextCount > maximumConnectionsPerAddress) {
        blockedAddresses.put(key, Boolean.TRUE);
        return active;
      }
      acquired[0] = true;
      return nextCount;
    });

    if (!acquired[0]) {
      rejectedConnections.incrementAndGet();
    }
    return acquired[0];
  }

  public void release(InetAddress address) {
    if (!enabled) {
      return;
    }
    activeConnections.computeIfPresent(address,
        (key, active) -> active <= 1 ? null : active - 1);
  }

  public long getRejectedConnectionCount() {
    return rejectedConnections.get();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getTrackedAddressCount() {
    return activeConnections.size();
  }
}
