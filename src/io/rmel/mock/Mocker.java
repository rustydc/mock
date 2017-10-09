package io.rmel.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;

final class Mocker<T> {
  private final T mock;
  private final Control<T> control;

  Mocker(Joiner j, Class<T> cls) {
    String ID = UUID.randomUUID().toString();
    mock = makeFake(cls,
        (proxy, method, args) -> (Object) j.call(ID, method.getName(), args));
    control = new Control(
        makeFake(cls,
            (proxy, method, args) -> {
              j.expectation(ID, method.getName(), args,
                  Thread.currentThread().getStackTrace());
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
