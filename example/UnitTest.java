import static io.rmel.mock.Mock.run;
import static io.rmel.mock.Mock.expect;
import static io.rmel.mock.Mock.expectThrow;
import static io.rmel.mock.Mock.assertEqual;

public class UnitTest {

  public static void main(String[] args) {
    System.err.println("Running.");

    run(Foo.class,
        foo -> {
          Unit unit = new Unit(foo, foo);
          String output = unit.method("input");
          assertEqual(output, "output2");
        },
        foo -> {
          expect(foo, "output1").fooMethod("input1");
          expect(foo).voidMethod(4);
          expect(foo, "output2").fooMethod("input2");
        });
    System.err.println("Pass.");
          
    run(Foo.class, Foo.class,
        (foo1, foo2) -> {
          Unit unit = new Unit(foo1, foo2);
          String output = unit.method("input");
          assertEqual(output, "output2");
        },
        (foo1, foo2) -> {
          expect(foo1, "output1").fooMethod("input1");
          expect(foo1).voidMethod(4);
          expect(foo2, "output2").fooMethod("input2");
        });
    
    System.err.println("Pass.");
  }
}
