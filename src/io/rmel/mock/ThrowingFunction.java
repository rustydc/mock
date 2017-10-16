package io.rmel.mock;

@FunctionalInterface
public interface ThrowingFunction {
  void run() throws Throwable;
}
