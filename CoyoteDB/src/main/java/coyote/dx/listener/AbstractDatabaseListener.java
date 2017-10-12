/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.jdbc.TableDefinition;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.db.Database;
import coyote.dx.db.DatabaseConnector;
import coyote.dx.db.FrameStore;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a base class for any listener which needs to work with a database.
 */
public abstract class AbstractDatabaseListener extends AbstractListener implements ContextListener {

  /** The thing we use to get connections to the database */
  private DatabaseConnector connector = null;

  /** The JDBC connection used by this writer to interact with the database */
  protected Connection connection;

  /** Private database object to assist with connections */
  private Database database = null;

  /** our symbol table used for looking up values specific to our connection */
  protected static final SymbolTable symbolTable = new SymbolTable();

  /** The database product name (Oracle, H2, etc.) to which we are connected. */
  protected String databaseProduct = null;

  private boolean initialized = false;
  private static final String SIMPLE_MODE = "SimpleMode";
  private static final String DEFAULT_IDENTITY = "00000000-0000-0000-0000-000000000000";

  private static final String SYSID = "SysId";
  private static final String VALUE = "Value";
  private static final String TYPE = "Type";




  /**
   * Initialize the listener.
   * 
   * <p>Because Listeners are created and opend first, they will not have the 
   * ability to see any context variables other components may have created 
   * and placed. Since we want to use any DatabaseFixtures Tasks may have 
   * bound to the context, we have to wait until used for the first time to 
   * initialize.
   */
  private void init() {

    if (context instanceof TransformContext) {

      // If we don't have a connection, prepare to create one
      if (connection == null) {

        // Look for a database connector in the context bound with the name specified in the TARGET attribute
        String target = getConfiguration().getString(ConfigTag.TARGET);
        target = Template.preProcess(target, context.getSymbols());
        Object obj = getContext().get(target);
        if (obj != null && obj instanceof DatabaseConnector) {
          setConnector((DatabaseConnector)obj);
          Log.debug("Using database connector found in context bound to '" + target + "'");
        }

        if (getConnector() == null) {
          // we have to create a Database based on our configuration
          Database database = new Database();
          Config cfg = new Config();

          if (StringUtil.isNotBlank(getString(ConfigTag.TARGET)))
            cfg.put(ConfigTag.TARGET, getString(ConfigTag.TARGET));

          if (StringUtil.isNotBlank(getString(ConfigTag.DRIVER)))
            cfg.put(ConfigTag.DRIVER, getString(ConfigTag.DRIVER));

          if (StringUtil.isNotBlank(getString(ConfigTag.LIBRARY)))
            cfg.put(ConfigTag.LIBRARY, getString(ConfigTag.LIBRARY));

          if (StringUtil.isNotBlank(getString(ConfigTag.USERNAME)))
            cfg.put(ConfigTag.USERNAME, getString(ConfigTag.USERNAME));

          if (StringUtil.isNotBlank(getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)))
            cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME, getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));

          if (StringUtil.isNotBlank(getString(ConfigTag.PASSWORD)))
            cfg.put(ConfigTag.PASSWORD, getString(ConfigTag.PASSWORD));

          if (StringUtil.isNotBlank(getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)))
            cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD, getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));

          setConnector(database);

          try {
            database.setConfiguration(cfg);
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_target", getClass().getName(), database.getTarget()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_driver", getClass().getName(), database.getDriver()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_library", getClass().getName(), database.getLibrary()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_user", getClass().getName(), database.getUserName()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_password", getClass().getName(), StringUtil.isBlank(database.getPassword()) ? 0 : database.getPassword().length()));
            }
          } catch (ConfigurationException e) {
            context.setError("Could not configure database connector: " + e.getClass().getName() + " - " + e.getMessage());
          }

          // if there is no schema in the configuration, set it to the same as the username
          if (StringUtil.isBlank(getString(ConfigTag.SCHEMA))) {
            getConfiguration().set(ConfigTag.SCHEMA, database.getUserName());
          }
        }
      } else {
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_existing_connection", getClass().getName()));
      }

      // make sure we have the table we need
      checkTable();

    }

    initialized = true;
  }




  /**
   * @see coyote.dx.listener.AbstractListener#close()
   */
  @Override
  public void close() throws IOException {
    if (database != null) {
      database.close();
    }
    super.close();
  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // get the table
    if (StringUtil.isBlank(getConfiguration().getString(ConfigTag.TABLE))) {
      throw new ConfigurationException("Configuration must contain a '" + ConfigTag.TABLE + "' value");
    }

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
   * Returns the configured setting for autocreating a table if it does not 
   * exist.
   * 
   * @return true if the component is to autocreate the table, false (default) 
   *         otherwise
   */
  public boolean isAutoCreate() {
    try {
      return configuration.getAsBoolean(ConfigTag.AUTO_CREATE);
    } catch (final DataFrameException ignore) {
      // must not be set or is invalid boolean value
    }
    return true;
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the name of the database driver to use
   */
  public String getDriver() {
    return getString(ConfigTag.DRIVER);
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the sysid of the the identity performing database modifications. 
   *         If no were configured a default value of all zeros is used.
   */
  public String getIdentity() {
    String retval = getString(ConfigTag.IDENTITY);
    if (StringUtil.isBlank(retval)) {
      retval = DEFAULT_IDENTITY;
    }
    return retval;
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the location of the JAR containing the database driver
   */
  public String getLibrary() {
    return getString(ConfigTag.LIBRARY);
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the password to use for database connections
   */
  public String getPassword() {
    String retval = getString(ConfigTag.PASSWORD);
    if (StringUtil.isEmpty(retval) && configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));
    }
    return retval;
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the user name to use in database connections
   */
  public String getUsername() {
    String retval = getString(ConfigTag.USERNAME);
    if (StringUtil.isEmpty(retval) && configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));
    }
    return retval;
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the name of the table to use
   */
  public String getTable() {
    return getString(ConfigTag.TABLE);
  }




  /**
   * This only reads from the configuration, not the shared database if used.
   * 
   * @return the target shared database to use
   */
  public String getTarget() {
    return getString(ConfigTag.TARGET);
  }




  /**
   * Retrieve the Database object holding our connections.
   * 
   * <p>This is the databse we created locally through our configuration. If 
   * the local configuration does not contain a database configuration, null 
   * will be returned.
   * 
   * @return the database helper class from which we get connections or null 
   *         if this listener has no database configuration.
   */
  protected synchronized Database getDatabase() {
    Database retval = null;
    if (retval == null) {
      retval = database;
      if (retval == null && getConfiguration().containsIgnoreCase(getDriver())) {
        retval = new Database();
        retval.setContext(getContext());
        retval.setLibrary(getLibrary());
        retval.setTarget(getDriver());
        retval.setDriver(getTarget());
        retval.setUsername(getUsername());
        retval.setPassword(getPassword());
        retval.setAutoCreate(isAutoCreate());
        retval.setSchema(getSchema());
        database = retval;
        Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_local_database_definition", database.toString()));
      }
    }
    return retval;
  }




  /**
   * @return the name of the database product to which we are connected
   */
  protected String getDatabaseProduct() {
    return databaseProduct;
  }




  @SuppressWarnings("unchecked")
  private Connection getConnection() {

    if (connection == null) {

      if (getConnector() == null) {
        Log.fatal("We don't have a connector to give us a connection to a database. The open method failed to do its job!");
      }

      // get the connection to the database
      try {
        connection = getConnector().getConnection();

        if (connection != null) {
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.connected_to", getClass().getName(), getTarget()));
          DatabaseMetaData meta = connection.getMetaData();
          String product = meta.getDatabaseProductName();
          databaseProduct = product.toUpperCase();

          // update the symbols with database information
          symbolTable.put(DatabaseDialect.DATABASE_SYM, product);
          Version version = DatabaseUtil.getDriverVersion(connection);
          symbolTable.put(DatabaseDialect.DATABASE_VERSION_SYM, version == null ? "unknown" : version.toString());
          symbolTable.put(DatabaseDialect.DATABASE_VERSION_FULL_SYM, meta.getDatabaseProductVersion());
          symbolTable.put(DatabaseDialect.DATABASE_MAJOR_SYM, meta.getDatabaseMajorVersion());
          symbolTable.put(DatabaseDialect.DATABASE_MINOR_SYM, meta.getDatabaseMinorVersion());
          version = DatabaseUtil.getDriverVersion(connection);
          symbolTable.put(DatabaseDialect.DRIVER_VERSION_SYM, version == null ? "unknown" : version.toString());
          symbolTable.put(DatabaseDialect.DRIVER_VERSION_FULL_SYM, meta.getDriverVersion());
          symbolTable.put(DatabaseDialect.DRIVER_MAJOR_SYM, meta.getDriverMajorVersion());
          symbolTable.put(DatabaseDialect.DRIVER_MINOR_SYM, meta.getDriverMinorVersion());

          // log debug information about the database
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.connected_to_product", getClass().getName(), meta.getDatabaseProductName(), meta.getDatabaseProductVersion(), meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion()));
        } else {
          getContext().setError("Connector could not get a connection to the database");
        }
      } catch (SQLException e) {
        getContext().setError("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      }
    }
    return connection;
  }




  private void commit() throws SQLException {
    connection.commit();
  }




  /**
   * Check to make sure our database schema exists. 
   */
  private void checkSchema() {
    String schema = determineSchema();

    Log.debug("Looking for schema '" + schema + "'");
    if (!DatabaseUtil.schemaExists(schema, getConnection())) {
      if (isAutoCreate()) {
        String sql = DatabaseDialect.getCreateSchema(databaseProduct, schema, determineUserName());
        Log.debug("Schema '" + schema + "' not found in database, creating it with:\n" + sql);
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate(sql);
          Log.debug("Schema created.");

          if (DatabaseUtil.schemaExists(schema, getConnection())) {
            Log.debug("Schema creation verified");
          } else {
            Log.error("Could not verify the creation of schema '" + schema + "'");
          }

        } catch (final SQLException e) {
          Log.error("Schema creation failed!");
          e.printStackTrace();
        }
      } else {
        Log.warn("The schema '" + schema + "' does not exist in the database and autocreate is set to '" + isAutoCreate() + "', expect subsequent database operations to fail.");
      }
    }
  }




  /**
   * This checks the database for the table to exist.
   * 
   * <p>If the table does not exist and autocreate is set to true, this method 
   * will attempt to create the table based on the schema generated for the 
   * records observed so far.</p>
   * 
   * @return true if the table exists and is ready to insert data, false otherwise
   */
  @SuppressWarnings("unchecked")
  private boolean checkTable() {
    boolean retval = false;
    checkSchema();

    String table = getTable();

    // check to see if the table exists
    if (!DatabaseUtil.tableExists(table, getConnection())) {

      if (isAutoCreate()) {
        Connection conn = getConnection();
        String schema = determineSchema();
        if (conn == null) {
          Log.error("Cannot get database connection");
          context.setError("Could not connect to the database");
          return false;
        }

        symbolTable.put(DatabaseDialect.TABLE_NAME_SYM, table);
        symbolTable.put(DatabaseDialect.DB_SCHEMA_SYM, schema);

        TableDefinition tabledef = FrameStore.getTableSchema(table, schema);

        String command = DatabaseDialect.getCreate(databaseProduct, tabledef);

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.creating_table", getClass().getName(), table, command));

        Statement stmt = null;
        try {
          stmt = conn.createStatement();
          stmt.executeUpdate(command);

        } catch (Exception e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Writer.jdbc_table_create_error", table, e.getMessage()));
        } finally {
          try {
            stmt.close();
          } catch (Exception e) {
            Log.warn(LogMsg.createMsg(CDX.MSG, "Problems closing create {} statement: {}", table, e.getMessage()));
          }
        }

        try {
          commit();
        } catch (final SQLException e) {
          Log.warn("Couldnt commit table creation: " + e.getMessage());
        }

        if (DatabaseUtil.tableExists(table, schema, getConnection())) {
          Log.debug("Table creation verified");
          retval = true;
        } else {
          Log.error("Could not verifiy the creation of '" + table + "." + schema + "' table, expect subsequent database operations to fail.");
        }
      } else {
        Log.warn("The table '" + table + "' does not exist in the database and autocreate is set to '" + isAutoCreate() + "', expect subsequent database operations to fail.");
      }

    } else {
      retval = true;
    }

    // return the connection to the pool if we are pooling connections
    if (getConnector().isPooled()) {
      quietlyClose(connection);
    }

    return retval;
  }




  /**
   * Close the given connection, consuming any exceptions.
   * 
   * @param conn the JDBC connection to close.
   */
  protected void quietlyClose(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException ignore) {
        // ignore exceptions
      }
    }
  }




  /**
   * Return the connector we use for getting a connection to the database.
   * 
   * @return the connector
   */
  public DatabaseConnector getConnector() {
    return connector;
  }




  /**
   * @param connector the connector to set
   */
  public void setConnector(DatabaseConnector connector) {
    this.connector = connector;
  }




  /**
   * This is the main entry point for all sub-classes.
   * 
   * <p>This metho check to see if the listener is enabled then if any conditions exist. If enabled and the 
   * @see coyote.dx.listener.AbstractListener#onMap(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onMap(TransactionContext context) {
    if (isEnabled()) {
      if (getCondition() != null) {
        try {
          if (evaluator.evaluateBoolean(getCondition())) {
            if (!initialized) {
              init();
            }
            execute(context);
          } else {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Listener.boolean_evaluation_false", getCondition()));
            }
          }
        } catch (final IllegalArgumentException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Listener.boolean_evaluation_error", getCondition(), e.getMessage()));
        }
      } else {
        if (!initialized) {
          init();
        }
        execute(context);
      }
    }
  }




  /**
   * @param context the transaction context on which to operate
   */
  protected void execute(TransactionContext context) {
    // listeners should override this method to perform ther database operation
  }




  /**
   * Returns the name of the schema based on configuration or the connected 
   * user.
   * 
   * <p>This checks the configuration for the name of the schema to use and if 
   * not found, checks the configuration for the name of the user. If both are 
   * blank, this method will query the name of the user used to connect to the 
   * database.
   * 
   * @return the configured schema or the user name of the connection.
   */
  protected String determineSchema() {
    String schema = getSchema();
    if (StringUtil.isBlank(schema)) {
      schema = DatabaseUtil.getUserName(getConnection());
    }
    return schema;
  }




  /**
   * Returns the name of the user in the configuration or the connection.
   * 
   * <p>This checks the configuration for the username and if it does not 
   * exist the connection is checket for the name of the user used in the 
   * connection.
   * 
   * @return the configured user name or the user name of the connection.
   */
  private String determineUserName() {
    String retval = getUsername();
    if (StringUtil.isBlank(retval)) {
      retval = DatabaseUtil.getUserName(connection);
    }
    return retval;
  }




  /**
   * @return true if this is to serialize the entire frame into the value 
   *         field, false to store each field in a separate row.
   */
  protected boolean isSimpleMode() {
    boolean retval = true;
    try {
      retval = configuration.getAsBoolean(SIMPLE_MODE);
    } catch (final DataFrameException ignore) {
      // must not be set or is invalid boolean value
    }

    return retval;
  }

}
