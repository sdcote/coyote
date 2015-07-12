/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * The Metric class models a basic metric.
 */
public class Metric {
  private String name = null;

  private String units = "ms";
  private long minValue = 0;
  private long maxValue = 0;
  private int samples = 0;
  private long total = 0;
  private long sumOfSquares = 0;

  static private final String NONE = "";
  static private final String TOTAL = "Total";
  static private final String MIN = "Min Value";
  static private final String MAX = "Max Value";
  static private final String SAMPLES = "Samples";
  static private final String AVG = "Avg";
  static private final String STANDARD_DEVIATION = "Std Dev";




  /**
   * 
   */
  public Metric( final String name ) {
    this.name = name;
  }




  public Metric( final String name, String units ) {
    this( name );
    this.units = units;
  }




  /**
   * Create a deep copy of this counter.
   */
  @Override
  public Object clone() {
    final Metric retval = new Metric( this.name );
    retval.units = this.units;
    retval.minValue = this.minValue;
    retval.maxValue = this.maxValue;
    retval.samples = this.samples;
    retval.total = this.total;
    retval.sumOfSquares = this.sumOfSquares;
    return retval;
  }




  /**
   * @return The currently set name of this object.
   */
  public String getName() {
    return name;
  }




  /**
   * @return The number of times the value was updated.
   */
  public long getSamplesCount() {
    return samples;
  }




  /**
   * Included for balance but it should not be used by the uninitiated.
   * 
   * @param name The new name to set.
   */
  void setName( final String name ) {
    this.name = name;
  }




  /**
   * @return Returns the maximum value the counter ever represented.
   */
  public long getMaxValue() {
    synchronized( name ) {
      return maxValue;
    }
  }




  /**
   * @return Returns the minimum value the counter ever represented.
   */
  public long getMinValue() {
    synchronized( name ) {
      return minValue;
    }
  }




  /**
   * @return Returns the units the counter measures.
   */
  public String getUnits() {
    return units;
  }




  /**
    * Increase the time by the specified amount of milliseconds.
    * 
    * <p>This is the method that keeps track of the various statistics being 
    * tracked.</p>
    *
    * @param value the amount to increase the accrued value.
    */
  public synchronized void sample( final long value ) {
    // Increment the number of samples
    samples++;

    // calculate min
    if ( value < minValue ) {
      minValue = value;
    }

    // calculate max
    if ( value > maxValue ) {
      maxValue = value;
    }

    // calculate total i.e. sumofX's
    total += value;

    sumOfSquares += value * value;
  }




  /**
   * Set the current, update count and Min/Max values to zero.
   * 
   * <p>The return value will represent a copy of the counter prior to the 
   * reset and is useful for applications that desire delta values. These delta
   * values are simply the return values of successive reset calls.</p>
   * 
   * @return a counter representing the state prior to the reset.
   */
  public Metric reset() {
    synchronized( name ) {
      final Metric retval = (Metric)clone();

      minValue = 0;
      maxValue = 0;
      samples = 0;
      total = 0;
      sumOfSquares = 0;

      return retval;
    }
  }




  /**
   * Sets the units the counter measures.
   * 
   * @param units The units to set.
   */
  public void setUnits( final String units ) {
    synchronized( name ) {
      this.units = units;
    }
  }




  /**
   * Access the current standard deviation for all samples using the Sum of Squares algorithm.
   *
   * @return The amount of one standard deviation of all the sample values. 
   */
  public long getStandardDeviation() {
    long stdDeviation = 0;
    if ( samples != 0 ) {
      final long sumOfX = total;
      final int n = samples;
      final int nMinus1 = ( n <= 1 ) ? 1 : n - 1; // avoid 0 divides;

      final long numerator = sumOfSquares - ( ( sumOfX * sumOfX ) / n );
      stdDeviation = (long)java.lang.Math.sqrt( numerator / nMinus1 );
    }

    return stdDeviation;
  }




  private String convertToString( final long value ) {
    final DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getNumberInstance();
    numberFormat.applyPattern( "#,###" );

    return numberFormat.format( value );
  }




  protected String getDisplayString( final String type, final String value, final String units ) {
    return type + "=" + value + " " + units + "  ";
  }




  private long getAverage() {
    if ( samples == 0 ) {
      return 0;
    } else {
      return total / samples;
    }
  }




  @Override
  public String toString() {
    final StringBuffer message = new StringBuffer( name );
    message.append( ": " );
    message.append( getDisplayString( SAMPLES, convertToString( samples ), NONE ) );

    if ( samples > 0 ) {
      message.append( getDisplayString( AVG, convertToString( getAverage() ), units ) );
      message.append( getDisplayString( TOTAL, convertToString( total ), units ) );
      message.append( getDisplayString( STANDARD_DEVIATION, convertToString( getStandardDeviation() ), units ) );
      message.append( getDisplayString( MIN, convertToString( minValue ), units ) );
      message.append( getDisplayString( MAX, convertToString( maxValue ), units ) );
    }

    return message.toString();
  }
}