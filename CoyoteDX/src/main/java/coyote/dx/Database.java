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
import coyote.commons.StringUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.dataframe.DataFrameException;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;


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
    if (connection != null) {
      connections.add(connection);
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
  private synchronized Connection createConnection() {
    Connection retval = null;
    try {
      // if we have not been initialized, register the driver
      if (driver == null) {
        String url = getLibrary();
        URL u = new URL(url);
        URLClassLoader ucl = new URLClassLoader(new URL[]{u});
        driver = (Driver)Class.forName(getDriver(), true, ucl).newInstance();
        DriverManager.registerDriver(new DriverDelegate(driver));
      }

      retval = DriverManager.getConnection(getTarget(), getUsername(), getPassword());
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e) {
      Log.error("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      Log.debug("ERROR: Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() + "\n" + ExceptionUtil.stackTrace(e));
    }
    return retval;
  }




  /**
   * @param value
   */
  public void setAutoCreate(boolean value) {
    configuration.put(ConfigTag.AUTO_CREATE, value);
  }




  public boolean isAutoCreate() {
    try {
      return configuration.getAsBoolean(ConfigTag.AUTO_CREATE);
    } catch (DataFrameException ignore) {}
    return false;
  }




  public String getDriver() {
    return getString(ConfigTag.DRIVER);
  }




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return getString(ConfigTag.TARGET);
  }




  /**
   * Set the URI to where the connection should be made.
   * 
   * @param value the URI to where the writer should write its data
   */
  public void setTarget(final String value) {
    configuration.put(ConfigTag.TARGET, value);
  }




  public String getPassword() {
    String retval = getString(ConfigTag.PASSWORD);
    if (StringUtil.isEmpty(retval) && configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));
    }
    return retval;
  }




  public String getUsername() {
    String retval = getString(ConfigTag.USERNAME);
    if (StringUtil.isEmpty(retval) && configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));
    }
    return retval;
  }




  /**
   * @param value
   */
  public void setUsername(String value) {
    configuration.put(ConfigTag.USERNAME, value);
  }




  public void setName(String value) {
    configuration.put(ConfigTag.NAME, value);
  }




  public String getName() {
    return getString(ConfigTag.NAME);
  }




  public String getLibrary() {
    return getString(ConfigTag.LIBRARY);
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    // This is not called by the framework as it is not a regular component
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    for (Connection connection : connections) {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException ignore) {}
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
  public String getProductName(Connection connection) {
    String retval = null;
    DatabaseMetaData meta = null;
    try {
      meta = connection.getMetaData();
    } catch (SQLException e) {
      getContext().setError("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
    }

    if (meta != null) {
      try {
        retval = meta.getDatabaseProductName();
      } catch (SQLException ignore) {}
      if (retval != null) {
        return retval.toUpperCase();
      }
    }
    return retval;
  }




  /**
   * Retrieves the version number of this database product.
   *
   * @return database version number, null if problems occurred or not supported
   */
  public String getProductVersion(Connection connection) {
    String retval = null;
    DatabaseMetaData meta = null;
    try {
      meta = connection.getMetaData();
    } catch (SQLException e) {
      getContext().setError("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
    }

    if (meta != null) {
      try {
        retval = meta.getDatabaseProductVersion();
      } catch (SQLException ignore) {}
      if (retval != null) {
        return retval.toUpperCase();
      }
    }
    return retval;
  }




  /**
   * Retrieves the user name as known to this database.
   *
   * @return the database user name
   */
  public String getConnectedUser(Connection connection) {
    String retval = null;
    DatabaseMetaData meta = null;
    try {
      meta = connection.getMetaData();
    } catch (SQLException e) {
      getContext().setError("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
    }

    if (meta != null) {
      try {
        retval = meta.getDatabaseProductVersion();
      } catch (SQLException ignore) {}
      if (retval != null) {
        return retval.toUpperCase();
      }
    }
    return retval;
  }




  /**
   * @param library
   */
  public void setLibrary(String library) {
    configuration.set(ConfigTag.LIBRARY, library);
  }




  /**
   * @param driver
   */
  public void setDriver(String driver) {
    configuration.set(ConfigTag.DRIVER, driver);
  }




  /**
   * @param password
   */
  public void setPassword(String password) {
    configuration.set(ConfigTag.PASSWORD, password);
  }




  /**
   * @param schema
   */
  public void setSchema(String schema) {
    configuration.set(ConfigTag.SCHEMA, schema);
  }




  /**
   * This retruns the name of the schema in the configuration, or the name of 
   * the user if the schema is not defined.
   * 
   *  @return the name of the schema for this table
   */
  public String getSchema() {
    String retval = getString(ConfigTag.SCHEMA);
    if (StringUtil.isBlank(retval)) {
      retval = getUsername();
    }
    return retval;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer("Database: ");
    String name = getName();
    if (StringUtil.isNotEmpty(name)) {
      b.append(name);
      b.append(" ");
    }
    b.append(getTarget());
    return b.toString();
  }

}
