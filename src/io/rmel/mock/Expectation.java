package io.rmel.mock;

final class Expectation {

  static void run(Runnable expectations, Runnable test) {
    Thread expectationsThread = new Thread(expectations, "expectations");
    expectationsThread.setUncaughtExceptionHandler((th, ex) -> {});
    expectationsThread.start();

    test.run();
  }
}
