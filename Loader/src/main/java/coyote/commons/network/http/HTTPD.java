/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import coyote.commons.StringUtil;
import coyote.commons.network.IpAcl;
import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.commons.network.http.auth.DefaultAuthProvider;
import coyote.commons.security.OperationFrequency;
import coyote.dataframe.DataField;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * This is the core of the HTTP Server.
 * 
 * <p>This class should be sub-classed and the {@link #serve(IHTTPSession)} 
 * method overridden to serve the request.
 */
public abstract class HTTPD {
  public static final String CLASS = "HTTPD";
  public static final long EVENT = Log.getCode( CLASS );

  private static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";

  static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile( CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE );

  private static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";

  static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile( CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE );

  private static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";

  static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile( CONTENT_DISPOSITION_ATTRIBUTE_REGEX );

  /**
   * Maximum time to wait on Socket.getInputStream().read() (in milliseconds)
   * This is required as the Keep-Alive HTTP connections would otherwise block
   * the socket reading thread forever (or as long the browser is open).
   */
  public static final int SOCKET_READ_TIMEOUT = 5000;

  /** 
   * Our IP address Access Control List. It is set to deny everything unless 
   * addresses match the entries in this list. 
   */
  final IpAcl acl = new IpAcl( IpAcl.DENY );

  /**
   * This is our Denial of Service tracker. It keeps a list of times a request
   * is made and if requests come in to frequently from an address or network,
   * the server can perform remediation such as blacklisting throttling and  of
   * course security event notification.
   */
  final OperationFrequency dosTable = new OperationFrequency();

  /**
   * The component responsible for providing authentication and authorization
   * processing for the server.
   */
  protected AuthProvider authProvider = new DefaultAuthProvider();

  /**
   * Pseudo-Parameter to use to store the actual query string in the
   * parameters map for later re-processing.
   */
  protected static final String QUERY_STRING_PARAMETER = "Httpd.QUERY_STRING";
  private static final String MIMETYPE_RESOURCE = "httpd/mimetypes.properties";

  /** Hashtable mapping file extension to MIME type */
  protected static Map<String, String> MIME_TYPES;

  final String hostname;
  final int myPort;

  volatile ServerSocket myServerSocket;

  private ServerSocketFactory serverSocketFactory = new DefaultServerSocketFactory();

  private Thread myThread;

  protected Executor asyncRunner;

  CacheManagerFactory cacheManagerFactory;




  /**
   * Decode parameters from a URL, handing the case where a single parameter
   * name might have been supplied several times, by return lists of values.
   * 
   * <p>In general these lists will contain a single element.</p>
   * 
   * @param parms original HTTPD parameters values, as passed to the 
   *        {@code serve()} method.
   * @return a map of {@code String} (parameter name) to
   *         {@code List<String>} - a list of the values supplied.
   */
  protected static Map<String, List<String>> decodeParameters( final Map<String, String> parms ) {
    return decodeParameters( parms.get( HTTPD.QUERY_STRING_PARAMETER ) );
  }




  /**
   * Decode parameters from a URL, handing the case where a single parameter
   * name might have been supplied several times, by return lists of values.
   * 
   * <p>In general these lists will contain a single element.</p>
   * 
   * @param queryString a query string pulled from the URL.
   * @return a map of {@code String} (parameter name) to 
   *         {@code List<String>} (a list of the values supplied).
   */
  protected static Map<String, List<String>> decodeParameters( final String queryString ) {
    final Map<String, List<String>> parms = new HashMap<String, List<String>>();
    if ( queryString != null ) {
      final StringTokenizer st = new StringTokenizer( queryString, "&" );
      while ( st.hasMoreTokens() ) {
        final String e = st.nextToken();
        final int sep = e.indexOf( '=' );
        final String propertyName = sep >= 0 ? decodePercent( e.substring( 0, sep ) ).trim() : decodePercent( e ).trim();
        if ( !parms.containsKey( propertyName ) ) {
          parms.put( propertyName, new ArrayList<String>() );
        }
        final String propertyValue = sep >= 0 ? decodePercent( e.substring( sep + 1 ) ) : null;
        if ( propertyValue != null ) {
          parms.get( propertyName ).add( propertyValue );
        }
      }
    }
    return parms;
  };




  /**
   * Decode percent encoded {@code String} values.
   * 
   * @param str the percent encoded {@code String}
   * 
   * @return expanded form of the input, for example "foo%20bar" becomes
   *         "foo bar"
   */
  protected static String decodePercent( final String str ) {
    String decoded = null;
    try {
      decoded = URLDecoder.decode( str, "UTF8" );
    } catch ( final UnsupportedEncodingException ignored ) {
      Log.append( EVENT, "Encoding not supported, ignored", ignored );
    }
    return decoded;
  }




  /**
    * Get MIME type from file name extension, if possible
    * 
    * @param uri the string representing a file
    * 
    * @return the connected mime/type
    */
  public static String getMimeTypeForFile( final String uri ) {
    return MimeType.get( uri ).get( 0 ).getType();
  }




  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and an
   * array of loaded KeyManagers. These objects must properly
   * loaded/initialized by the caller.
   */
  public static SSLServerSocketFactory makeSSLSocketFactory( final KeyStore loadedKeyStore, final KeyManager[] keyManagers ) throws IOException {
    SSLServerSocketFactory res = null;
    try {
      final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
      trustManagerFactory.init( loadedKeyStore );
      final SSLContext ctx = SSLContext.getInstance( "TLS" );
      ctx.init( keyManagers, trustManagerFactory.getTrustManagers(), null );
      res = ctx.getServerSocketFactory();
    } catch ( final Exception e ) {
      throw new IOException( e.getMessage() );
    }
    return res;
  }




  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and a
   * loaded KeyManagerFactory. These objects must properly loaded/initialized
   * by the caller.
   */
  public static SSLServerSocketFactory makeSSLSocketFactory( final KeyStore loadedKeyStore, final KeyManagerFactory loadedKeyFactory ) throws IOException {
    try {
      return makeSSLSocketFactory( loadedKeyStore, loadedKeyFactory.getKeyManagers() );
    } catch ( final Exception e ) {
      throw new IOException( e.getMessage() );
    }
  }




  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a KeyStore resource with your
   * certificate and passphrase
   */
  public static SSLServerSocketFactory makeSSLSocketFactory( final String keyAndTrustStoreClasspathPath, final char[] passphrase ) throws IOException {
    try {
      final KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
      final InputStream keystoreStream = HTTPD.class.getResourceAsStream( keyAndTrustStoreClasspathPath );

      if ( keystoreStream == null ) {
        throw new IOException( "Unable to load keystore from classpath: " + keyAndTrustStoreClasspathPath );
      }

      keystore.load( keystoreStream, passphrase );
      final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
      keyManagerFactory.init( keystore, passphrase );
      return makeSSLSocketFactory( keystore, keyManagerFactory );
    } catch ( final Exception e ) {
      throw new IOException( e.getMessage() );
    }
  }




  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static List<MimeType> getMimeTypes( String filename ) {

    if ( MIME_TYPES == null ) {
      // since the mimetype map is null, we apparently have not initialized yet 
      MIME_TYPES = new HashMap<String, String>();
      try {
        // try to load the custom mimetypes
        final Enumeration<URL> resources = HTTPD.class.getClassLoader().getResources( MIMETYPE_RESOURCE );
        while ( resources.hasMoreElements() ) {
          final URL url = resources.nextElement();
          final Properties properties = new Properties();
          InputStream stream = null;
          try {
            stream = url.openStream();
            properties.load( url.openStream() );
          } catch ( final IOException e ) {
            Log.append( EVENT, "Could not load custom mimetypes from " + url, e );
          }
          finally {
            safeClose( stream );
          }
          // put all the found types in the map
          MIME_TYPES.putAll( (Map)properties );

          // go through all the new types and add them to the static mapping
          for ( String key : MIME_TYPES.keySet() ) {
            String value = MIME_TYPES.get( key );
            if ( StringUtil.isNotBlank( value ) ) {
              MimeType.add( key, value, false );
            }
          }
        }
      } catch ( final IOException e ) {
        Log.append( EVENT, "no mime types available at " + MIMETYPE_RESOURCE );
      }
    }

    // return the list of MimeTypes for this filename
    return MimeType.get( filename );
  }




  static final void safeClose( final Object closeable ) {
    try {
      if ( closeable != null ) {
        if ( closeable instanceof Closeable ) {
          ( (Closeable)closeable ).close();
        } else if ( closeable instanceof Socket ) {
          ( (Socket)closeable ).close();
        } else if ( closeable instanceof ServerSocket ) {
          ( (ServerSocket)closeable ).close();
        } else {
          throw new IllegalArgumentException( "Unknown object to close" );
        }
      }
    } catch ( final IOException e ) {
      Log.append( EVENT, "Could not close", e );
    }
  }




  /**
   * Constructs an HTTP server on given port.
   */
  public HTTPD( final int port ) {
    this( null, port );
  }




  /**
   * Constructs an HTTP server on given hostname and port.
   */
  public HTTPD( final String hostname, final int port ) {
    this.hostname = hostname;
    myPort = port;
    setCacheManagerFactory( new DefaultCacheManagerFactory() );
    setAsyncRunner( new DefaultExecutor() );
    Log.append( EVENT, "Server initialized on port " + myPort );
  }




  /**
   * Add an entry into this servers IP Access Control List.
   * 
   * @param network the network address for the entry
   * @param allowed true to allow any address matching the given network to 
   *        access the server, false to reject the socket connection from any 
   *        address matching the network
   */
  public void addToACL( final IpNetwork network, final boolean allowed ) {
    if ( network != null ) {
      acl.add( network, allowed );
    }
  }




  /**
   * Changes the default allow mode of the IP Access Control List. 
   * 
   * <p>This is what the check will return if it does not find an explicit rule 
   * to match against.</p>
   * 
   * <p>Setting this to TRUE, turns the IP Access Control List into a blacklist 
   * which allows everything unless it is granted access by an entry in this 
   * list. Setting this to FALSE turns this into a whitelist which denies 
   * everything unless it is allows by an entry on this list. This is the 
   * default for the server and should not be changed unless there is a very 
   * specific need.</p>
   * 
   * @param allow The new default mode: True = allow by default, false = deny 
   *              by default.
   */
  protected void setDefaultAllow( final boolean allow ) {
    acl.setDefaultAllow( allow );
  }




  /**
   * @return the port on which this server was requested to run.
   * 
   * @see #getListeningPort()
   */
  public int getPort() {
    return myPort;
  }




  /**
   * Forcibly closes all connections that are open.
   */
  public synchronized void closeAllConnections() {
    stop();
  }




  /**
   * Create a instance of the client handler, subclasses can return a subclass
   * of the ClientHandler.
   * 
   * @param finalAccept the socket the client is connected to
   * @param inputStream the input stream
   * @param secured flag indicating if the connection is over an encrypted (secured) channel 
   * 
   * @return the client handler
   */
  protected ClientHandler createClientHandler( final Socket finalAccept, final InputStream inputStream, final boolean secured ) {
    return new ClientHandler( this, inputStream, finalAccept, secured );
  }




  /**
   * Instantiate the server runnable, can be overwritten by subclasses to
   * provide a subclass of the ServerRunnable.
   * 
   * @param timeout the socket timeout to use.
   * 
   * @return the server runnable.
   */
  protected ServerRunnable createServerRunnable( final int timeout ) {
    return new ServerRunnable( this, timeout );
  }




  public String getHostname() {
    return hostname;
  }




  /**
   * @return return the port on which this server is <i>actually</i> listening. May be -1 for an inactive socket.
   * 
   * @see #getPort()
   */
  public final int getListeningPort() {
    return myServerSocket == null ? -1 : myServerSocket.getLocalPort();
  }




  public ServerSocketFactory getServerSocketFactory() {
    return serverSocketFactory;
  }




  public CacheManagerFactory getCacheManagerFactory() {
    return cacheManagerFactory;
  }




  public final boolean isAlive() {
    return wasStarted() && !myServerSocket.isClosed() && myThread.isAlive();
  }




  /**
   * Call before {@code start()} to serve over HTTPS instead of HTTP
   */
  public void makeSecure( final SSLServerSocketFactory sslServerSocketFactory, final String[] sslProtocols ) {
    serverSocketFactory = new SecureServerSocketFactory( sslServerSocketFactory, sslProtocols );
  }




  /**
   * Override this to customize the server.
   * 
   * <p>This returns a 404 "Not Found" plain text error response.</p>
   * 
   * @param session The HTTP session
   * 
   * @return HTTP response, see class Response for details
   * @throws SecurityResponseException if processing the request generated a security exception
   */
  public Response serve( final IHTTPSession session ) throws SecurityResponseException {
    final Method method = session.getMethod();
    if ( Method.PUT.equals( method ) || Method.POST.equals( method ) ) {
      try {
        session.parseBody(); // this is not really necessary but here for demonstration
      } catch ( final IOException ioe ) {
        return Response.createFixedLengthResponse( Status.INTERNAL_ERROR, MimeType.TEXT.getType(), "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage() );
      } catch ( final ResponseException re ) {
        return Response.createFixedLengthResponse( re.getStatus(), MimeType.TEXT.getType(), re.getMessage() );
      }
    }

    final Map<String, String> parms = session.getParms();
    parms.put( HTTPD.QUERY_STRING_PARAMETER, session.getQueryParameterString() );

    return Response.createFixedLengthResponse( Status.NOT_FOUND, MimeType.TEXT.getType(), "Not Found" );
  }




  /**
   * Pluggable strategy for asynchronously executing requests.
   * 
   * @param asyncRunner strategy for handling threads.
   */
  public void setAsyncRunner( final Executor asyncRunner ) {
    this.asyncRunner = asyncRunner;
  }




  public void setServerSocketFactory( final ServerSocketFactory serverSocketFactory ) {
    this.serverSocketFactory = serverSocketFactory;
  }




  /**
   * Pluggable strategy for creating and cleaning up temporary files.
   * 
   * @param factory new strategy for handling temp files.
   */
  public void setCacheManagerFactory( final CacheManagerFactory factory ) {
    cacheManagerFactory = factory;
  }




  /**
   * Start the server.
   * 
   * @throws IOException if the socket is in use.
   */
  public void start() throws IOException {
    start( HTTPD.SOCKET_READ_TIMEOUT );
  }




  /**
   * Starts the server (in setDaemon(true) mode).
   */
  public void start( final int timeout ) throws IOException {
    start( timeout, true );
  }




  /**
   * Start the server.
   * 
   * @param timeout timeout to use for socket connections.
   * @param daemon start the thread daemon or not.
   * 
   * @throws IOException if the socket is in use.
   */
  public void start( final int timeout, final boolean daemon ) throws IOException {
    myServerSocket = getServerSocketFactory().create();
    myServerSocket.setReuseAddress( true );

    final ServerRunnable serverRunnable = createServerRunnable( timeout );
    myThread = new Thread( serverRunnable );
    myThread.setDaemon( daemon );
    myThread.setName( "HTTPD Listener" );
    myThread.start();
    while ( !serverRunnable.isBoundToPort && ( serverRunnable.bindException == null ) ) {
      try {
        Thread.sleep( 10L );
      } catch ( final Throwable e ) {
        // on some platforms (e.g. mobile devices) this may not be allowed, 
        // that is why we catch throwable. This should happen right away 
        // because we are just waiting for the socket to bind.
      }
    }
    if ( serverRunnable.bindException != null ) {
      throw serverRunnable.bindException;
    }
  }




  /**
   * Stop the server.
   */
  public void stop() {
    Log.append( EVENT, "Server terminating" );
    try {
      safeClose( myServerSocket );
      asyncRunner.closeAll();
      if ( myThread != null ) {
        myThread.join();
      }
    } catch ( final Exception e ) {
      Log.append( EVENT, "WARN: Could not stop all connections", e );
    }
    Log.append( EVENT, "Server termination complete" );
  }




  /**
   * @return true if the gzip compression should be used if the client accespts 
   *         it. Default this option is on for text content and off for 
   *         everything. Override this for custom semantics.
   */
  @SuppressWarnings("static-method")
  protected boolean useGzipWhenAccepted( final Response r ) {
    return ( r.getMimeType() != null ) && r.getMimeType().toLowerCase().contains( "text/" );
  }




  public final boolean wasStarted() {
    return ( myServerSocket != null ) && ( myThread != null );
  }




  /**
   * @return the component responsible for providing authentication and 
   *         authorization processing to the server.
   */
  public AuthProvider getAuthProvider() {
    return authProvider;
  }




  /**
   * @param provider the component responsible for providing authentication 
   *        and authorization processing to the server
   */
  public void setAuthProvider( AuthProvider provider ) {
    authProvider = provider;
  }




  /**
  * Parse the IP Access Control List section of the configuration.
  * 
  * <p>This allows the operator to define access control rules for the server.
  * When the connection is accepted by the server, the remote address is 
  * checked against the IP ACL. If the ACL denies access, the socket is closed 
  * and the request is not passed to a thread for processing.
  * 
  * <p>The following is an example configuration:<pre>
  * "IPACL":{
  *   "default": "deny",
  *   "127.0.0.1": "allow",
  *   "172.28.147.6/0": "allow",
  *   "192.168/16": "allow",
  *   "10/8": "deny"
  * },</pre>
  * 
  * <p>If a network does not contain a / character, it is assumed to be an 
  * address and will be converted to a /0 network which is a network with no 
  * netmask (i.e. a single address network).
  * 
  * <p>The {@code default} rule allow the operator to turn the ACL into either 
  * a whitelist or a blacklist. The default is a whitelist where all 
  * connections are denied by default. A blacklist is created by setting 
  * default to "allow" which allows everything unless specifically denied by a 
  * rule. The ACL is set to a whitelist ({@code "default":"deny"}) by 
  * default.
  * 
  * <p>Order is important. The first rule matching the address is what is used
  * to determine access. Therefore, place all /0 networks first in the list 
  * then broader networks later to make sure evaluation occurs as 
  * expected.
  * 
  * <p>For example, if you want to include everyone in the 192.168/16 network 
  * except for those in the 192.168.100/24 network, your configuration should 
  * look like this:<pre>
  * "IPACL":{
  *   "192.168.100/24": "deny",
  *   "192.168/16": "allow"
  * },</pre>
  * 
  * @param cfg The configuration to parse
  */
  public void configIpACL( Config cfg ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String network = field.getName();
        String access = field.getStringValue();

        IpNetwork ipNet = null;
        if ( StringUtil.isNotBlank( network ) ) {

          if ( "DEFAULT".equalsIgnoreCase( network.trim() ) ) {
            if ( StringUtil.isNotBlank( access ) ) {
              setDefaultAllow( IpAcl.ALLOW_TAG.equalsIgnoreCase( access.trim() ) );
            } else {
              Log.debug( "Blank access on default ACL rule" );
            }
          } else {

            try {
              ipNet = new IpNetwork( network );
            } catch ( IpAddressException e ) {
              // maybe it is an address
              try {
                IpAddress ipAdr = new IpAddress( network );
                ipNet = new IpNetwork( ipAdr.toString() + "/0" );
              } catch ( IpAddressException ex ) {
                Log.error( "Invalid network specification '" + network + "' - " + ex.getMessage() );
                ipNet = null;
              }
            }

            if ( ipNet != null ) {
              boolean allows = true;
              if ( StringUtil.isNotBlank( access ) ) {
                allows = IpAcl.ALLOW_TAG.equalsIgnoreCase( access.trim() );
              }
              Log.append( EVENT, "Adding " + ipNet + " to IP Access Control List with allows = " + allows );
              addToACL( ipNet, allows );
            } else {
              Log.error( "Network: " + ipNet + " not added to IP Access Control List" );
            }

          } // if default

        } else {
          Log.debug( "No network or address" );
        }

      } // for each field
    }

  }




  /**
   * @return the IP Access Control List
   */
  public IpAcl getIpAcl() {
    return acl;
  }




  /**
   * <pre>
   * "Frequency":{
   *   "default": { "Requests": 10, "Interval": 1000, "Threshold": 3, "Window": 3000, "Breach": "blacklist"},
   *   "192.168.100/24": { "Requests": 10, "Interval": 1000, "Threshold": 3, "Window": 3000, "Breach": "throttle:3000"}
   *  }</pre>
   *  requests are the number of requests to allow in an interval
   *  interval is the number of milliseconds in the interval
   *  threshold is the number of failures allowed in a window of time
   *  window is the number of milliseconds in the window
   *  breach is what to do when the number of failures are exceeded in the window; options are 
   *   - Blacklist the IP
   *   - Throttle the connection for the amount of milliseconds but allow it after waiting
   *   - FUTURE: Retract shutdown the server for the number of milliseconds then restart
   *   - FUTURE: Terminate terminate the server
   * @param cfg
   */
  public void configDosTables( Config cfg ) {
    // TODO: Make this work
  }

}
