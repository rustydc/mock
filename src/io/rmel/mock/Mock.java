package io.rmel.mock;

import java.util.Objects;
import org.hamcrest.Matcher;

public final class Mock {

  private static final InheritableThreadLocal<Joiner> j =
      new InheritableThreadLocal<>();

  public static <T> T expect(Control<T> mock) {
    return mock.getMock();
  }

  public static <T> T expect(Control<T> mock, Object obj) {
    j.get().expect(obj);
    return mock.getMock();
  }

  public static <T> T expectThrow(Control<T> mock, Throwable t) {
    if (j.get() == null) {
      throw new RuntimeException("Called 'expectThrow' outside expectation.");
    }
    j.get().expectThrow(t); 
    return mock.getMock();
  }

  public static <A> void run(Class<A> a,
      Function1<A> stimulus, Function1<Control<A>> expectation) {
    j.set(new Joiner());
    Mocker<A> m = new Mocker<>(j.get(), a);
    Expectation.run(j.get(),
        () -> expectation.apply(m.control()),
	() -> stimulus.apply(m.mock()));
    j.set(null);
  }

  public static <A, B> void run(Class<A> a, Class<B> b,
      Function2<A, B> stimulus,
      Function2<Control<A>, Control<B>> expectation) {
    j.set(new Joiner());
    Mocker<A> m1 = new Mocker<>(j.get(), a);
    Mocker<B> m2 = new Mocker<>(j.get(), b);
    Expectation.run(j.get(),
        () -> expectation.apply(m1.control(), m2.control()),
        () -> stimulus.apply(m1.mock(), m2.mock()));
    j.set(null);
  }

  public static <A, B, C> void run(Class<A> a, Class<B> b, Class<C> c,
      Function3<A, B, C> stimulus,
      Function3<Control<A>, Control<B>, Control<C>> expectation) {
    j.set(new Joiner());
    Mocker<A> m1 = new Mocker<>(j.get(), a);
    Mocker<B> m2 = new Mocker<>(j.get(), b);
    Mocker<C> m3 = new Mocker<>(j.get(), c);
    Expectation.run(j.get(),
        () -> expectation.apply(m1.control(), m2.control(), m3.control()),
        () -> stimulus.apply(m1.mock(), m2.mock(), m3.mock()));
    j.set(null);
  }
        
  @FunctionalInterface
  public interface Function1<A> {
    void apply(A a) throws Exception;
  }

  @FunctionalInterface
  public interface Function2<A, B> {
    void apply(A a, B b) throws Exception;
  }

  @FunctionalInterface
  public interface Function3<A, B, C> {
    void apply(A a, B b, C c) throws Exception;
  }

  public static <T> T argThat(Matcher<T> matcher) {
    j.get().argThat(matcher);
    return null;
  };

  public static void assertEqual(Object actual, Object expected) {
    if (!Objects.equals(expected, actual)) {
      throw new RuntimeException(
          "Assertion failed: Expected '" + expected + "', but got '"
              + actual + "'.");
    }
  }
}
