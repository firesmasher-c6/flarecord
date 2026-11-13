package me.firesmasher.flarecord.queue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlayerQueue {

  public enum JoinResult {
    ADDED,
    ALREADY_QUEUED,
    FULL
  }

  public static final class Entry {

    private final UUID playerId;
    private final String playerName;
    private final boolean priority;
    private final long sequence;

    private Entry(UUID playerId, String playerName, boolean priority, long sequence) {
      this.playerId = playerId;
      this.playerName = playerName;
      this.priority = priority;
      this.sequence = sequence;
    }

    public UUID getPlayerId() {
      return playerId;
    }

    public String getPlayerName() {
      return playerName;
    }

    public boolean hasPriority() {
      return priority;
    }
  }

  private static final Comparator<Entry> ORDER = Comparator
      .comparing(Entry::hasPriority).reversed()
      .thenComparingLong(entry -> entry.sequence);

  private final int capacity;
  private final Map<UUID, Entry> entries = new LinkedHashMap<>();
  private long nextSequence;

  public PlayerQueue(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Queue capacity must be positive");
    }
    this.capacity = capacity;
  }

  public synchronized JoinResult join(UUID playerId, String playerName, boolean priority) {
    Objects.requireNonNull(playerId, "playerId");
    Objects.requireNonNull(playerName, "playerName");
    if (entries.containsKey(playerId)) {
      return JoinResult.ALREADY_QUEUED;
    }
    if (entries.size() >= capacity) {
      return JoinResult.FULL;
    }
    entries.put(playerId, new Entry(playerId, playerName, priority, nextSequence++));
    return JoinResult.ADDED;
  }

  public synchronized boolean remove(UUID playerId) {
    return entries.remove(playerId) != null;
  }

  public synchronized Entry pollNext() {
    Entry next = orderedEntries().stream().findFirst().orElse(null);
    if (next != null) {
      entries.remove(next.playerId);
    }
    return next;
  }

  public synchronized int position(UUID playerId) {
    final Entry target = entries.get(playerId);
    if (target == null) {
      return -1;
    }
    int position = 1;
    for (Entry entry : orderedEntries()) {
      if (entry.playerId.equals(playerId)) {
        return position;
      }
      position++;
    }
    return -1;
  }

  public synchronized List<Entry> snapshot() {
    return List.copyOf(orderedEntries());
  }

  public synchronized int size() {
    return entries.size();
  }

  public int capacity() {
    return capacity;
  }

  private List<Entry> orderedEntries() {
    final List<Entry> ordered = new ArrayList<>(entries.values());
    ordered.sort(ORDER);
    return ordered;
  }
}
