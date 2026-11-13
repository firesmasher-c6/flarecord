package me.firesmasher.flarecord.animation;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public final class AnimationCatalog {

  public static final class Animation {

    private final long durationMillis;
    private final List<String> frames;

    private Animation(long durationMillis, List<String> frames) {
      this.durationMillis = durationMillis;
      this.frames = List.copyOf(frames);
    }

    public long getDurationMillis() {
      return durationMillis;
    }

    public List<String> getFrames() {
      return frames;
    }
  }

  private final Map<String, Animation> animations;

  private AnimationCatalog(Map<String, Animation> animations) {
    this.animations = Collections.unmodifiableMap(new LinkedHashMap<>(animations));
  }

  public static AnimationCatalog load(Path path) throws IOException {
    final Object loaded;
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      loaded = new Yaml().load(reader);
    }
    if (!(loaded instanceof Map<?, ?> root)
        || !(root.get("animations") instanceof Map<?, ?> entries)) {
      throw new IOException(
          "Animations configuration must contain an 'animations' mapping: " + path);
    }

    final Map<String, Animation> animations = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : entries.entrySet()) {
      if (!(entry.getKey() instanceof String name) || name.isBlank()
          || !(entry.getValue() instanceof Map<?, ?> definition)) {
        throw new IOException("Each animation must have a non-empty name and a mapping");
      }
      final Object durationValue = definition.get("duration-ms");
      if (!(durationValue instanceof Number duration)
          || !Double.isFinite(duration.doubleValue())
          || duration.doubleValue() != Math.rint(duration.doubleValue())
          || duration.doubleValue() < 1
          || duration.doubleValue() > Long.MAX_VALUE) {
        throw new IOException("Animation '" + name + "' must have a positive duration-ms");
      }
      final Object frameValue = definition.get("frames");
      if (!(frameValue instanceof List<?> rawFrames) || rawFrames.isEmpty()) {
        throw new IOException("Animation '" + name + "' must contain at least one frame");
      }
      final List<String> frames = new ArrayList<>(rawFrames.size());
      for (Object frame : rawFrames) {
        if (!(frame instanceof String text) || text.isBlank()) {
          throw new IOException("Animation '" + name + "' contains an empty or non-string frame");
        }
        frames.add(text);
      }
      animations.put(name, new Animation(duration.longValue(), frames));
    }
    if (animations.isEmpty()) {
      throw new IOException("At least one animation must be configured in " + path);
    }
    return new AnimationCatalog(animations);
  }

  public Map<String, Animation> getAnimations() {
    return animations;
  }

  public Animation get(String name) {
    return animations.get(name);
  }
}
