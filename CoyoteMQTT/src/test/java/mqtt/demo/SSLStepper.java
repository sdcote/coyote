package mqtt.demo;

import java.io.IOException;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.network.SSLSocketFactoryFactory;
import coyote.commons.network.mqtt.network.SSLTransport;
import coyote.commons.network.mqtt.network.Transport;


/**
 * 
 */
public class SSLStepper {

  /**
   * @param args
   * @throws MqttException 
   * @throws IOException 
   */
  public static void main( String[] args ) throws IOException, MqttException {

    String host = "http-proxy.nwie.net";
    int port = 8080;
    String clientId = "stepper";

    System.setProperty( "javax.net.ssl.keyStore", "src\\test\\resources\\clientkeystore.jks" );
    System.setProperty( "javax.net.ssl.keyStorePassword", "password" );
    System.setProperty( "javax.net.ssl.trustStore", "src\\test\\resources\\clientkeystore.jks" );

    if ( SSLSocketFactoryFactory.isSupportedOnJVM() ) {
      SSLSocketFactoryFactory factoryFactory = new SSLSocketFactoryFactory();

      factoryFactory.initialize( new Properties(), null );

      SocketFactory factory = factoryFactory.createSocketFactory( null );

      Transport transport = new SSLTransport( (SSLSocketFactory)factory, host, port, clientId );
      ( (SSLTransport)transport ).setSSLhandshakeTimeout( 30 );

      transport.start();
      
      transport.getInputStream();

    } else {
      System.err.println( "Factory is not supported on this JVM" );
    }

  }

}
