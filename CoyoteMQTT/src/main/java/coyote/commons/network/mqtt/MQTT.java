package coyote.commons.network.mqtt;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import coyote.commons.StringUtil;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class MQTT {

  public static String VERSION = "0.1";
  public static String BUILD_LEVEL = "L0";

  public static final int VERSION_DEFAULT = 0;
  public static final int VERSION_3_1 = 3;
  public static final int VERSION_3_1_1 = 4;

  protected static final int TCP_URI = 0;
  protected static final int SSL_URI = 1;
  protected static final int LOCAL_URI = 2;
  protected static final int WS_URI = 3;
  protected static final int WSS_URI = 4;

  private static final String separator = "- - - - - - - - -";
  private static final String lineSep = System.getProperty( "line.separator", "\n" );

  private static final ResourceBundle CODE;

  public static final long EVENT = Log.getCode( "MQTT" );
  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "BatchMQTTMsg" );

    //MAY throws MissingResourceException
    CODE = ResourceBundle.getBundle( "BatchMQTTCode" );

  }




  /**
   * All exceptions use a code to determine their cause; this allows the code 
   * to be converted into localized text.
   * 
   * @param id the reason code to lookup
   * 
   * @return the localized text relating to that code
   */
  public static String getLocalizedMessage( final int id ) {
    try {
      return CODE.getString( Integer.toString( id ) );
    } catch ( final MissingResourceException mre ) {
      return "MqttException";
    }
  }




  /**
   * Create a MQTT Client that can be used to communicate with an MQTT server.
   * 
   * @param brokerURI the URI of the server to which the client is to connect.
   * @param clientId a client identifier that is unique on the server
   * 
   * @return A usable client
   */
  public static MqttClient createClient( final String brokerURI, final String clientId ) {
    MqttClient retval = null;

    try {
      retval = new MqttClientImpl( brokerURI, clientId );
      Log.append( MQTT.EVENT, LogMsg.createMsg( MSG, "Client.creating_client" ) );
    } catch ( MqttException e ) {
      Log.append( MQTT.EVENT, "CLIENT CREATION FAILED: " + e.getMessage() );
    }
    return retval;
  }




  /**
   * Validate a URI
   * 
   * @param srvURI
   * 
   * @return the URI type
   */
  protected static int validateURI( final String srvURI ) {
    try {
      final URI vURI = new URI( srvURI );

      if ( !vURI.getPath().equals( "" ) ) {
        throw new IllegalArgumentException( srvURI );
      }

      if ( vURI.getScheme().equals( "ws" ) ) {
        // websocket connection
        return WS_URI;
      } else if ( vURI.getScheme().equals( "wss" ) ) {
        // secure websocket connection
        return WSS_URI;
      } else if ( vURI.getScheme().equals( "tcp" ) ) {
        // normal TCP connection
        return TCP_URI;
      } else if ( vURI.getScheme().equals( "ssl" ) ) {
        // SSL/TLS connection
        return SSL_URI;
      } else if ( vURI.getScheme().equals( "local" ) ) {
        // connection to IBM microbroker running in this JRE
        return LOCAL_URI;
      } else {
        throw new IllegalArgumentException( srvURI );
      }
    } catch ( final URISyntaxException ex ) {
      throw new IllegalArgumentException( srvURI );
    }
  }




  /**
   * Create an exception for the given return code.
   * 
   * <p>The return code is used to generate a localized message from the 
   * MqttCode properties file or its localized versions.</p>
   *  
   * @param reasonCode Reason for the exception.
   * 
   * @return the created exception
   */
  public static MqttException createMqttException( final int reasonCode ) {
    if ( ( reasonCode == MqttException.FAILED_AUTHENTICATION ) || ( reasonCode == MqttException.NOT_AUTHORIZED ) ) {
      return new MqttSecurityException( reasonCode );
    }

    return new MqttException( reasonCode );
  }




  public static MqttException createMqttConnectionException( final Throwable cause ) {
    if ( cause.getClass().getName().equals( "java.security.GeneralSecurityException" ) ) {
      return new MqttSecurityException( cause );
    }
    return new MqttException( cause );
  }




  /**
   * Returns whether or not the specified class is available to the current
   * class loader.
   * 
   * <p>This is used to protect the code against using Java SE APIs on Java 
   * ME.</p>
   */
  public static boolean isClassAvailable( final String className ) {
    boolean result = false;
    try {
      Class.forName( className );
      result = true;
    } catch ( final ClassNotFoundException ex ) {}
    return result;
  }




  /**
   * Dump of JVM wide debug info.
   * 
   * <p>This includes trace and system properties.</p>
   */
  public static void logBaseDebug() {
    logVersion();
    logSystemProperties();
  }




  /**
   * Dump maximum debug info.
   * 
   * <p>This includes state specific to a client as well as debug that is JVM 
   * wide like trace and system properties.</p>
   * 
   * @param client The client about which debug information should be logged.
   */
  public static void logClientDebug( MqttClient client ) {
    logConnection( client );
    logConnOptions( client );
    logClientState( client );
    logBaseDebug();
  }




  /**
   * Dump information that show the version of the MQTT client being used.
   */
  public static void logVersion() {
    final StringBuffer vInfo = new StringBuffer();
    vInfo.append( lineSep + separator + " Version Info " + separator + lineSep );
    vInfo.append( StringUtil.fixedLength( "Version", 20, StringUtil.LEFT_ALIGNMENT, ' ' ) + ":  " + MQTT.VERSION + lineSep );
    vInfo.append( StringUtil.fixedLength( "Build Level", 20, StringUtil.LEFT_ALIGNMENT, ' ' ) + ":  " + MQTT.BUILD_LEVEL + lineSep );
    vInfo.append( separator + separator + separator + lineSep );
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "debug.logVersion", vInfo.toString() ) );
  }




  /**
   * Dump the current set of system.properties to a log record
   */
  public static void logSystemProperties() {
    final Properties sysProps = System.getProperties();
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "debug.logSystemProperties", logProperties( sysProps, "SystemProperties" ).toString() ) );
  }




  /**
   * Return a set of properties as a formatted string
   */
  public static String logProperties( final Properties props, final String title ) {

    final StringBuffer propStr = new StringBuffer();
    final Enumeration<?> names = props.propertyNames();
    propStr.append( lineSep + separator + " " + title + " " + separator + lineSep );
    while ( names.hasMoreElements() ) {
      final String key = (String)names.nextElement();
      propStr.append( StringUtil.fixedLength( key, 28, StringUtil.LEFT_ALIGNMENT, ' ' ) + ":  " + props.get( key ) + lineSep );
    }
    propStr.append( separator + separator + separator + lineSep );

    return propStr.toString();
  }




  /**
   * Dump interesting variables from Connection
   * 
   * @param client 
   */
  public static void logConnection( MqttClient client ) {
    if ( client != null ) {
      Connection connection = client.getconnection();

      Properties props = null;
      if ( connection != null ) {
        props = connection.getDebug();
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "debug.logConnection", logProperties( props, client.getClientId() + " : Connection" ).toString() ) );
      }
    } else {
      Log.notice( "Passed a null reference to MQTT.logConnection" );
    }

  }




  /**
   * Dump Connection options
   */
  public static void logConnOptions( MqttClient client ) {
    if ( client != null ) {
      Connection connection = client.getconnection();
      Properties props = null;
      if ( connection != null ) {
        props = connection.getConOptions().getDebug();
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "debug.logConnOptions", logProperties( props, client.getClientId() + " : Connect Options" ).toString() ) );
      }
    } else {
      Log.notice( "Passed a null reference to MQTT.logConnOptions" );
    }
  }




  /**
   * Dump interesting variables from ClientState
   */
  public static void logClientState( MqttClient client ) {
    if ( client != null ) {
      Connection connection = client.getconnection();
      Properties props = null;
      if ( ( connection != null ) && ( connection.getClientState() != null ) ) {
        props = connection.getClientState().getDebug();
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "debug.logClientState", logProperties( props, client.getClientId() + " : ClientState" ).toString() ) );
      }
    } else {
      Log.notice( "Passed a null reference to MQTT.logClientState" );
    }
  }

}
