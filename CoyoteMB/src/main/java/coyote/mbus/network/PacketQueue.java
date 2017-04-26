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
package coyote.mbus.network;

import java.util.LinkedList;


/**
 * PacketQueue models a packet queue that holds PacketPackets for handling by
 * another component, like a dispatcher.
 */
public class PacketQueue {

  /** Field list */
  LinkedList list = new LinkedList();




  /**
   * Constructor PacketQueue
   */
  public PacketQueue() {}




  /**
   * Add the packet to the end of the list.
   *
   * @param packet The packet to add.
   */
  public void add( final Packet packet ) {
    synchronized( list ) {
      list.add( packet );
      list.notify();
    }
  }




  /**
   * Add the packet to the beginning of the list.
   * 
   * @param packet The packet to add.
   */
  void addFirst( final Packet packet ) {
    synchronized( list ) {
      list.addFirst( packet );
      list.notify();
    }
  }




  /**
   * Goes through each of the nodes and removes the PacketPackets older than the
   * given time interval.
   * 
   * <p>The given interval is added to timestamp to each packet in the list and 
   * if the result is less than or equal to the current time, it is removed 
   * from the list.</p>
   *
   * @param millis The age of packets to remove from the list in number of
   *          milliseconds
   *          
   * @return the last Packet sequence in the list, -1 if the list is empty.
   */
  long expire( final long millis ) {
    final long limit = System.currentTimeMillis() - millis;

    synchronized( list ) {
      for ( int i = 0; i < list.size(); i++ ) {
        final Packet packet = (Packet)list.get( i );

        if ( packet.timestamp < limit ) {
          list.remove( i );
          i--;
        }
      }
    }

    if ( list.size() > 0 ) {
      return ( (Packet)( list.getLast() ) ).sequence;
    }

    return -1;
  }




  /**
   * Method size
   *
   * @return TODO Complete Documentation
   */
  public int size() {
    return list.size();
  }




  /**
   * Performs a blocking retrieval operation on the queue.
   * 
   * <p>Block indefinitely if there are no packets to get.</p>
   *
   * @return The next packet in the queue.
   *
   * @throws InterruptedException
   */
  public Packet get() throws InterruptedException {
    synchronized( list ) {
      while ( list.size() == 0 ) {
        list.wait();
      }

      return (Packet)list.removeFirst();
    }
  }




  /**
   * Performs a blocking retrieval operation on the queue.
   * 
   * <p>Block for time-out if there are no packets to get.</p>
   *
   * @param millis the time to wait for a packet packet
   *
   * @return The next Packet in the queue, or null if timedout.
   *
   * @throws InterruptedException
   */
  public Packet get( final long millis ) throws InterruptedException {
    synchronized( list ) {
      if ( list.size() == 0 ) {
        list.wait( millis );
      }

      if ( list.size() == 0 ) {
        return null;
      }

      return (Packet)list.removeFirst();
    }
  }




  /**
   * Perform a non-blocking get on the next (first) packet in the queue.
   *
   * @return The next packet in the queue, null of there are no packets queued.
   */
  public Packet next() {
    synchronized( list ) {
      if ( list.size() == 0 ) {
        return null;
      }

      return (Packet)list.removeFirst();
    }
  }




  /**
   * Get the packet with the given sequence identifier.
   * 
   * <p>Performs a linear search from the beginning (oldest) for the packet with the
   * given sequence identifier.</p>
   *
   * @param sequence The sequence identifier of the packet to retrieve.
   * 
   * @return The packet in the queue with the given sequence identifier or null
   *         if the identifier is not found
   */
  Packet getPacket( final long sequence ) {
    synchronized( list ) {
      for ( int i = 0; i < list.size(); i++ ) {
        final Packet packet = (Packet)list.get( i );

        if ( packet.sequence == sequence ) {
          return packet;
        }
      }
    }

    return null;
  }




  /**
   * @return The sequence identifier of the last packet in the list or -1 if the 
   *         list is empty.
   */
  long getLastSequence() {
    final Object retval = list.getFirst();
    if ( retval != null ) {
      return ( (Packet)retval ).sequence;
    }
    return -1;
  }

}
