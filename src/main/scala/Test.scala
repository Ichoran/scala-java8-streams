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
    val bo: String = wo.bar
    val ao: String = wo.foo.aye
    val wd = new WrapDbl(new R)
    val bd = wd.bar
    val ad: Double = wd.foo.aye
    val wq = new WrapOb(new S)
    val bq = wq.bar
    val aq: Double = wq.foo.aye
    println(bo + ao + bd + ad + bq + aq)
  }
}
