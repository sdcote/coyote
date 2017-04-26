package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;


/**
 * A PUBLISH message is sent by a client to a server for distribution to 
 * interested subscribers. 
 * 
 * <p>Each PUBLISH message is associated with a topic name (also known as the 
 * Subject or Channel). This is a hierarchical name space that defines a 
 * taxonomy of information sources for which subscribers can register an 
 * interest. A message that is published to a specific topic name is delivered 
 * to connected subscribers for that topic.</p>
 * 
 * <p>If a client subscribes to one or more topics, any message published to 
 * those topics are sent by the server to the client as a PUBLISH message.</p>
 */
public class PublishMessage extends CachableMessage {

  protected static byte[] encodePayload( final MqttMessage message ) {
    return message.getPayload();
  }

  private final MqttMessage message;

  private final String topicName;

  private byte[] encodedPayload = null;




  /**
   * Constructs a new PublishMessage object.
   * @param info the message info byte
   * @param data the variable header and payload bytes
   */
  public PublishMessage( final byte info, final byte[] data ) throws MqttException, IOException {
    super( AbstractMessage.PUBLISH );
    message = new ReceivedMessage();
    message.setQos( ( info >> 1 ) & 0x03 );
    if ( ( info & 0x01 ) == 0x01 ) {
      message.setRetained( true );
    }
    if ( ( info & 0x08 ) == 0x08 ) {
      ( (ReceivedMessage)message ).setDuplicate( true );
    }

    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final CountingInputStream counter = new CountingInputStream( bais );
    final DataInputStream dis = new DataInputStream( counter );
    topicName = decodeUTF8( dis );
    if ( message.getQos() > 0 ) {
      msgId = dis.readUnsignedShort();
    }
    final byte[] payload = new byte[data.length - counter.getCounter()];
    dis.readFully( payload );
    dis.close();
    message.setPayload( payload );
  }




  public PublishMessage( final String name, final MqttMessage message ) {
    super( AbstractMessage.PUBLISH );
    topicName = name;
    this.message = message;
  }




  public MqttMessage getMessage() {
    return message;
  }




  @Override
  protected byte getMessageInfo() {
    byte info = (byte)( message.getQos() << 1 );
    if ( message.isRetained() ) {
      info |= 0x01;
    }
    if ( message.isDuplicate() || duplicate ) {
      info |= 0x08;
    }

    return info;
  }




  @Override
  public byte[] getPayload() throws MqttException {
    if ( encodedPayload == null ) {
      encodedPayload = encodePayload( message );
    }
    return encodedPayload;
  }




  @Override
  public int getPayloadLength() {
    int length = 0;
    try {
      length = getPayload().length;
    } catch ( final MqttException me ) {}
    return length;
  }




  public String getTopicName() {
    return topicName;
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );
      encodeUTF8( dos, topicName );
      if ( message.getQos() > 0 ) {
        dos.writeShort( msgId );
      }
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  @Override
  public boolean isMessageIdRequired() {
    // all publishes require a message ID as it's used as the key to the token store
    return true;
  }




  @Override
  public void setMessageId( final int msgId ) {
    super.setMessageId( msgId );
    if ( message instanceof ReceivedMessage ) {
      ( (ReceivedMessage)message ).setMessageId( msgId );
    }
  }




  @Override
  public String toString() {

    // Convert the first few bytes of the payload into a hex string
    final StringBuffer hex = new StringBuffer();
    final byte[] payload = message.getPayload();
    final int limit = Math.min( payload.length, 20 );
    for ( int i = 0; i < limit; i++ ) {
      final byte b = payload[i];
      String ch = Integer.toHexString( b );
      if ( ch.length() == 1 ) {
        ch = "0" + ch;
      }
      hex.append( ch );
    }

    // It will not always be possible to convert the binary payload into
    // characters, but never-the-less we attempt to do this as it is often
    // useful
    String string = null;
    try {
      string = new String( payload, 0, limit, "UTF-8" );
    } catch ( final Exception e ) {
      string = "?";
    }

    final StringBuffer sb = new StringBuffer();
    sb.append( super.toString() );
    sb.append( " qos:" ).append( message.getQos() );
    if ( message.getQos() > 0 ) {
      sb.append( " msgId:" ).append( msgId );
    }
    sb.append( " retained:" ).append( message.isRetained() );
    sb.append( " dup:" ).append( duplicate );
    sb.append( " topic:\"" ).append( topicName ).append( "\"" );
    sb.append( " payload:[hex:" ).append( hex );
    sb.append( " utf8:\"" ).append( string ).append( "\"" );
    sb.append( " length:" ).append( payload.length ).append( "]" );

    return sb.toString();
  }
}