package checkbytecode;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class CheckJBC {
  public static int floorSum(DoubleStream ds) {
    return ds.mapToInt(d -> (int)Math.floor(d)).sum();
  }
}
