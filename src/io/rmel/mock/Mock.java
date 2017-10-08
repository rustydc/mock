package io.rmel.mock;

import java.util.Objects;

public class Mock {

  // TODO(rmel): Threadlocal.
  private static Joiner j = null;

  public static <T> void expect(T in, T out) {
    if (j == null) {
      throw new RuntimeException("Called 'expect' outside expectation.");
    }
    j.expect(out); 
  }

  public static <T> void expectThrow(T in, Throwable t) {
    if (j == null) {
      throw new RuntimeException("Called 'expectThrow' outside expectation.");
    }
    j.expectThrow(t); 
  }

  public static <A> void run(Class<A> a,
      Function1<A> stimulus, Function1<A> expectation) {
    j = new Joiner();
    Mocker<A> m = new Mocker<>(j, a);
    Expectation.run(
        () -> expectation.apply(m.control()),
        () -> stimulus.apply(m.mock()));
    j = null;
  }

  public static <A, B> void run(Class<A> a, Class<B> b,
      Function2<A, B> stimulus, Function2<A, B> expectation) {
    j = new Joiner();
    Mocker<A> m1 = new Mocker<>(j, a);
    Mocker<B> m2 = new Mocker<>(j, b);
    Expectation.run(
        () -> expectation.apply(m1.control(), m2.control()),
        () -> stimulus.apply(m1.mock(), m2.mock()));
    j = null;
  }
        
  @FunctionalInterface
  public interface Function1<A> {
    void apply(A a);
  }

  @FunctionalInterface
  public interface Function2<A, B> {
    void apply(A a, B b);
  }

  public static void assertEqual(Object actual, Object expected) {
    if (!Objects.equals(expected, actual)) {
      throw new RuntimeException(
          "Assertion failed: Expected '" + expected + "', but got '"
              + actual + "'.");
    }
  }
}
