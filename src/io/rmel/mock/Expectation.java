package io.rmel.mock;

final class Expectation {

  static void run(
      Joiner j, ThrowingFunction expectations, ThrowingFunction test) {
    Thread expectationsThread = new Thread(
      () -> {
        try {
          expectations.run();
        } catch (Exception e) {
          throw new UnexpectedException(e);
        }
        j.endExpectations();
      }, "expectations");
    expectationsThread.setUncaughtExceptionHandler((th, ex) -> {});
    expectationsThread.start();

    try {
      test.run();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new UnexpectedException(e);
    }
    j.endStimulus();
  }

  @FunctionalInterface
  interface ThrowingFunction {
    void run() throws Exception;
  }
}
