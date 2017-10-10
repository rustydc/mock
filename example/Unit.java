public class Unit {
  private final Foo f1;
  private final Foo f2;

  public Unit(Foo f1, Foo f2) {
    this.f1 = f1;
    this.f2 = f2;
  }

  public String method(String input) {
    f1.fooMethod(input + "1");
    try {
      f1.voidMethod(4);
    } catch (RuntimeException e) {
      f2.voidMethod(5);
    }
    return f2.fooMethod(input + "2");
  }
}
