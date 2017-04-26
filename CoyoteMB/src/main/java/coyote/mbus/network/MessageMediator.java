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
 * Message Mediators send and route packets over some transport medium.
 * 
 * <p>Object of this class handle incoming packets received from transport media 
 * and send packet frame over said same. This provides a single interface that
 * represents a transport medium like TCP/UDP, file systems, databases, third
 * party messaging systems like JMS and vendor-specific mediators.</p>
 * 
 * <p>The sequence of packets is some MessageMediator receives raw data from it's
 * medium, as in the network. This raw data is converted into Packets and
 * often passed to an Object that handles the exchange of Packets with other
 * entities in the medium. This is handled most often by objects of the
 * RemoteNode class. These protocol entities make the decision to either
 * generate Packets that are passed back to the MessageMediator for routing to
 * channels it manages, or to generate Packets that get sent back over the
 * medium to affect protocol dialog. In short, this interface is used by the
 * Packet protocol handlers to talk to other Packet protocol handlers or
 * to allow the transport to route Packets to its clients.</p>
 */
interface MessageMediator {

  /**
   * Send the packet out to the network.
   * 
   * <p>Used to send information over the transport medium to affect a
   * communications protocol via PacketPackets.</p>
   *
   * @param packet The packet that is to be sent to the transport medium to affect
   *          the communications protocol implemented by the caller.
   */
  void send( Packet packet );




  /**
   * Process the message received from the network.
   * 
   * <p>Used to send processes a message received in a packet.</p>
   *
   * @param msg The message that is to be processed internally.
   */
  void process( Message msg );

}
