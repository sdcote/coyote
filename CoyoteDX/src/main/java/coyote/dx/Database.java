package coyote.dx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.dataframe.DataFrameException;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This represents a shared definition of a database.
 * 
 * <p>Database definitions are stored in the context with a name and allow 
 * components to reference a database connection by name instead of copying 
 * the database details in each section. This make maintaining shared database 
 * definitions simpler as there is only one location for the details to be 
 * updated.
 * 
 * <p>This is not a connection pool. It is a configuration convenience for 
 * components to obtain a connection from the context which is guaranteed to 
 * be closed at the end of the transform (if the transform exits normally).
 */
public class Database extends AbstractConfigurableComponent implements ConfigurableComponent {

  private final List<Connection> connections = new ArrayList<Connection>();
  private Driver driver = null;
  DatabaseMetaData meta = null;




  public Database() {}




  /**
   * Create a new connection using the configuration.
   * 
   * <p>This does not share nor pool connections, but creates a new connection 
   * on each request. This should be fine for this toolkit as it is expected 
   * that maybe two connections (one for a reader and one for a writer) might be 
   * created.
   * 
   * <p>The primary benefit of this class is that many components can 
   * reference one database configuration in the job and not have to duplicate 
   * the configuration in each component. Additionally, this class will keep a 
   * reference to all the connections and make sure they are closed when the 
   * JRE exits.
   * 
   * <p>Each connection is tracked and closed when this component is closed.
   * 
   * @return a new connection
   */
  public Connection getConnection() {
    Connection connection = null;
    connection = createConnection();
    if ( connection != null ) {
      connections.add( connection );
    }
    return connection;
  }




  /**
   * Create a connection to the database.
   * 
   * <p>Caller is responsible for closing the connection when done with it.
   * 
   * @return the connection to the database or null if there were problems
   */
  private Connection createConnection() {
    Connection retval = null;
    try {
      if ( driver == null ) {
        String url = getLibrary();
        URL u = new URL( url );
        URLClassLoader ucl = new URLClassLoader( new URL[] { u } );
        driver = (Driver)Class.forName( getDriver(), true, ucl ).newInstance();
        DriverManager.registerDriver( new DriverDelegate( driver ) );
      }
      retval = DriverManager.getConnection( getTarget(), getUsername(), getPassword() );
    } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e ) {
      Log.error( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
      Log.debug( "ERROR: Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
    }
    return retval;
  }




  /**
   * @param value
   */
  public void setAutoCreate( boolean value ) {
    configuration.put( ConfigTag.AUTO_CREATE, value );
  }




  public boolean isAutoCreate() {
    try {
      return configuration.getAsBoolean( ConfigTag.AUTO_CREATE );
    } catch ( DataFrameException ignore ) {}
    return false;
  }




  public String getDriver() {
    return configuration.getString( ConfigTag.DRIVER );
  }




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return configuration.getString( ConfigTag.TARGET );
  }




  /**
   * Set the URI to where the connection should be made.
   * 
   * @param value the URI to where the writer should write its data
   */
  public void setTarget( final String value ) {
    configuration.put( ConfigTag.TARGET, value );
  }




  public String getPassword() {
    if ( configuration.containsIgnoreCase( ConfigTag.PASSWORD ) ) {
      return configuration.getAsString( ConfigTag.PASSWORD );
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD ) ) {
      return CipherUtil.decryptString( configuration.getAsString( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD ) );
    } else {
      return null;
    }
  }




  public String getUsername() {
    if ( configuration.containsIgnoreCase( ConfigTag.USERNAME ) ) {
      return configuration.getFieldIgnoreCase( ConfigTag.USERNAME ).getStringValue();
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME ) ) {
      return CipherUtil.decryptString( configuration.getFieldIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME ).getStringValue() );
    } else {
      return null;
    }
  }




  /**
   * @param value
   */
  public void setUsername( String value ) {
    configuration.put( ConfigTag.USERNAME, value );
  }




  public void setName( String value ) {
    configuration.put( ConfigTag.NAME, value );
  }




  public String getName() {
    return configuration.getString( ConfigTag.NAME );
  }




  public String getLibrary() {
    return configuration.getString( ConfigTag.LIBRARY );
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    // This is not called by the framework as it is not a regular component
    // It may, however, be called programmatically when used separately
    if ( meta == null ) {
      try (Connection connection = createConnection()) {
        meta = connection.getMetaData();
      } catch ( SQLException e ) {
        getContext().setError( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
      }
    }
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    for ( Connection connection : connections ) {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( SQLException ignore ) {}
      }
    }
  }




  /**
   * Retrieves the name of this database product.
   * 
   * <p>This is converted to uppercase for uniformity with various versions of 
   * the drivers and API usages.
   *
   * @return database product name
   */
  public String getProductName() {
    String retval = null;
    if ( meta == null ) {
      open( null );
    }
    try {
      retval = meta.getDatabaseProductName();
      if ( retval != null ) {
        return retval.toUpperCase();
      }
    } catch ( SQLException e ) {
      e.printStackTrace();
    }
    return retval;
  }




  /**
   * Retrieves the version number of this database product.
   *
   * @return database version number, null if problems occurred or not supported
   */
  public String getProductVersion() {
    if ( meta == null ) {
      open( null );
    }
    try {
      return meta.getDatabaseProductVersion();
    } catch ( SQLException e ) {
      e.printStackTrace();
    }
    return null;
  }




  /**
   * Retrieves the major version number of the underlying database.
   *
   * @return underlying database's major version, -1 if problems occurred
   */
  public int getMajorVersion() {
    if ( meta == null ) {
      open( null );
    }
    try {
      return meta.getDatabaseMajorVersion();
    } catch ( SQLException e ) {
      e.printStackTrace();
    }
    return -1;
  }




  /**
   * Retrieves the minor version number of the underlying database.
   *
   * @return underlying database's minor version, -1 if problems occurred
   */
  public int getMinorVersion() {
    if ( meta == null ) {
      open( null );
    }
    try {
      return meta.getDatabaseMinorVersion();
    } catch ( SQLException e ) {
      e.printStackTrace();
    }
    return -1;
  }

}
