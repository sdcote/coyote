package coyote.i13n;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * This is a simple type which allows the caller to take samples of some 
 * number and track basic data about it.
 * 
 * <p>The most common use case is tracking how long something takes on 
 * average. When something starts, the System.currentTimeMillis() method is 
 * called to get the start time and when it stops System.currentTimeMillis(); 
 * is called again to get the end time. The difference is the elapsed time 
 * and this class can have its {@code sample(long)} method called with the
 * elapsed difference. Instance of this class can then have their various 
 * methods called to access such information as the number of samples, the 
 * average sampled, minimum, and maximum values.
 * 
 * <p>Time is not the only use case. Average, minimum, and maximum sizes of 
 * things can be tracked as well.
 */
public class SimpleMetric {
  private String name = null;
  private String units = "ms";
  private long minValue = 0L;
  private long maxValue = 0L;
  private int samples = 0;
  private long total = 0L;
  private long sumOfSquares = 0L;




  public SimpleMetric( final String name ) {
    this.name = name;
  }




  public SimpleMetric( final String name, final String units ) {
    this( name );
    this.units = units;
  }




  @Override
  public Object clone() {
    final SimpleMetric retval = new SimpleMetric( name );
    retval.units = units;
    retval.minValue = minValue;
    retval.maxValue = maxValue;
    retval.samples = samples;
    retval.total = total;
    retval.sumOfSquares = sumOfSquares;
    return retval;
  }




  private String convertToString( final long value ) {
    final DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getNumberInstance();
    numberFormat.applyPattern( "#,###,###,###,###,###,###" );
    return numberFormat.format( value );
  }




  private long getAverage() {
    if ( samples == 0 ) {
      return 0L;
    }
    return total / samples;
  }




  public long getAvgValue() {
    synchronized( name ) {
      return getAverage();
    }
  }




  protected String getDisplayString( final String type, final String value, final String units ) {
    return type + "=" + value + " " + units + " ";
  }




  public long getMaxValue() {
    synchronized( name ) {
      return maxValue;
    }
  }




  public long getMinValue() {
    synchronized( name ) {
      return minValue;
    }
  }




  public String getName() {
    return name;
  }




  public long getSamplesCount() {
    return samples;
  }




  public long getStandardDeviation() {
    long stdDeviation = 0L;
    if ( samples != 0 ) {
      final long sumOfX = total;
      final int n = samples;
      final int nMinus1 = n <= 1 ? 1 : n - 1;
      final long numerator = sumOfSquares - ( ( sumOfX * sumOfX ) / n );
      stdDeviation = (long)Math.sqrt( numerator / nMinus1 );
    }
    return stdDeviation;
  }




  public long getTotal() {
    return total;
  }




  public String getUnits() {
    return units;
  }




  public Metric reset() {
    synchronized( name ) {
      final Metric retval = (Metric)clone();
      minValue = 0L;
      maxValue = 0L;
      samples = 0;
      total = 0L;
      sumOfSquares = 0L;
      return retval;
    }
  }




  public synchronized void sample( final long value ) {
    samples += 1;
    if ( value < minValue ) {
      minValue = value;
    }
    if ( value > maxValue ) {
      maxValue = value;
    }
    total += value;
    sumOfSquares += value * value;
  }




  void setName( final String name ) {
    this.name = name;
  }




  public void setUnits( final String units ) {
    synchronized( name ) {
      this.units = units;
    }
  }




  @Override
  public String toString() {
    final StringBuffer message = new StringBuffer( name );
    message.append( ": " );
    message.append( getDisplayString( "Samples", convertToString( samples ), "" ) );
    if ( samples > 0 ) {
      message.append( getDisplayString( "Avg", convertToString( getAverage() ), units ) );
      message.append( getDisplayString( "Total", convertToString( total ), units ) );
      message.append( getDisplayString( "Std Dev", convertToString( getStandardDeviation() ), units ) );
      message.append( getDisplayString( "Min Value", convertToString( minValue ), units ) );
      message.append( getDisplayString( "Max Value", convertToString( maxValue ), units ) );
    }
    return message.toString();
  }
}
