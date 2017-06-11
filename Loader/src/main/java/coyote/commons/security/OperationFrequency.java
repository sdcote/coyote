/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.security;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;


/**
 * This provides a basic check for too frequent requests by IP address.
 * 
 * <p>This class sets up a map of check times mapped by IpAddress. For each 
 * entry in the table, a circular array contains times, if a check matches an 
 * entry, it is then checked to see if the next element in the array is less 
 * than the allowable interval, if so false is returned indicating the check 
 * happened too soon and that too may checks are being performed too often.
 * 
 * <p>This class is designed to provide a simple way to check for a Denial of 
 * Service attack by allowing the setting a limit of the number of requests by
 * IP Address in some time frame. 
 */
public class OperationFrequency {
  private static final short DEFAULT_LIMIT = 24;
  private static final long DEFAULT_DURATION = 500;

  private Map<IpNetwork, RequestTable> networks = new HashMap<IpNetwork, RequestTable>();
  private Map<IpAddress, RequestTable> addresses = new HashMap<IpAddress, RequestTable>();
  private short limit = DEFAULT_LIMIT;
  private long duration = DEFAULT_DURATION;




  public synchronized RequestTable addNetwork( IpNetwork addr, short limit, long duration ) {
    RequestTable retval = new RequestTable( limit, duration );
    networks.put( addr, retval );
    return retval;
  }




  public synchronized RequestTable addAddress( IpAddress addr, short limit, long duration ) {
    RequestTable retval = new RequestTable( limit, duration );
    addresses.put( addr, retval );
    return retval;
  }




  public boolean check( InetAddress addr ) {
    try {
      return check( new IpAddress( addr ) );
    } catch ( IpAddressException ignore ) {
      return false; // should never happen
    }
  }




  public synchronized boolean check( IpAddress addr ) {
    RequestTable table = null;

    // look for a network match
    for ( IpNetwork net : networks.keySet() ) {
      if ( net.contains( addr ) ) {
        table = networks.get( net );
        break;
      }
    }

    // else look for an address match
    if ( table == null ) {
      for ( IpAddress adr : addresses.keySet() ) {
        if ( adr.equals( addr ) ) {
          table = addresses.get( adr );
          break;
        }
      }
    }

    // else add a new address
    if ( table == null ) {
      table = addAddress( addr, limit, duration );
    }

    return table.check( System.currentTimeMillis() );
  }




  public int getNetworkCount() {
    return networks.size();
  }




  public int getAddressCount() {
    return addresses.size();
  }




  /**
   * @return the limit
   */
  public short getLimit() {
    return limit;
  }




  /**
   * @param limit the limit to set
   */
  public void setLimit( short limit ) {
    this.limit = limit;
  }




  /**
   * @return the duration
   */
  public long getDuration() {
    return duration;
  }




  /**
   * @param duration the duration to set
   */
  public void setDuration( long duration ) {
    this.duration = duration;
  }




  /**
   * Clear out all old address mappings by their last check time.
   * 
   * <p>Network Mappings are not touched
   * 
   * @param age any tables with last check times older than this number of 
   *        milliseconds will be removed from the mappings
   */
  public synchronized void expire( long age ) {
    long time = System.currentTimeMillis();

    Iterator<Map.Entry<IpAddress, RequestTable>> it = addresses.entrySet().iterator();
    while ( it.hasNext() ) {
      Map.Entry<IpAddress, RequestTable> entry = it.next();

      if ( time - entry.getValue().getLastCheck() > age ) {
        it.remove();
      }
    }

  }

  /**
   * This is a class which allows us to track the times and occurrences of 
   * checks.
   * 
   * <p>If the maximum number of requests have been received within the 
   * interval, the check will fail. This allows us to track if too many 
   * requests are being received in a particular interval. 
   */
  private class RequestTable {
    private final long[] times;
    private final long interval;
    private long count = -1;




    /**
     * Create a table with the given number of entries and interval window.
     * 
     * @param size the number of entries to allow in a particular interval
     * @param interval the number of milliseconds for the window
     */
    RequestTable( short size, long interval ) {
      times = new long[size];
      this.interval = interval;
    }




    /**
     * @return the time of the last check
     */
    public long getLastCheck() {
      // find the current position in the list of times
      int index = (int)( ( count < 0 ) ? 0 : count % times.length );
      return times[index];
    }




    /**
     * @param time the time in millis (Java epoch)
     * 
     * @return true if the maximum number of requests have not been reached, 
     *         false if too many requests have been reached
     */
    boolean check( long time ) {
      // in Java, overflows go negative not back to zero
      if ( count < 0 ) {
        count = 0;
      } else {
        count++;
      }

      // find the next position in the list of times
      int index = (int)( count % times.length );

      try {
        if ( times[index] > 0 && time - times[index] < interval )
          return false;
        else
          return true;
      }
      finally {
        times[index] = time;
      }
    }

  } // class

}