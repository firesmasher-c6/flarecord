package me.firesmasher.flarecord.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.proxy.config.VelocityConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SetupWizardTest {

  @TempDir
  Path tempDir;

  @Test
  void createsHeadlessSetupFromEnvironmentWithoutExampleHostRoutes() throws IOException {
    final Map<String, String> environment = Map.of(
        "FLARECORD_BACKEND_NAME", "lobby",
        "FLARECORD_BACKEND_ADDRESS", "127.0.0.1:30066",
        "FLARECORD_PUBLIC_HOST", "play.example.net");

    assertTrue(SetupWizard.runIfNeeded(tempDir, null, environment));
    final String config = Files.readString(tempDir.resolve("velocity.yml"));
    assertTrue(config.contains("lobby: \"127.0.0.1:30066\""));
    assertTrue(config.contains("\"play.example.net\":"));
    assertFalse(config.contains("factions.example.com"));

    final Path secretPath = tempDir.resolve("forwarding.secret");
    final Path configPath = tempDir.resolve("velocity.yml");
    Files.writeString(configPath,
        config.replace("\"forwarding.secret\"",
            "\"" + secretPath.toString().replace("\\", "/") + "\""));
    final VelocityConfiguration loaded = VelocityConfiguration.read(configPath);
    assertEquals(Set.of("lobby"), loaded.getServers().keySet());
    assertEquals(Set.of("lobby"),
        Set.copyOf(loaded.getForcedHosts().get("play.example.net")));
    assertFalse(SetupWizard.runIfNeeded(tempDir, null, environment));
  }

  @Test
  void leavesBackendsUnconfiguredWhenRunningHeadlessWithoutInputs() throws IOException {
    Setups.bootstrap(tempDir, null, Map.of());
    final String config = Files.readString(tempDir.resolve("velocity.yml"));
    assertTrue(config.contains("  try: []"));
    assertTrue(config.contains("forced-hosts: {}"));
    assertTrue(Files.exists(tempDir.resolve("flarecord.yml")));
    assertTrue(Files.exists(tempDir.resolve("animations.yml")));

    final Path secretPath = tempDir.resolve("forwarding.secret");
    final Path configPath = tempDir.resolve("velocity.yml");
    Files.writeString(configPath,
        config.replace("\"forwarding.secret\"",
            "\"" + secretPath.toString().replace("\\", "/") + "\""));
    assertTrue(VelocityConfiguration.read(configPath).validate());
  }

  @Test
  void forcedWizardRunBacksUpExistingConfig() throws IOException {
    final Path configPath = tempDir.resolve("velocity.yml");
    Files.writeString(configPath, "old config");

    assertTrue(SetupWizard.runIfNeeded(tempDir, null, Map.of("FLARECORD_SETUP", "1")));

    assertTrue(Files.exists(tempDir.resolve("velocity.yml.backup")));
    assertEquals("old config", Files.readString(tempDir.resolve("velocity.yml.backup")));
    assertTrue(Files.readString(configPath).contains("config-version:"));
  }

  @Test
  void existingTomlIsMigratedBeforeAnyYamlDefaultsAreWritten() throws IOException {
    Files.writeString(tempDir.resolve("velocity.toml"), "");

    Setups.bootstrap(tempDir, null, Map.of());

    final String yaml = Files.readString(tempDir.resolve("velocity.yml"));
    assertTrue(yaml.contains("config-version: \"2.9\""));
    assertTrue(yaml.contains("lobby.example.com"));
    assertTrue(Files.exists(tempDir.resolve("flarecord.yml")));
    assertTrue(Files.exists(tempDir.resolve("animations.yml")));
  }
}
