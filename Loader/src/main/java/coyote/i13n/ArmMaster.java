/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.i13n;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import coyote.dataframe.DataFrame;


/**
 * The ArmMaster class models the master of all ARM transactions with a given
 * name.
 */
public class ArmMaster {
  static private final String NAME = "Name";
  static private final String MILLISECONDS = "ms";
  static private final String NONE = "";
  static private final String TOTAL = "Total";
  static private final String MIN = "Min Value";
  static private final String MAX = "Max Value";
  static private final String CALLS = "Calls";
  static private final String AVG = "Avg";
  static private final String STANDARD_DEVIATION = "Std Dev";
  static private final String ACTIVE = "Active";
  static private final String AVGACTIVE = "Avg Active";
  static private final String MAXACTIVE = "Max Active";
  static private final String FIRSTACCESS = "First Access";
  static private final String LASTACCESS = "Last Access";




  /**
   * Convert a float value to a String
   *
   * @param value
   *
   * @return
   */
  protected static String convertToString( final double value ) {
    final DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getNumberInstance();
    numberFormat.applyPattern( "#,###.#" );
    return numberFormat.format( value );
  }

  /** The name of this master set of transactions. */
  String _name = null;
  /** Flag indicating if this set is enabled. */
  private volatile boolean _enabled = true;
  /** Times this class of ARM has been invoked. */
  private int hits;
  /** The number of transactions of this type currently active. */
  private volatile long activeCounter;
  /** Flag indicating whether or not to store the first accessed time */
  private boolean isFirstAccess = true;
  /** Epoch time in milliseconds when this ARM was first accessed */
  private long firstAccessTime;
  /** Epoch time in milliseconds when this ARM was last accessed */
  private long lastAccessTime;
  private long maxActive = 0;
  private long totalActive = 0;
  long accrued;
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private long total;
  private long sumOfSquares;




  /**
   * @param name
   */
  public ArmMaster( final String name ) {
    _name = name;
  }




  /**
   * Method convertToString
   *
   * @param value
   *
   * @return
   */
  protected String convertToString( final long value ) {
    final DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getNumberInstance();
    numberFormat.applyPattern( "#,###" );

    return numberFormat.format( value );
  }




  public ArmTransaction createArm( final String name, final String crid ) {
    ArmTransaction retval;
    if ( _enabled ) {
      retval = new TimingArm( this, name, crid );
      hits++;
    } else {
      retval = new NullArm( this, name, crid );
    }
    return retval;
  }




  /**
   * @return the average time for all stopped transactions for this master list.
   */
  private long getAverage() {
    // we can only average the total number of closures not just the hits
    final long closures = ( hits - activeCounter );

    if ( closures == 0 ) {
      return 0;
    } else {
      return total / closures;
    }
  }




  /**
   * @return the average number of active for the life of this master list.
   */
  final float getAvgActive() {
    if ( hits == 0 ) {
      return 0;
    } else {
      return (float)totalActive / hits;
    }
  }




  /**
   * Method getDateString
   *
   * @param time
   *
   * @return
   */
  private String getDateString( final long time ) {
    if ( time == 0 ) {
      return "";
    } else {
      return DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.DEFAULT ).format( new Date( time ) );
    }
  }




  /**
   * Method getDisplayString
   *
   * @param type
   * @param value
   * @param units
   *
   * @return
   */
  protected String getDisplayString( final String type, final String value, final String units ) {
    return type + "=" + value + " " + units + " ";
  }




  /**
   * Access the current standard deviation for all stopped transactions using
   * the Sum of Squares alogrithm.
   *
   * @return The amount of one standard deviation of all the interval times.
   */
  private long getStandardDeviation() {
    long stdDeviation = 0;
    if ( hits != 0 ) {
      final long sumOfX = total;
      final int n = hits;
      final int nMinus1 = ( n <= 1 ) ? 1 : n - 1; // avoid 0 divides;

      final long numerator = sumOfSquares - ( ( sumOfX * sumOfX ) / n );
      stdDeviation = (long)java.lang.Math.sqrt( numerator / nMinus1 );
    }

    return stdDeviation;
  }




  /**
   * Increase the time by the specified amount of milliseconds.
   *
   * <p>This is the method that keeps track of the various statistics being
   * tracked.
   *
   * @param value the amount to increase the accrued value.
   */
  public synchronized void increase( final long value ) {
    // calculate min
    if ( value < min ) {
      min = value;
    }

    // calculate max
    if ( value > max ) {
      max = value;
    }

    // total _accrued value
    accrued += value;

    // calculate total i.e. sumofX's
    total += value;

    sumOfSquares += value * value;
  }




  /**
   * @return True if the ARM set is enabled, false otherwise.
   */
  public synchronized boolean isEnabled() {
    return _enabled;
  }




  /**
   * Enable or disable all the ARM transactions in this list.
   *
   * @param flag True to enable the ARMs, false to keep it from processing.
   */
  public synchronized void setEnabled( final boolean flag ) {
    _enabled = flag;
  }




  public synchronized void start( final ArmTransaction arm ) {
    activeCounter++;
    if ( activeCounter > maxActive ) {
      maxActive = activeCounter;
    }

    totalActive += activeCounter;

    final long now = System.currentTimeMillis();
    lastAccessTime = now;

    if ( isFirstAccess ) {
      isFirstAccess = false;
      firstAccessTime = now;
    }
  }




  public synchronized void stop( final ArmTransaction arm ) {
    activeCounter--;
    accrued += arm.getTotalTime();
  }




  @Override
  public String toString() {
    final StringBuffer message = new StringBuffer( _name );
    message.append( ": " );
    message.append( getDisplayString( ArmMaster.CALLS, convertToString( hits ), ArmMaster.NONE ) );

    if ( ( hits - activeCounter ) > 0 ) {
      message.append( getDisplayString( ArmMaster.AVG, convertToString( getAverage() ), ArmMaster.MILLISECONDS ) );
      message.append( getDisplayString( ArmMaster.TOTAL, convertToString( total ), ArmMaster.MILLISECONDS ) );
      message.append( getDisplayString( ArmMaster.STANDARD_DEVIATION, convertToString( getStandardDeviation() ), ArmMaster.MILLISECONDS ) );
      message.append( getDisplayString( ArmMaster.MIN, convertToString( min ), ArmMaster.MILLISECONDS ) );
      message.append( getDisplayString( ArmMaster.MAX, convertToString( max ), ArmMaster.MILLISECONDS ) );
    }
    message.append( getDisplayString( ArmMaster.ACTIVE, convertToString( activeCounter ), ArmMaster.NONE ) );
    message.append( getDisplayString( ArmMaster.MAXACTIVE, convertToString( maxActive ), ArmMaster.NONE ) );
    message.append( getDisplayString( ArmMaster.AVGACTIVE, ArmMaster.convertToString( getAvgActive() ), ArmMaster.NONE ) );
    message.append( getDisplayString( ArmMaster.FIRSTACCESS, getDateString( firstAccessTime ), ArmMaster.NONE ) );
    message.append( getDisplayString( ArmMaster.LASTACCESS, getDateString( lastAccessTime ), ArmMaster.NONE ) );

    return message.toString();
  }




  public synchronized DataFrame toFrame() {
    DataFrame retval = new DataFrame();
    retval.put( NAME, _name );
    retval.put( CALLS, new Integer( hits ) );
    retval.put( AVG, new Long( getAverage() ) );
    retval.put( TOTAL, new Long( total ) );
    retval.put( STANDARD_DEVIATION, new Long( getStandardDeviation() ) );
    retval.put( MIN, new Long( min ) );
    retval.put( MAX, new Long( max ) );
    retval.put( ACTIVE, new Long( activeCounter ) );
    retval.put( MAXACTIVE, new Long( maxActive ) );
    retval.put( AVGACTIVE, new Float( getAvgActive() ) );
    retval.put( FIRSTACCESS, new Date( firstAccessTime ) );
    retval.put( LASTACCESS, new Date( lastAccessTime ) );
    return retval;
  }

}
