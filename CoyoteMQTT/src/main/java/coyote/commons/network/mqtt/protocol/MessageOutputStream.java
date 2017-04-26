package coyote.commons.network.mqtt.protocol;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import coyote.commons.network.mqtt.ClientState;
import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * An {@code MessageOutputStream} lets applications write instances of
 * {@code AbstractMessage}. 
 */
public class MessageOutputStream extends OutputStream {
  private ClientState clientState = null;
  private final BufferedOutputStream out;




  public MessageOutputStream( final ClientState clientState, final OutputStream out ) {
    this.clientState = clientState;
    this.out = new BufferedOutputStream( out );
  }




  @Override
  public void close() throws IOException {
    out.close();
  }




  @Override
  public void flush() throws IOException {
    out.flush();
  }




  @Override
  public void write( final byte[] b ) throws IOException {
    out.write( b );
    clientState.notifySentBytes( b.length );
  }




  @Override
  public void write( final byte[] b, final int off, final int len ) throws IOException {
    out.write( b, off, len );
    clientState.notifySentBytes( len );
  }




  @Override
  public void write( final int b ) throws IOException {
    out.write( b );
  }




  /**
   * Writes an {@code MqttWireMessage} to the stream.
   */
  public void write( final AbstractMessage message ) throws IOException, MqttException {
    final byte[] bytes = message.getHeader();
    final byte[] pl = message.getPayload();
    out.write( bytes, 0, bytes.length );
    clientState.notifySentBytes( bytes.length );

    int offset = 0;
    final int chunckSize = 1024;
    while ( offset < pl.length ) {
      final int length = Math.min( chunckSize, pl.length - offset );
      out.write( pl, offset, length );
      offset += chunckSize;
      clientState.notifySentBytes( length );
    }

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "messageoutput.wrote_message", message ) );
  }
}
