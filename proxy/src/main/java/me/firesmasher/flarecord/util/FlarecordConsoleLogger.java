package me.firesmasher.flarecord.util;

import me.firesmasher.flarecord.Flarecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FlarecordConsoleLogger {

  private static final Logger logger = LogManager.getLogger("Flarecord");

  private FlarecordConsoleLogger() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void banner() {
    logger.info("============================================================");
    logger.info("Starting {}", Flarecord.getDisplayName());
    logger.info("Author: {}", Flarecord.AUTHOR);
    logger.info("============================================================");
  }

  public static void info(String message, Object... args) {
    logger.info(message, args);
  }

  public static void warn(String message, Object... args) {
    logger.warn(message, args);
  }

  public static void error(String message, Object... args) {
    logger.error(message, args);
  }
}
