package coyote.mc;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MetricUtil {

  public static float round(float value, int precision){
     return new BigDecimal(Float.toString(value)).setScale(precision, RoundingMode.HALF_UP).floatValue();
  }

}
