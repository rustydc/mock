package io.rmel.mock;

class ExpectationResult {
  private final Throwable throwable;
  private final Object returnValue;
  private final String failure;
  private final StackTraceElement[] stackTrace;

  ExpectationResult(Object returnValue, Throwable throwable) {
    // TODO(rmel): Assert that they're not both non-null.
    this.returnValue = returnValue;
    this.throwable = throwable;
    this.failure = null;
    this.stackTrace = null;
  }

  ExpectationResult(String failure, StackTraceElement[] stackTrace) {
    this.failure = failure;
    this.returnValue = null;
    this.throwable = null;
    this.stackTrace = stackTrace;
  }

  boolean successful() {
    return failure == null;
  }

  String getFailure() {
    return failure;
  }

  Object getReturnValue() {
    return returnValue;
  }

  Throwable getThrowable() {
    return throwable;
  }

  StackTraceElement[] getStackTrace() {
    return stackTrace;
  }
}
