package coyote.batch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.commons.jdbc.DriverDelegate;
import coyote.dataframe.DataFrameException;


/**
 * 
 */
public class DataStore extends AbstractConfigurableComponent implements ConfigurableComponent {
  final Logger log = LoggerFactory.getLogger( getClass() );

  protected Connection connection;




  public DataStore() {}




  public Connection getConnection() {

    if ( connection == null ) {
      // get the connection to the database
      try {
        URL u = new URL( getLibrary() );
        URLClassLoader ucl = new URLClassLoader( new URL[] { u } );
        Driver driver = (Driver)Class.forName( getDriver(), true, ucl ).newInstance();
        DriverManager.registerDriver( new DriverDelegate( driver ) );

        connection = DriverManager.getConnection( getTarget(), getUsername(), getPassword() );

        if ( connection != null ) {
          log.debug( "Connected to {}", getTarget() );
        }
      } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e ) {
        log.error( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
      }
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




  /**
   * @param value
   */
  private void setDriver( String value ) {
    configuration.put( ConfigTag.DRIVER, value );
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




  /**
   * @param value
   */
  private void setPassword( String value ) {
    configuration.put( ConfigTag.PASSWORD, value );
  }




  public String getPassword() {
    return configuration.getAsString( ConfigTag.PASSWORD );
  }




  /**
   * @param value
   */
  public void setUsername( String value ) {
    configuration.put( ConfigTag.USERNAME, value );
  }




  public String getUsername() {
    return configuration.getAsString( ConfigTag.USERNAME );
  }




  public void setName( String value ) {
    configuration.put( ConfigTag.NAME, value );
  }




  public String getName() {
    return configuration.getAsString( ConfigTag.NAME );
  }




  /**
   * @param value
   */
  private void setLibrary( String value ) {
    configuration.put( ConfigTag.LIBRARY, value );
  }




  public String getLibrary() {
    return configuration.getAsString( ConfigTag.LIBRARY );
  }




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
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
    if ( connection != null ) {
      try {
        connection.close();
      } catch ( SQLException ignore ) {
        // don't care - right now
      }
    }
  }

}
