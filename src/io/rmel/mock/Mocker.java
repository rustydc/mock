package io.rmel.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;

final class Mocker<T> {
  private final Class<T> cls;
  private final T mock;
  private final T control;

  private final Joiner j;
  private final String ID = UUID.randomUUID().toString();

  public Mocker(Joiner j, Class<T> cls) {
    this.cls = cls;
    this.j = j;
    mock = makeFake(cls,
        (proxy, method, args) -> (String) j.call(ID, method.getName(), args));
    control = makeFake(cls, 
        (proxy, method, args) -> {
          j.expectation(ID, method.getName(), args,
              Thread.currentThread().getStackTrace());
          return null;
        });
  }
 
  @SuppressWarnings("unchecked")
  private T makeFake(Class<T> cls, InvocationHandler handler) {
    return ((T) Proxy.newProxyInstance(
        cls.getClassLoader(), new Class[] { cls }, handler));
  }

  public T mock() {
    return mock;
  }

  public T control() {
    return control;
  }
}
