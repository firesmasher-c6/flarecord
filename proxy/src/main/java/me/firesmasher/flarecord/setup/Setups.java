package me.firesmasher.flarecord.setup;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import me.firesmasher.flarecord.Flarecord;
import me.firesmasher.flarecord.setup.flarecord.FlarecordSetup;
import me.firesmasher.flarecord.setup.velocity.VelocitySetup;
import me.firesmasher.flarecord.util.FlarecordConsoleLogger;

public final class Setups {

  public static final String VELOCITY_YML = "velocity.yml";
  public static final String FLARECORD_YML = "flarecord.yml";
  public static final String ANIMATIONS_YML = "animations.yml";

  private Setups() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void bootstrap(Path baseDirectory) throws IOException {
    bootstrap(baseDirectory, System.console(), System.getenv());
  }

  static void bootstrap(Path baseDirectory, Console console, Map<String, String> environment)
      throws IOException {
    final Path directory = baseDirectory == null ? Path.of("") : baseDirectory;
    if (SetupWizard.isRequested(environment)) {
      SetupWizard.runIfNeeded(directory, console, environment);
    } else if (VelocitySetup.shouldMigrateToml(directory)) {
      VelocitySetup.migrateTomlToYaml(directory);
      FlarecordConsoleLogger.info("Migrated existing velocity.toml to {}", VELOCITY_YML);
    } else if (!Files.exists(directory.resolve(VELOCITY_YML))
        && !Files.exists(directory.resolve("velocity.toml"))) {
      SetupWizard.runIfNeeded(directory, console, environment);
    }
    ensureDefaultConfigs(directory);
  }

  public static void ensureDefaultConfigs(Path directory) throws IOException {
    Files.createDirectories(directory);
    writeIfAbsent(directory.resolve(VELOCITY_YML), FlarecordSetup.defaultVelocityYaml());
    writeIfAbsent(directory.resolve(FLARECORD_YML), FlarecordSetup.defaultFlarecordYaml());
    writeIfAbsent(directory.resolve(ANIMATIONS_YML), FlarecordSetup.defaultAnimationsYaml());
  }

  public static Path resolvePreferredConfigPath(Path directory) {
    final Path dir = directory == null ? Path.of("") : directory;
    if (Files.exists(dir.resolve(VELOCITY_YML))) {
      return dir.resolve(VELOCITY_YML);
    }
    if (Files.exists(dir.resolve("velocity.toml"))) {
      return dir.resolve("velocity.toml");
    }
    return dir.resolve(VELOCITY_YML);
  }

  public static void writeIfAbsent(Path path, String content) throws IOException {
    if (Files.exists(path)) {
      return;
    }
    Files.writeString(path, content, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
    FlarecordConsoleLogger.info("Created default config file {} using {} branding",
        path.getFileName(), Flarecord.getDisplayName());
  }

  public static String normalizedConfigName(String name) {
    return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
  }
}
