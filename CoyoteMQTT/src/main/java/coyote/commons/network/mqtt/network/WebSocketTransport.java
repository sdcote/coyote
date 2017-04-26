package coyote.commons.network.mqtt.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;

import javax.net.SocketFactory;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.network.websocket.WebSocketFrame;
import coyote.commons.network.mqtt.network.websocket.WebSocketHandshake;
import coyote.commons.network.mqtt.network.websocket.WebSocketReceiver;


public class WebSocketTransport extends TCPTransport {

  private final String uri;
  private final String host;
  private final int port;
  private final PipedInputStream pipedInputStream;
  private WebSocketReceiver webSocketReceiver;
  ByteBuffer recievedPayload;

  /**
   * Overrides the flush method.
   * This allows us to encode the MQTT payload into a WebSocket
   *  Frame before passing it through to the real socket.
   */
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {

    @Override
    public void flush() throws IOException {
      final ByteBuffer byteBuffer;
      synchronized( this ) {
        byteBuffer = ByteBuffer.wrap( toByteArray() );
        reset();
      }
      final WebSocketFrame frame = new WebSocketFrame( (byte)0x02, true, byteBuffer.array() );
      final byte[] rawFrame = frame.encodeFrame();
      getSocketOutputStream().write( rawFrame );
      getSocketOutputStream().flush();

    }
  };




  public WebSocketTransport( final SocketFactory factory, final String uri, final String host, final int port, final String resourceContext ) {
    super( factory, host, port, resourceContext );
    this.uri = uri;
    this.host = host;
    this.port = port;
    pipedInputStream = new PipedInputStream();
  }




  @Override
  public InputStream getInputStream() throws IOException {
    return pipedInputStream;
  }




  @Override
  public OutputStream getOutputStream() throws IOException {
    return outputStream;
  }




  @Override
  public String getServerURI() {
    return "ws://" + host + ":" + port;
  }




  private InputStream getSocketInputStream() throws IOException {
    return super.getInputStream();
  }




  private OutputStream getSocketOutputStream() throws IOException {
    return super.getOutputStream();
  }




  @Override
  public void start() throws IOException, MqttException {
    super.start();
    final WebSocketHandshake handshake = new WebSocketHandshake( getSocketInputStream(), getSocketOutputStream(), uri, host, port );
    handshake.execute();
    webSocketReceiver = new WebSocketReceiver( getSocketInputStream(), pipedInputStream );
    webSocketReceiver.start( "webSocketReceiver" );
  }




  /**
   * Stops the transport, by closing the TCP socket.
   */
  @Override
  public void stop() throws IOException {
    // Creating Close Frame
    final WebSocketFrame frame = new WebSocketFrame( (byte)0x08, true, "1000".getBytes() );
    final byte[] rawFrame = frame.encodeFrame();
    getSocketOutputStream().write( rawFrame );
    getSocketOutputStream().flush();

    if ( webSocketReceiver != null ) {
      webSocketReceiver.stop();
    }
    super.stop();
  }

}
