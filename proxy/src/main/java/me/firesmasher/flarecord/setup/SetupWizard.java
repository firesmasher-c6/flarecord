package me.firesmasher.flarecord.setup;

import com.velocitypowered.proxy.util.AddressUtil;
import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import me.firesmasher.flarecord.util.FlarecordConsoleLogger;

public final class SetupWizard {

  private static final String DEFAULT_BIND = "0.0.0.0:25565";
  private static final String DEFAULT_BACKEND_NAME = "lobby";

  private SetupWizard() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static boolean runIfNeeded(Path directory, Console console,
      Map<String, String> environment) throws IOException {
    final Path configPath = directory.resolve(Setups.VELOCITY_YML);
    final boolean force = isRequested(environment);
    if (!force && (Files.exists(configPath) || Files.exists(directory.resolve("velocity.toml")))) {
      return false;
    }

    Files.createDirectories(directory);
    if (force && Files.exists(configPath)) {
      final Path backupPath = backupPath(configPath);
      Files.copy(configPath, backupPath);
      FlarecordConsoleLogger.warn("Existing configuration was backed up to {}",
          backupPath.toAbsolutePath());
    }
    final boolean interactive = console != null;
    if (interactive) {
      console.printf("%n=== Flarecord first-run setup ===%n");
      console.printf("Press Enter to accept a default. Leave the backend address blank to "
          + "configure it later.%n");
    } else {
      FlarecordConsoleLogger.info(
          "No interactive console detected; using environment variables and safe defaults.");
    }

    final String bind = addressAnswer(console, environment, "FLARECORD_BIND",
        "Proxy bind address", DEFAULT_BIND, false);
    final String backendAddress = addressAnswer(console, environment,
        "FLARECORD_BACKEND_ADDRESS", "Backend address (host:port)", "", true);
    final String backendName = backendAddress.isEmpty()
        ? DEFAULT_BACKEND_NAME
        : nameAnswer(console, environment, "FLARECORD_BACKEND_NAME", "Backend name",
            DEFAULT_BACKEND_NAME);
    final String publicHost = textAnswer(console, environment, "FLARECORD_PUBLIC_HOST",
        "Public hostname (optional)", "");

    final String yaml = createVelocityYaml(bind, backendName, backendAddress, publicHost);
    if (force) {
      Files.writeString(configPath, yaml, StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
    } else {
      Files.writeString(configPath, yaml, StandardCharsets.UTF_8,
          StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    FlarecordConsoleLogger.info("First-run setup created {}",
        configPath.toAbsolutePath());
    if (backendAddress.isEmpty()) {
      FlarecordConsoleLogger.warn(
          "No backend was configured. Add a server under 'servers' in {} before connecting players.",
          configPath.getFileName());
    } else {
      FlarecordConsoleLogger.info("Configured backend '{}' at {}", backendName, backendAddress);
    }
    return true;
  }

  public static boolean isRequested(Map<String, String> environment) {
    final String requested = environment.get("FLARECORD_SETUP");
    return requested != null && (requested.equalsIgnoreCase("true")
        || requested.equals("1") || requested.equalsIgnoreCase("yes"));
  }

  private static Path backupPath(Path configPath) {
    final String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    Path backupPath = configPath.resolveSibling(configPath.getFileName() + ".backup");
    if (Files.exists(backupPath)) {
      backupPath = configPath.resolveSibling(
          configPath.getFileName() + "." + timestamp + ".backup");
    }
    return backupPath;
  }

  private static String addressAnswer(Console console, Map<String, String> environment,
      String environmentKey, String label, String defaultValue, boolean allowEmpty)
      throws IOException {
    final String configured = environment.get(environmentKey);
    String answer = configured == null || configured.isBlank()
        ? prompt(console, label, defaultValue)
        : configured.trim();
    while (true) {
      if (answer.isEmpty() && allowEmpty) {
        return "";
      }
      try {
        AddressUtil.parseAddress(answer);
        return answer;
      } catch (IllegalArgumentException e) {
        if (console == null) {
          throw new IOException("Invalid address in " + environmentKey + ": " + answer, e);
        }
        console.printf("Invalid address. Enter a host and port, for example 127.0.0.1:25565.%n");
        answer = prompt(console, label, defaultValue);
      }
    }
  }

  private static String nameAnswer(Console console, Map<String, String> environment,
      String environmentKey, String label, String defaultValue) throws IOException {
    final String configured = environment.get(environmentKey);
    final String answer = configured == null || configured.isBlank()
        ? prompt(console, label, defaultValue)
        : configured.trim();
    if (!answer.matches("[A-Za-z0-9_-]+")) {
      if (console == null) {
        throw new IOException("Invalid backend name in " + environmentKey
            + "; use only letters, numbers, underscores, and hyphens.");
      }
      console.printf("Invalid name. Use only letters, numbers, underscores, and hyphens.%n");
      return nameAnswer(console, Map.of(environmentKey, defaultValue), environmentKey,
          label, defaultValue);
    }
    return answer;
  }

  private static String textAnswer(Console console, Map<String, String> environment,
      String environmentKey, String label, String defaultValue) throws IOException {
    final String configured = environment.get(environmentKey);
    final String answer = configured == null ? prompt(console, label, defaultValue) : configured;
    if (answer.chars().anyMatch(Character::isWhitespace)) {
      if (console == null) {
        throw new IOException(environmentKey + " must not contain whitespace");
      }
      console.printf("Hostname must not contain whitespace.%n");
      return textAnswer(console, Map.of(environmentKey, defaultValue), environmentKey,
          label, defaultValue);
    }
    return answer.trim();
  }

  private static String prompt(Console console, String label, String defaultValue) {
    if (console == null) {
      return defaultValue;
    }
    final String answer = console.readLine("%s [%s]: ", label, defaultValue);
    return answer == null || answer.isBlank() ? defaultValue : answer.trim();
  }

  private static String createVelocityYaml(String bind, String backendName, String backendAddress,
      String publicHost) {
    final StringBuilder yaml = new StringBuilder(String.join(System.lineSeparator(),
        "# Flarecord first-run configuration. Edit this file to add or change backends.",
        "config-version: \"2.9\"",
        "bind: " + quote(bind),
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
        "ping-passthrough:",
        "  version: false",
        "  players: false",
        "  description: false",
        "  favicon: false",
        "  modinfo: false",
        "packet-limiter:",
        "  interval: 7",
        "  packets-per-second: -1",
        "  bytes-per-second: -1",
        "  decompressed-bytes-per-second: 5242880",
        "servers:"));
    if (backendAddress.isEmpty()) {
      yaml.append(System.lineSeparator()).append("  try: []");
    } else {
      yaml.append(System.lineSeparator()).append("  ").append(backendName)
          .append(": ").append(quote(backendAddress))
          .append(System.lineSeparator()).append("  try:")
          .append(System.lineSeparator()).append("    - ").append(quote(backendName));
    }

    yaml.append(System.lineSeparator()).append("forced-hosts:");
    if (!publicHost.isEmpty() && !backendAddress.isEmpty()) {
      yaml.append(System.lineSeparator()).append("  ").append(quote(publicHost)).append(":")
          .append(System.lineSeparator()).append("    - ").append(quote(backendName));
    } else {
      yaml.append(" {}");
    }
    yaml.append(System.lineSeparator()).append(String.join(System.lineSeparator(),
        "advanced:",
        "  compression-threshold: 256",
        "  compression-level: -1",
        "  login-ratelimit: 3000",
        "  connection-timeout: 5000",
        "  read-timeout: 30000",
        "query:",
        "  enabled: false",
        "  port: 25565",
        "  map: \"Flarecord\"",
        "  show-plugins: false",
        "metrics:",
        "  enabled: true"));
    return yaml.append(System.lineSeparator()).toString();
  }

  private static String quote(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
