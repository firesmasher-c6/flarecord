package me.firesmasher.flarecord;

public final class Flarecord {

  public static final String NAME = "Flarecord";
  public static final String AUTHOR = "FireSmasher";
  public static final String VENDOR = "Flarecord Contributors";
  public static final String VERSION = "1.00.0-A";
  public static final String DISPLAY_NAME = NAME + " " + VERSION;

  private Flarecord() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String getName() {
    return NAME;
  }

  public static String getVersion() {
    return VERSION;
  }

  public static String getDisplayName() {
    return DISPLAY_NAME;
  }

  public static String getVendor() {
    return VENDOR;
  }
}
