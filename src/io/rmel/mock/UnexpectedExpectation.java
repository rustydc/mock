package io.rmel.mock;

class UnexpectedException extends RuntimeException {
  public UnexpectedException(Throwable t) {
    super(t);
  }
}
