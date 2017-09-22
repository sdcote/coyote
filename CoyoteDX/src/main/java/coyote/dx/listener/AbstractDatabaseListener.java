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
import java.sql.SQLException;
import java.sql.Statement;

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.Database;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransformContext;
import coyote.dx.db.ColumnDefinition;
import coyote.dx.db.ColumnType;
import coyote.dx.db.DatabaseDialect;
import coyote.dx.db.TableDefinition;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a base class for any listener which needs to work with a database.
 */
public abstract class AbstractDatabaseListener extends AbstractListener implements ContextListener {

  /** The JDBC connection used by this writer to interact with the database */
  protected Connection connection;

  /** Private database object to assist with connections */
  private Database database = null;

  /** our symbol table used for looking up values specific to our connection */
  protected static final SymbolTable symbolTable = new SymbolTable();

  /** The database product name (Oracle, H2, etc.) to which we are connected. */
  private String databaseProduct = null;

  /** The database table definition */
  private final TableDefinition tableschema = new TableDefinition("DATAFRAME");




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




  public AbstractDatabaseListener() {
    // This is the normalized data model, each field is stored as a record
    tableschema.addColumn(new ColumnDefinition("SysId", ColumnType.STRING).setLength(36).setPrimaryKey(true));
    tableschema.addColumn(new ColumnDefinition("Parent", ColumnType.STRING).setLength(36));
    tableschema.addColumn(new ColumnDefinition("Sequence", ColumnType.INT).setNullable(true));
    tableschema.addColumn(new ColumnDefinition("Name", ColumnType.STRING).setLength(64));
    tableschema.addColumn(new ColumnDefinition("Value", ColumnType.STRING).setLength(4096).setNullable(true));
    tableschema.addColumn(new ColumnDefinition("Type", ColumnType.SHORT).setNullable(true));
    tableschema.addColumn(new ColumnDefinition("CreatedBy", ColumnType.STRING).setLength(32));
    tableschema.addColumn(new ColumnDefinition("CreatedOn", ColumnType.DATE));
    tableschema.addColumn(new ColumnDefinition("ModifiedBy", ColumnType.STRING).setLength(32));
    tableschema.addColumn(new ColumnDefinition("ModifiedOn", ColumnType.DATE));
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
    } catch (final DataFrameException ignore) {}
    return false;
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
   * <p>This is either a shared database or one we created locally.
   * 
   * @return the database helper class from which we get connections
   */
  protected synchronized Database getDatabase() {
    Database retval = context.getDatabase(getString(ConfigTag.SOURCE));
    if (retval == null) {
      retval = database;
      if (retval == null) {
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
   * Get either a cached connection, or a new connection from the database we 
   * are using.
   * 
   * @return a connection to our configured or shared database
   */
  protected Connection getConnection() {
    Connection retval = connection;
    if (retval == null) {
      Database db = getDatabase();
      if (db != null) {
        retval = db.getConnection();
      } else {
        Log.error("Could not connect to database: no database reference available");
      }
    }
    return retval;
  }




  private void commit() throws SQLException {
    connection.commit();
  }




  /**
   * Check to make sure our database schema exists. 
   */
  private void checkSchema() {
    tableschema.setSchemaName(getDatabase().getSchema());
    boolean autocreate = getDatabase().isAutoCreate();
    String username = getDatabase().getUsername();

    Log.debug("Looking for schema '" + tableschema.getSchemaName() + "'");
    if (!DatabaseUtil.schemaExists(tableschema.getSchemaName(), getConnection())) {
      if (autocreate) {
        String sql = DatabaseDialect.getCreateSchema(databaseProduct, tableschema.getSchemaName(), username);
        Log.debug("Schema '" + tableschema.getSchemaName() + "' not found in database, creating it with:\n" + sql);
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate(sql);
          Log.debug("Schema created.");

          if (DatabaseUtil.schemaExists(tableschema.getSchemaName(), getConnection())) {
            Log.debug("Schema creation verified");
          } else {
            Log.error("Could not verify the creation of schema '" + tableschema.getSchemaName() + "'");
          }

        } catch (final SQLException e) {
          Log.error("Schema creation failed!");
          e.printStackTrace();
        }
      } else {
        Log.warn("The schema '" + tableschema.getSchemaName() + "' does not exist in the database and autocreate is set to '" + autocreate + "', expect subsequent database operations to fail.");
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

    boolean autocreate = getDatabase().isAutoCreate();
    tableschema.setName(getTable());

    // check to see if the table exists
    if (!DatabaseUtil.tableExists(tableschema.getName(), getConnection())) {

      if (autocreate) {
        Connection conn = getConnection();

        if (conn == null) {
          Log.error("Cannot get database connection");
          context.setError("Could not connect to the database");
          return false;
        }

        symbolTable.put(DatabaseDialect.TABLE_NAME_SYM, tableschema.getName());
        symbolTable.put(DatabaseDialect.DB_SCHEMA_SYM, tableschema.getSchemaName());

        String command = DatabaseDialect.getCreate(databaseProduct, tableschema);

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.creating_table", getClass().getName(), tableschema.getName(), command));

        Statement stmt = null;
        try {
          stmt = conn.createStatement();
          stmt.executeUpdate(command);

        } catch (Exception e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Writer.jdbc_table_create_error", tableschema.getName(), e.getMessage()));
        } finally {
          try {
            stmt.close();
          } catch (Exception e) {
            Log.warn(LogMsg.createMsg(CDX.MSG, "Problems closing create {} statement: {}", tableschema.getName(), e.getMessage()));
          }
        }

        try {
          commit();
        } catch (final SQLException e) {
          Log.warn("Couldnt commit table creation: " + e.getMessage());
        }

        if (DatabaseUtil.tableExists(tableschema.getName(), tableschema.getSchemaName(), getConnection())) {
          Log.debug("Table creation verified");
          retval = true;
        } else {
          Log.error("Could not verifiy the creation of '" + tableschema.getName() + "." + tableschema.getSchemaName() + "' table, expect subsequent database operations to fail.");
        }
      } else {
        Log.warn("The table '" + tableschema.getName() + "' does not exist in the database and autocreate is set to '" + autocreate + "', expect subsequent database operations to fail.");
      }

    } else {
      retval = true;
    }

    return retval;
  }




  /**
   * @see coyote.dx.listener.AbstractListener#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    // get a connection
    connection = getConnection();

    // get the product we are using for use in the DatabaseDialect helper
    databaseProduct = getDatabase().getProductName(connection);

    // make sure we have the table we need
    checkTable();

  }

}
