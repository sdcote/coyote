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
 * This class models a message that contains data related to Operations, 
 * Administration or Monitoring (OAM) in ADMIN messages of the MicroBus. 
 * 
 * <p>All OAM messages have an action parameter. This parameter essentially 
 * defines the type of OAM message it is.</p>
 */
public class OamMessage {
  /** Tag used in various class identifying locations */
  static final String CLASS_TAG = "OAM";

  /** the next OAM message identifier from this JVM */
  private static long nextId = 0;

  /**
   * All OAM Packets have an action describing its purpose. This is the name of
   * that field.
   */
  static final String ACTION = "ACTION";

  /**
   * Some OAMPackets can have a correlation identifier so OAM entities can match
   * requests to responses.
   */
  static final String OAMID = "OAMID";

  /** The name of the field that holds a message string. */
  static final String MESSAGE = "MSG";

  /** The name of the field that holds a generic token string. */
  static final String TOKEN = "TOKEN";

  /** The name of the field that holds a description string. */
  static final String DESCRIPTION = "DESC";

  /** The name of the field that holds a TCP Address string. */
  static final String TCPADR = "TCPADR";

  /** The name of the field that holds a TCP Port string. */
  static final String TCPPRT = "TCPPRT";

  /** The name of the field that holds an endpoint identifier. */
  static final String ENDPOINT = "ENDPT";

  /** The standard name for an identifier field */
  public static final String IDENTIFIER = "ID";
  public static final String NODE_ADDRESS = "NADR";
  public static final String NODE_PORT = "NPRT";
  public static final String SERVICE_PORT = "SPRT";

  static final String ARP = "ARP";
  static final String SOURCE_ADDRESS = "SIP";
  static final String SOURCE_ENDPOINT = "SEP";
  static final String DEST_ADDRESS = "DIP";
  static final String DEST_ENDPOINT = "DEP";

  static final String INSERT = "INSERT";
  static final String WITHDRAW = "WITHDRAW";
  static final String HEARTBEAT = "HEARTBEAT";
  static final String JOIN = "JOIN";
  static final String LEAVE = "LEAV";
  static final String GROUP = "GRP";

  /** The reference to the originating BusMessage */
  private Message _msg = new Message();




  /**
   * Create an object instance from the data contained in the given Message.
   *
   * @param message The Message to use as the source of our data.
   */
  OamMessage( final Message message ) {
    super();

    _msg = (Message)message.clone();
  }




  /**
   * Return the OamMessage as XML suitable for transmission over some medium or
   * persistance in some data store.
   *
   * @return TODO Complete Documentation
   */
  String toXML() {
    return _msg.toXml();
  }




  /**
   * Method setAction
   *
   * @param action
   */
  void setAction( final String action ) {}




  /**
   * @return the action of this OAM.
   */
  String getAction() {
    return _msg.getAsString( OamMessage.ACTION );
  }




  /**
   * Return the correlation identifier set in this message.
   * 
   * <p>Identifiers are sequential for the static instance of the OamMessage in 
   * the JVM. These identifiers are NOT unique across virtual machines clients 
   * or daemons.</p>
   *
   * @param message
   *
   * @return the identifier if it is set, null if it has not been set.
   */
  static String getCorrelationId( final Message message ) {
    return message.getField( OamMessage.OAMID ).getObjectValue().toString();
  }




  /**
   * Method getCorrelationId
   *
   * @return TODO Complete Documentation
   */
  String getCorrelationId() {
    return _msg.getAsString( OamMessage.OAMID );
  }




  /**
   * Method getTcpAddress
   *
   * @return TODO Complete Documentation
   */
  String getTcpAddress() {
    return _msg.getAsString( OamMessage.TCPADR );
  }




  /**
   * Method getTcpPort
   *
   * @return TODO Complete Documentation
   */
  String getTcpPort() {
    return _msg.getAsString( OamMessage.TCPPRT );
  }




  /**
   * Method getEndPoint
   *
   * @return TODO Complete Documentation
   */
  String getEndPoint() {
    return _msg.getAsString( OamMessage.ENDPOINT );
  }




  /**
   * Method getToken
   *
   * @return TODO Complete Documentation
   */
  String getToken() {
    return _msg.getAsString( OamMessage.TOKEN );
  }




  /**
   * Return the String value of the named attribute.
   *
   * @param name The name of the OamMessage field to retrive.
   *
   * @return The value of the named field, or null if the field does not exist.
   */
  String get( final String name ) {
    return _msg.getAsString( name );
  }




  /**
   * Set a field with a given name and String value.
   *
   * @param name The name of the field to set.
   * @param value The value to set in that named field.
   */
  void set( final String name, final String value ) {
    if ( name != null ) {
      _msg.put( name, value );
    }
  }




  /**
   * @return TODO Complete Documentation
   */
  Message getOriginalPacket() {
    return _msg;
  }




  /**
   * Method getNextId
   * @return TODO Complete Documentation
   */
  private synchronized static long getNextId() {
    return OamMessage.nextId++;
  }




  /**
   * Method isHeartbeat
   *
   * @return TODO Complete Documentation
   */
  boolean isHeartbeat() {
    return OamMessage.HEARTBEAT.equals( _msg.getAsString( OamMessage.ACTION ) );
  }




  /**
   * Method createHeartbeatPacket
   *
   * @param addr
   * @param port
   *
   * @return TODO Complete Documentation
   */
  static Message createHeartbeatMessage( final String addr, final int port ) {
    final Message message = new Message();
    message.add( OamMessage.ACTION, OamMessage.HEARTBEAT );
    message.add( OamMessage.TCPADR, addr );
    message.add( OamMessage.TCPPRT, port );

    return message;
  }




  /**
   * Return a message to indicate an insertion of a node into a bus.
   *
   * @param endpoint the intended endpoint identifier being used
   * @param token further specifies uniqueness of the insertion
   *
   * @return A message suitable for use as an insertion message.
   */
  static Message createInsertionMessage( final String endpoint, final String token ) {
    final Message message = new Message();
    message.add( OamMessage.ACTION, OamMessage.INSERT );
    message.add( OamMessage.ENDPOINT, endpoint );
    message.add( OamMessage.TOKEN, token );

    return message;
  }




  /**
   * Return a message to indicate a withdrawal of a node from the bus.
   *
   * @param endpoint - the endpoint identifier of the withdrawing message node
   *
   * @return A message suitable for use as a withdrawal message.
   */
  static Message createWithdrawalMessage( final String endpoint ) {
    final Message message = new Message();
    message.add( OamMessage.ACTION, OamMessage.WITHDRAW );
    message.add( OamMessage.ENDPOINT, endpoint );

    return message;
  }




  /**
   * Return a message to indicate an interest in packets of a given group.
   *
   * @param group - the group being joined.
   *
   * @return A message suitable for use as a join message.
   */
  static Message createJoinMessage( final String group ) {
    final Message message = new Message();
    message.add( OamMessage.ACTION, OamMessage.JOIN );
    message.add( OamMessage.GROUP, group );
    return message;
  }




  /**
   * Return a message to indicate the discontinued interest in packets of a 
   * given group.
   *
   * @param group - the group being left.
   *
   * @return A message suitable for use as a leave message.
   */
  static Message createLeaveMessage( final String group ) {
    final Message message = new Message();
    message.add( OamMessage.ACTION, OamMessage.LEAVE );
    message.add( OamMessage.GROUP, group );
    return message;
  }




  static Message createArpMessage( final String sip, final long sep, final long token, final String dip, final long dep ) {
    final Message message = new Message();
    message.add( OamMessage.ACTION, OamMessage.ARP );
    message.add( OamMessage.SOURCE_ADDRESS, sip );
    message.add( OamMessage.SOURCE_ENDPOINT, sip );
    message.add( OamMessage.TOKEN, token );
    message.add( OamMessage.DEST_ADDRESS, sip );
    message.add( OamMessage.DEST_ENDPOINT, sip );
    return message;
  }




  /**
   * Create a message that descrives the given node.
   * 
   * @param node The remote node object to represent.
   * 
   * @return The message representing the node.
   */
  public static Message createNodePacket( final RemoteNode node ) {
    final Message message = new Message();
    if ( node.udpAddress != null ) {
      message.add( OamMessage.NODE_ADDRESS, node.udpAddress.getHostAddress() );
    }
    message.add( OamMessage.NODE_PORT, node.tcpPort );
    message.add( OamMessage.SERVICE_PORT, node.udpPort );
    message.add( OamMessage.IDENTIFIER, node.remoteEndPoint );
    return message;
  }
}
