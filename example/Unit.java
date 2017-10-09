public class Unit {
  private final Foo f1;
  private final Foo f2;

  public Unit(Foo f1, Foo f2) {
    this.f1 = f1;
    this.f2 = f2;
  }

  public String method(String input) {
    f1.fooMethod(input + "1");
    f1.voidMethod(4);
    return f2.fooMethod(input + "2");
  }
}
