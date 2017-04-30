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
package coyote.mbus;

import coyote.mbus.network.MessageChannel;


/**
 * An interface used to inform interested entities when the state of a Message
 * Channel has changed.
 * 
 * <p>An observer can use this interface to be notified when messages arrive 
 * when a blocking wait for a message to arrive is not acceptable.</p> 
 */
public interface MessageChannelListener {

  /**
   * Called when the MessageChannel joins a group.
   *
   * @param group The name of the group that the MessageChannel is interested.
   * @param channel The packet channel that issued or is affected by the
   *          subscription.
   */
  public void channelJoined( String group, MessageChannel channel );




  /**
   * Called when the MessageChannel leaves a group.
   *
   * @param group The name of the group that the MessageChannel is no longer
   *          interested.
   * @param channel The packet channel that issued or is affected by the
   *          unsubscription.
   */
  public void channelLeft( String group, MessageChannel channel );




  /**
   * Called when the MessageChannel adds a Message to its outbound packet queue.
   * 
   * <p>This call is designed to let components know when there are packets to be
   * read from the outbound queue and sent or routed to other channels.</p>
   *
   * @param channel The packet channel that has added a packet to its outbound
   *          queue.
   */
  public void channelSend( MessageChannel channel );




  /**
   * Called when the MessageChannel adds an Message to its inbound packet queue.
   * 
   * <p>This call is designed to let components know when there are packets to 
   * be read from the inbound queue. These packets are intended to be read by 
   * the channel consumer.</p>
   *
   * @param channel The packet channel that has added a packet to its inbound
   *          queue.
   */
  public void channelReceive( MessageChannel channel );




  /**
   * Called when the MessageChannel connects to another.
   * 
   * <p>This call is designed to let components know when a (re)connection has 
   * been accomplished. The listener can then perform whatever (re)connection
   * processing is required. It is possible that a previous connection was lost
   * and a new connection has been established. In that case, this method will
   * be called when the connection as been re-established.</p>
   * 
   * <p>This method only really applies to MessageChannels that are used by
   * PacketSessions, as only sessions have the concept of connecting.</p>
   *
   * @param channel The packet channel that has (re)connected to another.
   */
  public void channelConnect( MessageChannel channel );




  /**
   * Called when the MessageChannel becomes disconnected.
   * 
   * <p>This call is designed to let components know when a disconnection has
   * occurred on a channel.</p>
   * 
   * <p>This method only really applies to MessageChannels that are used by 
   * PacketSessions, as only sessions have the concept of connecting.</p>
   *
   * @param channel The packet channel that has disconnected from another.
   */
  public void channelDisconnect( MessageChannel channel );

}
