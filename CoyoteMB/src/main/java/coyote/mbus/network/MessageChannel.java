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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import coyote.commons.SegmentFilter;
import coyote.mbus.MessageChannelListener;
import coyote.mbus.MessageQueue;
import coyote.mbus.MessageSink;
import coyote.mbus.message.ClosureMessage;
import coyote.mbus.message.Message;
import coyote.mbus.message.MessageAddress;


/**
* MessageChannel provides a decoupled inbound and outbound queue for 
* sending and receiving Messages.
* 
* <p>The concept of message addresses to support messaging technologies which
* support point-to-point messages through addressing.</p>
*/
public class MessageChannel {
  public static final String PRIVATE_PREFIX = "PRIVATE.";

  /** Our address in the Message network */
  public MessageAddress address = new MessageAddress( -1, -1 );

  /** The queue that holds our inbound messages */
  private final MessageQueue inbound = new MessageQueue();

  /**
   * The default inbound sink for this channel. All sinks have an onMessage 
   * method to consume packets. 
   */
  public MessageSink inSink = null;

  /** The default outbound sink for this channel. */
  public MessageSink outSink = null;

  /** The queue that holds our outbound messages */
  private final MessageQueue outbound = new MessageQueue();

  /** List of group names (SegmentFilter) which this channel has joined */
  final ArrayList<SegmentFilter> groupNames = new ArrayList<SegmentFilter>();

  /** The logical client identifier owning this channel. May be null */
  String clientId = null;

  /** A group of Channel listeners to be notified when this channel changes */
  private Vector<MessageChannelListener> channelListeners = null;

  private final HashSet<String> privateGroupMap = new HashSet<String>();

  boolean closed = false;




  /**
   * This constructor should be used by the Message system only.
   */
  public MessageChannel() {}




  /**
   * Constructor for creation only by the Message system allowing for the
   * registration of the packet inSink.
   *
   * @param sink The inbound sink this channel is to use as a call-back.
   */
  public MessageChannel( final MessageSink sink ) {
    this.inSink = sink;
  }




  /**
   * Add a MessageChannelListener to the listener list.
   *
   * @param listener The MessageChannelListener to be added
   */
  public synchronized void addListener( final MessageChannelListener listener ) {
    if ( channelListeners == null ) {
      channelListeners = new Vector<MessageChannelListener>( 10 );
    }

    if ( channelListeners.contains( listener ) ) {
      return;
    }

    channelListeners.addElement( listener );
  }




  /**
   * Marks the channel as closed and prevents any new packets from being added 
   * to the inbound or outbound queues.
   * 
   * <p>The bus will continue to service this channel for only as long as it 
   * takes to send all the messages in its outbound queue and process the 
   * messages in its inbound queue. This means even though the channel is 
   * closed, the messages already in the channel will still be processed.</p>
   */
  public void close() {
    closed = true;

    // add a message to the inbound queue indicating the end of the data
    inbound.add( new ClosureMessage() );

    // add a message to the outbound queue indicating the end of the data
    outbound.add( new ClosureMessage() );
  }




  /**
   * Create a group that only this channel knows about and can join.
   * 
   * <p>Other channels may send messages to this group, but only this channel
   * can join it resulting in a multi-point to single-point transport scheme.
   * This is also know as an inbox pattern.</p> 
   *  
   * @return a group name only this channel may join but to which any other may 
   *         send.
   */
  public String createPrivateGroup() {
    final String retval = MessageChannel.PRIVATE_PREFIX + UUID.randomUUID().toString();
    synchronized( privateGroupMap ) {
      privateGroupMap.add( retval );
    }
    return retval;
  }




  /**
   * Null out the sinks, clear the queues and remove the listeners so resources 
   * can be reclaimed.
   */
  public void destroy() {
    closed = true;
    inSink = null;
    outSink = null;
    inbound.clear();
    outbound.clear();
    if ( channelListeners != null ) {
      channelListeners.clear();
    }
  }




  /**
   * Dispatch messages received to the handler
   * 
   * TODO pass inbound and outbound packets to the appropriate components
   */
  void doWork() {
    //while( !shutdown )
    {
      if ( inSink != null ) {
        // perform a blocking get on the inbound queue
        final Message packet = getNextInbound();

        if ( packet != null ) {
          if ( packet instanceof ClosureMessage ) {
            //restart = false;
            //shutdown = true;
          }

          try {
            inSink.onMessage( packet );
          } catch ( final Throwable t ) {
            t.printStackTrace();
          }
        }
      }
    }
  }




  /**
   * Let the listeners know that a connection has been made to another channel.
   */
  void fireConnect() {
    if ( channelListeners != null ) {
      final int size = channelListeners.size();

      for ( int i = 0; i < size; i++ ) {
        final MessageChannelListener target = (MessageChannelListener)channelListeners.elementAt( i );
        if ( target != null ) {
          target.channelConnect( this );
        }
      }
    }
  }




  /**
   * Let the listeners know that a connection has been lost to another channel.
   */
  void fireDisconnect() {
    if ( channelListeners != null ) {
      final int size = channelListeners.size();

      for ( int i = 0; i < size; i++ ) {
        final MessageChannelListener target = (MessageChannelListener)channelListeners.elementAt( i );
        if ( target != null ) {
          target.channelDisconnect( this );
        }
      }
    }
  }




  /**
   * Inform all the listeners that this channel is joining a particular group.
   *
   * @param groupName
   */
  void fireJoin( final String groupName ) {
    if ( channelListeners != null ) {
      final int size = channelListeners.size();

      for ( int i = 0; i < size; i++ ) {
        final MessageChannelListener target = (MessageChannelListener)channelListeners.elementAt( i );
        if ( target != null ) {
          target.channelJoined( groupName, this );
        }
      }
    }
  }




  /**
   * Inform all the listeners that this channel is leaving a particular group.
   *
   * @param groupName
   */
  void fireLeave( final String groupName ) {
    try {
      if ( channelListeners != null ) {
        final int size = channelListeners.size();

        for ( int i = 0; i < size; i++ ) {
          final MessageChannelListener target = (MessageChannelListener)channelListeners.elementAt( i );
          if ( target != null ) {
            target.channelLeft( groupName, this );
          }
        }
      }
    } catch ( final Exception e ) {
      e.printStackTrace();
    }
  }




  /**
   * Inform all channel listeners that a packet has been placed in the inbound
   * packet queue for receiving.
   * 
   * <p>This means that an Message is waiting for retrieval with a call to the
   * MessageChannel.get() method.</p>
   */
  void fireReceive() {
    if ( channelListeners != null ) {
      final int size = channelListeners.size();

      for ( int i = 0; i < size; i++ ) {
        final MessageChannelListener target = (MessageChannelListener)channelListeners.elementAt( i );
        if ( target != null ) {
          target.channelReceive( this );
        }
      }
    }
  }




  /**
   * Inform all channel listeners that a message has been placed in the 
   * outbound message queue for sending.
   */
  void fireSend() {
    if ( channelListeners != null ) {
      final int size = channelListeners.size();

      for ( int i = 0; i < size; i++ ) {
        final MessageChannelListener target = (MessageChannelListener)channelListeners.elementAt( i );
        if ( target != null ) {
          target.channelSend( this );
        }
      }
    }
  }




  /**
   * @return  The currently set MessageAddress, or null if no address is set.
   */
  public MessageAddress getAddress() {
    return address;
  }




  /**
   * @return The channel identifier assigned to this channel by the channel
   *         manager.
   */
  public int getChannelId() {
    return address.getChannelId();
  }




  /**
   * @return  The currently set channel identifier
   */
  public String getClientId() {
    return clientId;
  }




  /**
   * @return a count of the current group names to which this channel has joined.
   */
  public int getGroupCount() {
    return groupNames.size();
  }




  /**
   * @return a list of group names to which this channel has joined.
   */
  public List getGroups() {
    return groupNames;
  }




  /**
   * @return  a reference to our inbound packet queue.
   */
  public MessageQueue getInbound() {
    return inbound;
  }




  /**
   * @return The currently set MessageSink or null if the inSink is not set
   */
  public MessageSink getInboundSink() {
    return inSink;
  }




  /**
   * Performs a blocking retrieval operation on the inbound queue.
   * 
   * <p>Block indefinitely if there are no Packets to get.</p>
   *
   * @return The next Message in the queue or null if the operation was
   *         interrupted.
   */
  public Message getNextInbound() {
    try {
      return inbound.get();
    } catch ( final InterruptedException e ) {}

    return null;
  }




  /**
   * Performs a blocking retrieval operation on the inbound queue.
   * 
   * <p>Block for time-out if there are no Packets to get.</p>
   *
   * @param millis the time to wait for a Message.
   * 
   * @return The next Message in the queue, or null if the operation was
   *         interrupted or timed-out.
   */
  public Message getNextInbound( final long millis ) {
    try {
      return inbound.get( millis );
    } catch ( final InterruptedException e ) {}

    return null;
  }




  /**
   * Performs a blocking retrieval operation on the outbound queue.
   * 
   * <p>Block indefinitely if there are no Packets to get.</p>
   *
   * @return The next Message in the queue or null if the operation was
   *         interrupted.
   */
  public Message getNextOutbound() {
    try {
      return outbound.get();
    } catch ( final InterruptedException e ) {}

    return null;
  }




  /**
   * Performs a blocking retrieval operation on the outbound queue.
   * 
   * <p>Block for time-out if there are no Packets to get.</p>
   *
   * @param millis the time to wait for a Message
  
   * @return The next Message in the queue, or null if the operation was
   *         interrupted or timed-out.
   */
  public Message getNextOutbound( final long millis ) {
    try {
      return outbound.get( millis );
    } catch ( final InterruptedException e ) {}

    return null;
  }




  /**
   * @return  a reference to our outbound packet queue.
   */
  public MessageQueue getOutbound() {
    return outbound;
  }




  /**
   * Method hasInboundPackets
   *
   * @return TODO Complete Documentation
   */
  public boolean hasInboundPackets() {
    return ( inbound.size() > 0 );
  }




  /**
   * Method hasOutboundPackets
   *
   * @return TODO Complete Documentation
   */
  public boolean hasOutboundPackets() {
    return ( outbound.size() > 0 );
  }




  /**
   * Method inboundDepth
   *
   * @return TODO Complete Documentation
   */
  public int inboundDepth() {
    return inbound.size();
  }




  /**
   * @return  true if the channel will not send / pass packets.
   */
  public boolean isClosed() {
    return closed;
  }




  /**
   * Registers the name of a group name to which this channel is to join.
   *
   * @param group The name of the group to which this channel is to join.
   */
  public void join( final String group ) {
    if ( ( group != null ) && ( group.trim().length() > 0 ) ) {
      // Check for private groups
      if ( group.startsWith( MessageChannel.PRIVATE_PREFIX ) ) {
        synchronized( privateGroupMap ) {
          // If it is not in our list of private groups throw an exception
          if ( !privateGroupMap.contains( group ) ) {
            throw new IllegalArgumentException( "Can not subscribe to private group" );
          }
        }
      }

      // Create a segment filter for the group name so it will be easy to match
      // the group names of incoming messages to those groups we have joined. 
      final SegmentFilter filter = new SegmentFilter( group );

      // add the group name (in the form of a segment filter) to the list of 
      // groups in which this channel is interested.
      synchronized( groupNames ) {
        groupNames.add( filter );
        fireJoin( group );
      }
    }
  }




  /**
   * Leaves the group and stops receiving messages from that group.
   * 
   * <p>Removes a group from the list of memberships.</p>
   *
   * @param group The name of the channelName from which this channel is to
   *          unsubscribe.
   */
  public void leave( final String group ) {
    try {
      if ( ( group != null ) && ( group.trim().length() > 0 ) ) {
        synchronized( groupNames ) {
          for ( int i = 0; i < group.length(); i++ ) {
            final SegmentFilter filter = (SegmentFilter)groupNames.get( i );

            if ( filter.toString().equals( group ) ) {
              groupNames.remove( i );
              fireLeave( group );
              return;
            }
          }
        }
      }
    } catch ( final Exception e ) {}
  }




  /**
   * Called by packet routers to see if this channel is interested in a message
   * they have on the merit of the packets group name.
   *
   * @param group The name of the group against which to check.
  
   * @return True if this channel is a member of the given group name, false
   *         otherwise.
   */
  public boolean matchGroup( final String group ) {
    synchronized( groupNames ) {
      for ( int i = 0; i < groupNames.size(); i++ ) {
        if ( groupNames.get( i ).matches( group ) ) {
          return true;
        }
      }
    }
    return false;
  }




  /**
   * @return The number of packets waiting in our outbound packet queue.
   */
  public int outboundDepth() {
    return outbound.size();
  }




  /**
   * Method put
   *
   * @param packet
   */
  public void put( final Message packet ) {
    outbound.add( packet );
  }




  /**
   * Place the packet in the inbound queue for later retrieval by the get()
   * method call.
   * 
   * <p><strong>NOTE:</strong> If there is an inbound MessageSink Listener 
   * registered with this channel, the packet will be passed to the sink and 
   * NOT placed in the queue and the firing of the channelReceive method on the 
   * registered Channel Listeners will NOT occur.</p>
   *
   * @param packet The packet that is to be received by the channel.
   */
  public void receive( final Message packet ) {
    if ( packet != null ) {
      if ( inSink != null ) {
        // pass this directly to the processor
        inSink.onMessage( packet );
      } else {
        // notify listeners that a packet has been queued and needs attention
        inbound.add( packet );
      }

      // Notify the listeners that an inbound packet was received on this channel
      fireReceive();
    }
  }




  /**
   * Remove a MessageChannelListener from the listener list.
   *
   * @param listener The MessageChannelListener to be removed
   */
  public synchronized void removeListener( final MessageChannelListener listener ) {
    if ( channelListeners == null ) {
      return;
    }

    channelListeners.removeElement( listener );
  }




  /**
   * Place the message in the outbound queue.
   *
   * @param msg The message to send.
   */
  public void send( final Message msg ) {
    if ( !closed ) {
      if ( msg != null ) {
        // make sure this packet does not get routed back to this channel
        if ( msg.sourceChannel == null ) {
          msg.sourceChannel = this;
        }

        // make sure all packets have a source address
        if ( msg.getSource() == null ) {
          msg.setSource( address );
        }

        // If there is an outbound sink, pass the message to it
        if ( outSink != null ) {
          // pass this directly to the processor
          outSink.onMessage( msg );
        } else {
          // add the message to out outbound queue
          outbound.add( msg );
        }

        // Notify the listeners that an outbound packet was sent over this channel
        fireSend();

      } // message !null

    } // !closed
  }




  /**
   * @param address  The reference to the MessageAddress to set.
   */
  public void setAddress( final MessageAddress address ) {
    this.address = address;
  }




  /**
   * Set the channel identifier.
   * 
   * <p>If there is an existing address, the address will be re-created with a 
   * new channel identifier and the remaining values as they exist at the 
   * time.</p>
   *
   * @param i the channel identifier to set in the channel.
   */
  public void setChannelId( final int i ) {
    if ( address != null ) {
      address = new MessageAddress( address.getAddress(), address.getPort(), address.getEndPoint(), i );
    } else {
      address = new MessageAddress( -1, i );
    }
  }




  /**
   * @param string  The identifier to set in this packet channel.
   */
  public void setClientId( final String string ) {
    clientId = string;
  }




  /**
   * @param sink The reference to the MessageSink to set.
   */
  public void setInboundSink( final MessageSink sink ) {
    this.inSink = sink;
  }




  /**
   * @return Nice, informative string representation of the channel.
   */
  public String toString() {
    final StringBuffer buf = new StringBuffer();
    buf.append( "Channel:" );
    buf.append( address.getChannelId() );
    buf.append( " subs[" );
    buf.append( groupNames.size() );
    buf.append( "]:(" );
    for ( int i = 0; i < groupNames.size(); i++ ) {
      buf.append( ( groupNames.get( i ) ).toString() );
      if ( i + 1 < groupNames.size() ) {
        buf.append( ',' );
      }
    }

    buf.append( ") src:" );
    buf.append( address );

    buf.append( " depth:[in=" );
    buf.append( inbound.size() );
    buf.append( "][out=" );
    buf.append( outbound.size() );
    buf.append( "]" );

    // Check to see if there are any channel listeners
    if ( channelListeners != null ) {
      buf.append( " listeners=" );
      buf.append( channelListeners.size() );
    }

    buf.append( " inSink:" );
    if ( inSink != null ) {
      buf.append( inSink );
    } else {
      buf.append( "null" );
    }

    buf.append( " outSink:" );
    if ( outSink != null ) {
      buf.append( outSink );
    } else {
      buf.append( "null" );
    }

    return buf.toString();
  }

}
