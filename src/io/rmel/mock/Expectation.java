package io.rmel.mock;

final class Expectation {

  static void run(Joiner j, Runnable expectations, Runnable test) {
    Thread expectationsThread = new Thread(
      () -> {
        expectations.run();
        j.endExpectations();
      }, "expectations");
    expectationsThread.setUncaughtExceptionHandler((th, ex) -> {});
    expectationsThread.start();

    test.run();
    j.endStimulus();
  }
}
