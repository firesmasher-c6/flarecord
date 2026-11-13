package me.firesmasher.flarecord.setup.velocity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VelocitySetup {

  private VelocitySetup() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static boolean shouldMigrateToml(Path directory) {
    final Path dir = directory == null ? Path.of("") : directory;
    return Files.exists(dir.resolve("velocity.toml")) && !Files.exists(dir.resolve("velocity.yml"));
  }

  public static boolean migrateTomlToYaml(Path directory) throws IOException {
    final Path dir = directory == null ? Path.of("") : directory;
    final Path tomlPath = dir.resolve("velocity.toml");
    final Path yamlPath = dir.resolve("velocity.yml");
    if (!Files.exists(tomlPath) || Files.exists(yamlPath)) {
      return false;
    }

    final String toml = Files.readString(tomlPath, StandardCharsets.UTF_8);
    final String yaml = convertTomlToYaml(toml);
    Files.writeString(yamlPath, yaml, StandardCharsets.UTF_8);
    return true;
  }

  public static String convertTomlToYaml(String toml) {
    if (toml == null || toml.isBlank()) {
      return defaultYaml();
    }

    final StringBuilder output = new StringBuilder();
    String currentSection = null;
    final String[] lines = toml.split("\\R");
    for (String rawLine : lines) {
      String line = rawLine.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      if (line.startsWith("[") && line.endsWith("]")) {
        currentSection = normalizeSectionName(line.substring(1, line.length() - 1));
        output.append(currentSection).append(":\n");
        continue;
      }
      if (line.contains("=")) {
        final String[] split = line.split("=", 2);
        final String key = split[0].trim();
        final String value = split[1].trim();
        output.append("  ").append(key.replace("-", "-")).append(": ")
            .append(normalizeTomlValue(value)).append("\n");
      }
    }
    return output.length() == 0 ? defaultYaml() : output.toString();
  }

  private static String normalizeSectionName(String section) {
    return section == null ? "root"
        : section.trim().replace("\"", "").replace(' ', '-').toLowerCase(Locale.ROOT);
  }

  private static String normalizeTomlValue(String value) {
    String normalized = value.trim();
    if (normalized.startsWith("\"") && normalized.endsWith("\"")) {
      return normalized.substring(1, normalized.length() - 1);
    }
    if (normalized.equalsIgnoreCase("true") || normalized.equalsIgnoreCase("false")) {
      return normalized.toLowerCase(Locale.ROOT);
    }
    if (normalized.matches("-?\\d+")) {
      return normalized;
    }
    return normalized;
  }

  public static String defaultYaml() {
    return String.join(System.lineSeparator(),
        "# Flarecord proxy configuration",
        "# This file is the active primary config file for the proxy.",
        "# Velocity's default-velocity.toml remains as a fallback template for migration and compatibility.",
        "",
        "config-version: \"2.9\"",
        "bind: \"0.0.0.0:25565\"",
        "motd: \"<#09add3>A Flarecord proxy\"",
        "show-max-players: 500",
        "online-mode: true",
        "force-key-authentication: true",
        "prevent-client-proxy-connections: false",
        "player-info-forwarding-mode: \"NONE\"",
        "forwarding-secret-file: \"forwarding.secret\"",
        "announce-forge: false",
        "kick-existing-players: false",
        "sample-players-in-ping: false",
        "enable-player-address-logging: true",
        "",
        "ping-passthrough:",
        "  version: false",
        "  players: false",
        "  description: false",
        "  favicon: false",
        "  modinfo: false",
        "",
        "packet-limiter:",
        "  interval: 7",
        "  packets-per-second: -1",
        "  bytes-per-second: -1",
        "  decompressed-bytes-per-second: 5242880",
        "",
        "servers:",
        "  lobby: \"127.0.0.1:30066\"",
        "  factions: \"127.0.0.1:30067\"",
        "  minigames: \"127.0.0.1:30068\"",
        "  try:",
        "    - \"lobby\"",
        "",
        "forced-hosts:",
        "  \"lobby.example.com\":",
        "    - \"lobby\"",
        "  \"factions.example.com\":",
        "    - \"factions\"",
        "  \"minigames.example.com\":",
        "    - \"minigames\"",
        "",
        "advanced:",
        "  compression-threshold: 256",
        "  compression-level: -1",
        "  login-ratelimit: 3000",
        "  connection-timeout: 5000",
        "  read-timeout: 30000",
        "  haproxy-protocol: false",
        "  tcp-fast-open: false",
        "  bungee-plugin-message-channel: true",
        "  show-ping-requests: false",
        "  failover-on-unexpected-server-disconnect: true",
        "  announce-proxy-commands: true",
        "  log-command-executions: false",
        "  log-player-connections: true",
        "  accepts-transfers: false",
        "  enable-reuse-port: false",
        "  command-rate-limit: 50",
        "  forward-commands-if-rate-limited: true",
        "  kick-after-rate-limited-commands: 0",
        "  tab-complete-rate-limit: 10",
        "  kick-after-rate-limited-tab-completes: 0",
        "",
        "query:",
        "  enabled: false",
        "  port: 25565",
        "  map: \"Flarecord\"",
        "  show-plugins: false",
        "",
        "metrics:",
        "  enabled: true");
  }
}
