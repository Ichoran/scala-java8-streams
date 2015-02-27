package scala.collection.j8

import language.higherKinds

import java.util._
import stream.StreamSupport
import stream.{Stream => ObjectStream, DoubleStream , IntStream, LongStream}

package spec {
  trait AutoSpecColl[@specialized(Double, Int, Long) A, CC, XA] {
    def apply(original: CC): XA
  }
  trait AutoSpec[@specialized(Double, Int, Long) A, @specialized(Double, Int, Long) B, CA, KB] {
    def apply(original: CA): KB
  }
  
  trait Unboxer[A, S[A], SU] {
    def apply(original: S[A]): SU
  }
  
  final class RichObjectSpliterator[A](val underlying: Spliterator[A]) extends AnyVal {
    def stream = StreamSupport.stream(underlying, false)
    def richStream = new RichObjectStream(StreamSupport.stream(underlying, false))
    def unboxed[SU](implicit unboxer: Unboxer[A, Spliterator, SU]) = unboxer(underlying)
  }
  
  final class RichDoubleSpliterator(val underlying: Spliterator.OfDouble) extends AnyVal {
    def stream = StreamSupport.doubleStream(underlying, false)
    def richStream = new RichDoubleStream(StreamSupport.doubleStream(underlying, false))
  }
  
  final class RichIntSpliterator(val underlying: Spliterator.OfInt) extends AnyVal {
    def stream = StreamSupport.intStream(underlying, false)
    def richStream = new RichIntStream(StreamSupport.intStream(underlying, false))
  }
  
  final class RichLongSpliterator(val underlying: Spliterator.OfLong) extends AnyVal {
    def stream = StreamSupport.longStream(underlying, false)
    def richStream = new RichLongStream(StreamSupport.longStream(underlying, false))
  }
  
  final class RichObjectStream[A](val underlying: ObjectStream[A]) extends AnyVal {
    def unboxed[SU](implicit unboxer: Unboxer[A, ObjectStream, SU]) = unboxer(underlying)
  }
  
  final class RichDoubleStream(val underlying: DoubleStream) extends AnyVal {
  }
  
  final class RichIntStream(val underlying: IntStream) extends AnyVal {
  }
  
  final class RichLongStream(val underlying: LongStream) extends AnyVal {
  }
}

package object spec {
}
