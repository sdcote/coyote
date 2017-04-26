package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttMessage;


/**
 * When a TCP/IP socket connection is established from a client to a server, a 
 * protocol level session must be created using a CONNECT flow.
 */
public class ConnectMessage extends AbstractMessage {

  public static final String KEY = "Con";

  private final String clientId;
  private boolean cleanSession;
  private MqttMessage willMessage;
  private String username;
  private char[] password;
  private final int keepAliveInterval;
  private String willDestination;
  private int MqttVersion;




  /**
   * Constructor for an on the wire MQTT connect message
   * 
   * @param info
   * @param data
   * @throws IOException
   * @throws MqttException
   */
  public ConnectMessage( final byte info, final byte[] data ) throws IOException, MqttException {
    super( AbstractMessage.CONNECT );
    final ByteArrayInputStream bais = new ByteArrayInputStream( data );
    final DataInputStream dis = new DataInputStream( bais );

    decodeUTF8( dis );
    dis.readByte();
    dis.readByte();
    keepAliveInterval = dis.readUnsignedShort();
    clientId = decodeUTF8( dis );
    dis.close();
  }




  public ConnectMessage( final String clientId, final int MqttVersion, final boolean cleanSession, final int keepAliveInterval, final String userName, final char[] password, final MqttMessage willMessage, final String willDestination ) {
    super( AbstractMessage.CONNECT );
    this.clientId = clientId;
    this.cleanSession = cleanSession;
    this.keepAliveInterval = keepAliveInterval;
    this.username = userName;
    this.password = password;
    this.willMessage = willMessage;
    this.willDestination = willDestination;
    this.MqttVersion = MqttVersion;
  }




  @Override
  public String getKey() {
    return KEY;
  }




  @Override
  protected byte getMessageInfo() {
    return (byte)0;
  }




  @Override
  public byte[] getPayload() throws MqttException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );
      encodeUTF8( dos, clientId );

      if ( willMessage != null ) {
        encodeUTF8( dos, willDestination );
        dos.writeShort( willMessage.getPayload().length );
        dos.write( willMessage.getPayload() );
      }

      if ( username != null ) {
        encodeUTF8( dos, username );
        if ( password != null ) {
          encodeUTF8( dos, new String( password ) );
        }
      }
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  @Override
  protected byte[] getVariableHeader() throws MqttException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );

      if ( MqttVersion == 3 ) {
        encodeUTF8( dos, "MQIsdp" );
      } else if ( MqttVersion == 4 ) {
        encodeUTF8( dos, "MQTT" );
      }
      dos.write( MqttVersion );

      byte connectFlags = 0;

      if ( cleanSession ) {
        connectFlags |= 0x02;
      }

      if ( willMessage != null ) {
        connectFlags |= 0x04;
        connectFlags |= ( willMessage.getQos() << 3 );
        if ( willMessage.isRetained() ) {
          connectFlags |= 0x20;
        }
      }

      if ( username != null ) {
        connectFlags |= 0x80;
        if ( password != null ) {
          connectFlags |= 0x40;
        }
      }
      dos.write( connectFlags );
      dos.writeShort( keepAliveInterval );
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ioe ) {
      throw new MqttException( ioe );
    }
  }




  public boolean isCleanSession() {
    return cleanSession;
  }




  /**
   * Returns whether or not this message needs to include a message ID.
   */
  @Override
  public boolean isMessageIdRequired() {
    return false;
  }




  @Override
  public String toString() {
    StringBuffer b = new StringBuffer( super.toString() );
    b.append( " clientId:" );
    b.append( clientId );
    b.append( " keepAliveInterval:" );
    b.append( keepAliveInterval );
    if ( username != null ) {
      b.append( " user:" );
      b.append( username );
      if ( password != null ) {
        b.append( " pass:" );
        b.append( password.length );
        b.append( "chars" );
      }
    }

    return b.toString();
  }
}
