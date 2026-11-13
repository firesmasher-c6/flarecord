package me.firesmasher.flarecord.setup.flarecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FlarecordSetup {

  private FlarecordSetup() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void createDefaultConfigs(Path directory) throws IOException {
    final Path base = directory == null ? Path.of("") : directory;
    Files.createDirectories(base);
    Files.writeString(base.resolve("velocity.yml"), defaultVelocityYaml(), StandardCharsets.UTF_8);
    Files.writeString(base.resolve("flarecord.yml"), defaultFlarecordYaml(), StandardCharsets.UTF_8);
    Files.writeString(base.resolve("animations.yml"), defaultAnimationsYaml(), StandardCharsets.UTF_8);
  }

  public static String defaultVelocityYaml() {
    return String.join(System.lineSeparator(),
        "# Flarecord proxy configuration",
        "# This file replaces the legacy Velocity TOML config as the active primary config.",
        "# The upstream default-velocity.toml remains only as a fallback template for migration.",
        "",
        "# Bind address and port for the proxy listener.",
        "bind: \"0.0.0.0:25565\"",
        "",
        "# The public MOTD shown in server list responses.",
        "# MiniMessage formatting is supported here.",
        "motd: \"<#09add3>A Flarecord proxy\"",
        "",
        "show-max-players: 500",
        "online-mode: true",
        "player-info-forwarding-mode: NONE",
        "forwarding-secret-file: \"forwarding.secret\"",
        "",
        "servers:",
        "  lobby: \"127.0.0.1:30066\"",
        "  try:",
        "    - \"lobby\"",
        "",
        "forced-hosts: {}",
        "",
        "advanced:",
        "  compression-threshold: 256",
        "  compression-level: -1",
        "  login-ratelimit: 3000",
        "  connection-timeout: 5000",
        "  read-timeout: 30000",
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

  public static String defaultFlarecordYaml() {
    return String.join(System.lineSeparator(),
        "# Flarecord feature configuration",
        "# Values here override the equivalent values from velocity.yml where applicable.",
        "",
        "general:",
        "  enabled: true",
        "  debug: false",
        "  logging:",
        "    console: \"INFO\"",
        "    player-connections: true",
        "",
        "performance:",
        "  async-processing: true",
        "  worker-threads: 2",
        "  queue-limit: 2048",
        "  scheduling: \"bounded\"",
        "",
        "security:",
        "  anti-bot:",
        "    enabled: true",
        "    max-connections-per-ip: 20",
        "    temporary-block-duration-seconds: 120",
        "  join-rate-limit:",
        "    max-joins-per-minute: 30",
        "    max-joins-per-ip: 10",
        "",
        "queues:",
        "  enabled: true",
        "  max-size: 1000",
        "  priority-permission: \"flarecord.queue.priority\"",
        "",
        "motd:",
        "  enabled: true",
        "  line-1: \"<#09add3>Flarecord\"",
        "  line-2: \"<#8ecae6>Better Velocity\"",
        "",
        "animations:",
        "  enabled: true",
        "  default-animation: \"default\"",
        "",
        "messages:",
        "  queue-position: \"<gray>You are #<queue_position> in queue</gray>\"",
        "  connection-limited: \"<red>Too many connections from this IP</red>\"",
        "  queue-join: \"<green>You have joined the queue.</green>\"");
  }

  public static String defaultAnimationsYaml() {
    return String.join(System.lineSeparator(),
        "# Flarecord actionbar animation definitions",
        "# Frames are sent with MiniMessage formatting and a per-frame delay.",
        "",
        "animations:",
        "  default:",
        "    duration-ms: 500",
        "    frames:",
        "      - \"<aqua>Loading server...</aqua>\"",
        "      - \"<blue>Loading server...</blue>\"",
        "      - \"<dark_aqua>Loading server...</dark_aqua>\"",
        "",
        "  welcome:",
        "    duration-ms: 1250",
        "    frames:",
        "      - \"<gold>Welcome to the network!</gold>\"",
        "      - \"<yellow>Welcome to the network!</yellow>\"");
  }
}
