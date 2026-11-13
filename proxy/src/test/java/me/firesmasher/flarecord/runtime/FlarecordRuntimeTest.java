package me.firesmasher.flarecord.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlarecordRuntimeTest {

  @TempDir
  Path tempDir;

  @Test
  void loadsFeatureSettingsAndValidatedAnimations() throws IOException {
    Files.writeString(tempDir.resolve("flarecord.yml"), """
        general:
          enabled: true
        queues:
          enabled: true
          max-size: 5
        animations:
          enabled: true
          default-animation: default
        """);
    Files.writeString(tempDir.resolve("animations.yml"), """
        animations:
          default:
            duration-ms: 500
            frames:
              - "<aqua>Ready</aqua>"
        """);

    final FlarecordRuntime runtime = FlarecordRuntime.load(tempDir);
    assertTrue(runtime.getConfiguration().isEnabled());
    assertEquals(5, runtime.getPlayerQueue().capacity());
    assertEquals(1, runtime.getAnimations().get("default").getFrames().size());
  }

  @Test
  void rejectsAnUndefinedConfiguredDefaultAnimation() throws IOException {
    Files.writeString(tempDir.resolve("flarecord.yml"), """
        animations:
          enabled: true
          default-animation: missing
        """);
    Files.writeString(tempDir.resolve("animations.yml"), """
        animations:
          default:
            duration-ms: 500
            frames:
              - "Ready"
        """);

    assertThrows(IOException.class, () -> FlarecordRuntime.load(tempDir));
  }
}
