package io.rmel.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

final class Mocker<T> {
  private static AtomicLong idCounter = new AtomicLong(0);
  private final T mock;
  private final Control<T> control;

  Mocker(MockRun mockRun, Class<T> cls) {
    long id = idCounter.incrementAndGet();
    mock = makeFake(cls,
        (proxy, method, args) ->
            (Object) mockRun.call(id, method.getName(), args));
    control = new Control(
        makeFake(cls,
            (proxy, method, args) -> {
              mockRun.setExpectation(id, method.getName(), args);
              return null;
             }));
  }
 
  @SuppressWarnings("unchecked")
  private T makeFake(Class<T> cls, InvocationHandler handler) {
    return ((T) Proxy.newProxyInstance(
        cls.getClassLoader(), new Class[] { cls }, handler));
  }

  T mock() {
    return mock;
  }

  Control<T> control() {
    return control;
  }
}
