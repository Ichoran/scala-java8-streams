package test

/*
trait Uhoh[@specialized(Double) A] extends Any {
  def a: A;
}

class Danger(private val underlying: Double) extends AnyVal with Uhoh[Double] {
  def a = underlying
}
*/

// Implementation of generic Java interface
class Q extends Ob[String] {
  def foo = new Foo[String] { def aye = "Q" }
  def bar = "Q"
  def baz(s: String) { println(s) }
}

// Implementation of manually specialized Java interface
class R extends Dbl {
  def foo = new DblFoo { def aye = new java.lang.Double(1.5); def ayeDbl = 1.5 }
  def bar = 5.1
  def baz(d: Double) { println(d) }
}

// Implementation of generic Java interface with type that is usually manually specialized
class S extends Ob[Double] {
  def foo = new Foo[Double] { def aye = -1.0 }
  def bar = -2.0
  def baz(d: Double) { println(d) }
}

trait WrapFoo[@specialized(Double) A] extends Any {
  def aye: A
}

class WrapFooAny[A](private val foo: Foo[A]) extends AnyVal with WrapFoo[A] {
  @inline def aye = foo.aye
}

class WrapFooDbl(private val foo: DblFoo) extends AnyVal with WrapFoo[Double] {
  @inline def aye = foo.ayeDbl
}

trait Wrap[@specialized(Double) A] extends Any {
  def foo: WrapFoo[A]
  def bar: A
}

class WrapOb[A](private val ob: Ob[A]) extends AnyVal with Wrap[A] {
  @inline def foo = new WrapFooAny(ob.foo)
  @inline def bar = ob.bar
  def baz(a: A) { ob.baz(a) }
}

class WrapDbl(private val db: Dbl) extends AnyVal with Wrap[Double] {
  @inline def foo = new WrapFooDbl(db.foo)
  @inline def bar = db.bar
  def baz(d: Double) { db.baz(d) }
}

object Test {
  def main(args: Array[String]) {
    val wo = new WrapOb(new Q)
//    0:   new     #16; //class test/Q
//    3:   dup
//    4:   invokespecial   #17; //Method test/Q."<init>":()V
//    7:   astore  4
//    9:   getstatic       #22; //Field test/WrapOb$.MODULE$:Ltest/WrapOb$;
//    12:  astore_2

    val bo: String = wo.bar
//    13:  aload   4
//    15:  invokeinterface #28,  1; //InterfaceMethod test/Ob.bar:()Ljava/lang/Object;
//    20:  checkcast       #30; //class java/lang/String
//    23:  astore  14
    
    val ao: String = wo.foo.aye
//    25:  getstatic       #35; //Field test/WrapFooAny$.MODULE$:Ltest/WrapFooAny$;
//    28:  getstatic       #22; //Field test/WrapOb$.MODULE$:Ltest/WrapOb$;
//    31:  astore_3
//    32:  aload   4
//    34:  invokeinterface #39,  1; //InterfaceMethod test/Ob.foo:()Ltest/Foo;
//    39:  astore  6
//    41:  astore  5
//    43:  aload   6
//    45:  invokeinterface #44,  1; //InterfaceMethod test/Foo.aye:()Ljava/lang/Object;
//    50:  checkcast       #30; //class java/lang/String
//    53:  astore  15

    val wd = new WrapDbl(new R)
//    55:  new     #46; //class test/R
//    58:  dup
//    59:  invokespecial   #47; //Method test/R."<init>":()V
//    62:  astore  8
//    64:  new     #49; //class test/WrapDbl
//    67:  dup
//    68:  aload   8
//    70:  invokespecial   #52; //Method test/WrapDbl."<init>":(Ltest/Dbl;)V

    val bd = wd.bar
//    73:  invokevirtual   #56; //Method test/WrapDbl.bar$mcD$sp:()D
//    76:  dstore  16

    val ad: Double = wd.foo.aye
//    78:  new     #58; //class test/WrapFooDbl
//    81:  dup
//    82:  getstatic       #63; //Field test/WrapDbl$.MODULE$:Ltest/WrapDbl$;
//    85:  astore  7
//    87:  aload   8
//    89:  invokeinterface #68,  1; //InterfaceMethod test/Dbl.foo:()Ltest/DblFoo;
//    94:  invokespecial   #71; //Method test/WrapFooDbl."<init>":(Ltest/DblFoo;)V
//    97:  invokevirtual   #74; //Method test/WrapFooDbl.aye$mcD$sp:()D
//    100: dstore  18

    val wq = new WrapOb(new S)
//    102: new     #76; //class test/S
//    105: dup
//    106: invokespecial   #77; //Method test/S."<init>":()V
//    109: astore  11
//    111: getstatic       #22; //Field test/WrapOb$.MODULE$:Ltest/WrapOb$;
//    114: astore  9

    val bq = wq.bar
//    116: aload   11
//    118: invokeinterface #28,  1; //InterfaceMethod test/Ob.bar:()Ljava/lang/Object;
//    123: invokestatic    #83; //Method scala/runtime/BoxesRunTime.unboxToDouble:(Ljava/lang/Object;)D
//    126: dstore  20

    val aq: Double = wq.foo.aye
//    128: getstatic       #35; //Field test/WrapFooAny$.MODULE$:Ltest/WrapFooAny$;
//    131: getstatic       #22; //Field test/WrapOb$.MODULE$:Ltest/WrapOb$;
//    134: astore  10
//    136: aload   11
//    138: invokeinterface #39,  1; //InterfaceMethod test/Ob.foo:()Ltest/Foo;
//    143: astore  13
//    145: astore  12
//    147: aload   13
//    149: invokeinterface #44,  1; //InterfaceMethod test/Foo.aye:()Ljava/lang/Object;
//    154: invokestatic    #83; //Method scala/runtime/BoxesRunTime.unboxToDouble:(Ljava/lang/Object;)D
//    157: dstore  22

      println(bo + ao + bd + ad + bq + aq)
  }
}
