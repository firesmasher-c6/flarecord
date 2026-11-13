package me.firesmasher.flarecord.performance;

public final class FlarecordThreading {

  public static final String WORKER_THREADS_PROPERTY = "flarecord.netty-worker-threads";

  private FlarecordThreading() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Returns the configured worker count, or zero to let Netty size workers for available CPUs.
   *
   * @return the configured worker count or zero for Netty's adaptive default
   */
  public static int workerThreadCount() {
    return parseWorkerThreadCount(System.getProperty(WORKER_THREADS_PROPERTY));
  }

  static int parseWorkerThreadCount(String configured) {
    if (configured == null || configured.isBlank()) {
      return 0;
    }

    final int threadCount;
    try {
      threadCount = Integer.parseInt(configured);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          WORKER_THREADS_PROPERTY + " must be a positive integer", e);
    }
    if (threadCount < 1) {
      throw new IllegalArgumentException(WORKER_THREADS_PROPERTY + " must be a positive integer");
    }
    return threadCount;
  }
}
