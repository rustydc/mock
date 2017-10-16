package io.rmel.mock;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.hamcrest.Matcher;

final class MockRun {

  private MockExpectation expectation;
  private ExpectationResult result;

  private long instanceId;
  private String methodName;
  private Object[] parameters;

  private boolean done = false;
  private final CyclicBarrier barrier =
      new CyclicBarrier(2, () -> MockRun.this.checkExpectation());

  private Throwable toThrow = null;

  MockRun() {
    this.expectation = new MockExpectation(this);
  }

  void run(ThrowingFunction expectations, ThrowingFunction test) {
    Thread mainThread = Thread.currentThread();
    Thread expectationsThread = new Thread(
        () -> {
          try {
            expectations.run();
            expectationsDone();
          } catch (Throwable t) {
            if (!(t instanceof ExpectationFailedException)) {
              toThrow = t;
              mainThread.interrupt();
            }
          }
        }, "Expectations");
    expectationsThread.setUncaughtExceptionHandler((th, ex) -> {});
    expectationsThread.start();

    try {
      test.run();
      stimDone();
    } catch (Throwable t) {
      if (!done) {
        done = true;
        expectationsThread.interrupt();
      }

      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      throw new UnexpectedException("Unexpected exception from test", t);
    }
  }

  void awaitCall() {
    if (done) {
      throw new RuntimeException(
          "Can't expect call, expectations already failed.", toThrow);
    }
    
    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      throw new RuntimeException(e);
    }

    this.expectation = new MockExpectation(this);;
  }

  Object call(long instanceId, String methodName, Object[] parameters)
      throws Throwable {
    if (done) {
      throw trimTrace(3,
          new RuntimeException(
              "Can't call method, expectations already failed.", toThrow));
    }

    this.instanceId = instanceId;
    this.methodName = methodName;
    this.parameters = parameters;

    try {
      barrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      if (toThrow != null) {
        done = true;
        throw trimTrace(
            3,
            new UnexpectedException("Exception from expectations.", toThrow));
      }
      throw new RuntimeException(e);
    }

    if (result.successful()) {
      Throwable t = result.getThrowable();
      if (t != null) {
        throw t;
      }
      return result.getReturnValue();
    } else {
      Exception cause = new ExpectationFailedException("Awaiting expectation.");
      cause.setStackTrace(result.getStackTrace());
      trimTrace(5, cause);
      toThrow = trimTrace(
          3, new ExpectationFailedException(result.getFailure(), cause));
      throw toThrow;
    }
  }

  private void expectationsDone() {
    expectation.setReturnValue(null);
    expectation.setExpectation(0, null, null);
  }

  private void stimDone() throws Throwable {
    call(0, null, null);
  }

  private <T extends Throwable> T trimTrace(int count, T t) {
    StackTraceElement[] trace = t.getStackTrace();
    t.setStackTrace(Arrays.copyOfRange(trace, count, trace.length));
    return t;
  }

  void checkExpectation() {
    result = expectation.validateCall(instanceId, methodName, parameters);
    if (!result.successful()) {
      expectation = null;
      done = true;
    } else {
      expectation = new MockExpectation(this);
    }
  }

  void setExpectation(long instanceId, String methodName, Object[] params) {
    expectation.setExpectation(instanceId, methodName, params);
  }

  void setReturnValue(Object returnValue) {
    this.expectation.setReturnValue(returnValue);
  }

  void setThrowable(Throwable t) {
    this.expectation.setThrowable(t);
  }

  void argThat(Matcher<Object> matcher) {
    this.expectation.addMatcher(matcher);
  }
}
