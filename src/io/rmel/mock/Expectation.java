package io.rmel.mock;

public class Expectation {

  public static void run(Runnable expectations, Runnable test) {
    final Holder h = new Holder();
    Thread expectationsThread = new Thread(expectations, "expectations");
    expectationsThread.setUncaughtExceptionHandler((th, ex) -> {});
    expectationsThread.start();

    Thread testThread = new Thread(test, "test");
    testThread.setUncaughtExceptionHandler(
        (th, ex) -> {h.value = ex;});
    testThread.start();

    try { 
      testThread.join();
      if (h.value != null) {
        throw new RuntimeException("Test failed.", h.value);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while running test.", e);
    }
  }

  static class Holder {
    Throwable value = null;
  }
}
