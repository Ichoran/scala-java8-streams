package test;

public interface Ob<A> extends Base<A, Ob<A>> {
  public A bar();
  public void baz(A a);
}
