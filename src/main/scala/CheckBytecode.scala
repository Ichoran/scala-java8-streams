package checkbytecode

object CheckBytecode {
  import scala.collection.j8._
  
  def roundDownInt(ds: java.util.stream.DoubleStream) = 
    (ds: ScalaFlavoredJavaDoubleStream).map(x => math.floor(x).toInt).underlying.sum
  
  def roundDownIntDirect(ds: java.util.stream.DoubleStream) =
    ds.mapToInt(x => math.floor(x).toInt).sum
}
