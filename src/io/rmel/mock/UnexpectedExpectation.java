package io.rmel.mock;

class UnexpectedException extends RuntimeException {
  UnexpectedException(String message, Throwable t) {
    super(message, t);
  }

  UnexpectedException(Throwable t) {
    super(t);
  }
}
