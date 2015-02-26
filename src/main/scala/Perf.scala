package xp.perf

trait Iter[@specialized(Int) A] { self =>
  @inline def hasNext: Boolean
  @inline def next: A
  def map[@specialized(Int) B](f: A => B): Iter[B] = new Iter[B] {
    @inline def hasNext = self.hasNext
    @inline def next = f(self.next)
  }
  def acc[@specialized(Int) B](z: B)(f: (B,A) => B): B = {
    var b = z
    while (hasNext) { b = f(b, next) }
    b
  }
  def toStep = new Step[A] {
    def tryNext(f: A => Unit) = if (hasNext) { f(next); true } else false
  }
  def toRetro = new Retro[A] {
    var completed = false
    def value = if (hasNext) { next } else { completed = true; null.asInstanceOf[A] }
  }
}

trait Step[@specialized(Int) A] { self =>
  @inline def tryNext(f: A => Unit): Boolean
  def map[@specialized(Int) B](f: A => B): Step[B] = new Step[B] {
    @inline def tryNext(g: B => Unit) = self.tryNext(a => g(f(a)))
  }
  def acc[@specialized(Int) B](z: B)(f: (B,A) => B): B = {
    var b = z
    while (tryNext(a => b = f(b,a))) {}
    b
  }
  def toIter = new Iter[A] {
    private final var cache: A = _
    private final var status = 0
    def hasNext = (status > 0) || (status == 0 && {
      val got = tryNext{ a => cache = a }
      status = if (got) 1 else -1
      got
    })
    def next = if (status > 0 || hasNext) { status = 0; cache } else throw new NoSuchElementException("Empty Iter")
  }
  def toRetro = new Retro[A] {
    private[this] final var cache: A = _
    var completed = true
    def value = { completed = tryNext{ x => cache = x }; cache }
  }
}

trait Retro[@specialized(Int) A] { self =>
  @inline def completed: Boolean
  @inline def value: A
  def map[@specialized(Int) B](f: A => B): Retro[B] = new Retro[B] {
    @inline def completed = self.completed
    @inline def value = f(self.value)
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
    private final var cache: A = _
    private final var status = 0
    def hasNext = (status > 0) || (status == 0 && {
      cache = value
      val c = completed
      status = if (c) 1 else -1
      c
    })
    def next = if (status > 0 || hasNext) { status = 0; cache } else throw new NoSuchElementException("Empty Iter")
  }
}
