package io.rmel.mock;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Joiner {

  private String failure;
  private StackTraceElement[] trace;
  
  private Object[] arguments;
  private Object[] expected;
  private Object returnValue;
  private Object pendingReturnValue;
  private String expectationInstanceId;
  private String expectationMethodId;
  private String callInstanceId;
  private String callMethodId;

  private final CyclicBarrier barrier =
      new CyclicBarrier(2, () -> Joiner.this.checkExpectation());

  public Joiner() {}

  private void checkExpectation() {
    failure = null;

    if (!expectationInstanceId.equals(callInstanceId)) {
      failure = "Method call to wrong mock instance.";
      return;
    }

    if (!expectationInstanceId.equals(callInstanceId)) {
      failure = "Method call to wrong method on right mock.";
      return;
    }

    if (expected.length != arguments.length) {
      failure = "Expected " + expected.length + " arguments but got "
          + arguments.length + ".";
    }

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != arguments[i] && !expected[i].equals(arguments[i])) {
        failure =
            "Expected '" + expected[i] + "' but got '" + arguments[i] + "'.";
        return;
      }
    }

    this.returnValue = this.pendingReturnValue;
  }

  private StackTraceElement[] trimTrace(StackTraceElement[] trace) {
    return Arrays.copyOfRange(trace, 1, trace.length);
  }

  public void expect(Object returnValue) {
    this.pendingReturnValue = returnValue;

    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      throw new RuntimeException("Interrupted while waiting for mock call.", e);
    }

    if (failure != null) {
      throw new ExpectationFailedException("Expectations thread.");
    }
  }

  public void expectation(
      String instanceId,
      String methodId,
      Object[] expected,
      StackTraceElement[] trace) {
    this.expectationInstanceId = instanceId;
    this.expectationMethodId = methodId;
    this.expected = expected;
    this.trace = trace;
  }

  public Object call(String instanceId, String methodId, Object[] arguments) {
    this.callInstanceId = instanceId;
    this.callMethodId = methodId;
    this.arguments = arguments;

    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      throw new RuntimeException(
          "Interrupted while waiting for expectation.", e);
    }

    if (failure != null) {
      // Stack trace, message.
      Exception cause = new ExpectationFailedException("Awaiting expectation.");
      cause.setStackTrace(trimTrace(trace));
      ExpectationFailedException e =
          new ExpectationFailedException(failure, cause);
      e.setStackTrace(trimTrace(e.getStackTrace()));
      throw e;
    }

    return returnValue;
  }
}

