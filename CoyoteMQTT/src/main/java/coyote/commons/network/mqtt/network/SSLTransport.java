package coyote.commons.network.mqtt.network;

import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * A network transport for connecting over SSL.
 */
public class SSLTransport extends TCPTransport {

  private String[] enabledCiphers;
  private int handshakeTimeoutSecs;




  /**
   * Constructs a new SSLTransport using the specified host and port.
   * 
   * <p>The supplied SSLSocketFactory is used to supply the network socket.</p>
   */
  public SSLTransport( final SSLSocketFactory factory, final String host, final int port, final String resourceContext ) {
    super( factory, host, port, resourceContext );
  }




  /**
   * Returns the enabled cipher suites.
   */
  public String[] getEnabledCiphers() {
    return enabledCiphers;
  }




  @Override
  public String getServerURI() {
    return "ssl://" + host + ":" + port;
  }




  /**
   * Sets the enabled cipher suites on the underlying network socket.
   */
  public void setEnabledCiphers( final String[] enabledCiphers ) {
    this.enabledCiphers = enabledCiphers;
    if ( ( socket != null ) && ( enabledCiphers != null ) ) {
      if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
        String ciphers = "";
        for ( int i = 0; i < enabledCiphers.length; i++ ) {
          if ( i > 0 ) {
            ciphers += ",";
          }
          ciphers += enabledCiphers[i];
        }
        Log.debug( LogMsg.createMsg( MQTT.MSG, "Client.ssl_ciphers", ciphers ) );
      }
      ( (SSLSocket)socket ).setEnabledCipherSuites( enabledCiphers );
    }
  }




  public void setSSLhandshakeTimeout( final int seconds ) {
    super.setConnectTimeout( seconds );
    handshakeTimeoutSecs = seconds;
  }




  /**
   * This connects to the server and negotiates the SSL connection.
   * 
   * @see coyote.commons.network.mqtt.network.TCPTransport#start()
   */
  @Override
  public void start() throws IOException, MqttException {
    // create the socket and connect it
    super.start();

    setEnabledCiphers( enabledCiphers );
    final int soTimeout = socket.getSoTimeout();
    if ( soTimeout == 0 ) {
      // RTC 765: Set a timeout to avoid the SSL handshake being blocked indefinitely
      socket.setSoTimeout( handshakeTimeoutSecs * 1000 );
    }
    ( (SSLSocket)socket ).startHandshake();

    // reset timeout to its original value
    socket.setSoTimeout( soTimeout );
  }

}
