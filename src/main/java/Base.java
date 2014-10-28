package test;

public interface Base<A, B extends Base<A,B>> {
  public Foo<A> foo();
}
