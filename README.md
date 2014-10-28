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
