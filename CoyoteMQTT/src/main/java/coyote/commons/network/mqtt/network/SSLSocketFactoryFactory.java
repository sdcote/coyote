package coyote.commons.network.mqtt.network;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import coyote.commons.network.mqtt.MQTT;
import coyote.commons.network.mqtt.MqttSecurityException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * An SSLSocketFactoryFactory provides a socket factory and a server socket
 * factory that then can be used to create SSL client sockets or SSL server
 * sockets.
 * 
 * <p>The SSLSocketFactoryFactory is configured using IBM SSL properties, i.e.
 * properties of the format "com.ibm.ssl.propertyName", e.g. 
 * "com.ibm.ssl.keyStore". The class supports multiple configurations, each
 * configuration is identified using a name or configuration ID. The
 * configuration ID with "null" is used as a default configuration. When a
 * socket factory is being created for a given configuration, properties of 
 * that configuration are first picked. If a property is not defined there, 
 * then that property is looked up in the default configuration. Finally, if a 
 * property element is still not found, then the corresponding system property 
 * is inspected, i.e. javax.net.ssl.keyStore. If the system property is not set
 * either, then the system's default value is used (if available) or an 
 * exception is thrown.</p>
 * 
 * <p>The SSLSocketFacotryFactory can be reconfigured at any time. A 
 * reconfiguration does not affect existing socket factories.</p>
 * 
 * <p>All properties share the same key space; i.e. the configuration ID is not
 * part of the property keys.</p>
 * 
 * <p>The methods should be called in the following order:<ol>
 * <li><b>isSupportedOnJVM()</b>: to check whether this class is supported on
 * the runtime platform. Not all runtimes support SSL/TLS.</li>
 * <li><b>SSLSocketFactoryFactory()</b>: the constructor. Clients 
 * (in the same JVM) may share an SSLSocketFactoryFactory, or have one each.</li>
 * <li><b>initialize(properties, configID)</b>: to initialize this object with
 * the required SSL properties for a configuration. This may be called multiple
 * times, once for each required configuration.It may be called again to change the required SSL
 * properties for a particular configuration</li>
 * <li><b>getEnabledCipherSuites(configID)</b>: to later set the enabled
 * cipher suites on the socket [see below].</li></ol>
 * <i>For a server:</i>
 * <ol>
 * <li><b>getKeyStore(configID)</b>: Optionally, to check that if there is no
 * keystore, then that all the enabled cipher suits are anonymous.</li>
 * <li><b>createServerSocketFactory(configID)</b>: to create an
 * SSLServerSocketFactory.</li>
 * <li><b>getClientAuthentication(configID)</b>: to later set on the
 * SSLServerSocket (itself created from the SSLServerSocketFactory) whether
 * client authentication is needed.</li>
 * </ol>
 * <i>For a client:</i>
 * <ol>
 * <li><b>createSocketFactory(configID)</b>: to create an SSLSocketFactory.</li>
 * </ol>
 */
public class SSLSocketFactoryFactory {
  /** Property keys specific to IBM. */
  public static final String SSLPROTOCOL = "com.ibm.ssl.protocol";
  public static final String JSSEPROVIDER = "com.ibm.ssl.contextProvider";
  public static final String KEYSTORE = "com.ibm.ssl.keyStore";
  public static final String KEYSTOREPWD = "com.ibm.ssl.keyStorePassword";
  public static final String KEYSTORETYPE = "com.ibm.ssl.keyStoreType";
  public static final String KEYSTOREPROVIDER = "com.ibm.ssl.keyStoreProvider";
  public static final String KEYSTOREMGR = "com.ibm.ssl.keyManager";
  public static final String TRUSTSTORE = "com.ibm.ssl.trustStore";
  public static final String TRUSTSTOREPWD = "com.ibm.ssl.trustStorePassword";
  public static final String TRUSTSTORETYPE = "com.ibm.ssl.trustStoreType";
  public static final String TRUSTSTOREPROVIDER = "com.ibm.ssl.trustStoreProvider";
  public static final String TRUSTSTOREMGR = "com.ibm.ssl.trustManager";
  public static final String CIPHERSUITES = "com.ibm.ssl.enabledCipherSuites";
  public static final String CLIENTAUTH = "com.ibm.ssl.clientAuthentication";

  /** Property keys used for java system properties */
  public static final String SYSKEYSTORE = "javax.net.ssl.keyStore";
  public static final String SYSKEYSTORETYPE = "javax.net.ssl.keyStoreType";
  public static final String SYSKEYSTOREPWD = "javax.net.ssl.keyStorePassword";
  public static final String SYSTRUSTSTORE = "javax.net.ssl.trustStore";
  public static final String SYSTRUSTSTORETYPE = "javax.net.ssl.trustStoreType";
  public static final String SYSTRUSTSTOREPWD = "javax.net.ssl.trustStorePassword";
  public static final String SYSKEYMGRALGO = "ssl.KeyManagerFactory.algorithm";
  public static final String SYSTRUSTMGRALGO = "ssl.TrustManagerFactory.algorithm";

  public static final String DEFAULT_PROTOCOL = "TLS";

  private static final String propertyKeys[] = { SSLPROTOCOL, JSSEPROVIDER, KEYSTORE, KEYSTOREPWD, KEYSTORETYPE, KEYSTOREPROVIDER, KEYSTOREMGR, TRUSTSTORE, TRUSTSTOREPWD, TRUSTSTORETYPE, TRUSTSTOREPROVIDER, TRUSTSTOREMGR, CIPHERSUITES, CLIENTAUTH };

  private final Hashtable configs; // a hashtable that maps configIDs to properties.

  private Properties defaultProperties;

  private static final byte[] key = { (byte)0x9d, (byte)0xa7, (byte)0xd9, (byte)0x80, (byte)0x05, (byte)0xb8, (byte)0x89, (byte)0x9c };

  private static final String xorTag = "{xor}";




  /**
   * The inverse operation of obfuscate: returns a cleartext password that was
   * previously obfuscated using the XOR scrambler.
   * 
   * @see coyote.commons.network.mqtt.network.SSLSocketFactoryFactory#obfuscate
   * 
   * @param ePassword An obfuscated password.
   * 
   * @return An array of char, containing the clear text password.
   */
  public static char[] deObfuscate( final String ePassword ) {
    if ( ePassword == null ) {
      return null;
    }
    byte[] bytes = null;
    try {
      bytes = decode( ePassword.substring( xorTag.length() ) );
    } catch ( final Exception e ) {
      return null;
    }

    for ( int i = 0; i < bytes.length; i++ ) {
      bytes[i] = (byte)( ( bytes[i] ^ key[i % key.length] ) & 0x00ff );
    }
    return toChar( bytes );
  }




  /**
   * Not all of the JVM/Platforms support all of its security features; This 
   * method determines if is supported.
   * 
   * @return whether dependent classes can be instantiated on the current
   *         JVM/platform.
   * 
   * @throws Error if any unexpected error encountered whilst checking. Note 
   *         this should not be a ClassNotFoundException, which should cause 
   *         the method to return false.
   */
  public static boolean isSupportedOnJVM() throws LinkageError, ExceptionInInitializerError {
    final String requiredClassname = "javax.net.ssl.SSLServerSocketFactory";
    try {
      Class.forName( requiredClassname );
    } catch ( final ClassNotFoundException e ) {
      return false;
    }
    return true;
  }




  /**
   * Obfuscates the password using a simple and not very secure XOR mechanism.
   * 
   * <p>This should not be used for cryptographical purpose, it's a simple 
   * scrambler to obfuscate clear-text passwords.</p>
   * 
   * @see coyote.commons.network.mqtt.network.SSLSocketFactoryFactory#deObfuscate
   * 
   * @param password The password to be encrypted, as a char[] array.
   * 
   * @return An obfuscated password as a String.
   */
  public static String obfuscate( final char[] password ) {
    if ( password == null ) {
      return null;
    }
    final byte[] bytes = toByte( password );
    for ( int i = 0; i < bytes.length; i++ ) {
      bytes[i] = (byte)( ( bytes[i] ^ key[i % key.length] ) & 0x00ff );
    }
    final String encryptedValue = xorTag + new String( encode( bytes ) );
    return encryptedValue;
  }




  /**
   * Converts an array of ciphers into a single String.
   * 
   * @param ciphers The array of cipher names.
   * 
   * @return A string containing the name of the ciphers, separated by comma.
   */
  public static String packCipherSuites( final String[] ciphers ) {
    String cipherSet = null;
    if ( ciphers != null ) {
      final StringBuffer buf = new StringBuffer();
      for ( int i = 0; i < ciphers.length; i++ ) {
        buf.append( ciphers[i] );
        if ( i < ( ciphers.length - 1 ) ) {
          buf.append( ',' );
        }
      }
      cipherSet = buf.toString();
    }
    return cipherSet;
  }




  /**
   * Convert char array to byte array, where each char is split into two
   * bytes.
   * 
   * @param c char array
   * 
   * @return byte array
   */
  public static byte[] toByte( final char[] c ) {
    if ( c == null ) {
      return null;
    }
    final byte[] b = new byte[c.length * 2];
    int i = 0;
    int j = 0;
    while ( j < c.length ) {
      b[i++] = (byte)( c[j] & 0xFF );
      b[i++] = (byte)( ( c[j++] >> 8 ) & 0xFF );
    }
    return b;
  }




  /**
   * Convert byte array to char array, where each char is constructed from two
   * bytes.
   * 
   * @param b byte array
   * 
   * @return char array
   */
  public static char[] toChar( final byte[] b ) {
    if ( b == null ) {
      return null;
    }
    final char[] c = new char[b.length / 2];
    int i = 0;
    int j = 0;
    while ( i < b.length ) {
      c[j++] = (char)( ( b[i++] & 0xFF ) + ( ( b[i++] & 0xFF ) << 8 ) );
    }
    return c;
  }




  /**
   * Inverse operation of packCipherSuites: converts a string of cipher names
   * into an array of cipher names
   * 
   * @param ciphers A list of ciphers, separated by comma.
   * 
   * @return An array of string, each string containing a single cipher name.
   */
  public static String[] unpackCipherSuites( final String ciphers ) {
    // can't use split as split is not available on all java platforms.
    if ( ciphers == null ) {
      return null;
    }
    final Vector c = new Vector();
    int i = ciphers.indexOf( ',' );
    int j = 0;
    // handle all commas.
    while ( i > -1 ) {
      // add stuff before and up to (but not including) the comma.
      c.add( ciphers.substring( j, i ) );
      j = i + 1; // skip the comma.
      i = ciphers.indexOf( ',', j );
    }
    // add last element after the comma or only element if no comma is present.
    c.add( ciphers.substring( j ) );
    final String[] s = new String[c.size()];
    c.toArray( s );
    return s;
  }




  /**
   * Create new instance of class.
   * 
   * Constructor used by clients.
   */
  public SSLSocketFactoryFactory() {
    configs = new Hashtable();
  }




  /**
   * Checks whether the property keys belong to the supported IBM SSL property
   * key set.
   * 
   * @param properties
   * @throws IllegalArgumentException if any of the properties is not a valid 
   *         IBM SSL property key.
   */
  private void checkPropertyKeys( final Properties properties ) throws IllegalArgumentException {
    final Set keys = properties.keySet();
    final Iterator i = keys.iterator();
    while ( i.hasNext() ) {
      final String k = (String)i.next();
      if ( !keyValid( k ) ) {
        throw new IllegalArgumentException( k + " is not a valid IBM SSL property key." );
      }
    }
  }




  /**
   * Obfuscate any key & trust store passwords within the given properties.
   * 
   * @see coyote.commons.network.mqtt.network.SSLSocketFactoryFactory#obfuscate
   * 
   * @param p properties
   */
  private void convertPassword( final Properties p ) {
    String pw = p.getProperty( KEYSTOREPWD );
    if ( ( pw != null ) && !pw.startsWith( xorTag ) ) {
      final String epw = obfuscate( pw.toCharArray() );
      p.put( KEYSTOREPWD, epw );
    }
    pw = p.getProperty( TRUSTSTOREPWD );
    if ( ( pw != null ) && !pw.startsWith( xorTag ) ) {
      final String epw = obfuscate( pw.toCharArray() );
      p.put( TRUSTSTOREPWD, epw );
    }
  }




  /**
   * Returns an SSL socket factory for the given configuration. 
   * 
   * <p>If no SSLProtocol is already set, uses DEFAULT_PROTOCOL. Throws 
   * IllegalArgumentException if the socket factory could not be created due
   * to underlying configuration problems.</p>
   * 
   * @see coyote.commons.network.mqtt.network.SSLSocketFactoryFactory#DEFAULT_PROTOCOL
   * 
   * @param configID The configuration identifier for selecting a configuration.
   * 
   * @return An SSLSocketFactory
   * 
   * @throws MqttSecurityException
   */
  public SSLSocketFactory createSocketFactory( final String configID ) throws MqttSecurityException {
    final SSLContext ctx = getSSLContext( configID );
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.create_factory", configID != null ? configID : "null (broker defaults)", getEnabledCipherSuites( configID ) != null ? getProperty( configID, CIPHERSUITES, null ) : "null (using platform-enabled cipher suites)" ) );
    return ctx.getSocketFactory();
  }




  /**
   * Returns whether client authentication is required.
   * 
   * @param configID
   *            The configuration identifier for selecting a configuration or
   *            null for the default configuration.
   * @return true, if clients are required to authenticate, false otherwise.
   */
  public boolean getClientAuthentication( final String configID ) {
    final String auth = getProperty( configID, CLIENTAUTH, null );
    boolean res = false;
    if ( auth != null ) {
      res = Boolean.valueOf( auth ).booleanValue();
    }
    return res;
  }




  /**
   * Returns the configuration of the SSLSocketFactoryFactory for a given
   * configuration. Note that changes in the property are reflected in the
   * SSLSocketFactoryFactory.
   * 
   * @param configID
   *            The configuration identifier for selecting a configuration or
   *            null for the default configuration.
   * @return A property object containing the current configuration of the
   *         SSLSocketFactoryFactory.  Note that it could be null.
   */
  public Properties getConfiguration( final String configID ) {
    return (Properties)( configID == null ? defaultProperties : configs.get( configID ) );
  }




  /**
   * Returns an array with the enabled ciphers.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return an array with the enabled ciphers
   */
  public String[] getEnabledCipherSuites( final String configID ) {
    final String ciphers = getProperty( configID, CIPHERSUITES, null );
    final String[] res = unpackCipherSuites( ciphers );
    return res;
  }




  /**
   * Gets the JSSE provider of the indicated configuration
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The JSSE provider.
   */
  public String getJSSEProvider( final String configID ) {
    return getProperty( configID, JSSEPROVIDER, null );
  }




  /**
   * Gets the key manager algorithm that is used.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The key manager algorithm.
   */
  public String getKeyManager( final String configID ) {
    return getProperty( configID, KEYSTOREMGR, SYSKEYMGRALGO );
  }




  /**
   * Gets the name of the keystore file that is used.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The name of the file that contains the keystore.
   */
  public String getKeyStore( final String configID ) {
    final String ibmKey = KEYSTORE;
    final String sysProperty = SYSKEYSTORE;

    String res = null;
    res = getPropertyFromConfig( configID, ibmKey );
    if ( res != null ) {
      return res;
    }

    // scan system property, if it exists.
    if ( sysProperty != null ) {
      res = System.getProperty( sysProperty );
    }

    return res;
  }




  /**
   * Gets the plain-text password that is used for the keystore.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The password in plain text.
   */
  public char[] getKeyStorePassword( final String configID ) {
    final String pw = getProperty( configID, KEYSTOREPWD, SYSKEYSTOREPWD );
    char[] r = null;
    if ( pw != null ) {
      if ( pw.startsWith( xorTag ) ) {
        r = deObfuscate( pw );
      } else {
        r = pw.toCharArray();
      }
    }
    return r;
  }




  /**
   * Gets the keystore provider.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The name of the keystore provider.
   */
  public String getKeyStoreProvider( final String configID ) {
    return getProperty( configID, KEYSTOREPROVIDER, null );
  }




  /**
   * Gets the type of keystore.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The keystore type.
   */
  public String getKeyStoreType( final String configID ) {
    return getProperty( configID, KEYSTORETYPE, SYSKEYSTORETYPE );
  }




  /**
   * Returns the property of a given key or null if it doesn't exist. 
   * 
   * <p>It first scans the indicated configuration, then the default 
   * configuration, then the system properties.</p>
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * @param ibmKey the IBM key for the property
   * @param sysProperty The key for the System property.
   * 
   * @return the property of a given key or null if it doesn't exist.
   */
  private String getProperty( final String configID, final String ibmKey, final String sysProperty ) {
    String res = null;
    res = getPropertyFromConfig( configID, ibmKey );
    if ( res != null ) {
      return res;
    }
    // scan system property, if it exists.
    if ( sysProperty != null ) {
      res = System.getProperty( sysProperty );
    }
    return res;
  }




  /**
   * Returns the property of a given key or null if it doesn't exist. 
   * 
   * <p>It first scans the indicated configuration, then the default 
   * configuration.</p>
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * @param ibmKey the IBM key for the property
   * 
   * @return the property of a given key or null if it doesn't exist. It first
   *         scans the indicated configuration, then the default configuration
   */
  private String getPropertyFromConfig( final String configID, final String ibmKey ) {
    String res = null;
    Properties p = null;
    if ( configID != null ) {
      p = (Properties)configs.get( configID );
    }
    if ( p != null ) {
      res = p.getProperty( ibmKey );
      if ( res != null ) {
        return res;
      }
    }
    // not found in config. try default properties.
    p = defaultProperties;
    if ( p != null ) {
      res = p.getProperty( ibmKey );
      if ( res != null ) {
        return res;
      }
    }
    return res;
  }




  /**
   * Initializes key- and truststore. 
   * 
   * <p>Returns an SSL context factory. If no SSLProtocol is already set, uses 
   * DEFAULT_PROTOCOL.</p>
   * 
   * @see coyote.commons.network.mqtt.network.SSLSocketFactoryFactory#DEFAULT_PROTOCOL
   * 
   * @param configID The configuration ID
   * 
   * @return An SSL context factory.
   * 
   * @throws MqttDirectException
   */
  private SSLContext getSSLContext( final String configID ) throws MqttSecurityException {
    SSLContext ctx = null;

    String protocol = getSSLProtocol( configID );
    if ( protocol == null ) {
      protocol = DEFAULT_PROTOCOL;
    }
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_init", configID != null ? configID : "null (broker defaults)", protocol ) );

    final String provider = getJSSEProvider( configID );
    try {
      if ( provider == null ) {
        ctx = SSLContext.getInstance( protocol );
      } else {
        ctx = SSLContext.getInstance( protocol, provider );
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_load", configID != null ? configID : "null (broker defaults)", ctx.getProvider().getName() ) );

      String keyStoreName = getProperty( configID, KEYSTORE, null );
      KeyStore keyStore = null;
      KeyManagerFactory keyMgrFact = null;
      KeyManager[] keyMgr = null;

      if ( keyStore == null ) {
        if ( keyStoreName == null ) {
          //No keystore in config. Try to get config from system properties.
          keyStoreName = getProperty( configID, KEYSTORE, SYSKEYSTORE );
        }
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_keystore_name", configID != null ? configID : "null (broker defaults)", keyStoreName != null ? keyStoreName : "null" ) );

        final char[] keyStorePwd = getKeyStorePassword( configID );
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_keystore_password", configID != null ? configID : "null (broker defaults)", keyStorePwd != null ? obfuscate( keyStorePwd ) : "null" ) );

        String keyStoreType = getKeyStoreType( configID );
        if ( keyStoreType == null ) {
          keyStoreType = KeyStore.getDefaultType();
        }
        Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_keystore_type", configID != null ? configID : "null (broker defaults)", keyStoreType != null ? keyStoreType : "null" ) );

        String keyMgrAlgo = KeyManagerFactory.getDefaultAlgorithm();
        final String keyMgrProvider = getKeyStoreProvider( configID );
        final String keyManager = getKeyManager( configID );
        if ( keyManager != null ) {
          keyMgrAlgo = keyManager;
        }

        if ( ( keyStoreName != null ) && ( keyStoreType != null ) && ( keyMgrAlgo != null ) ) {
          try {
            keyStore = KeyStore.getInstance( keyStoreType );
            keyStore.load( new FileInputStream( keyStoreName ), keyStorePwd );
            if ( keyMgrProvider != null ) {
              keyMgrFact = KeyManagerFactory.getInstance( keyMgrAlgo, keyMgrProvider );
            } else {
              keyMgrFact = KeyManagerFactory.getInstance( keyMgrAlgo );
            }
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_keystore_algo", configID != null ? configID : "null (broker defaults)", keyMgrAlgo != null ? keyMgrAlgo : "null" ) );
            Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_keystore_provider", configID != null ? configID : "null (broker defaults)", keyMgrFact.getProvider().getName() ) );
            keyMgrFact.init( keyStore, keyStorePwd );
            keyMgr = keyMgrFact.getKeyManagers();
          } catch ( final KeyStoreException e ) {
            throw new MqttSecurityException( e );
          } catch ( final CertificateException e ) {
            throw new MqttSecurityException( e );
          } catch ( final FileNotFoundException e ) {
            throw new MqttSecurityException( e );
          } catch ( final IOException e ) {
            throw new MqttSecurityException( e );
          } catch ( final UnrecoverableKeyException e ) {
            throw new MqttSecurityException( e );
          }
        }
      }
      // now the same for the truststore.
      final String trustStoreName = getTrustStore( configID );
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_truststore_name", configID != null ? configID : "null (broker defaults)", trustStoreName != null ? trustStoreName : "null" ) );
      KeyStore trustStore = null;
      TrustManagerFactory trustMgrFact = null;
      TrustManager[] trustMgr = null;
      final char[] trustStorePwd = getTrustStorePassword( configID );
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_truststore_password", configID != null ? configID : "null (broker defaults)", trustStorePwd != null ? obfuscate( trustStorePwd ) : "null" ) );
      String trustStoreType = getTrustStoreType( configID );
      if ( trustStoreType == null ) {
        trustStoreType = KeyStore.getDefaultType();
      }
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_truststore_type", configID != null ? configID : "null (broker defaults)", trustStoreType != null ? trustStoreType : "null" ) );

      String trustMgrAlgo = TrustManagerFactory.getDefaultAlgorithm();
      final String trustMgrProvider = getTrustStoreProvider( configID );
      final String trustManager = getTrustManager( configID );
      if ( trustManager != null ) {
        trustMgrAlgo = trustManager;
      }

      if ( ( trustStoreName != null ) && ( trustStoreType != null ) && ( trustMgrAlgo != null ) ) {
        try {
          trustStore = KeyStore.getInstance( trustStoreType );
          trustStore.load( new FileInputStream( trustStoreName ), trustStorePwd );
          if ( trustMgrProvider != null ) {
            trustMgrFact = TrustManagerFactory.getInstance( trustMgrAlgo, trustMgrProvider );
          } else {
            trustMgrFact = TrustManagerFactory.getInstance( trustMgrAlgo );
          }

          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_truststore_algo", configID != null ? configID : "null (broker defaults)", trustMgrAlgo != null ? trustMgrAlgo : "null" ) );
          Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "ssl.get_context_truststore_provider", configID != null ? configID : "null (broker defaults)", trustMgrFact.getProvider().getName() ) );
          trustMgrFact.init( trustStore );
          trustMgr = trustMgrFact.getTrustManagers();
        } catch ( final KeyStoreException e ) {
          throw new MqttSecurityException( e );
        } catch ( final CertificateException e ) {
          throw new MqttSecurityException( e );
        } catch ( final FileNotFoundException e ) {
          throw new MqttSecurityException( e );
        } catch ( final IOException e ) {
          throw new MqttSecurityException( e );
        }
      }
      // done.
      ctx.init( keyMgr, trustMgr, null );
    } catch ( final NoSuchAlgorithmException e ) {
      throw new MqttSecurityException( e );
    } catch ( final NoSuchProviderException e ) {
      throw new MqttSecurityException( e );
    } catch ( final KeyManagementException e ) {
      throw new MqttSecurityException( e );
    }
    return ctx;
  }




  /**
   * Gets the SSL protocol variant of the indicated configuration or the
   * default configuration.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   *
   * @return The SSL protocol variant.
   */
  public String getSSLProtocol( final String configID ) {
    return getProperty( configID, SSLPROTOCOL, null );
  }




  /**
   * Gets the trust manager algorithm that is used.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The trust manager algorithm.
   */
  public String getTrustManager( final String configID ) {
    return getProperty( configID, TRUSTSTOREMGR, SYSTRUSTMGRALGO );
  }




  /**
   * Gets the name of the truststore file that is used.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The name of the file that contains the truststore.
   */
  public String getTrustStore( final String configID ) {
    return getProperty( configID, TRUSTSTORE, SYSTRUSTSTORE );
  }




  /**
   * Gets the plain-text password that is used for the truststore.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The password in plain text.
   */
  public char[] getTrustStorePassword( final String configID ) {
    final String pw = getProperty( configID, TRUSTSTOREPWD, SYSTRUSTSTOREPWD );
    char[] r = null;
    if ( pw != null ) {
      if ( pw.startsWith( xorTag ) ) {
        r = deObfuscate( pw );
      } else {
        r = pw.toCharArray();
      }
    }
    return r;
  }




  /**
   * Gets the truststore provider.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The name of the truststore provider.
   */
  public String getTrustStoreProvider( final String configID ) {
    return getProperty( configID, TRUSTSTOREPROVIDER, null );
  }




  /**
   * Gets the type of truststore.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return The truststore type.
   */
  public String getTrustStoreType( final String configID ) {
    return getProperty( configID, TRUSTSTORETYPE, null );
  }




  /**
   * Initializes the SSLSocketFactoryFactory with the provided properties for
   * the provided configuration.
   * 
   * @param props A properties object containing IBM SSL properties that are 
   *        qualified by one or more configuration identifiers.
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @throws IllegalArgumentException if any of the properties is not a valid IBM SSL property key.
   */
  public void initialize( final Properties props, final String configID ) throws IllegalArgumentException {
    checkPropertyKeys( props );
    // copy the properties.
    final Properties p = new Properties();
    p.putAll( props );
    convertPassword( p );
    if ( configID != null ) {
      configs.put( configID, p );
    } else {
      defaultProperties = p;
    }
  }




  /**
   * Checks whether a key belongs to the supported IBM SSL property keys.
   * 
   * @param key
   * 
   * @return whether a key belongs to the supported IBM SSL property keys.
   */
  private boolean keyValid( final String key ) {
    int i = 0;
    while ( i < propertyKeys.length ) {
      if ( propertyKeys[i].equals( key ) ) {
        break;
      }
      ++i;
    }
    return i < propertyKeys.length;
  }




  /**
   * Merges the given IBM SSL properties into the existing configuration,
   * overwriting existing properties. 
   * 
   * <p>This method is used to selectively change properties for a given 
   * configuration. The method throws an IllegalArgumentException if any of the 
   * properties is not a valid IBM SSL property key.</p>
   * 
   * @param props A properties object containing IBM SSL properties
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @throws IllegalArgumentException if any of the properties is not a valid 
   *         IBM SSL property key.
   */
  public void merge( final Properties props, final String configID ) throws IllegalArgumentException {
    checkPropertyKeys( props );
    Properties p = defaultProperties;
    if ( configID != null ) {
      p = (Properties)configs.get( configID );
    }
    if ( p == null ) {
      p = new Properties();
    }
    convertPassword( props );
    p.putAll( props );
    if ( configID != null ) {
      configs.put( configID, p );
    } else {
      defaultProperties = p;
    }

  }




  /**
   * Remove the configuration of a given configuration identifier.
   * 
   * @param configID The configuration identifier for selecting a configuration 
   *        or null for the default configuration.
   * 
   * @return true, if the configuration could be removed.
   */
  public boolean remove( final String configID ) {
    boolean res = false;
    if ( configID != null ) {
      res = configs.remove( configID ) != null;
    } else {
      if ( null != defaultProperties ) {
        res = true;
        defaultProperties = null;
      }
    }
    return res;
  }

  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

  // Base64 Encoding Routines

  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

  // if this string is changed, then the decode method must also be adapted.
  private static final String PWDCHARS_STRING = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final char[] PWDCHARS_ARRAY = PWDCHARS_STRING.toCharArray();




  /**
   * Decode the given base64 string into bytes.
   * 
   * @param string Base64 formatted string
   * 
   * @return the byte represented by the given string
   * 
   * @see #encode(byte[])
   */
  public static byte[] decode( final String string ) {
    final byte[] encoded = string.getBytes();
    final int len = encoded.length;
    final byte[] decoded = new byte[( len * 3 ) / 4];
    int i = 0;
    int j = len;
    int k = 0;
    while ( j >= 4 ) {
      long d = from64( encoded, i, 4 );
      j -= 4;
      i += 4;
      for ( int l = 2; l >= 0; l-- ) {
        decoded[k + l] = (byte)( d & 0xff );
        d = d >> 8;
      }
      k += 3;
    }
    // j==3 | j==2 
    if ( j == 3 ) {
      long d = from64( encoded, i, 3 );
      for ( int l = 1; l >= 0; l-- ) {
        decoded[k + l] = (byte)( d & 0xff );
        d = d >> 8;
      }
    }
    if ( j == 2 ) {
      final long d = from64( encoded, i, 2 );
      decoded[k] = (byte)( d & 0xff );
    }
    return decoded;
  }




  /**
   * Encodes an array of byte into a string of printable ASCII characters using 
   * a base-64 encoding.
   * 
   * @param bytes The array of bytes to e encoded
   * 
   * @return The encoded array.
   * 
   * @see #decode(String)
   */
  public static String encode( final byte[] bytes ) {
    // Allocate a string buffer.
    final int len = bytes.length;
    final StringBuffer encoded = new StringBuffer( ( ( len + 2 ) / 3 ) * 4 );
    int i = 0;
    int j = len;
    while ( j >= 3 ) {
      encoded.append( to64( ( ( ( bytes[i] & 0xff ) << 16 ) | ( bytes[i + 1] & 0xff ) << 8 | bytes[i + 2] & 0xff ), 4 ) );
      i += 3;
      j -= 3;
    }
    // j==2 | j==1 | j==0
    if ( j == 2 ) {
      // there is a rest of 2 bytes. This encodes into 3 chars.
      encoded.append( to64( ( ( bytes[i] & 0xff ) << 8 ) | ( ( bytes[i + 1] & 0xff ) ), 3 ) );
    }
    if ( j == 1 ) {
      // there is a rest of 1 byte. This encodes into 1 char.
      encoded.append( to64( ( ( bytes[i] & 0xff ) ), 2 ) );
    }
    return encoded.toString();
  }




  /**
   * The reverse operation of to64
   */
  private final static long from64( final byte[] encoded, int idx, int size ) {
    long res = 0;
    int f = 0;
    while ( size > 0 ) {
      size--;
      long r = 0;
      // convert encoded[idx] back into a 6-bit value.
      final byte d = encoded[idx++];
      if ( d == '/' ) {
        r = 1;
      }
      if ( ( d >= '0' ) && ( d <= '9' ) ) {
        r = ( 2 + d ) - '0';
      }
      if ( ( d >= 'A' ) && ( d <= 'Z' ) ) {
        r = ( 12 + d ) - 'A';
      }
      if ( ( d >= 'a' ) && ( d <= 'z' ) ) {
        r = ( 38 + d ) - 'a';
      }
      res = res + ( r << f );
      f += 6;
    }
    return res;
  }




  /**
   * Translates an input integer into a string of the given length.
   */
  private final static String to64( long input, int size ) {
    final StringBuffer result = new StringBuffer( size );
    while ( size > 0 ) {
      size--;
      result.append( PWDCHARS_ARRAY[( (int)( input & 0x3f ) )] );
      input = input >> 6;
    }
    return result.toString();
  }

}
