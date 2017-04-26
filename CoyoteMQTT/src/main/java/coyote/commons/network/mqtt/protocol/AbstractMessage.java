package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.cache.Cacheable;


/**
 * An abstract representation of an MQTT message.
 */
public abstract class AbstractMessage {
  public static final byte CONNECT = 1;
  public static final byte CONNACK = 2;
  public static final byte PUBLISH = 3;
  public static final byte PUBACK = 4;
  public static final byte PUBREC = 5;
  public static final byte PUBREL = 6;
  public static final byte PUBCOMP = 7;
  public static final byte SUBSCRIBE = 8;
  public static final byte SUBACK = 9;
  public static final byte UNSUBSCRIBE = 10;
  public static final byte UNSUBACK = 11;
  public static final byte PINGREQ = 12;
  public static final byte PINGRESP = 13;
  public static final byte DISCONNECT = 14;

  protected static final String STRING_ENCODING = "UTF-8";

  private static final String PACKET_NAMES[] = {
      //
      "reserved",
      //
      "CONNECT",
      //
      "CONNACK",
      //
      "PUBLISH",
      //
      "PUBACK",
      //
      "PUBREC",
      //
      "PUBREL",
      //
      "PUBCOMP",
      //
      "SUBSCRIBE",
      //
      "SUBACK",
      //
      "UNSUBSCRIBE",
      //
      "UNSUBACK",
      //
      "PINGREQ",
      //
      "PINGRESP",
      //
      "DISCONNECT" };




  public static AbstractMessage createWireMessage( final byte[] bytes ) throws MqttException {
    final ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
    return createWireMessage( bais );
  }




  private static AbstractMessage createWireMessage( final InputStream inputStream ) throws MqttException {
    try {
      final CountingInputStream counter = new CountingInputStream( inputStream );
      final DataInputStream in = new DataInputStream( counter );
      int first = in.readUnsignedByte();
      final byte type = (byte)( first >> 4 );
      final byte info = (byte)( first &= 0x0f );
      final long remLen = readMBI( in ).getValue();
      final long totalToRead = counter.getCounter() + remLen;

      AbstractMessage result;
      final long remainder = totalToRead - counter.getCounter();
      byte[] data = new byte[0];
      // The remaining bytes must be the payload...
      if ( remainder > 0 ) {
        data = new byte[(int)remainder];
        in.readFully( data, 0, data.length );
      }

      if ( type == AbstractMessage.CONNECT ) {
        result = new ConnectMessage( info, data );
      } else if ( type == AbstractMessage.PUBLISH ) {
        result = new PublishMessage( info, data );
      } else if ( type == AbstractMessage.PUBACK ) {
        result = new PubAckMessage( info, data );
      } else if ( type == AbstractMessage.PUBCOMP ) {
        result = new PubCompMessage( info, data );
      } else if ( type == AbstractMessage.CONNACK ) {
        result = new ConnAckMessage( info, data );
      } else if ( type == AbstractMessage.PINGREQ ) {
        result = new PingReqMessage( info, data );
      } else if ( type == AbstractMessage.PINGRESP ) {
        result = new PingRespMessage( info, data );
      } else if ( type == AbstractMessage.SUBSCRIBE ) {
        result = new SubscribeMessage( info, data );
      } else if ( type == AbstractMessage.SUBACK ) {
        result = new SubackMessage( info, data );
      } else if ( type == AbstractMessage.UNSUBSCRIBE ) {
        result = new MqttUnsubscribe( info, data );
      } else if ( type == AbstractMessage.UNSUBACK ) {
        result = new UnsubAckMessage( info, data );
      } else if ( type == AbstractMessage.PUBREL ) {
        result = new PubRelMessage( info, data );
      } else if ( type == AbstractMessage.PUBREC ) {
        result = new PubRecMessage( info, data );
      } else if ( type == AbstractMessage.DISCONNECT ) {
        result = new DisconnectMessage( info, data );
      } else {
        throw MQTT.createMqttException( MqttException.UNEXPECTED_ERROR );
      }
      return result;
    } catch ( final IOException io ) {
      throw new MqttException( io );
    }
  }




  public static AbstractMessage createWireMessage( final Cacheable data ) throws MqttException {
    byte[] payload = data.getPayloadBytes();
    // The persistable interface allows a message to be restored entirely in the header array
    // Need to treat these two arrays as a single array of bytes and use the decoding
    // logic to identify the true header/payload split
    if ( payload == null ) {
      payload = new byte[0];
    }
    final MultiByteArrayInputStream mbais = new MultiByteArrayInputStream( data.getHeaderBytes(), data.getHeaderOffset(), data.getHeaderLength(), payload, data.getPayloadOffset(), data.getPayloadLength() );
    return createWireMessage( mbais );
  }




  /**
   * Encode a Multi-Byte Integer
   * 
   * @param number the number to encode
   * 
   * @return bytes representing the number
   */
  protected static byte[] encodeMBI( final long number ) {
    int numBytes = 0;
    long no = number;
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // Encode the remaining length fields in the four bytes
    do {
      byte digit = (byte)( no % 128 );
      no = no / 128;
      if ( no > 0 ) {
        digit |= 0x80;
      }
      bos.write( digit );
      numBytes++;
    }
    while ( ( no > 0 ) && ( numBytes < 4 ) );

    return bos.toByteArray();
  }




  /**
   * Decodes an MQTT Multi-Byte Integer from the given stream.
   */
  protected static MultiByteInteger readMBI( final DataInputStream in ) throws IOException {
    byte digit;
    long msgLength = 0;
    int multiplier = 1;
    int count = 0;

    do {
      digit = in.readByte();
      count++;
      msgLength += ( ( digit & 0x7F ) * multiplier );
      multiplier *= 128;
    }
    while ( ( digit & 0x80 ) != 0 );

    return new MultiByteInteger( msgLength, count );
  }

  //The type of the message (e.g. CONNECT, PUBLISH, PUBACK, etc.)
  private final byte msgType;

  //The MQTT message ID
  protected int msgId;

  protected boolean duplicate = false;




  public AbstractMessage( final byte type ) {
    msgType = type;
    msgId = 0;
  }




  /**
   * Decodes a UTF-8 string from the DataInputStream provided.
   * 
   * @param input The input stream from which to read the encoded string
   * 
   * @return a decoded String from the DataInputStream
   * 
   * @throws MqttException when an error occurs with either reading from the 
   *         stream or decoding the encoded string.
   */
  protected String decodeUTF8( final DataInputStream input ) throws MqttException {
    int encodedLength;
    try {
      encodedLength = input.readUnsignedShort();

      final byte[] encodedString = new byte[encodedLength];
      input.readFully( encodedString );

      return new String( encodedString, "UTF-8" );
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  protected byte[] encodeMessageId() throws MqttException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );
      dos.writeShort( msgId );
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  /**
   * Encodes a String given into UTF-8, before writing this to the 
   * DataOutputStream the length of the encoded string is encoded into 2 bytes 
   * and then written to the DataOutputStream. 
   *
   * @param dos The stream to write the encoded UTF-8 String to.
   * @param stringToEncode The String to be encoded
   *  
   * @throws MqttException when an error occurs with either the encoding or 
   *         writing the data to the stream
   */
  protected void encodeUTF8( final DataOutputStream dos, final String stringToEncode ) throws MqttException {
    try {

      final byte[] encodedString = stringToEncode.getBytes( "UTF-8" );
      final byte byte1 = (byte)( ( encodedString.length >>> 8 ) & 0xFF );
      final byte byte2 = (byte)( ( encodedString.length >>> 0 ) & 0xFF );

      dos.write( byte1 );
      dos.write( byte2 );
      dos.write( encodedString );
    } catch ( final UnsupportedEncodingException ex ) {
      throw new MqttException( ex );
    } catch ( final IOException ex ) {
      throw new MqttException( ex );
    }
  }




  public byte[] getHeader() throws MqttException {
    try {
      final int first = ( ( getType() & 0x0f ) << 4 ) ^ ( getMessageInfo() & 0x0f );
      final byte[] varHeader = getVariableHeader();
      final int remLen = varHeader.length + getPayload().length;

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream( baos );
      dos.writeByte( first );
      dos.write( encodeMBI( remLen ) );
      dos.write( varHeader );
      dos.flush();
      return baos.toByteArray();
    } catch ( final IOException ioe ) {
      throw new MqttException( ioe );
    }
  }




  /** 
   * Returns a key associated with the message. 
   * 
   * <p>For most message types this will be unique. For connect, disconnect and 
   * ping only one message of this type is allowed so a fixed key will be 
   * returned.</p>
   * 
   * @return key a key associated with the message
   */
  public String getKey() {
    return new Integer( getMessageId() ).toString();
  }




  /**
   * Returns the MQTT message ID.
   */
  public int getMessageId() {
    return msgId;
  }




  /**
   * Sub-classes should override this to encode the message info.
   * Only the least-significant four bits will be used.
   */
  protected abstract byte getMessageInfo();




  /**
   * Sub-classes should override this method to supply the payload bytes.
   */
  public byte[] getPayload() throws MqttException {
    return new byte[0];
  }




  /**
   * Returns the type of the message.
   */
  public byte getType() {
    return msgType;
  }




  protected abstract byte[] getVariableHeader() throws MqttException;




  /**
   * Returns whether or not this message needs to include a message ID.
   */
  public boolean isMessageIdRequired() {
    return true;
  }




  public boolean isRetryable() {
    return false;
  }




  public void setDuplicate( final boolean dup ) {
    duplicate = dup;
  }




  /**
   * Sets the MQTT message ID.
   */
  public void setMessageId( final int msgId ) {
    this.msgId = msgId;
  }




  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append( PACKET_NAMES[msgType] );
    b.append( "(" );
    byte[] payload;
    try {
      payload = getPayload();
      if ( payload.length > 0 ) {
        b.append( payload.length );
        b.append( "b)" );
      } else {
        b.append( "no payload)" );
      }
    } catch ( MqttException e ) {
      b.append( "Unknown Payload)" );
    }
    return b.toString();
  }

}
