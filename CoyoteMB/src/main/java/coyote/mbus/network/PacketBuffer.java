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

import coyote.mbus.message.Message;


/**
 * The PacketBuffer class models a PacketQueue where packets are stored in 
 * their correct order for later retrieval.
 */

public class PacketBuffer extends PacketQueue {

  /**
   * Place the given packet in the buffer in the correct sequence.
   * 
   * <p>If there are packets missing between this packet and the last packet in 
   * the buffer, then the buffer will fill the missing slots in the buffer with 
   * empty packets in their proper order.</p>
   * 
   * @param packet The packet to place in the buffer.
   */
  public void buffer( final Packet packet ) {
    if ( size() == 0 ) {
      super.add( packet );
    } else {
      if ( packet.sequence < getFirstPacket().sequence ) {
        final long first = getFirstPacket().sequence;

        // add some placeholders to the beginning
        for ( long x = first - 1; x > packet.sequence; x-- ) {
          final Packet tmp = new Packet( x );
          tmp.timestamp = packet.timestamp;
          addFirst( tmp );
        }

        addFirst( packet );
      } else if ( packet.sequence > getLastPacket().sequence ) {
        for ( long x = getLastPacket().sequence + 1; x < packet.sequence; x++ ) {
          final Packet tmp = new Packet( x );
          tmp.timestamp = packet.timestamp;
          add( tmp );
        }

        add( packet );
      } else {
        // place the packet inside the list
        for ( int i = 0; i < list.size(); i++ ) {
          final Packet tmp = (Packet)list.get( i );

          if ( packet.sequence == tmp.sequence ) {
            synchronized( list ) {
              list.set( i, packet );
              list.notify();
            }
            break;
          }
        }
      }
    }
  }




  Packet getFirstPacket() {
    if ( list.size() > 0 ) {
      return (Packet)list.getFirst();
    } else {
      return null;
    }
  }




  Packet getLastPacket() {
    if ( list.size() > 0 ) {
      return (Packet)list.getLast();
    } else {
      return null;
    }
  }




  /**
   * Indicates the buffer has a complete set of packets in proper sequence.
   * 
   * @return true if the buffer set is complete
   */
  public boolean isComplete() {
    for ( int i = 0; i < list.size(); i++ ) {
      if ( ( ( (Packet)list.get( i ) ) ).message == null ) {
        return false;
      }
    }
    return true;
  }




  /**
   * Indicates the first packet in the buffer has a packet ready for processing.
   * 
   * <p>Normally used to see if there is at least one packet ready to be sent 
   * to the local packetwork by checking the first packet in the buffer. If it 
   * contains a reference to a packet, then at least one packet is ready for 
   * processing in its proper order.</p>
   *  
   * @return True if the first packet in the buffer has a packet, false if the 
   *         first packet in the buffer is a placeholder for a packet that has 
   *         yet to arrive.
   *
   * @see #getNextPacket()
   */
  public boolean isReady() {
    final Packet first = getFirstPacket();
    return ( ( first != null ) && ( first.message != null ) );
  }




  /**
   * Remove all the empty packets from the front of the list.
   * 
   * @return the number of empty packets removed
   */
  public int makeReady() {
    int count = 0;
    synchronized( list ) {
      while ( ( list.size() > 0 ) && !isReady() ) {
        list.removeFirst();
        count++;
      }

      return count;
    }

  }




  /**
   * Remove packets from the buffer in their present order stopping only when a
   * reference to a packet is found.
   * 
   * <p>This perfroms a destructive retrieval operation in that the first
   * packet is removed from the buffer and its packet is returned. If the packet
   * reference is null, then this method will continue to remove packets until a 
   * packet is found or the list is empty.</p>
   * 
   * <p>It is usually a good idea to call <code>isReady()</code> prior to 
   * calling this method to make sure packets are not removed before they are 
   * ready.</p>
   * 
   * @return The first packet in the packet buffer or null if there are no 
   *         packets in the buffer or no buffered packets contained a packet.
   */
  public Message getNextPacket() {
    Message retval = null;

    synchronized( list ) {
      while ( retval == null ) {
        if ( list.size() > 0 ) {
          retval = ( ( (Packet)list.removeFirst() ) ).message;
          list.notify();
        }
      }

      return retval;
    }
  }




  /**
   * Remove all packets up to and including the specified packet unless that 
   * packet contains a packet.
   * 
   * <p>This method starts from the beginning of the buffer and removes all 
   * packets with a sequence identifier less than or equal to (<code>&lt;=</code>) 
   * the passed parameter. The only exception to this rule is if there is 
   * already a packet to one of the "expired" packets. If there is a packet 
   * associated with an expired packet then it will be left in the buffer so it 
   * can be passed to the the rest of the packetwork.</p>
   * 
   * <p>Used by the EXPIRE packet processing to remove any expectations for 
   * packets that have been expired from the senders cache.</p>
   * 
   * @param seq The packet sequirence 
   */
  public void expireToPacket( final long seq ) {
    synchronized( list ) {
      for ( int i = 0; i < list.size(); i++ ) {
        final Packet tmp = (Packet)list.get( i );

        if ( ( tmp.sequence <= seq ) && ( tmp.message == null ) ) {
          list.remove( i );
          i--;
        } else {
          break;
        }
      } // for

      list.notify();
    } // sync
  }




  public String dump() {
    final StringBuffer buffer = new StringBuffer();
    for ( int i = 0; i < list.size(); i++ ) {
      final Packet packet = (Packet)list.get( i );

      buffer.append( packet.sequence );
      buffer.append( ": " );
      buffer.append( packet.timestamp );

      if ( packet.message == null ) {
        buffer.append( " empty" );
      } else {
        buffer.append( " packet:" );
        buffer.append( packet.message.getTimestamp() );
      }
      if ( i + 1 < list.size() ) {
        buffer.append( "\n" );
      }
    }
    return buffer.toString();
  }

}
