package me.firesmasher.flarecord.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FlarecordThreadingTest {

  @Test
  void rejectsNonPositiveExplicitWorkerCounts() {
    assertThrows(IllegalArgumentException.class,
        () -> FlarecordThreading.parseWorkerThreadCount("0"));
    assertThrows(IllegalArgumentException.class,
        () -> FlarecordThreading.parseWorkerThreadCount("-2"));
  }

  @Test
  void usesNettyDefaultWhenNoOverrideIsConfigured() {
    assertEquals(0, FlarecordThreading.parseWorkerThreadCount(null));
    assertEquals(0, FlarecordThreading.parseWorkerThreadCount(" "));
  }

  @Test
  void acceptsAnExplicitWorkerCount() {
    assertEquals(6, FlarecordThreading.parseWorkerThreadCount("6"));
  }
}
