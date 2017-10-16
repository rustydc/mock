package io.rmel.mock;

interface MockCall {
  void call(int instanceId, String methodName, Object[] parameters) throws Throwable;
}
