package io.rmel.mock;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

final class MockExpectation {
  // TODO(rmel): More restrictive interface?
  private final MockRun mockRun;
 
  private long instanceId;
  private String methodName;
  private Object[] parameters;
  private Object returnValue;
  private Throwable throwable;
  private StackTraceElement[] trace;

  private boolean hasReturn;
  private boolean hasExpectation;

  private List<Matcher<Object>> matchers = new ArrayList<>();

  MockExpectation(MockRun mockRun) {
    this.mockRun = mockRun;
  }

  void setReturnValue(Object returnValue) {
    // TODO(rmel): Assert !hasReturn.
    this.returnValue = returnValue;

    hasReturn = true;
    waitIfReady();
  }

  void setThrowable(Throwable throwable) {
    // TODO(rmel): Assert returnValue/throwable are null.
    this.throwable = throwable;

    hasReturn = true;
    waitIfReady();
  }
  
  void setExpectation(long instanceId, String methodName, Object[] parameters) {
    // TODO(rmel): If any matchers, make sure we have the right number.
    // TODO(rmel): Assert instanceId/methodName are null.
    this.instanceId = instanceId;
    this.methodName = methodName;
    this.parameters = parameters;
    this.trace = Thread.currentThread().getStackTrace();

    hasExpectation = true;
    waitIfReady();
  }

  void addMatcher(Matcher<Object> matcher) {
    // TODO(rmel): Assert parameters is null.
    matchers.add(matcher);
  }

  private void waitIfReady() {
    if (!hasReturn || !hasExpectation) {
      return;
    }
    mockRun.awaitCall();
  }

  ExpectationResult validateCall(
      long instanceId, String methodName, Object[] params) {
    assert hasReturn && hasExpectation;

    if (this.instanceId == 0 && instanceId != 0) {
      // TODO(rmel): No expectation stack-trace needed here.
      return fail("No more method calls were expected.");
    }

    if (this.instanceId != 0 && instanceId == 0) {
      return fail("Not all expected methods were called.");
    }

    if (this.instanceId == 0 && instanceId == 0) {
      return new ExpectationResult(true, null);
    }

    if (this.instanceId != instanceId) {
      return fail("Call to wrong mock instance: " + instanceId + " vs " + this.instanceId);
    }
    if (this.methodName != methodName) {
      return fail("Call to wrong mock method: " + methodName + " vs " + this.methodName);
    }
    if (!matchers.isEmpty()) {
      for (int i = 0; i < params.length; i++) {
        if (!matchers.get(i).matches(params[i])) {
          return fail(matchDescription(matchers.get(i), params[i]));
        }
      }
    } else {
      for (int i = 0; i < params.length; i++) {
        if (this.parameters[i] != params[i]
            && !this.parameters[i].equals(params[i])) {
          return fail("Expected '" + parameters[i] + "' but got '"
              + params[i] + "'.");
        }
      }
    }

    // Success.
    return new ExpectationResult(returnValue, throwable);
  }

  private ExpectationResult fail(String failure) {
    return new ExpectationResult(failure, trace);
  }

  private <T> String matchDescription(Matcher<T> matcher, T object) {
    StringDescription d = new StringDescription();
    d.appendText("Expected [");
    matcher.describeTo(d);
    d.appendText("], but got [");
    matcher.describeMismatch(object, d);
    d.appendText("].");
    return d.toString();
  }
}
