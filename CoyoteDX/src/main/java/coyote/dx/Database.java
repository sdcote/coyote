package coyote.dx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
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




  public Database() {}




  /**
   * Create a new connection using the configuration.
   * 
   * <p>This does not share nor pool connections, but creates a new connection 
   * on each request. This should be fine for this toolkit as it is expected 
   * that maybe two connections (one for a reader and one for a writer) might be 
   * created.
   * 
   * <p>The primary benifit of this class is that many components can 
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

    try {
      if ( driver == null ) {
        URL u = new URL( getLibrary() );
        URLClassLoader ucl = new URLClassLoader( new URL[] { u } );
        driver = (Driver)Class.forName( getDriver(), true, ucl ).newInstance();
        DriverManager.registerDriver( new DriverDelegate( driver ) );
      }

      connection = DriverManager.getConnection( getTarget(), getUsername(), getPassword() );

      if ( connection != null ) {
        Log.debug( LogMsg.createMsg( CDX.MSG, "Database.connected_to", getTarget() ) );
      }
    } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e ) {
      Log.error( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
      Log.debug( "ERROR: Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() + "\n" + ExceptionUtil.stackTrace( e ) );
    }

    if ( connection != null ) {
      connections.add( connection );
    }

    return connection;
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
    return configuration.getAsString( ConfigTag.DRIVER );
  }




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return configuration.getAsString( ConfigTag.TARGET );
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
    return configuration.getAsString( ConfigTag.NAME );
  }




  public String getLibrary() {
    return configuration.getAsString( ConfigTag.LIBRARY );
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    // this is not called as it is not a regular component
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

}
