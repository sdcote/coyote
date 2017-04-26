package coyote.commons.network.mqtt.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import coyote.commons.ByteUtil;
import coyote.commons.network.mqtt.ClientState;
import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * An {@code MesageInputStream} lets applications read instances of
 * {@code AbstractMessage}. 
 */
public class MessageInputStream extends InputStream {
  private ClientState clientState = null;
  private final DataInputStream in;




  public MessageInputStream( final ClientState clientState, final InputStream in ) {
    this.clientState = clientState;
    this.in = new DataInputStream( in );
  }




  @Override
  public int available() throws IOException {
    return in.available();
  }




  @Override
  public void close() throws IOException {
    in.close();
  }




  @Override
  public int read() throws IOException {
    return in.read();
  }




  /**
   * 
   * @param buffer
   * @param offset
   * @param length
   * @throws IOException
   */
  private void readFully( final byte buffer[], final int offset, final int length ) throws IOException {
    if ( length < 0 ) {
      throw new IndexOutOfBoundsException();
    }
    int n = 0;
    while ( n < length ) {
      final int count = in.read( buffer, offset + n, length - n );
      clientState.notifyReceivedBytes( count );

      if ( count < 0 ) {
        throw new EOFException();
      }
      n += count;
    }
  }




  /**
   * This reads in the bytes from the broker and creates a message.
   * 
   * <p>The first byte is read in and the first 4 bits are used to determine 
   * the type of message has been received. The remaining 4 bits are discarded 
   * as they apply to brokers, not clients.</p>
   * 
   * <p>The next byte is read in as the length of the packet and used to setup
   * an array to hold the raw data. The rest of the packet is read in and 
   * passed to the AbstractMessage for parsing into a message. This message is 
   * then returned to the caller.</p> 
   * 
   * @return the message received from the broker
   * 
   * @throws MqttException if the type in the header is invalid
   * @throws MqttException if the data could not be parsed into a message
   * @throws IOException if there were problems reading data from the network
   */
  public AbstractMessage readMessage() throws IOException, MqttException {
    final ByteArrayOutputStream bais = new ByteArrayOutputStream();
    final byte first = in.readByte();
    clientState.notifyReceivedBytes( 1 );

    // the first four bits are the message type 
    final byte type = (byte)( ( first >>> 4 ) & 0x0F );
    if ( ( type < AbstractMessage.CONNECT ) || ( type > AbstractMessage.DISCONNECT ) ) {
      throw MQTT.createMqttException( MqttException.INVALID_MESSAGE );
    }

    final long remainingLength = AbstractMessage.readMBI( in ).getValue();

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "messageinput.read_header", type, remainingLength ) );

    // place the first read byte into packet for parsing by the AbstractMessage 
    bais.write( first );

    // place the length into the packet for parsing by the AbstractMessage
    bais.write( AbstractMessage.encodeMBI( remainingLength ) );

    // create an array large enough for the while packet including the first 2 
    // bytes we read
    final byte[] packet = new byte[(int)( bais.size() + remainingLength )];

    // now we read the remainder of the packet
    readFully( packet, bais.size(), packet.length - bais.size() );

    final byte[] header = bais.toByteArray();
    System.arraycopy( header, 0, packet, 0, header.length );

    final AbstractMessage message = AbstractMessage.createWireMessage( packet );
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "messageinput.read_message", message ) );

    return message;
  }
}
