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
    def apply(boxed: S[A]): SU
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
  
  final class UnboxedDoubleSpliterator(boxed: Spliterator[Double]) extends Spliterator.OfDouble {
    override def characteristics() = boxed.characteristics()
    override def estimateSize() = boxed.estimateSize()
    override def forEachRemaining(action: function.Consumer[_ >: java.lang.Double]) = boxed.forEachRemaining(action.asInstanceOf[function.Consumer[_ >: Double]])
    override def forEachRemaining(action: function.DoubleConsumer) = boxed.forEachRemaining(d => action.accept(d))
    override def getComparator(): Comparator[_ >: java.lang.Double] = boxed.getComparator().asInstanceOf[Comparator[_ >: java.lang.Double]]
    override def getExactSizeIfKnown() = boxed.getExactSizeIfKnown()
    override def hasCharacteristics(characteristics: Int) = boxed.hasCharacteristics(characteristics)
    override def tryAdvance(action: function.Consumer[_ >: java.lang.Double]) = boxed.tryAdvance(action.asInstanceOf[function.Consumer[_ >: Double]])
    def tryAdvance(action: function.DoubleConsumer) = boxed.tryAdvance(d => action.accept(d))
    override def trySplit() = boxed.trySplit() match { case null => null; case x => new UnboxedDoubleSpliterator(x) }
  }
  
  final class UnboxedIntSpliterator(boxed: Spliterator[Int]) extends Spliterator.OfInt {
    override def characteristics() = boxed.characteristics()
    override def estimateSize() = boxed.estimateSize()
    override def forEachRemaining(action: function.Consumer[_ >: java.lang.Integer]) { boxed.forEachRemaining(action.asInstanceOf[function.Consumer[_ >: Int]]) }
    override def forEachRemaining(action: function.IntConsumer) = boxed.forEachRemaining(d => action.accept(d))
    override def getComparator(): Comparator[_ >: java.lang.Integer] = boxed.getComparator().asInstanceOf[Comparator[_ >: java.lang.Integer]]
    override def getExactSizeIfKnown() = boxed.getExactSizeIfKnown()
    override def hasCharacteristics(characteristics: Int) = boxed.hasCharacteristics(characteristics)
    override def tryAdvance(action: function.Consumer[_ >: java.lang.Integer]) = boxed.tryAdvance(action.asInstanceOf[function.Consumer[_ >: Int]])
    def tryAdvance(action: function.IntConsumer) = boxed.tryAdvance(d => action.accept(d))
    override def trySplit() = boxed.trySplit() match { case null => null; case x => new UnboxedIntSpliterator(x) }
  }
  
  final class UnboxedLongSpliterator(boxed: Spliterator[Long]) extends Spliterator.OfLong {
    override def characteristics() = boxed.characteristics()
    override def estimateSize() = boxed.estimateSize()
    override def forEachRemaining(action: function.Consumer[_ >: java.lang.Long]) = boxed.forEachRemaining(action.asInstanceOf[function.Consumer[_ >: Long]])
    override def forEachRemaining(action: function.LongConsumer) = boxed.forEachRemaining(d => action.accept(d))
    override def getComparator(): Comparator[_ >: java.lang.Long] = boxed.getComparator().asInstanceOf[Comparator[_ >: java.lang.Long]]
    override def getExactSizeIfKnown() = boxed.getExactSizeIfKnown()
    override def hasCharacteristics(characteristics: Int) = boxed.hasCharacteristics(characteristics)
    override def tryAdvance(action: function.Consumer[_ >: java.lang.Long]) = boxed.tryAdvance(action.asInstanceOf[function.Consumer[_ >: Long]])
    def tryAdvance(action: function.LongConsumer) = boxed.tryAdvance(d => action.accept(d))
    override def trySplit() = boxed.trySplit() match { case null => null; case x => new UnboxedLongSpliterator(x) }
  }
  
  trait Java8StreamImplicits2 {
  }
  trait Java8StreamImplicits1 extends Java8StreamImplicits2 {
    implicit def enrichObjectSpliterator[A](sp: Spliterator[A]) = new RichObjectSpliterator(sp)
    implicit def enrichObjectStream[A](os: ObjectStream[A]) = new RichObjectStream(os)
  }
  trait Java8StreamImplicits extends Java8StreamImplicits1 {
    implicit def enrichDoubleSpliterator(spd: Spliterator.OfDouble) = new RichDoubleSpliterator(spd)
    implicit def enrichIntSpliterator(spi: Spliterator.OfInt) = new RichIntSpliterator(spi)
    implicit def enrichLongSpliterator(spl: Spliterator.OfLong) = new RichLongSpliterator(spl)
    implicit def enrichDoubleStream(ds: DoubleStream) = new RichDoubleStream(ds)
    implicit def enrichIntStream(is: IntStream) = new RichIntStream(is)
    implicit def enrichLongStream(ls: LongStream) = new RichLongStream(ls)
  
    implicit val spliteratorUnboxDouble = new Unboxer[Double, Spliterator, Spliterator.OfDouble] {
      def apply(boxed: Spliterator[Double]) = new UnboxedDoubleSpliterator(boxed)
    }
    implicit val spliteratorUnboxInt = new Unboxer[Int, Spliterator, Spliterator.OfInt] {
      def apply(boxed: Spliterator[Int]) = new UnboxedIntSpliterator(boxed)
    }
    implicit val spliteratorUnboxLong = new Unboxer[Long, Spliterator, Spliterator.OfLong] {
      def apply(boxed: Spliterator[Long]) = new UnboxedLongSpliterator(boxed)
    }
    implicit val jstreamUnboxDouble = new Unboxer[Double, ObjectStream, DoubleStream] {
      def apply(boxed: ObjectStream[Double]): DoubleStream = boxed.mapToDouble(x => x)
    }
    implicit val jstreamUnboxInt = new Unboxer[Int, ObjectStream, IntStream] {
      def apply(boxed: ObjectStream[Int]): IntStream = boxed.mapToInt(x => x)
    }
    implicit val jstreamUnboxLong = new Unboxer[Long, ObjectStream, LongStream] {
      def apply(boxed: ObjectStream[Long]): LongStream = boxed.mapToLong(x => x)
    }
  }
}

package object spec extends Java8StreamImplicits {
}
