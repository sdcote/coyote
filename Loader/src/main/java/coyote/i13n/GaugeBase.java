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

import coyote.dataframe.DataFrame;


/**
 * The GaugeBase class allow for the creation of objects that track rates of
 * change in some performance.
 *
 * <p>A linked list of samples is managed to calculate the current rate per
 * second. This list will grow in size to accommodate the highest sampling rate
 * and will not shrink. The nodes will, however, be re-used as they expire,
 * allowing gauge to keep from creating and destroying temporary objects and
 * causing unnecessary garbage collection.
 *
 * <p>This class also uses a circular array of one-second samples to provide a
 * minute-based rate calculation.
 *
 * <p>Preliminary performance tests show gauges are quite a bit slower than
 * other metric structures:
 * <ul>
 *  <li>Null factory test: 1.5E-5 ms/c Avg (66,666,668 cps)</li>
 *  <li>Timing Gauge test: 7.621E-4 ms/c Avg (1,312,163 cps)</li>
 * </ul> Because of this, gauges should be used only in specialized situations
 * or updated only once, maybe twice, per second.
 */
public class GaugeBase extends NullGauge {
  private static final String CLASS_TAG = "Gauge";
  public static final String VPS = "Vps";
  public static final String LAST_VPS = "LastVps";
  public static final String AVG_VPS = "AvgVps";
  public static final String MAX_VPS = "MaxVps";
  public static final String MIN_VPS = "MinVps";
  public static final String ELAPSED = "Elapsed";
  public static final String TOTAL = "Total";
  public static final String FIRST_ACCESS = "FirstAccess";
  public static final String LAST_ACCESS = "LastAccess";
  public static final String GAUGE = "Gauge";
  public static final String NAME = "Name";




  /**
   * @param args
   */
  public static void main( final String[] args ) {
    final GaugeBase gauge = new GaugeBase();

    System.out.println( "VPS: " + gauge.getValuePerSecond() );
    System.out.println( "lastVPS: " + gauge.getLastValuePerSecond() );
    System.out.println( "avgVPS: " + gauge.getAvgValuePerSecond() );
    System.out.println( "maxVPS: " + gauge.getMaxValuePerSecond() );
    System.out.println( "minVPS: " + gauge.getMinValuePerSecond() );
    System.out.println( "elapsed: " + gauge.getElapsedSeconds() );
    System.out.println( "total: " + gauge.getTotal() );
    System.out.println();

    // Run a 750eps for 5 seconds
    long endtime = System.currentTimeMillis() + 5000;

    while ( System.currentTimeMillis() < endtime ) {
      try {
        Thread.sleep( 300 );
        gauge.update( 250 );
      } catch ( final InterruptedException ignore ) {}
    }

    System.out.println( "VPS: " + gauge.getValuePerSecond() );
    System.out.println( "lastVPS: " + gauge.getLastValuePerSecond() );
    System.out.println( "avgVPS: " + gauge.getAvgValuePerSecond() );
    System.out.println( "maxVPS: " + gauge.getMaxValuePerSecond() );
    System.out.println( "minVPS: " + gauge.getMinValuePerSecond() );
    System.out.println( "elapsed: " + gauge.getElapsedSeconds() );
    System.out.println( "total: " + gauge.getTotal() );
    System.out.println();

    // run at 10 eps for 2 seconds
    endtime = System.currentTimeMillis() + 5000;

    while ( System.currentTimeMillis() < endtime ) {
      try {
        Thread.sleep( 500 );
        gauge.update( 5 );
      } catch ( final InterruptedException ignore ) {}
    }
    System.out.println( "VPS: " + gauge.getValuePerSecond() );
    System.out.println( "lastVPS: " + gauge.getLastValuePerSecond() );
    System.out.println( "avgVPS: " + gauge.getAvgValuePerSecond() );
    System.out.println( "maxVPS: " + gauge.getMaxValuePerSecond() );
    System.out.println( "minVPS: " + gauge.getMinValuePerSecond() );
    System.out.println( "elapsed: " + gauge.getElapsedSeconds() );
    System.out.println( "total: " + gauge.getTotal() );
    System.out.println();

    // run at 10 eps for 2 seconds
    endtime = System.currentTimeMillis() + 5000;
    System.out.println( "Testing for 10vps" );
    gauge.reset();
    while ( System.currentTimeMillis() < endtime ) {
      try {
        Thread.sleep( 4000 );
        gauge.update( 20 );
      } catch ( final InterruptedException ignore ) {}
    }
    System.out.println( "VPS: " + gauge.getValuePerSecond() );
    System.out.println( "lastVPS: " + gauge.getLastValuePerSecond() );
    System.out.println( "avgVPS: " + gauge.getAvgValuePerSecond() );
    System.out.println( "maxVPS: " + gauge.getMaxValuePerSecond() );
    System.out.println( "minVPS: " + gauge.getMinValuePerSecond() );
    System.out.println( "elapsed: " + gauge.getElapsedSeconds() );
    System.out.println( "total: " + gauge.getTotal() );
    System.out.println();

    System.out.println( gauge );

  }

  private long firstAccess;
  private long lastAccess;
  private long totalCount;
  private final StringBuffer buffer = new StringBuffer();
  private Node firstSample = null;
  private Node lastSample = null;
  private long maxvps = 0;

  private long minvps = Long.MAX_VALUE;

  /** Represents the last 1 second sample calculated from recent markings. */
  private volatile Node lastMinuteNode = null;




  public GaugeBase() {
    this( GaugeBase.CLASS_TAG );
  }




  /**
   * Create a new gauge using the string as its name.
   */
  public GaugeBase( final String name ) {
    super( name );

    // Prime the one minute sample array
    final Node start = new Node( 0, 0, null );
    lastMinuteNode = start;
    for ( int x = 0; x < 59; x++ ) {
      lastMinuteNode = new Node( 0, 0, lastMinuteNode );
    }

    // link the start and end nodes to complete the circle
    start.prevNode = lastMinuteNode;
    lastMinuteNode.nextNode = start;
  }




  /**
   * @see coyote.i13n.Gauge#getAvgValuePerSecond()
   */
  @Override
  public float getAvgValuePerSecond() {
    if ( ( lastAccess - firstAccess ) > 0 ) {
      return (float)( totalCount / ( lastAccess - firstAccess ) ) * 1000;
    }

    return totalCount;
  }




  /**
   * @see coyote.i13n.Gauge#getElapsedSeconds()
   */
  @Override
  public float getElapsedSeconds() {
    if ( ( lastAccess - firstAccess ) > 0 ) {
      return (float)( lastAccess - firstAccess ) / 1000;
    }
    return 0.0F;
  }




  /**
   * @return  Returns the firstAccess.
   */
  public long getFirstAccess() {
    return firstAccess;
  }




  /**
   * @return  Returns the lastAccess.
   */
  public long getLastAccess() {
    return lastAccess;
  }




  /**
   * @see coyote.i13n.Gauge#getLastValuePerSecond()
   */
  @Override
  public long getLastValuePerSecond() {
    if ( lastMinuteNode.prevNode.timestamp > 0 ) {
      return lastMinuteNode.prevNode.value;
    }
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getMaxValuePerSecond()
   */
  @Override
  public long getMaxValuePerSecond() {
    if ( maxvps == Long.MIN_VALUE ) {
      return 0;
    }
    return maxvps;
  }




  /**
   * @see coyote.i13n.Gauge#getMinuteTotal()
   */
  @Override
  public long getMinuteTotal() {
    synchronized( buffer ) {
      long retval = 0;
      Node currNode = lastMinuteNode;
      for ( int x = 0; x < 60; x++ ) {
        retval += currNode.value;
        currNode = currNode.prevNode;
      }

      return retval;
    }
  }




  /**
   * @see coyote.i13n.Gauge#getMinValuePerSecond()
   */
  @Override
  public long getMinValuePerSecond() {
    if ( minvps == Long.MAX_VALUE ) {
      return 0;
    }
    return minvps;
  }




  /**
   * @see coyote.i13n.Gauge#getTotal()
   */
  @Override
  public long getTotal() {
    return totalCount;
  }




  /**
   * @see coyote.i13n.Gauge#getValuePerMinute()
   */
  @Override
  public float getValuePerMinute() {
    // TODO complete this
    final float retval = 0.0F;
    if ( ( lastAccess - firstAccess ) < 60000 ) {
      // extrapolate vpm since we have not been running for a whole minute yet
    }
    return retval;
  }




  /**
   * @see coyote.i13n.Gauge#getValuePerSecond()
   */
  @Override
  public float getValuePerSecond() {
    if ( lastSample != null ) {
      final long now = System.currentTimeMillis();
      long cntr = 0;

      long endTime = lastMinuteNode.prevNode.timestamp;
      if ( endTime == 0 ) {
        endTime = firstSample.timestamp;
      }

      Node cNode = lastSample;
      while ( cNode.timestamp >= endTime ) {
        cntr += cNode.value;
        cNode = cNode.prevNode;
        if ( cNode == null ) {
          break;
        }
      }

      return ( (float)cntr / ( now - endTime ) ) * 1000;
    }

    return 0.0F;
  }




  public synchronized void removeNode( final Node node ) {
    if ( node.prevNode == null ) {
      firstSample = node.nextNode;
    } else {
      node.prevNode.nextNode = node.nextNode;
    }

    if ( node.nextNode == null ) {
      lastSample = node.prevNode;
    } else {
      node.nextNode.prevNode = node.prevNode;
    }
  }




  /**
   * Reset all samples and counters effectively returning this gauge to a state
   * similar to that of a new gauge.
   */
  @Override
  public void reset() {
    synchronized( buffer ) {
      // Clear out all the samples so they can be GC'd
      Node cNode = firstSample;
      while ( cNode != null ) {
        cNode.prevNode = null; // remove back ref
        final Node node = cNode.nextNode; // save forward ref
        cNode.nextNode = null; // remove forward ref
        cNode = node; // use old forward ref as current
      }

      // remove the reference to the last sample
      lastSample = null;

      // clear out minute samples
      for ( int x = 0; x < 60; x++ ) {
        lastMinuteNode.timestamp = lastMinuteNode.value = 0;
      }

      // reset totals
      firstAccess = lastAccess = totalCount = 0;

      maxvps = Long.MIN_VALUE;
      minvps = Long.MAX_VALUE;

    }
  }




  @Override
  public String toString() {
    synchronized( buffer ) {
      buffer.delete( 0, buffer.length() );

      buffer.append( lastMinuteNode.value );

      // return our string representation
      return buffer.toString();
    }

  }




  /**
   * Update the sample data with the given value.
   *
   * <p>This method assumes it will be called at regular intervals during
   * periods of activity and not be called if there is nothing to measure. This
   * implies that the value of any update call will only be applied to the last
   * 1000 milliseconds of time. If this method was not called for 29 seconds
   * and then called with a value of 60 in the 30th second, then the entire
   * value of 60 will be applied to the last second of time, resulting in a
   * rate of 60 events per second and not 2 events per second as would be the
   * case of the activity was averaged out over the missing updates.
   *
   * <p>Last profiler metric: 0.001187 ms per call - 847,459cps
   *
   * @param val the value of the last sample since the last update was called.
   */
  @Override
  public void update( final long val ) {
    synchronized( buffer ) {
      lastAccess = System.currentTimeMillis();
      totalCount += val;

      if ( lastSample == null ) {
        // We have no samples in the current list, create a new first/last node
        lastSample = new Node( lastAccess, val, null );
        firstSample = lastSample;
        firstAccess = lastAccess;
      } else {
        if ( lastSample.nextNode == null ) {
          // We are at the end of our list of samples, create a new node
          lastSample = new Node( lastAccess, val, lastSample );
        } else {
          // reuse the next node in the list
          lastSample = lastSample.nextNode;

          // set the timestamp
          lastSample.timestamp = lastAccess;

          // set the value
          lastSample.value = val;
        }
      }

      // make sure the last node has a timestamp; this will happen only once
      if ( lastMinuteNode.timestamp == 0 ) {
        lastMinuteNode.timestamp = lastSample.timestamp;
      }

      // Now check to see if it is time to update the minute sample array with
      // one or more seconds worth of data
      if ( ( lastAccess - lastMinuteNode.timestamp ) >= 1000 ) {
        // move to the next minute node
        lastMinuteNode = lastMinuteNode.nextNode;
        // update the timestamp
        lastMinuteNode.timestamp = lastMinuteNode.prevNode.timestamp + 1000;

        // Start posting samples to their proper nodes based on timestamps
        Node currNode = lastMinuteNode;
        while ( lastSample != null ) {
          while ( lastSample.timestamp > currNode.prevNode.timestamp ) {
            currNode.value += lastSample.value;
            lastSample = lastSample.prevNode;
            if ( lastSample == null ) {
              break;
            }
          }
          currNode = currNode.prevNode;
        } // for all the samples

        if ( lastMinuteNode.prevNode.value > maxvps ) {
          maxvps = lastMinuteNode.prevNode.value;
        }

        if ( lastMinuteNode.prevNode.value < minvps ) {
          minvps = lastMinuteNode.prevNode.value;
        }

      } // if more than 1000ms has passed

    } // sync
  }




  public DataFrame toFrame() {
    synchronized( buffer ) {
      final DataFrame retval = new DataFrame();
      //retval.setType( GAUGE );
        retval.put( NAME, name );
        retval.put( AVG_VPS, getAvgValuePerSecond() );
        retval.put( VPS, getValuePerSecond() );
        retval.put( LAST_VPS, getLastValuePerSecond() );
        retval.put( MAX_VPS, getMaxValuePerSecond() );
        retval.put( MIN_VPS, getMinValuePerSecond() );
        retval.put( ELAPSED, getElapsedSeconds() );
        retval.put( TOTAL, getTotal() );
        retval.put( FIRST_ACCESS, getFirstAccess() );
        retval.put( LAST_ACCESS, getLastAccess() );
      return retval;
    }

  }

  /**
   * The NetworkService class models a time-stamped sampling of data.
   *
   * <p>This class is designed to be used in a linked list pattern, allowing
   * for smooth movement from one node to the next without having to check
   * array indexes for wraps in circular arrays.
   */
  private class Node {
    long timestamp;
    long value;
    Node nextNode;
    Node prevNode;




    /**
     * A sample of occurrences.
     *
     * @param time Time the node represents
     * @param sample The number of occurrences in this node
     * @param prev The previous node for linking
     */
    Node( final long time, final long sample, final Node prev ) {
      timestamp = time;
      value = sample;
      prevNode = prev;

      // create the forward link on the previous node
      if ( prevNode != null ) {
        prevNode.nextNode = this;
      }
    }

  } // NetworkService inner class

} // GaugeBase Class
