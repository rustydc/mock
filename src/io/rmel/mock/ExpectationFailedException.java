package io.rmel.mock;

public class ExpectationFailedException extends RuntimeException {
  public ExpectationFailedException(String message) {
    super(message);
  }

  public ExpectationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
