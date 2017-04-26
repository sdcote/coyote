package coyote.commons.network.mqtt.utilities;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import coyote.commons.network.mqtt.MqttBlockingClient;
import coyote.commons.network.mqtt.MqttClient;
import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.MqttToken;


/**
 * General purpose test utilities  
 */
public class Utility {

  static final String className = Utility.class.getName();
  static final Logger log = Logger.getLogger( className );




  /**
   * @return the current method name for the caller.
   */
  public static String getMethodName() {
    StackTraceElement[] stack = ( new Throwable() ).getStackTrace();
    String methodName = stack[1].getMethodName();

    // Skip over synthetic accessor methods
    if ( methodName.equals( "access$0" ) ) {
      methodName = stack[2].getMethodName();
    }

    return methodName;
  }




  /**
   * @return 'true' if running on Windows
   */
  public static boolean isWindows() {
    String osName = System.getProperty( "os.name" );
    if ( osName.startsWith( "Windows" ) ) {
      return true;
    }
    return false;
  }




  /**
   * @param client
   * @throws MqttException
   */
  public static void disconnectAndCloseClient( MqttClient client ) throws MqttException {
    if ( client != null ) {
      if ( client.isConnected() ) {
        MqttToken token = client.disconnect( null, null );
        token.waitForCompletion();
      }
      client.close();
    }
  }




  /**
   * @param client
   * @throws MqttException
   */
  public static void disconnectAndCloseClient( MqttBlockingClient client ) throws MqttException {
    if ( client != null ) {
      if ( client.isConnected() ) {
        client.disconnect( 0 );
      }
      client.close();
    }
  }

 



}
