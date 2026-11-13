package com.velocitypowered.proxy.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VelocityYamlConfigurationTest {

  @TempDir
  Path tempDir;

  @Test
  void readsNestedServersAndForcedHostsFromYaml() throws IOException {
    final Path secretPath = tempDir.resolve("forwarding.secret");
    final String secretPathYaml = secretPath.toString().replace("\\", "/");
    final String yaml = """
        bind: "127.0.0.1:25565"
        forwarding-secret-file: "%s"
        servers:
          lobby: "127.0.0.1:30066"
          factions: "127.0.0.1:30067"
          try:
            - "lobby"
        forced-hosts:
          "lobby.example.com":
            - "lobby"
        """.formatted(secretPathYaml);
    final Path configPath = tempDir.resolve("velocity.yml");
    Files.writeString(configPath, yaml);

    final VelocityConfiguration config = VelocityConfiguration.read(configPath);

    assertEquals(Set.of("lobby", "factions"), config.getServers().keySet());
    assertEquals(Set.of("lobby"),
        Set.copyOf(config.getForcedHosts().get("lobby.example.com")));
  }
}
