package io.rmel.mock;

import java.util.Objects;

public final class Assertions {
  private Assertions() {}

  public static void assertEqual(Object actual, Object expected) {
    if (!Objects.equals(expected, actual)) {
      throw new RuntimeException(
          "Assertion failed: Expected '" + expected + "', but got '"
              + actual + "'.");
    }
  }

  public static <T> void assertThrows(Class<T> expected, ThrowingFunction f) {
    try {
      f.run();
    } catch (Throwable t) {
      if (!expected.isAssignableFrom(t.getClass())) {
        throw new RuntimeException("Assertion failed: Expected '" +
            expected + "', but was '" + t + "'.");
      }
    }
    throw new RuntimeException("Assertion failed: Expected an exception.");
  }

  @FunctionalInterface
  public interface ThrowingFunction {
    void run() throws Throwable;
  }
}
