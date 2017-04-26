package coyote.commons.network.mqtt.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import coyote.commons.network.mqtt.client.TestClientFactory;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * Contains the classes and utilities used to configure the MQTT testcases. 
 *
 * <p>The way in which a test is run is controlled by properties which are 
 * typically supplied in property file. A {@link 
 * coyote.commons.network.mqtt.properties.TestProperties TestProperties} class provides 
 * default values and getter methods to supported properties. When the 
 * properties have been loaded, the framework logs the non-default values.</p> 
 * 
 * <p>Test properties are initialized by loading a property file as follows:<ul>
 * <li>Get filename from system property</li><ul>
 * <li>Get filename from system property <code>TEST_PROPERTY_FILENAME</code> with a default value of <code>test.properties</code></li>
 * <li>Load properties using this filename as a file on the filesystem</li></ul>  
 * <li>Else use the default filename<ul>
 * <li>Use the default filename <code>test.properties</code></li>
 * <li>Load properties using this filename as a resource in the same package as the TestProperties class</li>
 * </ul></li></ul></p>  
 * 
 * <p>A property loaded from a file is overridden by a system property of the 
 * same name.</p>
 *  
 * <p>A property file loaded as a resource may be located in any eclipse 
 * project on the runtime classpath.<ul>
 * <li>Note: If you intend to run a testcase to run against a server but the 
 * the property file is in a server eclipse project, remember to set the 
 * "project" setting on the eclipse run configuration to make eclipse add the 
 * server eclipse project to the runtime classpath, otherwise the testcase 
 * will load the wrong properties</li></ul></p>      
 * 
 */
public class TestProperties {
  static private final Class<?> cclass = TestProperties.class;
  /**
   * The URI of the test MQTT Server.
   * <p>
   * The default value is <code>tcp://&lt;localhost&gt;:1883</code>> where <code>&lt;localhost&gt;</code> is expressed as a IPv4 dotted decimal value 
   */
  static public final String KEY_SERVER_URI = "SERVER_URI";

  /**
   * The working directory usd by the framework.
   * <p> 
   * The default value is system property <code>java.io.tmpdir</code>
   */
  static public final String KEY_WORKING_DIR = "WORKING_DIR";

  /**
   * The class name of the client factory the tests are to be run against.
   */
  static public final String KEY_CLIENT_TYPE = "CLIENT_TYPE";

  static public final String KEY_CLIENT_KEY_STORE = "CLIENT_KEY_STORE";

  static public final String KEY_CLIENT_KEY_STORE_PASSWORD = "CLIENT_KEY_STORE_PASSWORD";

  static public final String KEY_CLIENT_TRUST_STORE = "CLIENT_TRUST_STORE";

  static public final String KEY_SERVER_SSL_PORT = "SERVER_SSL_PORT";

  static public final String KEY_SERVER_WEBSOCKET_URI = "SERVER_WEBSOCKET_URI";

  static private Map<String, String> defaults = new HashMap<String, String>();

  private static TestProperties singleton;
  private Properties properties = new Properties();

  static {
    String temporaryDirectoryName = System.getProperty( "java.io.tmpdir" );

    String localhost = "localhost";
    try {
      localhost = InetAddress.getLocalHost().getHostAddress();
    } catch ( UnknownHostException e ) {
      // empty
    }
    String defaultServerURI = "tcp://" + localhost + ":1883";

    putDefault( KEY_WORKING_DIR, temporaryDirectoryName );
    putDefault( KEY_SERVER_URI, defaultServerURI );
    putDefault( KEY_CLIENT_TYPE, TestClientFactory.class.getName() );
    putDefault( KEY_SERVER_SSL_PORT, "8883" );

    // Make sure all the property classes we know about get initialized
    List<String> list = new ArrayList<String>();
    list.add( " coyote.mqtt.test.properties.ClientTestProperties" );
    list.add( " coyote.mqtt.test.properties.MqTestProperties" );
    list.add( " coyote.mqtt.test.properties.ImsTestProperties" );

    for ( String name : list ) {
      try {
        Class.forName( name );
      } catch ( ClassNotFoundException exception ) {
        Log.debug( "Property class '" + name + "' not found" );
      }
    }

    // TODO: Configure test logging via properties
    // Add a logger that will send log messages to the console 
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.ALL_EVENTS ) );

  }




  /**
   * @param key 
   * @param defaultValue 
   */
  public static void putDefault( String key, String defaultValue ) {
    defaults.put( key, defaultValue );
  }




  /**
   * @return TestProperties
   */
  public static TestProperties getInstance() {
    if ( singleton == null ) {
      singleton = new TestProperties();
    }
    return singleton;
  }




  /**
   * Reads properties from a properties file in the same path as this class
   *   - first look for the property file on the filesystem 
   */
  public TestProperties() {

    InputStream stream = null;
    try {
      String filename = System.getProperty( "TEST_PROPERTY_FILENAME", "test.properties" );
      stream = getPropertyFileAsStream( filename );

      if ( stream == null ) {
        filename = "test.properties";
        stream = cclass.getClassLoader().getResourceAsStream( filename );
      }

      // Read the properties from the property file
      if ( stream != null ) {
        Log.info( "Loading properties from: '" + filename + "' via classpath" );
        properties.load( stream );
      }
    } catch ( Exception e ) {
      Log.error( "caught exception:", e );
    }
    finally {
      if ( stream != null ) {
        try {
          stream.close();
        } catch ( IOException e ) {
          Log.error( "caught exception:", e );
        }
      }
    }

    // Override the default property values from SystemProperties
    for ( String key : defaults.keySet() ) {
      String systemValue = System.getProperty( key );
      if ( systemValue != null ) {
        properties.put( key, systemValue );
      }
    }

    for ( Object object : properties.keySet() ) {
      if ( object instanceof String ) {
        String key = (String)object;

        // Override the property values from SystemProperties
        String systemValue = System.getProperty( key );
        if ( systemValue != null ) {
          properties.put( key, systemValue );
        }

        String defaultValue = defaults.get( key );
        String value = getProperty( key );

        // Output the non-default properties
        boolean isSame = false;

        if ( defaultValue == null ) {
          if ( value == null ) {
            isSame = true;
          }
        } else if ( value != null ) {
          isSame = defaultValue.equals( value );
        }

        if ( systemValue != null ) {
          Log.info( "    System property: " + key + " = " + getProperty( key ) );
        } else if ( isSame == false ) {
          Log.info( "                     " + key + " = " + getProperty( key ) );
        }
      }
    }
  }




  /**
   * @param filename
   * @return stream
   * @throws IOException 
   */
  private InputStream getPropertyFileAsStream( String filename ) throws IOException {
    InputStream stream = null;
    try {
      stream = new FileInputStream( filename );
    } catch ( Exception exception ) {
      Log.debug( "Property file: '" + filename + "' (" + new File( filename ).getAbsolutePath() + ") not found" );
    }

    return stream;
  }




  /**
   * This is equivalent to class.getResourceAsStream but allows us to report the URL location
   *    
   * @param filename
   * @return stream
   * @throws IOException 
   */
  private InputStream getPropertyResourceAsStream( String filename ) throws IOException {

    InputStream stream = null;
    URL url = TestProperties.class.getResource( filename );

    if ( url == null ) {
      Log.info( "Property resource: '" + filename + "' not found" );
    } else {
      Log.info( "Property resource: '" + filename + "' found at '" + url + "'" );
      stream = url.openStream();

      if ( stream == null ) {
        Log.info( "Could not open stream to Property resource: '" + filename + "'" );
      }
    }

    return stream;
  }




  /**
   * @param key
   * @return value
   */
  public String getProperty( String key ) {
    String value = properties.getProperty( key );

    if ( value == null ) {
      value = defaults.get( key );
    }

    return value;
  }




  /**
   * @param key
   * @return value
   */
  public boolean getBooleanProperty( String key ) {
    String value = getProperty( key );
    return Boolean.parseBoolean( value );
  }




  /**
   * @param key
   * @return value
   */
  public int getIntProperty( String key ) {
    String value = getProperty( key );
    return Integer.parseInt( value );
  }




  /**
   * @return working directory
   */
  public static File getTemporaryDirectory() {
    String pathname = getInstance().getProperty( KEY_WORKING_DIR );
    return new File( pathname );
  }




  /**
   * @return keystore file
   */

  public static String getClientKeyStore() {
    URL keyStore = cclass.getClassLoader().getResource( getInstance().getProperty( KEY_CLIENT_KEY_STORE ) );
    return keyStore.getPath();
  }




  /**
   * @return keystore file password
   */

  public static String getClientKeyStorePassword() {
    String keyStorePassword = getInstance().getProperty( KEY_CLIENT_KEY_STORE_PASSWORD );
    return keyStorePassword;
  }




  /**
   * @return truststore file
   */

  public static String getClientTrustStore() {
    URL trustStore = cclass.getClassLoader().getResource( getInstance().getProperty( KEY_CLIENT_TRUST_STORE ) );
    return trustStore.getPath();
  }




  /**
   * @return the SSL port of the server for testing
   */

  public static int getServerSSLPort() {
    int port = Integer.parseInt( getInstance().getProperty( KEY_SERVER_SSL_PORT ) );
    return port;
  }




  /**
   * @return The server URI which may be set in the constructor of an MqttClient
   * @throws URISyntaxException 
   */
  public static URI getServerURI() throws URISyntaxException {

    String string = getInstance().getProperty( KEY_SERVER_URI );
    URI uri = new URI( string );

    return uri;
  }




  /**
   * @return The WebSocket Server URI which may be set in the constructor of an MqttClient
   * @throws URISyntaxException
   */
  public static URI getWebSocketServerURI() throws URISyntaxException {

    String string = getInstance().getProperty( KEY_SERVER_WEBSOCKET_URI );
    URI uri = new URI( string );

    return uri;
  }




  /**
   * Returns a list of URIs which may set in the MQTTConnectOptions for an HA testcase
   * 
   * @return value
   * @throws URISyntaxException 
   */
  public static List<URI> getServerURIs() throws URISyntaxException {
    TestProperties testProperties = getInstance();

    List<URI> list = new ArrayList<URI>();
    int index = 0;
    String uri = testProperties.getProperty( KEY_SERVER_URI + "." + index );
    while ( uri != null ) {
      list.add( new URI( uri ) );
      index++;
      uri = testProperties.getProperty( KEY_SERVER_URI + "." + index );
    }

    return list;
  }




  /**
   * Returns an array list or URIs which may be used by an HA testcase
   * 
   * @return value
   * @throws URISyntaxException 
   */
  public static List<String> getServerURIsAsListOfStrings() throws URISyntaxException {
    List<URI> list1 = getServerURIs();

    List<String> list2 = new ArrayList<String>();

    for ( int i = 0; i < list1.size(); i++ ) {
      list2.add( list1.get( i ).toString() );
    }

    return list2;
  }




  /**
   * Returns an array list or URIs which may be used by an HA testcase
   * 
   * @return value
   * @throws URISyntaxException 
   */
  public static String[] getServerURIsAsStringArray() throws URISyntaxException {
    List<URI> list = getServerURIs();

    String[] array = new String[list.size()];

    for ( int i = 0; i < list.size(); i++ ) {
      array[i] = list.get( i ).toString();
    }

    return array;
  }

}
