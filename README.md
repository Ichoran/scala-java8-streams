## Scala-Java8-Streams

This project explores adding an interoperability layer for Java 8 streams to Scala.

### Overview of Java 8 Streams

Java 8 has added _streams_, a type of high-level parallelizable iterator that supports closure-based processing.  The stream API, residing at `java.util.streams`, presents a set of interfaces for generic (object-based) streams plus streams manually specialized for primitive doubles, longs, or ints.  A stream is created using a single generator method.  Zero or more _intermediate operations_ may be requested by calling methods.  Finally, a _terminal operation_ will run, possibly traversing the entire stream, that computes some sort of result or produces a side-effect.  Side-effects are discouraged, as parallelized streams make few guarantees about order of processing.

Streams are implemented using `Spliterator`s, a type of iterator that can partition itself into multiple pieces for parallel processing.  Although in principle this can be done with any `Iterator`, it is generally more efficient to implement the partitioning at a lower level.  By using or not using the partitioning capability of `Spliterator`s, streams can effectively work either in parallel or serial.

Streams have many fewer built-in operations than do even the most limited of the Scala collections.

Generating operations for `Stream` are static methods of the `Stream` class and include

  * `concat`  (equivalent to Scala `++` but as a static method)
  * `empty`  (same as Scala `empty` on companion object)
  * `generate`  (same as Scala `Iterator.continually`)
  * `iterate`  (same as Scala `Iterator.iterate`)
  * `of`  (same as Scala `apply` on companion object--both single and varargs forms)

plus there is a `builder` method to create a `Stream.Builder` (which works much like Scala builders).

Intermediate operations, which have a new stream as a result type and perform their operations lazily, include

  * `distinct`  (same as Scala `distinct`)
  * `filter`  (same as Scala `filter`)
  * `flatMap`  (same as Scala `flatMap`)
  * `flatMapToDouble`  (manually specialized `flatMap`)
  * `flatMapToInt`  (manually specialized `flatMap`)
  * `flatMapToLong`  (manually specialized `flatMap`)
  * `map`  (same as Scala `map`)
  * `mapToDouble`  (manually specialized `map`)
  * `mapToInt`  (manually specialized `map`)
  * `mapToLong`  (manually specialized `map`)
  * `peek`  (no direct equivalent--runs a side effect as the stream is consumed)
  * `skip`  (same as Scala `drop`)
  * `sorted`  (two variants, equivalent to Scala `sort` and `sortWith`)

Terminal operations, which eagerly (but perhaps incompletely) evaluate include

  * `allMatch`  (same as Scala `forall`)
  * `anyMatch`  (same as Scala `exists`)
  * `collect`  (two forms; similar in principle to Scala's `aggregate`)
  * `count`  (same as Scala `size`, but returns a `long`)
  * `findAny`  (similar to Scala `headOption`, but may be any element)
  * `findFirst`  (same as Scala `headOption`)
  * `forEach` (similar to Scala `foreach`, but explicitly has "anything goes" order)
  * `forEachOrdered` (similar to Scala `foreach`, but guarantees processing in natural order)
  * `max` (would be called `maxWith` in Scala)
  * `min` (would be called `minWith` in Scala)
  * `noneMatch` (equivalent to Scala `!exists`)
  * `reduce` (three forms, one the same as Scala `reduceOption`, and two like `fold`)
  * `toArray` (two forms, essentially the same as Scala `toArray`)

Streams also have methods to switch between parallel and sequential processing, and between ordered and unordered representations.

### Goals

Scala iteroperability with Java 8 Streams should accomplish five goals.

1. Seamlessly use Java 8 Streams as in Java 8, but with the syntactic advantages of Scala.
2. Easily use Java 8 Streams as a Scala collection (perhaps behind an `asScala` guard).
3. Provide the full set of Scala collections methods transparently and with minimal runtime penalty on top of Java 8 Streams.  May or may not be the same as 2.
4. Generate `Spliterator`s for Scala collections that are compatible with Java 8 Streams and can be used in Java or Scala.  In particular, this will enable all of Scala's collections to run operations in parallel.
5. Reduce the specialization burden for `Object` vs. `double`, `int`, or `long`.

Special care must be taken to avoid superfluous boxing of `Array`-based streams.  Note that `java.lang.Arrays` contains a profusion of manually specialized methods to accomplish this in Java.

### Architecture

#### Goal 1 -- Use Java 8 Streams within Scala

This should not require any extra tooling; basic Scala-Java compatibility should suffice.  However, comprehensive unit tests should be written to make sure they do suffice.

#### Goal 2 -- Present Java 8 Streams as a Scala collection

An implicit value class can be used to add an `asScala` method to each `Stream` class.  This method can instantiate a wrapper class that implements the Scala methods in terms of the Java ones (extending `TraversableOnce`, most likely).

#### Goal 3 -- Scala collections transparently and with low overhead on Java 8 Streams

Implementing Scala methods in terms of Java 8 Stream methods should not require any state.  Thus, a value class should be able to implement the Scala methods, possibly with type classes to provide specialized functionality for Double etc. specialized versions.

#### Goal 4 -- `Spliterator`s for Scala collections

This is one of the most challenging goals to achieve since in many cases acceptable performance requires an implementation that has access to private methods.

Adding the functionality using implicit conversion would reduce the intersection with the rest of the library, though it would need to be determined to what extent pattern matching would be needed to figure out the underlying type of the collection.  By making key methods private[collection] instead of private, adequate safety should be maintained while still allowing the implicit conversion strategy to work.

#### Goal 5 -- Reduced burden for manual specialization

Should investigate replacing the profusion of manually defined methods in Java with Scala specialized variants that defer to type class selected implementations.  Note that Java does _not_ allow you to abstract over type of stream, despite all four interfaces having nearly-identical method names (e.g. `map` on `IntStream` maps from `Int` to `Int`, and there is a `mapToObj` instead of the `mapToInt` on object `Stream`).

This may be too awkward to succeed, but inspiration can be taken from Spire.
