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
 * The TimingMaster class models the master of all timers with a given name.
 *
 * <p>This class is used to summarize all the timers in its list.
 */
public class TimingMaster implements TimerMaster {
  public static final String CLASS_TAG = "Timer";

  /** The number of global timers currently active. */
  private static volatile long globalCounter;

  static private final String MILLISECONDS = "ms";

  static private final String NONE = "";

  static private final String TOTAL = "Total";

  static private final String MIN = "Min Value";

  static private final String MAX = "Max Value";

  static private final String HITS = "Hits";

  static private final String AVG = "Avg";
  static private final String STANDARD_DEVIATION = "Std Dev";

  static private final String ACTIVE = "Active";
  static private final String AVGACTIVE = "Avg Active";
  static private final String MAXACTIVE = "Max Active";
  static private final String FIRSTACCESS = "First Access";
  static private final String LASTACCESS = "Last Access";
  public static final String NAME = "Name";




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

  /** The name of this master set of timers. */
  String _name = null;
  /** Flag indicating we are currently running; the start() has been called */
  boolean running = true;
  /** Flag indicating if this timer is enabled */
  private volatile boolean _enabled = true;
  /** The number of timers currently active. */
  private volatile long activeCounter;
  /** Flag indicating whether or not to store the first accessed time */
  private boolean isFirstAccess = true;
  /** Epoch time in milliseconds when this timer was first accessed */
  private long firstAccessTime;
  /** Epoch time in milliseconds when this timer was last accessed */
  private long lastAccessTime;
  private long maxActive = 0;

  // -

  private long totalActive = 0;
  long accrued;
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private int hits;
  private long total;

  private long sumOfSquares;




  /**
   *
   */
  public TimingMaster( final String name ) {
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




  /**
   * Create a new instance of a timer that will track times and other datum.
   *
   * @return A Timer that can be stopped at some time to generate datum.
   */
  public Timer createTimer() {
    Timer retval;
    if ( _enabled ) {
      retval = new TimingTimer( this );
      hits++;
    } else {
      retval = new NullTimer( this );
    }

    return retval;
  }




  /**
   * @return  Returns the accrued datum for all stopped timers.
   */
  public long getAccrued() {
    return accrued;
  }




  /**
   * @return the average time for all stopped timers for this master list.
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
  private final float getAvgActive() {
    if ( hits == 0 ) {
      return 0;
    } else {
      return (float)totalActive / hits;
    }
  }




  /**
   * Access the number of timers active for this timer master.
   *
   * @return Returns the number of timers currently active (started) for this
   *         master timer.
   */
  public long getCurrentActive() {
    return activeCounter;
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
    if ( TimingMaster.NONE.equals( units ) ) {
      return type + "=" + value + ", ";
    } else {
      return type + "=" + value + " " + units + ", ";
    }
  }




  /**
   * Access the number of timers started in the runtime environment.
   *
   * @return Returns the number of timers currently active (started) for all
   *         master timers.
   */
  public long getGloballyActive() {
    return TimingMaster.globalCounter;
  }




  /**
   * @return The name of this timer set.
   */
  @Override
  public String getName() {
    return _name;
  }




  /**
   * Access the current standard deviation for all stopped timers using the
   * Sum of Squares algorithm.
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
  @Override
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
   * @return True if the timer set is enabled, false otherwise.
   */
  public synchronized boolean isEnabled() {
    return _enabled;
  }




  /**
   * Reset all variables for this master timer instance.
   *
   * <p>The effect of this is to reset this objects variables to the state they
   * were in when the object was first created.
   */
  synchronized protected void resetThis() {
    min = Long.MAX_VALUE;
    max = Long.MIN_VALUE;
    total = accrued = sumOfSquares = maxActive = totalActive = hits = 0;
    firstAccessTime = lastAccessTime = System.currentTimeMillis();
  }




  /**
   * Enable or disable all the timers in this list.
   *
   * @param flag True to enable the timer, false to keep it from processing.
   */
  public synchronized void setEnabled( final boolean flag ) {
    _enabled = flag;
  }




  /**
   * Start the timer in the context of this master timer.
   *
   * @param timr the timer to start.
   *
   * @see coyote.i13n.TimerMaster#start(coyote.i13n.Timer)
   */
  @Override
  public synchronized void start( final Timer timr ) {
    activeCounter++;
    TimingMaster.globalCounter++;

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




  /**
   * Stop the timer in the context of this master timer.
   *
   * @param mon the timer to stop.
   *
   * @see coyote.i13n.TimerMaster#stop(coyote.i13n.Timer)
   */
  @Override
  public synchronized void stop( final Timer mon ) {
    activeCounter--;
    TimingMaster.globalCounter--;
    accrued += mon.getAccrued();
  }




  /**
   * Method toString
   *
   * @return a string representing the timer.
   */
  @Override
  public String toString() {
    final StringBuffer message = new StringBuffer( _name );
    message.append( ": " );
    message.append( getDisplayString( TimingMaster.HITS, convertToString( hits ), TimingMaster.NONE ) );

    if ( ( hits - activeCounter ) > 0 ) {
      message.append( getDisplayString( TimingMaster.AVG, convertToString( getAverage() ), TimingMaster.MILLISECONDS ) );
      message.append( getDisplayString( TimingMaster.TOTAL, convertToString( total ), TimingMaster.MILLISECONDS ) );
      message.append( getDisplayString( TimingMaster.STANDARD_DEVIATION, convertToString( getStandardDeviation() ), TimingMaster.MILLISECONDS ) );
      message.append( getDisplayString( TimingMaster.MIN, convertToString( min ), TimingMaster.MILLISECONDS ) );
      message.append( getDisplayString( TimingMaster.MAX, convertToString( max ), TimingMaster.MILLISECONDS ) );
    }
    message.append( getDisplayString( TimingMaster.ACTIVE, convertToString( activeCounter ), TimingMaster.NONE ) );
    message.append( getDisplayString( TimingMaster.MAXACTIVE, convertToString( maxActive ), TimingMaster.NONE ) );
    message.append( getDisplayString( TimingMaster.AVGACTIVE, TimingMaster.convertToString( getAvgActive() ), TimingMaster.NONE ) );
    message.append( getDisplayString( TimingMaster.FIRSTACCESS, getDateString( firstAccessTime ), TimingMaster.NONE ) );
    message.append( getDisplayString( TimingMaster.LASTACCESS, getDateString( lastAccessTime ), TimingMaster.NONE ) );

    return message.toString();
  }




  public synchronized DataFrame toFrame() {
    final DataFrame retval = new DataFrame();
    retval.put( NAME, _name );
    retval.put( HITS, hits );
    retval.put( AVG, getAverage() );
    retval.put( TOTAL, total );
    retval.put( STANDARD_DEVIATION, getStandardDeviation() );
    retval.put( MIN, min );
    retval.put( MAX, max );
    retval.put( ACTIVE, activeCounter );
    retval.put( MAXACTIVE, maxActive );
    retval.put( AVGACTIVE, getAvgActive() );
    retval.put( FIRSTACCESS, new Date( firstAccessTime ) );
    retval.put( LASTACCESS, new Date( lastAccessTime ) );
    return retval;
  }

}
