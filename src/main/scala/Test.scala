package test

class Q extends Ob[String] {
  def foo = new Foo[String] { def aye = "Q" }
  def bar = "Q"
  def baz(s: String) { println(s) }
}

class WrapOb[A](private val ob: Ob[A]) extends AnyVal {
  def foo = ob.foo
  @inline def bar = ob.bar
  def baz(a: A) { ob.baz(a) }
}

object Test {
  def main(args: Array[String]) {
    val wo = new WrapOb(new Q)
    wo.bar
  }
}