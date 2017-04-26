package coyote.commons.network.mqtt.network.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLSocketFactory;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.network.SSLTransport;


public class WebSocketSecureTransport extends SSLTransport {
  private final PipedInputStream pipedInputStream;
  private WebSocketReceiver webSocketReceiver;
  private final String uri;
  private final String host;
  private final int port;
  ByteBuffer recievedPayload;

  /**
   * Overrides the flush method.
   * 
   * <p>This allows us to encode the MQTT payload into a WebSocket Frame before 
   * passing it through to the real socket.</p>
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




  public WebSocketSecureTransport( final SSLSocketFactory factory, final String uri, final String host, final int port, final String clientId ) {
    super( factory, host, port, clientId );
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
    return "wss://" + host + ":" + port;
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
    final WebSocketHandshake handshake = new WebSocketHandshake( super.getInputStream(), super.getOutputStream(), uri, host, port );
    handshake.execute();
    webSocketReceiver = new WebSocketReceiver( getSocketInputStream(), pipedInputStream );
    webSocketReceiver.start( "WSSReceiver" );

  }




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
