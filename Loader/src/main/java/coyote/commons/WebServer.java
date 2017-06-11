/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import coyote.commons.NetUtil;
import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.commons.network.IpAddress;
import coyote.commons.network.IpNetwork;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTP;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SecurityResponseException;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.AbstractLoader;
import coyote.loader.ConfigTag;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.component.AbstractManagedComponent;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This starts a configurable web server.
 * 
 * <p>This is a specialization of a Loader which loads a HTTP server and keeps
 * it running in memory.
 * 
 * <p>As an extension of the AbstractLoader, this also supports the loading of 
 * components, all of which will have a reference to this loader/webserver so 
 * it can use this as a coordination point for operations if necessary.
 * 
 * <p>All routes and handlers are specified in the configuration. This does 
 * not serve anything by default.
 */
public class WebServer extends AbstractLoader {
  /** Tag used in various class identifying locations. */
  public static final String NAME = WebServer.class.getSimpleName();

  /** The default port on which this listens */
  private static final int DEFAULT_PORT = 80;

  /** The port on which we should bind as specified from the command line - overrides all, even configuration file */
  private int bindPort = -1;

  /** Our main server */
  private HTTPDRouter server = null;

  /** Server on a normal port which sends a redirect to our main server. (E.g., any http: requests are redirected to https:) */
  private HTTPD redirectServer = null;

  /** The version of this server. */
  public static final Version VERSION = new Version( 0, 0, 3, Version.DEVELOPMENT );

  // the port on which this server listens, defaults to 80
  private static final String PORT = "Port";

  // Perform a redirect for all requests to this port to the port on which we are listening. Normally set to 80 when the port is 443
  private static final String REDIRECT_PORT = "RedirectPort";

  // indicates SSL should be enabled; automatically enable when port=443
  private static final String SECURESERVER = "SecureServer";

  private static final String ENABLE_ARM = "EnableARM";
  private static final String ENABLE_GAUGES = "EnableGauges";
  private static final String ENABLE_TIMING = "EnableTiming";

  // mapping attributes
  private static final String MAPPINGS = "Mappings";
  private static final String CLASS = "Class";
  private static final String PRIORITY = "Priority";

  // command line argument for the port on which we should bind
  private static final String PORT_ARG = "-p";




  /**
   * @see coyote.loader.AbstractLoader#configure(coyote.loader.cfg.Config)
   */
  @Override
  public void configure( Config cfg ) throws ConfigurationException {
    super.configure( cfg );

    // command line argument override all other configuration settings
    parseArgs( getCommandLineArguments() );

    int redirectport = 0;

    // we need to get the port first as part of the server constructor
    int port = DEFAULT_PORT; // set default

    if ( cfg != null ) {

      if ( cfg.containsIgnoreCase( PORT ) ) {
        try {
          port = cfg.getInt( PORT );
          port = NetUtil.validatePort( port );
          if ( port == 0 ) {
            Log.error( "Configured port of " + port + " is not a valid port (out of range) - ignoring" );
            port = DEFAULT_PORT;
          }
        } catch ( NumberFormatException e ) {
          port = DEFAULT_PORT;
          Log.error( "Port configuration option was not a valid integer - ignoring" );
        }
      }

      if ( cfg.containsIgnoreCase( REDIRECT_PORT ) ) {
        try {
          redirectport = cfg.getInt( REDIRECT_PORT );
          redirectport = NetUtil.validatePort( redirectport );
          if ( redirectport == 0 ) {
            Log.error( "Configured port of " + redirectport + " is not a valid port (out of range) - ignoring" );
            redirectport = 0;
          }
        } catch ( NumberFormatException e ) {
          redirectport = 0;
          Log.error( "RedirectPort configuration option was not a valid integer - ignoring" );

        }

      }

      boolean secureServer;
      try {
        secureServer = cfg.getAsBoolean( SECURESERVER );
      } catch ( DataFrameException e1 ) {
        secureServer = false;
      }

      // If we have a valid bind port from the command line arguments, use it instead of configured port
      if ( bindPort > 0 && bindPort != port ) {
        Log.warn( "Command line argument of port " + bindPort + " overrides configuration port of " + port );
        port = bindPort;
      }

      // create a server with the default mappings
      server = new HTTPDRouter( port );

      if ( port == 443 || secureServer ) {
        try {
          server.makeSecure( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), null );
        } catch ( IOException e ) {
          Log.error( "Could not make the server secure: " + e.getMessage() );
        }
      }

      // At this point the servers are up, but nothing is being served.
      // Configure security, and add handlers; in that order

      if ( cfg != null ) {
        Config sectn = cfg.getSection( GenericAuthProvider.AUTH_SECTION );
        if ( sectn != null ) {
          server.setAuthProvider( new GenericAuthProvider( sectn ) );
        }

        // configure the IPACL with any found configuration data; 
        // localhost only access if no configuration data is found
        server.configIpACL( cfg.getSection( ConfigTag.IPACL ) );

        // Configure Denial of Service frequency tables
        server.configDosTables( cfg.getSection( ConfigTag.FREQUENCY ) );

        // Add the default routes to ensure basic operation
        server.addDefaultRoutes();

        // remove the root handlers, the configuration will contain our handlers
        server.removeRoute( "/" );
        server.removeRoute( "/index.html" );

        List<Config> mapsections = cfg.getSections( MAPPINGS );
        for ( Config section : mapsections ) {
          for ( DataField field : section.getFields() ) {
            if ( field.getName() != null && field.isFrame() ) {
              loadMapping( field.getName(), new Config( (DataFrame)field.getObjectValue() ) );
            }
          }
        }
        // if we have no components defined, install a wedge to keep the server open
        if ( components.size() == 0 ) {
          Wedge wedge = new Wedge();
          wedge.setLoader( this );
          components.put( wedge, cfg );
          activate( wedge, cfg ); // activate it
        }

        // configure the server to use our statistics board
        server.setStatBoard( getStats() );

        // Configure the statistics board to enable collecting metrics
        try {
          getStats().enableArm( cfg.getAsBoolean( ENABLE_ARM ) );
        } catch ( DataFrameException e ) {
          getStats().enableArm( false );
        }
        try {
          getStats().enableGauges( cfg.getAsBoolean( ENABLE_GAUGES ) );
        } catch ( DataFrameException e ) {
          getStats().enableGauges( false );
        }
        try {
          getStats().enableTiming( cfg.getAsBoolean( ENABLE_TIMING ) );
        } catch ( DataFrameException e ) {
          getStats().enableTiming( false );
        }

        // Set our version in the stats board
        getStats().setVersion( NAME, VERSION );

        if ( redirectport > 0 ) {
          redirectServer = new RedirectServer( redirectport );
        }

        Log.info( "Configured server with " + server.getMappings().size() + " mappings" );
      }
    } else {
      Log.fatal( "No configuration passed to server" );
    } // if there is a cfg

  }




  /**
   * @param args
   */
  private void parseArgs( String[] args ) {
    if ( args != null && args.length > 0 ) {
      for ( int x = 0; x < args.length; x++ ) {
        if ( PORT_ARG.equalsIgnoreCase( args[x] ) ) {
          try {
            bindPort = Integer.parseInt( args[x + 1] );
            Log.info( "Binding to port " + bindPort + " as specified on the command line" );
            bindPort = NetUtil.validatePort( bindPort );
            if ( bindPort == 0 ) {
              Log.error( "Command line port argument '" + args[x + 1] + "' is not a valid port (out of range) - ignoring" );
            }
          } catch ( NumberFormatException e ) {
            Log.error( "Command line port argument '" + args[x + 1] + "' is not a valid integer - ignoring" );
          }
        }
      }
    }
  }




  /**
   * Load the mapping represented in the given configuration into the server.
   * 
   * <p>Init Parameter:<ol><li>this server<li>configuration
   * 
   * @param route the route regex to map in the router
   * @param config the configuration of the route handler with at least a class attribute
   */
  private void loadMapping( String route, Config config ) {
    // if we have a route
    if ( StringUtil.isNotEmpty( route ) ) {
      // pull out the class name
      String className = config.getString( CLASS );

      // get the priority of the routing
      int priority = 0;
      if ( config.contains( PRIORITY ) ) {
        try {
          priority = config.getAsInt( PRIORITY );
        } catch ( DataFrameException e ) {
          Log.warn( "Problems parsing mapping priority into a numeric value for rout '" + route + "' using default" );
        }
      }

      // If we found a class to map to the route
      if ( StringUtil.isNotBlank( className ) ) {
        try {
          // load the class
          Class<?> clazz = Class.forName( className );
          Log.info( "Loading " + className + " to handle requests for '" + route + "'" );
          if ( priority > 0 ) {
            server.addRoute( route, priority, clazz, this, config );
          } else {
            server.addRoute( route, clazz, this, config );
          }
        } catch ( Exception e ) {
          Log.warn( "Problems adding route mapping '" + route + "', handler: " + className + " Reason:" + e.getClass().getSimpleName() );
        }
      } else {
        Log.warn( "No class defined in mapping for '" + route + "' - " + config );
      }
    } else {
      Log.warn( "No route specified in mapping for " + config );
    }
  }




  /**
   * Start the components running.
   * 
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {
    // only start once, this is not foolproof as the active flag is set only
    // when the watchdog loop is entered
    if ( isActive() ) {
      return;
    }
    Log.info( "Running server with " + server.getMappings().size() + " mappings" );
    try {
      server.start( HTTPD.SOCKET_READ_TIMEOUT, false );
    } catch ( IOException ioe ) {
      Log.append( HTTPD.EVENT, "ERROR: Could not start server on port '" + server.getPort() + "' - " + ioe.getMessage() );
      System.err.println( "Couldn't start server:\n" + ioe );
      System.exit( 1 );
    }

    if ( redirectServer != null ) {
      try {
        redirectServer.start( HTTPD.SOCKET_READ_TIMEOUT, false );
      } catch ( IOException ioe ) {
        Log.append( HTTPD.EVENT, "ERROR: Could not start redirection server on port '" + redirectServer.getPort() + "' - " + ioe.getMessage() );
        System.err.println( "Couldn't start redirection server:\n" + ioe );
      }
    }

    // Save the name of the thread that is running this class
    final String oldName = Thread.currentThread().getName();

    // Rename this thread to the name of this class
    Thread.currentThread().setName( NAME );

    // very important to get park(millis) to operate
    current_thread = Thread.currentThread();

    // Parse through the configuration and initialize all the components
    initComponents();
    Log.info( LogMsg.createMsg( MSG, "Loader.components_initialized" ) );

    final StringBuffer b = new StringBuffer( NAME );
    b.append( " v" );
    b.append( VERSION.toString() );
    b.append( " initialized - Loader:" );
    b.append( Loader.API_NAME );
    b.append( " v" );
    b.append( Loader.API_VERSION );
    b.append( " - Runtime: " );
    b.append( System.getProperty( "java.version" ) );
    b.append( " (" );
    b.append( System.getProperty( "java.vendor" ) );
    b.append( ")" );
    b.append( " - Platform: " );
    b.append( System.getProperty( "os.arch" ) );
    b.append( " OS: " );
    b.append( System.getProperty( "os.name" ) );
    b.append( " (" );
    b.append( System.getProperty( "os.version" ) );
    b.append( ")" );
    Log.info( b );

    // enter a loop performing watchdog and maintenance functions
    watchdog();

    // The watchdog loop has exited, so we are done processing
    terminateComponents();

    Log.info( LogMsg.createMsg( MSG, "Loader.terminated" ) );

    // Rename the thread back to what it was called before we were being run
    Thread.currentThread().setName( oldName );

  }




  /**
   * Shut everything down when the JRE terminates.
   * 
   * <p>There is a shutdown hook registered with the JRE when this Service is
   * loaded. The shutdown hook will call this method when the JRE is 
   * terminating so that the Service can terminate any long-running processes.
   * 
   * <p>Note: this is different from {@code close()} but {@code shutdown()} 
   * will normally result in {@code close()} being invoked at some point.
   * 
   * @see coyote.loader.thread.ThreadJob#shutdown()
   */
  @Override
  public void shutdown() {
    // call the threadjob shutdown to exit the watchdog routine
    super.shutdown();

    // shutdown the servers
    if ( server != null ) {
      server.stop();
    }
    if ( redirectServer != null ) {
      redirectServer.stop();
    }
  }




  /**
   * Add the given IP address to the server blacklist.
   * 
   * <p>This results in any TCP connection from this address being dropped by 
   * the HTTPD server before any HTTP processing.
   * 
   * @param address The address to ban from this server
   */
  public synchronized void blacklist( IpAddress address ) {
    blacklist( new IpNetwork( address, IpNetwork.HOSTMASK ) );
  }




  /**
   * Add the given IP network to the server blacklist.
   * 
   * <p>This results in any TCP connection from any address in this network 
   * being dropped by the HTTPD server before any HTTP processing.
   * 
   * @param network The address to ban from this server
   */
  public synchronized void blacklist( IpNetwork network ) {
    server.addToACL( network, false );
    if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
      Log.append( HTTPD.EVENT, "Blacklisted " + network.toString() );
      Log.append( HTTPD.EVENT, "ACL: " + server.getIpAcl().toString() );
    }
  }

  //

  //

  //

  /**
   * Keep the server watchdog busy if there are no components to run.
   * 
   * <p>BTW, This is an example of the simplest runnable component a Loader 
   * can manage. Initialize it, continually calling doWork() while the loader
   * is running then call terminate() when the loader shuts down.
   */
  private class Wedge extends AbstractManagedComponent implements ManagedComponent {

    @Override
    public void initialize() {
      setIdleWait( 5000 );
      setIdle( true );
    }




    @Override
    public void doWork() {}




    @Override
    public void terminate() {}

  }

  /**
   * Listens on a particular port and sends a redirect for the same URL to the 
   * secure port.
   */
  private class RedirectServer extends HTTPD {
    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";




    public RedirectServer( int port ) {
      super( port );
    }




    /**
     * Perform a case insensitive search for a header with a given name and 
     * return its value if found.
     * 
     * @param name the name of the request header to query
     * @param session the session containing the request headers
     * 
     * @return the value in the header or null if that header was not found in 
     *         the session.
     */
    private String findRequestHeaderValue( String name, IHTTPSession session ) {
      if ( StringUtil.isNotBlank( name ) && session != null && session.getRequestHeaders() != null ) {
        final Set<Map.Entry<String, String>> entries = session.getRequestHeaders().entrySet();
        for ( Map.Entry<String, String> header : entries ) {
          if ( name.equalsIgnoreCase( header.getKey() ) )
            return header.getValue();
        }
      }
      return null;
    }




    /**
     * Take what ever URI was requested and send a 301 (moved permanently) 
     * response with the new url.
     *  
     * @see coyote.commons.network.http.HTTPD#serve(coyote.commons.network.http.IHTTPSession)
     */
    @Override
    public Response serve( IHTTPSession session ) throws SecurityResponseException {
      String host = findRequestHeaderValue( HTTP.HDR_HOST, session );
      if ( StringUtil.isNotBlank( host ) ) {
        String uri;
        if ( server.getPort() == 443 ) {
          uri = HTTPS_SCHEME + host + session.getUri();
        } else {
          uri = HTTP_SCHEME + host + ":" + server.getPort() + session.getUri();
        }
        Log.append( HTTPD.EVENT, "Redirecting to " + uri );
        Response response = Response.createFixedLengthResponse( Status.REDIRECT, MimeType.HTML.getType(), "<html><body>Moved: <a href=\"" + uri + "\">" + uri + "</a></body></html>" );
        response.addHeader( HTTP.HDR_LOCATION, uri );
        return response;
      }
      return super.serve( session );
    }

  }




  /**
   * @return the port on which this server is listening or 0 if the server is not running.
   */
  public int getPort() {
    if ( server != null )
      return server.getPort();
    else
      return 0;
  }




  /**
   * Add a handler at the given route.
   * 
   * <p>This is intended for the programmatic or embedded use of the server in 
   * code. 
   * 
   * @param route the route regular expression
   * @param handler the handler class
   * @param initParams initialization parameters
   */
  void addHandler( final String route, final Class<?> handler, final Object... initParams ) {
    Object[] params;
    if ( initParams != null ) {
      params = new Object[initParams.length + 2];
      params[0] = this;
      params[1] = new Config();
      for ( int x = 0; x < initParams.length; x++ ) {
        params[x + 2] = initParams[x];
      }
    } else {
      params = new Object[] { this, new Config() };
    }

    server.addRoute( route, 100, handler, params );
  }




  /**
   * Run the server in a separate thread.
   * 
   * @return the thread in which the server is running
   */
  public Thread execute() {
    shutdown = false;
    Thread serverThread = new Thread( new Runnable() {
      @Override
      public void run() {
        start();
      }
    } );

    // start the thread running, calling this server start()
    serverThread.start();
    try {
      Thread.yield();
      Thread.sleep( 200 );
    } catch ( InterruptedException e ) {}
    return serverThread;
  }

}
