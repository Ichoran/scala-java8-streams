package xp.perf

trait Iter[@specialized(Int) A] { self =>
  def hasNext: Boolean
  def next: A
  def map[@specialized(Int) B](f: A => B): Iter[B] = new Iter[B] {
    def hasNext = self.hasNext
    def next = f(self.next)
  }
  def acc[@specialized(Int) B](z: B)(f: (B,A) => B): B = {
    var b = z
    while (hasNext) { b = f(b, next) }
    b
  }
  def toIter = new Iter[A] {
    def hasNext = self.hasNext
    def next = self.next
  }
  def toStep = new Step[A] {
    def tryNext(f: A => Unit) = if (hasNext) { f(next); true } else false
  }
  def toPost = new Post[A] {
    var completed = false
    def value = if (hasNext) { next } else { completed = true; null.asInstanceOf[A] }
  }
}

trait Step[@specialized(Int) A] { self =>
  def tryNext(f: A => Unit): Boolean
  def map[@specialized(Int) B](f: A => B): Step[B] = new Step[B] {
    def tryNext(g: B => Unit) = self.tryNext(a => g(f(a)))
  }
  def acc[@specialized(Int) B](z: B)(f: (B,A) => B): B = {
    var b = z
    while (tryNext(a => b = f(b,a))) {}
    b
  }
  def toIter: Iter[A] = new Iter[A] with (A => Unit) {
    private[this] final var cache: A = _
    private[this] final var status = 0
    def apply(a: A) { cache = a }
    def hasNext = (status > 0) || (status == 0 && {
      val got = self.tryNext{ this }
      status = if (got) 1 else -1
      got
    })
    def next = if (status > 0 || hasNext) { status = 0; cache } else throw new NoSuchElementException("Empty Iter")
  }
  def toStep = new Step[A] { def tryNext(f: A => Unit) = self.tryNext(f) }
  def toPost: Post[A] = new Post[A] with (A => Unit) {
    private[this] final var cache: A = _
    private[this] final var myCompleted: Boolean = false
    def apply(a: A) { cache = a }
    def completed = myCompleted
    def value = { myCompleted = !tryNext{ this }; cache }
  }
}

trait Post[@specialized(Int) A] { self =>
  def completed: Boolean
  def value: A
  def map[@specialized(Int) B](f: A => B): Post[B] = new Post[B] {
    def completed = self.completed
    def value = { val v = self.value; if (self.completed) null.asInstanceOf[B] else f(self.value) }
  }
  def acc[@specialized(Int) B](z: B)(f: (B,A) => B): B = {
    def inner(b: B): B = {
      val v = value
      if (completed) b
      else inner(f(b,v))
    }
    inner(z)
  }
  def toIter = new Iter[A] {
    private[this] final var cache: A = _
    private[this] final var status = 0
    def hasNext = (status > 0) || (status == 0 && {
      cache = value
      val c = completed
      status = if (c) -1 else 1
      !c
    })
    def next = if (status > 0 || hasNext) { status = 0; cache } else throw new NoSuchElementException("Empty Iter")
  }
  def toStep = new Step[A] {
    def tryNext(f: A => Unit) = { val a = value; val go = !completed; if (go) f(a); go }
  }
  def toPost = new Post[A] {
    def completed = self.completed
    def value = self.value
  }
}

class IterAI(a: Array[Int]) extends Iter[Int] {
  private[this] final var i = 0
  def hasNext = i < a.length
  def next = {
    if (hasNext) {
      val ans = a(i)
      i += 1
      ans
    }
    else throw new NoSuchElementException("Empty Iter")
  }
}

class StepAI(a: Array[Int]) extends Step[Int] {
  private[this] final var i = 0
  def tryNext(f: Int => Unit) = if (i < a.length) { f(a(i)); i += 1; true } else false
}

class PostAI(a: Array[Int]) extends Post[Int] {
  private[this] final var i = 0
  var completed = false
  def value = if (i < a.length) { val ans = a(i); i += 1; ans } else { completed = true; 0 }
}

object Perf {
  val a = Array.range(0,1024)
  def wh = { var s, i = 0; while (i < a.length) { val x = a(i); s += x*x; i += 1 }; s }
  def si = (new IterAI(a)).map(x => x*x).acc(0)(_ + _)
  def ss = (new StepAI(a)).map(x => x*x).acc(0)(_ + _)
  def sp = (new PostAI(a)).map(x => x*x).acc(0)(_ + _)
  def sii = (new IterAI(a)).toIter.map(x => x*x).acc(0)(_ + _)
  def sis = (new IterAI(a)).toStep.map(x => x*x).acc(0)(_ + _)
  def sip = (new IterAI(a)).toPost.map(x => x*x).acc(0)(_ + _)
  def ssi = (new StepAI(a)).toIter.map(x => x*x).acc(0)(_ + _)
  def sss = (new StepAI(a)).toStep.map(x => x*x).acc(0)(_ + _)
  def ssp = (new StepAI(a)).toPost.map(x => x*x).acc(0)(_ + _)
  def spi = (new PostAI(a)).toIter.map(x => x*x).acc(0)(_ + _)
  def sps = (new PostAI(a)).toStep.map(x => x*x).acc(0)(_ + _)
  def spp = (new PostAI(a)).toPost.map(x => x*x).acc(0)(_ + _)
}
