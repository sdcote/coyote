package coyote.commons.network.mqtt.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.SocketFactory;

import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * A network transport for communicating over TCP. 
 */
public class TCPTransport implements Transport {

  protected Socket socket;
  private final SocketFactory factory;
  protected final String host;
  protected final int port;
  private int conTimeout;




  /**
   * Constructs a new TCPTransport using the specified host and port.  
   * 
   * <p>The supplied SocketFactory is used to supply the network socket.</p>
   */
  public TCPTransport( final SocketFactory factory, final String host, final int port, final String resourceContext ) {
    this.factory = factory;
    this.host = host;
    this.port = port;
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    return socket.getInputStream();
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    return socket.getOutputStream();
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#getServerURI()
   */
  @Override
  public String getServerURI() {
    return "tcp://" + host + ":" + port;
  }




  /**
   * Set the maximum time to wait for a socket to be established.
   * 
   * @param timeout in milliseconds
   */
  public void setConnectTimeout( final int timeout ) {
    conTimeout = timeout;
  }




  /**
   * Starts the transport, by creating a TCP socket to the server.
   */
  @Override
  public void start() throws IOException, MqttException {
    try {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tcptransport.connecting", host, port, conTimeout ) );

      final SocketAddress sockaddr = new InetSocketAddress( host, port );
      socket = factory.createSocket();
      socket.connect( sockaddr, conTimeout * 1000 );

    } catch ( final ConnectException ex ) {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "tcptransport.failed_to_create_tcp_socket", ex.getMessage() ) );
      throw new MqttException( MqttException.SERVER_CONNECT_ERROR, ex );
    }
  }




  /**
   * Stops the transport, by closing the TCP socket.
   */
  @Override
  public void stop() throws IOException {
    if ( socket != null ) {
      socket.close();
    }
  }

}
