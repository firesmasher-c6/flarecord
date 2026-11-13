package me.firesmasher.flarecord.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerQueueTest {

  @Test
  void enforcesCapacityAndOrdersPriorityBeforeFifo() {
    final PlayerQueue queue = new PlayerQueue(2);
    final UUID first = UUID.randomUUID();
    final UUID priority = UUID.randomUUID();
    final UUID overflow = UUID.randomUUID();

    assertEquals(PlayerQueue.JoinResult.ADDED, queue.join(first, "first", false));
    assertEquals(PlayerQueue.JoinResult.ALREADY_QUEUED, queue.join(first, "first", true));
    assertEquals(PlayerQueue.JoinResult.ADDED, queue.join(priority, "priority", true));
    assertEquals(PlayerQueue.JoinResult.FULL, queue.join(overflow, "overflow", false));
    assertEquals(1, queue.position(priority));
    assertEquals(2, queue.position(first));

    assertEquals(priority, queue.pollNext().getPlayerId());
    assertTrue(queue.remove(first));
    assertFalse(queue.remove(first));
    assertNull(queue.pollNext());
  }
}
