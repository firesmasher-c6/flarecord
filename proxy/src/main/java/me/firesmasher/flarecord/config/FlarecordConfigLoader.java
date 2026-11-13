package me.firesmasher.flarecord.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public final class FlarecordConfigLoader {

  private FlarecordConfigLoader() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static FlarecordConfiguration load(Path path) throws IOException {
    final Map<?, ?> root;
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      final Object loaded = new Yaml().load(reader);
      if (!(loaded instanceof Map<?, ?> map)) {
        throw new IOException("Flarecord configuration must contain a YAML mapping: " + path);
      }
      root = map;
    }

    final Map<?, ?> general = section(root, "general");
    final Map<?, ?> security = section(root, "security");
    final Map<?, ?> antiBot = section(security, "anti-bot");
    final Map<?, ?> queues = section(root, "queues");
    final Map<?, ?> animations = section(root, "animations");
    final int queueLimit = integer(queues, "max-size", 1000);
    final int maxConnectionsPerIp = integer(antiBot, "max-connections-per-ip", 20);
    final int blockDuration = integer(antiBot, "temporary-block-duration-seconds", 120);
    if (queueLimit < 1 || maxConnectionsPerIp < 1 || blockDuration < 1) {
      throw new IOException("Queue and anti-bot limits must be positive in " + path);
    }

    return new FlarecordConfiguration(
        bool(general, "enabled", true),
        bool(general, "debug", false),
        bool(queues, "enabled", true),
        queueLimit,
        string(queues, "priority-permission", "flarecord.queue.priority"),
        bool(antiBot, "enabled", true),
        maxConnectionsPerIp,
        blockDuration,
        bool(animations, "enabled", true),
        string(animations, "default-animation", "default"));
  }

  private static Map<?, ?> section(Map<?, ?> parent, String key) throws IOException {
    final Object value = parent.get(key);
    if (value == null) {
      return Map.of();
    }
    if (value instanceof Map<?, ?> map) {
      return map;
    }
    throw new IOException("Expected '" + key + "' to be a YAML mapping");
  }

  private static boolean bool(Map<?, ?> section, String key, boolean defaultValue)
      throws IOException {
    final Object value = section.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Boolean result) {
      return result;
    }
    throw new IOException("Expected '" + key + "' to be a boolean");
  }

  private static int integer(Map<?, ?> section, String key, int defaultValue) throws IOException {
    final Object value = section.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number number) {
      final double numericValue = number.doubleValue();
      if (Double.isFinite(numericValue) && numericValue == Math.rint(numericValue)
          && numericValue >= Integer.MIN_VALUE && numericValue <= Integer.MAX_VALUE) {
        return (int) numericValue;
      }
    }
    throw new IOException("Expected '" + key + "' to be an integer");
  }

  private static String string(Map<?, ?> section, String key, String defaultValue)
      throws IOException {
    final Object value = section.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof String result && !result.isBlank()) {
      return result;
    }
    throw new IOException("Expected '" + key + "' to be a non-empty string");
  }
}
