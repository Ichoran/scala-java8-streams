package scala.collection

package j8 {

trait ManualSpec[@specialized(Double, Int, Long) A, SI] {
  def apply(sw: Sweeper[A]): SI
}

trait Sweeper[@specialized(Double, Int, Long) A] { self =>
  def hasNext: Boolean
  def next: A
  def divideOrNull: Sweeper[A]
  def estimateSize: Long
  
  def toIterator = new Iterator[A] {
    def hasNext = self.hasNext
    def next = self.next
  }
  def toSpliterator[SI](implicit ms: ManualSpec[A, SI]): SI = ms(self)
}

class SweepList[A](list: List[A]) extends Sweeper[A] {
  private[this] var myList = list
  def hasNext = myList.nonEmpty
  def next = { val ans = myList.head; myList = myList.tail; ans }
  def divideOrNull = null
  def estimateSize = if (hasNext) Long.MaxValue else 0
}

class SweepArray[A](array: Array[A]) extends Sweeper[A] {
  private[this] var i = 0
  def hasNext = i < array.length
  def next = if (hasNext) { val ans = array(i); i += 1; ans } else ???
  def divideOrNull = null
  def estimateSize = array.length - i
}

class SweepIterator[A](it: Iterator[A]) extends Sweeper[A] {
  def hasNext = it.hasNext
  def next = it.next
  def divideOrNull = null
  def estimateSize = if (hasNext) Long.MaxValue else 0
}

class SpliterateList[A](list: List[A]) extends java.util.Spliterator[A] {
  private[this] var myList = list
  def tryAdvance(c: java.util.function.Consumer[_ >: A]): Boolean = if (myList.isEmpty) false else {
    val h = myList.head
    myList = myList.tail
    c.accept(h)
    true
  }
  def trySplit() = null
  def characteristics() = java.util.Spliterator.ORDERED
  def estimateSize = if (myList.isEmpty) 0 else Long.MaxValue
}

class SpliterateArray[A](array: Array[A]) extends java.util.Spliterator[A] {
  private[this] var i = 0
  def tryAdvance(c: java.util.function.Consumer[_ >: A]): Boolean = {
    if (i < array.length) {
      c.accept(array(i))
      i += 1
      true
    }
    else false
  }
  def trySplit() = null
  def characteristics() = java.util.Spliterator.ORDERED | java.util.Spliterator.SIZED | java.util.Spliterator.SUBSIZED
  def estimateSize = array.length - i
}

class SpliterateIterator[A](it: Iterator[A]) extends java.util.Spliterator[A] {
  def tryAdvance(c: java.util.function.Consumer[_ >: A]): Boolean = {
    if (it.hasNext) {
      c.accept(it.next)
      true
    }
    else false
  }
  def trySplit() = null
  def characteristics() = java.util.Spliterator.ORDERED
  def estimateSize = if (it.hasNext) Long.MaxValue else 0
}

trait Stepper[A] extends TraversableOnce[A] with java.util.Spliterator[A] { self =>
  def step[U](f: A => U): Boolean
  
  def tryAdvance(c: java.util.function.Consumer[_ >: A]): Boolean = step(c accept _)
  def trySplit: java.util.Spliterator[A] = null
  def characteristics = 0
  def estimateSize = Long.MaxValue
  
  def copyToArray[B >: A](xs: Array[B], start: Int, len: Int) {
    var i = start
    val N = math.min(xs.length, start+len)
    while (i < N && step{ x => xs(i) = x; i += 1 }) {}
  }
  def exists(p: A => Boolean) = { var ans = false; while (step(x => ans = p(x)) && !ans) {}; ans }
  def find(p: A => Boolean) = { var ans: Option[A] = None; while (step{ x => if (p(x)) ans = Some(x) } && (ans eq None)) {}; ans }
  def forall(p: A => Boolean) = { var ans = true; while (step(x => ans = p(x)) && ans) {}; ans }
  def foreach[U](f: A => U) { while(step(f)) {} }
  
  def hasDefiniteSize = false
  def isEmpty = ???
  def isTraversableAgain = false
  
  def seq = this
  def toIterator = new IteratorFromStepper(self)
  def toStream = (new IteratorFromStepper(self)).toStream
  def toTraversable: Traversable[A] = { val lb = List.newBuilder[A]; foreach(lb += _); lb.result() }
}

class WrappedSpliterator[A](underlying: java.util.Spliterator[A]) extends Stepper[A] {
  override def tryAdvance(c: java.util.function.Consumer[_ >: A]): Boolean = underlying.tryAdvance(c)
  override def trySplit = { val split = underlying.trySplit; if (split == null) null else new WrappedSpliterator(split) }
  override def characteristics = underlying.characteristics
  override def estimateSize = underlying.estimateSize
  def step[U](f: A => U): Boolean = tryAdvance((new java.util.function.Consumer[A]{ def accept(a: A) { f(a) } }).asInstanceOf[java.util.function.Consumer[_ >: A]])
}

class IteratorFromStepper[+A](stepper: Stepper[A]) extends Iterator[A] {
  private[this] var cached: A = null.asInstanceOf[A]
  private[this] var status = 0
  def hasNext = (status > 0) || {
    if (status == 0) {
      val loaded = stepper.step(cached = _)
      status = if (loaded) 1 else -1
      loaded
    }
    else false
  }
  def next() = if (hasNext) { status = 0; cached } else Iterator.empty.next()
}

final class EnrichObjectSpliterator[A](private val underlying: java.util.Spliterator[A]) extends AnyVal {
  @inline def stream = java.util.stream.StreamSupport.stream(underlying, false)
  @inline def parallelStream = java.util.stream.StreamSupport.stream(underlying, true)
}

final class ScalaFlavoredJavaObjectStream[A, SA <: java.util.stream.Stream[A]](val underlying: SA) extends AnyVal {
  @inline def map[B, WSB](f: A => B)(implicit specmap: MapAsJavaSpecialized[A, B, SA, WSB]): WSB = specmap(f, underlying)
}

final class ScalaFlavoredJavaDoubleStream(val underlying: java.util.stream.DoubleStream) extends AnyVal {
  @inline def map[B, WSB](f: Double => B)(implicit specmap: MapAsJavaSpecialized[Double, B, java.util.stream.DoubleStream, WSB]): WSB = specmap(f, underlying)
}

final class ScalaFlavoredJavaIntStream(val underlying: java.util.stream.IntStream) extends AnyVal {
  @inline def map[B, WSB](f: Int => B)(implicit specmap: MapAsJavaSpecialized[Int, B, java.util.stream.IntStream, WSB]): WSB = specmap(f, underlying)
}

final class ScalaFlavoredJavaLongStream(val underlying: java.util.stream.LongStream) extends AnyVal {
  @inline def map[B, WSB](f: Long => B)(implicit specmap: MapAsJavaSpecialized[Long, B, java.util.stream.LongStream, WSB]): WSB = specmap(f, underlying)
}

trait LowerPriorityJ8Implicits {
  implicit def mkJavaObjectStreamToObjectStreamMap[A, B] =
    new MapAsJavaSpecialized[A, B, java.util.stream.Stream[A], ScalaFlavoredJavaObjectStream[B, java.util.stream.Stream[B]]] {
      def apply(f: A => B, sa: java.util.stream.Stream[A]) = new ScalaFlavoredJavaObjectStream[B, java.util.stream.Stream[B]](sa.map(x => f(x)))
    }
}

trait LowPriorityJ8Implicits extends LowerPriorityJ8Implicits {
  implicit def mkObjectSpliteratorSpec[A] = new ManualSpec[A, java.util.Spliterator[A]] {
    def apply(sw: Sweeper[A]) = new java.util.Spliterator[A] {
      def characteristics = java.util.Spliterator.ORDERED
      def estimateSize = sw.estimateSize
      def tryAdvance(f: java.util.function.Consumer[_ >: A]) = if (sw.hasNext) { f.accept(sw.next); true } else false
      def trySplit = { val div = sw.divideOrNull; if (div == null) null else apply(div) }
    }
  }
  
  implicit def defaultSpliteratorEnrichment[A](spliterator: java.util.Spliterator[A]) = new EnrichObjectSpliterator(spliterator)

  implicit def mkJavaObjectStreamToDoubleStreamMap[A] =
    new MapAsJavaSpecialized[A, Double, java.util.stream.Stream[A], ScalaFlavoredJavaDoubleStream] {
      def apply(f: A => Double, sa: java.util.stream.Stream[A]) = new ScalaFlavoredJavaDoubleStream(sa.mapToDouble(x => f(x)))
    }
  
  implicit def mkJavaObjectStreamToIntStreamMap[A] =
    new MapAsJavaSpecialized[A, Int, java.util.stream.Stream[A], ScalaFlavoredJavaIntStream] {
      def apply(f: A => Int, sa: java.util.stream.Stream[A]) = new ScalaFlavoredJavaIntStream(sa.mapToInt(x => f(x)))
    }
  
  implicit def mkJavaObjectStreamToLongStreamMap[A] =
    new MapAsJavaSpecialized[A, Long, java.util.stream.Stream[A], ScalaFlavoredJavaLongStream] {
      def apply(f: A => Long, sa: java.util.stream.Stream[A]) = new ScalaFlavoredJavaLongStream(sa.mapToLong(x => f(x)))
    }
  
  implicit def mkJavaDoubleStreamToObjectStreamMap[B] =
    new MapAsJavaSpecialized[Double, B, java.util.stream.DoubleStream, ScalaFlavoredJavaObjectStream[B, java.util.stream.Stream[B]]] {
      def apply(f: Double => B, sa: java.util.stream.DoubleStream) = new ScalaFlavoredJavaObjectStream(sa.mapToObj(x => f(x)))
    }
    
  implicit def scalaizeJavaObjectStream[A, SA <: java.util.stream.Stream[A]](sa: SA) = new ScalaFlavoredJavaObjectStream[A, SA](sa)
}

trait BoxedStreamUnboxer[A, SA] {
  def apply(s: java.util.stream.Stream[A]): SA
}

trait MapAsJavaSpecialized[@specialized(Double, Int, Long) A, @specialized(Double, Int, Long) B, SA, WSB] {
  def apply(f: A => B, sa: SA): WSB
}

}


package object j8 extends LowPriorityJ8Implicits {
  implicit object DoubleSpliteratorSpec extends ManualSpec[Double, java.util.Spliterator.OfDouble] {
    def apply(sw: Sweeper[Double]) = new java.util.Spliterator.OfDouble {
      def characteristics = java.util.Spliterator.ORDERED
      def estimateSize = sw.estimateSize
      def tryAdvance(f: java.util.function.DoubleConsumer): Boolean = if (sw.hasNext) { f.accept(sw.next); true } else false
      def trySplit() = { val div = sw.divideOrNull; if (div == null) null else apply(div) }
    }
  }

  implicit final class EnrichDoubleSpliterator(private val underlying: java.util.Spliterator.OfDouble) extends AnyVal {
    @inline def stream = java.util.stream.StreamSupport.doubleStream(underlying, false)
    @inline def parallelStream = java.util.stream.StreamSupport.doubleStream(underlying, true)
  }
  implicit final class EnrichIntSpliterator(private val underlying: java.util.Spliterator.OfInt) extends AnyVal {
    @inline def stream = java.util.stream.StreamSupport.intStream(underlying, false)
    @inline def parallelStream = java.util.stream.StreamSupport.intStream(underlying, true)
  }
  implicit final class EnrichLongSpliterator(private val underlying: java.util.Spliterator.OfLong) extends AnyVal {
    @inline def stream = java.util.stream.StreamSupport.longStream(underlying, false)
    @inline def parallelStream = java.util.stream.StreamSupport.longStream(underlying, true)
  }
  implicit object BoxedDoubleStreamUnboxer extends BoxedStreamUnboxer[Double, java.util.stream.DoubleStream] {
    def apply(s: java.util.stream.Stream[Double]) = s.mapToDouble(x => x)
  }
  implicit final class EnrichObjectStream[A](private val underlying: java.util.stream.Stream[A]) extends AnyVal {
    def unbox[SA](implicit u: BoxedStreamUnboxer[A, SA]) = u(underlying)
  }
  
  implicit object JavaDoubleStreamToDoubleStreamMap extends MapAsJavaSpecialized[Double, Double, java.util.stream.DoubleStream, ScalaFlavoredJavaDoubleStream] {
    def apply(f: Double => Double, sa: java.util.stream.DoubleStream) = new ScalaFlavoredJavaDoubleStream(sa.map(x => f(x)))
  }
  
  implicit object JavaDoubleStreamToIntStreamMap extends MapAsJavaSpecialized[Double, Int, java.util.stream.DoubleStream, ScalaFlavoredJavaIntStream] {
    def apply(f: Double => Int, sa: java.util.stream.DoubleStream) = new ScalaFlavoredJavaIntStream(sa.mapToInt(x => f(x)))
  }
  
  implicit def scalizeJavaDoubleStream(ds: java.util.stream.DoubleStream) = new ScalaFlavoredJavaDoubleStream(ds)
}
