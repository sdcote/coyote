package coyote.commons.network.mqtt.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttException;


/**
 * Special transport class that allows a MQTT client to use a non TCP / optimized 
 * mechanism to talk to an IBM MQTT server when running in the same JRE as the 
 * IBM MQTT server.  
 *
 * <p>This class checks for the existence of the IBM adapter class that 
 * provides the optimized communication mechanism. If not available the request
 * to connect is rejected.</p>  
 *  
 * <p>The only known server that implements this is the IBM microbroker:- a 
 * MQTT server that ships with some IBM products.</p>
 */
public class LocalTransport implements Transport {
  private Class localListener;
  private final String brokerName;
  private Object localAdapter;




  public LocalTransport( final String brokerName ) {
    this.brokerName = brokerName;
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    InputStream stream = null;
    try {
      final Method m = localListener.getMethod( "getClientInputStream", new Class[] {} );
      stream = (InputStream)m.invoke( localAdapter, new Object[] {} );
    } catch ( final Exception e ) {}
    return stream;
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    OutputStream stream = null;
    try {
      final Method m = localListener.getMethod( "getClientOutputStream", new Class[] {} );
      stream = (OutputStream)m.invoke( localAdapter, new Object[] {} );
    } catch ( final Exception e ) {}
    return stream;
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#getServerURI()
   */
  @Override
  public String getServerURI() {
    return "local://" + brokerName;
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#start()
   */
  @Override
  public void start() throws IOException, MqttException {
    if ( !MQTT.isClassAvailable( "com.ibm.mqttdirect.modules.local.bindings.localListener" ) ) {
      throw MQTT.createMqttException( MqttException.SERVER_CONNECT_ERROR );
    }
    try {
      localListener = Class.forName( "com.ibm.mqttdirect.modules.local.bindings.localListener" );
      final Method connect_m = localListener.getMethod( "connect", new Class[] { java.lang.String.class } );
      localAdapter = connect_m.invoke( null, new Object[] { brokerName } );
    } catch ( final Exception e ) {}
    if ( localAdapter == null ) {
      throw MQTT.createMqttException( MqttException.SERVER_CONNECT_ERROR );
    }
  }




  /**
   * @see coyote.commons.network.mqtt.network.Transport#stop()
   */
  @Override
  public void stop() throws IOException {
    if ( localAdapter != null ) {
      try {
        final Method m = localListener.getMethod( "close", new Class[] {} );
        m.invoke( localAdapter, new Object[] {} );
      } catch ( final Exception e ) {}
    }
  }

}
