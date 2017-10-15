package io.rmel.mock;

public class Control<T> {
  private final T mock;
  
  Control(T mock) {
    this.mock = mock;
  }

  T getMock() {
    return mock;
  }
}
