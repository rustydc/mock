package io.rmel.mock;

import org.hamcrest.Matcher;

public final class Mock {
  private static final InheritableThreadLocal<MockRun> mockRunInstance =
      new InheritableThreadLocal<>();

  public static <T> T expect(Control<T> mock) {
    mockRunInstance.get().setReturnValue(null);
    return mock.getMock();
  }

  public static <T> T expect(Control<T> mock, Object obj) {
    mockRunInstance.get().setReturnValue(obj);
    return mock.getMock();
  }

  public static <T> T expectThrow(Control<T> mock, Throwable t) {
    mockRunInstance.get().setThrowable(t);
    return mock.getMock();
  }

  public static <A> void run(Class<A> a,
      Function1<A> stimulus, Function1<Control<A>> expectation) {
    MockRun mockRun = new MockRun();
    mockRunInstance.set(mockRun);
    Mocker<A> m = new Mocker<>(mockRun, a);
    mockRun.run(
        () -> expectation.apply(m.control()),
	() -> stimulus.apply(m.mock()));
    mockRunInstance.set(null);
  }

  public static <A, B> void run(Class<A> a, Class<B> b,
      Function2<A, B> stimulus,
      Function2<Control<A>, Control<B>> expectation) {
    MockRun mockRun = new MockRun();
    mockRunInstance.set(mockRun);
    Mocker<A> m1 = new Mocker<>(mockRun, a);
    Mocker<B> m2 = new Mocker<>(mockRun, b);
    mockRun.run(
        () -> expectation.apply(m1.control(), m2.control()),
        () -> stimulus.apply(m1.mock(), m2.mock()));
    mockRunInstance.set(null);
  }

  public static <A, B, C> void run(Class<A> a, Class<B> b, Class<C> c,
      Function3<A, B, C> stimulus,
      Function3<Control<A>, Control<B>, Control<C>> expectation) {
    MockRun mockRun = new MockRun();
    mockRunInstance.set(mockRun);
    Mocker<A> m1 = new Mocker<>(mockRun, a);
    Mocker<B> m2 = new Mocker<>(mockRun, b);
    Mocker<C> m3 = new Mocker<>(mockRun, c);
    mockRun.run(
        () -> expectation.apply(m1.control(), m2.control(), m3.control()),
        () -> stimulus.apply(m1.mock(), m2.mock(), m3.mock()));
    mockRunInstance.set(null);
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

  public static <T> T argThat(Matcher<Object> matcher) {
    mockRunInstance.get().argThat(matcher);
    return null;
  };
}
