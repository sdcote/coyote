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

import java.util.LinkedList;


/**
 * The EventList class models a synchronized list of application events.
 */
public class EventList {
  /** The maximum number of events to keep in our Event list. */
  private static int _maxEvents = 1000;
  private final Object _lock = new Object();

  private volatile long _nextEventSequence = 0;

  static LinkedList<AppEvent> _list = new LinkedList<AppEvent>();




  /**
   *
   */
  public EventList() {
    super();
  }




  public synchronized void add( final AppEvent event ) {
    _list.add( event );
    while ( _list.size() > _maxEvents ) {
      _list.removeFirst();
    }
  }




  /**
   * Create an event with a message and add it to the list.
   *
   * <p>The event will contain a severity of 0 (indeterminate) and will not be
   * flagged as an error. All identifiers and codes will either not be filled
   * in or set to 0 (zero).
   *
   * @param msg The message text to place within the event.
   *
   * @return A sequenced event with the appropriate values filled-in.
   */
  public AppEvent createEvent( final String msg ) {
    return createEvent( null, null, null, msg, AppEvent.INDETERMINATE, 0, 0, null );
  }




  /**
   * Create an event with a message and major and minor codes and add it to the
   * list.
   *
   * <p>The event will contain a severity of 0 (indeterminate) and will not be
   * flagged as an error. All other identifiers will be set to null.
   *
   * @param msg The message text to place within the event.
   * @param majorcode
   * @param minorcode
   *
   * @return A sequenced event with the appropriate values filled-in.
   */
  public AppEvent createEvent( final String msg, final int majorcode, final int minorcode ) {
    return createEvent( null, null, null, msg, AppEvent.INDETERMINATE, majorcode, minorcode, null );
  }




  /**
   * Create an event and add it to the list.
   *
   * <p>This is the preferred call when generating an event because it will
   * explicitly set all the values in the returned event.
   *
   * @param appid A correlating identifier for an application.
   * @param sysid A correlating identifier for a sub-system within an application.
   * @param cmpid A correlating identifier of a component within a sub-system.
   * @param msg The message text to place within the event.
   * @param sv The severity of the event usually in the range of 0(unknown) to 5 (critical).
   * @param maj Major Code - An identifying code of the event.
   * @param min Minor Code - A finer grained identifying code for the event used as a sub code ot the major code.
   * @param cat The category of the event; exception, application, database, etc.
   *
   * @return A sequenced event with the appropriate values filled-in.
   */
  public synchronized AppEvent createEvent( final String appid, final String sysid, final String cmpid, final String msg, final int sv, final int maj, final int min, final String cat ) {

    final AppEvent retval = new AppEvent( nextSequence(), appid, sysid, cmpid, msg, sv, maj, min, cat, this );
    add( retval );
    return retval;
  }




  /**
   * Return the event with the given sequence number from the event list.
   *
   * @param seq The sequence identifier of the event to retrieve.
   *
   * @return The event with the given sequence number or null if not found.
   */
  public synchronized AppEvent get( final long seq ) {
    // make sure the event exists
    if ( ( _list.getFirst() ).getSequence() <= seq ) {
      if ( ( ( _list.getLast() ) ).getSequence() >= seq ) {
        // perform a binary search on the event list.
        int indx = 0;
        int size = _list.size() - 1;
        while ( indx <= size ) {
          final int i = ( indx + size ) >> 1;
          final AppEvent retval = _list.get( i );
          final long value = retval.getSequence();
          if ( value < seq ) {
            indx = i + 1;
          } else if ( value > seq ) {
            size = i - 1;
          } else {
            return _list.get( i );
          }
        }
        return null;

      } else {
        // System.out.println( "Not created yet" );
      }
    } else {
      // System.out.println( "Expired" );
    }

    return null;
  }




  public synchronized AppEvent getFirst() {
    return _list.getFirst();
  }




  public synchronized AppEvent getLast() {
    return _list.getLast();
  }




  /**
   * @return  Returns the maximum number of events to keep in the list.
   */
  public static int getMaxEvents() {
    return EventList._maxEvents;
  }




  /**
   * @return the number of entries currently in the list/
   */
  public int getSize() {
    return _list.size();
  }




  public long lastSequence() {
    synchronized( _lock ) {
      return _nextEventSequence - 1;
    }
  }




  private long nextSequence() {
    synchronized( _lock ) {
      return _nextEventSequence++;
    }
  }




  /**
   * Remove the given event from the list.
   *
   * @param event The event to remove from the list.
   */
  public synchronized void remove( final AppEvent event ) {
    _list.remove( event );
  }




  public synchronized AppEvent removeFirst() {
    return _list.removeFirst();
  }




  /**
   * Set the maximum number of events to keep in the list.
   *
   * @param max  The maximum number of events to keep.
   */
  public static void setMaxEvents( final int max ) {
    _maxEvents = max;
    while ( _list.size() > _maxEvents ) {
      _list.removeFirst();
    }
  }

}
