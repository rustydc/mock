package io.rmel.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.hamcrest.Matcher;

final class Joiner {

  private String failure;
  private StackTraceElement[] trace;
  
  private Object[] arguments;
  private List<Matcher<? extends Object>> matchers = new ArrayList<>();
  private Object[] expected;
  private Object returnValue;
  private Throwable throwable;
  private Object pendingReturnValue;
  private Throwable pendingThrowable;
  private String expectationInstanceId;
  private String expectationMethodId;
  private String callInstanceId;
  private String callMethodId;

  private final CyclicBarrier barrier =
      new CyclicBarrier(2, () -> Joiner.this.checkExpectation());

  private void checkExpectation() {
    failure = null;
    if (expectationMethodId == null && callMethodId == null) {
      // Successful completion.
      return;
    }

    if (expectationMethodId == null) {
      failure = "Unexpected method call.";
      return;
    }

    if (callMethodId == null) {
      failure = "Expectation unfulfilled.";
      return;
    }

    if (!expectationInstanceId.equals(callInstanceId)) {
      failure = "Method call to wrong mock instance.";
      return;
    }

    if (!expectationInstanceId.equals(callInstanceId)) {
      failure = "Method call to wrong method.";
      return;
    }

    if (expected.length > 0 && !matchers.isEmpty()) {
      failure = "Can't mix and match values and matchers.";
      return;
    }

    if (expected.length > 0 && arguments.length > 0) {
      if (expected.length != arguments.length) {
        failure = "Expected " + expected.length + " arguments but got "
            + arguments.length + ".";
        return;
      }

      for (int i = 0; i < expected.length; i++) {
        if (expected[i] != arguments[i] && !expected[i].equals(arguments[i])) {
          failure =
              "Expected '" + expected[i] + "' but got '" + arguments[i] + "'.";
          return;
        }
      }
    }

    if (expected.length > 0 && !matchers.isEmpty()) {
      if (expected.length != matchers.size()) {
        failure = "Expected " + matchers.size() + " arguments but got "
            + arguments.length + ".";
        return;
      }

      for (int i = 0; i < matchers.size(); i++) {
        if (!matchers.get(i).matches(arguments[i])) {
        }
        if (expected[i] != arguments[i] && !expected[i].equals(arguments[i])) {
          failure =
              "Expected '" + expected[i] + "' but got '" + arguments[i] + "'.";
          return;
        }
      }
    }

    this.returnValue = this.pendingReturnValue;
    this.throwable = this.pendingThrowable;
  }

  private StackTraceElement[] trimTrace(StackTraceElement[] trace) {
    return Arrays.copyOfRange(trace, 3, trace.length);
  }


  void expectThrow(Throwable t) {
    this.pendingThrowable = t;

    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      throw new RuntimeException("Interrupted while waiting for mock call.", e);
    }

    if (failure != null) {
      throw new ExpectationFailedException("Expectations thread.");
    }
  }

  void expect(Object returnValue) {
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

  void expectation(
      String instanceId,
      String methodId,
      Object[] expected,
      StackTraceElement[] trace) {
    this.expectationInstanceId = instanceId;
    this.expectationMethodId = methodId;
    this.expected = expected;
    this.trace = trace;
  }

  void endExpectations() {
    this.expectationMethodId = null;
    this.trace = null;
    expect(null);
  }

  void endStimulus() {
    this.callMethodId = null;

    // TODO(rmel): Deduplicate.
    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      throw new RuntimeException(
          "Interrupted while waiting for expectation.", e);
    }

    if (failure != null) {
      if (trace != null) {
        // Stack trace, message.
        ExpectationFailedException e =
            new ExpectationFailedException(failure);
        e.setStackTrace(trimTrace(trace));
        throw e;
      }
      ExpectationFailedException e =
          new ExpectationFailedException(failure);
      e.setStackTrace(trimTrace(e.getStackTrace()));
      throw e;
    }
  }

  Object call(String instanceId, String methodId, Object[] arguments)
      throws Throwable {
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
      if (trace != null) {
        // Stack trace, message.
        Exception cause =
            new ExpectationFailedException("Awaiting expectation.");
        cause.setStackTrace(trimTrace(trace));
        ExpectationFailedException e =
            new ExpectationFailedException(failure, cause);
        e.setStackTrace(trimTrace(e.getStackTrace()));
        throw e;
      }
      ExpectationFailedException e =
          new ExpectationFailedException(failure);
      e.setStackTrace(trimTrace(e.getStackTrace()));
      throw e;
    }

    if (throwable != null) {
      throw throwable;
    }

    return returnValue;
  }
 
  void argThat(Matcher<? extends Object> matcher) {
    matchers.add(matcher);
  }
}

