/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import coyote.commons.JdbcUtil;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.dx.db.ColumnDefinition;
import coyote.dx.db.ColumnType;
import coyote.dx.db.DatabaseDialect;
import coyote.dx.db.TableDefinition;
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

  /** our symbol table used for looking up values specific to our connection */
  protected static final SymbolTable symbolTable = new SymbolTable();

  /** The database product name (Oracle, H2, etc.) to which we are connected. */
  private String databaseProduct = null;

  /** The database table definition */
  private final TableDefinition tableschema = new TableDefinition("DATAFRAME");




  public AbstractDatabaseListener() {
    tableschema.addColumn(new ColumnDefinition("SysId", ColumnType.STRING).setLength(36).setPrimaryKey(true));
    tableschema.addColumn(new ColumnDefinition("Parent", ColumnType.STRING).setLength(36));
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

    // get the database

    // get the table

  }




  public String getSchema() {
    String retval = configuration.getString(ConfigTag.SCHEMA);
    if (StringUtil.isBlank(retval)) {
      retval = configuration.getString(ConfigTag.USERNAME);
    }
    return retval;
  }




  public boolean isAutoCreate() {
    try {
      return configuration.getAsBoolean(ConfigTag.AUTO_CREATE);
    } catch (final DataFrameException ignore) {}
    return false;
  }




  public String getDriver() {
    return configuration.getString(ConfigTag.DRIVER);
  }




  public String getLibrary() {
    return configuration.getString(ConfigTag.LIBRARY);
  }




  public String getPassword() {
    return configuration.getString(ConfigTag.PASSWORD);
  }




  public String getTable() {
    return configuration.getString(ConfigTag.TABLE);
  }




  public String getUsername() {
    return configuration.getString(ConfigTag.USERNAME);
  }




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return configuration.getAsString(ConfigTag.TARGET);
  }




  private Connection getConnection() {

    if (connection == null) {
      // get the connection to the database
      try {
        final URL u = new URL(getLibrary());
        final URLClassLoader ucl = new URLClassLoader(new URL[]{u});
        final Driver driver = (Driver)Class.forName(getDriver(), true, ucl).newInstance();
        DriverManager.registerDriver(new DriverDelegate(driver));

        connection = DriverManager.getConnection(getTarget(), getUsername(), getPassword());

        if (connection != null) {
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.connected_to", getClass().getName(), getTarget()));
          DatabaseMetaData meta = connection.getMetaData();

          // get the product name 
          String product = meta.getDatabaseProductName();
          // save it for later
          databaseProduct = product.toUpperCase();

          // update the symbols with database information
          symbolTable.put(DatabaseDialect.DATABASE_SYM, product);
          symbolTable.put(DatabaseDialect.DATABASE_VERSION_SYM, meta.getDatabaseProductVersion());
          symbolTable.put(DatabaseDialect.DATABASE_MAJOR_SYM, meta.getDatabaseMajorVersion());
          symbolTable.put(DatabaseDialect.DATABASE_MINOR_SYM, meta.getDatabaseMinorVersion());

          // log debug information about the database
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.connected_to_product", getClass().getName(), meta.getDatabaseProductName(), meta.getDatabaseProductVersion(), meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion()));

        }
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e) {
        getContext().setError("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      }
    }
    return connection;
  }




  private void commit() throws SQLException {
    connection.commit();
  }




  /**
   * 
   */
  private void checkSchema() {
    Log.debug("Looking for schema '" + getSchema() + "'");
    if (!JdbcUtil.schemaExists(getSchema(), getConnection())) {
      if (isAutoCreate()) {
        String sql = DatabaseDialect.getCreateSchema(databaseProduct, getSchema(), getUsername());
        Log.debug("Schema '" + getSchema() + "' not found in database, creating it with:\n" + sql);
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate(sql);
          Log.debug("Schema created.");

          if (JdbcUtil.schemaExists(getSchema(), getConnection())) {
            Log.debug("Schema creation verified");
          } else {
            Log.error("Could not verify the creation of schema '" + getSchema() + "'");
          }

        } catch (final SQLException e) {
          Log.error("Schema creation failed!");
          e.printStackTrace();
        }
      } else {
        Log.warn("The schema '" + getSchema() + "' does not exist in the database and autocreate is set to '" + isAutoCreate() + "', expect subsequent database operations to fail.");
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

    checkSchema();

    // check to see if the table exists
    if (!JdbcUtil.tableExists(getTable(), getConnection())) {

      if (isAutoCreate()) {
        Connection conn = getConnection();

        if (conn == null) {
          Log.error("Cannot get database connection");
          context.setError("Could not connect to the database");
          return false;
        }

        symbolTable.put(DatabaseDialect.TABLE_NAME_SYM, getTable());
        symbolTable.put(DatabaseDialect.DB_SCHEMA_SYM, getSchema());

        final TableDefinition tdef = new TableDefinition(getTable());
        tdef.setSchemaName(getSchema());

        String command = DatabaseDialect.getCreate(databaseProduct, tdef);

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.creating_table", getClass().getName(), getTable(), command));

        Statement stmt = null;
        try {
          stmt = conn.createStatement();
          stmt.executeUpdate(command);

        } catch (Exception e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Writer.jdbc_table_create_error", getTable(), e.getMessage()));
        } finally {
          try {
            stmt.close();
          } catch (Exception e) {
            Log.warn(LogMsg.createMsg(CDX.MSG, "Problems closing create {} statement: {}", getTable(), e.getMessage()));
          }
        }

        try {
          commit();
        } catch (final SQLException e) {
          Log.warn("Couldnt commit table creation: " + e.getMessage());
        }

        if (JdbcUtil.tableExists(getTable(), getSchema(), getConnection())) {
          Log.debug("Table creation verified");
        } else {
          Log.error("Could not verifiy the creation of '" + getTable() + "." + getSchema() + "' table, expect subsequent database operations to fail.");
        }
      }

    }

    // get the schema for the database table we are using
    //tableschema = getTableSchema(getTable());

    return true;
  }




  /**
  * <p>Mappings are generally those suggested in the Oracle JDBC mapping guide 
  * with minor exceptions for DECIMAL and NUMERIC as BigDecimal is not 
  * supported by data frame at this time.
  * 
  * @param tablename name of the table being generated
  * 
  * @return a table schema for the database table to which this writer is writing.
  */
  private TableDefinition getTableSchema(String tablename) {
    TableDefinition retval = null;
    if (StringUtil.isNotBlank(tablename)) {
      Connection conn = getConnection();
      if (conn == null) {
        context.setError("Could not connect to the database");
        return null;
      }

      String tableSchemaName = null;

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = conn.getMetaData();

        // get all the tables so we can perform a case insensitive search
        rs = meta.getTables(null, null, "%", null);
        while (rs.next()) {
          if (StringUtil.equalsIgnoreCase(tablename, rs.getString("TABLE_NAME"))) {
            tableSchemaName = rs.getString("TABLE_NAME");
            break;
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
        context.setError("Problems confirming table name: " + e.getMessage());
      } finally {
        if (rs != null) {
          try {
            rs.close();
          } catch (SQLException ignore) {
            //ignore.printStackTrace();
          }
        }
      }

      if (StringUtil.isNotEmpty(tableSchemaName)) {
        retval = new TableDefinition(tableSchemaName);

        rs = null;
        try {
          DatabaseMetaData meta = conn.getMetaData();
          rs = meta.getColumns(null, null, tableSchemaName, "%");

          String name;
          ColumnType type;
          int length;
          boolean readOnly;
          boolean mandatory;
          boolean primaryKey;
          boolean unique;
          boolean nullable;
          int pos;
          String remarks;

          while (rs.next()) {
            readOnly = nullable = mandatory = primaryKey = unique = false;
            length = pos = 0;
            name = remarks = null;

            // If there is a catalog name and it is not already set, set it
            if (rs.getString("TABLE_CAT") != null && retval.getCatalogName() == null) {
              retval.setCatalogName(rs.getString("TABLE_CAT"));
            }

            // If there is a schema name and it is not already set, set it
            if (rs.getString("TABLE_SCHEM") != null && retval.getSchemaName() == null) {
              retval.setSchemaName(rs.getString("TABLE_SCHEM"));
            }

            // Retrieve data about the column
            name = rs.getString("COLUMN_NAME");
            length = rs.getInt("COLUMN_SIZE");
            pos = rs.getInt("ORDINAL_POSITION");
            remarks = rs.getString("REMARKS");

            switch (rs.getInt("DATA_TYPE")) {
              case Types.TIME:
              case Types.TIMESTAMP:
              case Types.DATE:
                type = ColumnType.DATE;
                break;
              case Types.BOOLEAN:
                type = ColumnType.BOOLEAN;
                break;
              case Types.TINYINT:
                type = ColumnType.BYTE;
                break;
              case Types.SMALLINT:
                type = ColumnType.SHORT;
                break;
              case Types.INTEGER:
                type = ColumnType.INT;
                break;
              case Types.FLOAT:
              case Types.DOUBLE:
              case Types.REAL:
                type = ColumnType.FLOAT;
                break;
              case Types.DECIMAL:
              case Types.NUMERIC:
                type = ColumnType.DOUBLE;
                break;
              case Types.BIGINT:
                type = ColumnType.LONG;
                break;
              case Types.DISTINCT:
                unique = true;
                type = ColumnType.STRING;
                break;
              default:
                type = ColumnType.STRING;
                break;
            }

            switch (rs.getInt("NULLABLE")) {
              case DatabaseMetaData.columnNoNulls:
                nullable = false;
                break;
              case DatabaseMetaData.columnNullable:
                nullable = true;
                break;
              case DatabaseMetaData.columnNullableUnknown:
                nullable = false;
                break;
              default:
                nullable = false;
                break;
            }

            retval.addColumn(new ColumnDefinition(name, type, length, nullable, readOnly, mandatory, primaryKey, unique, remarks, pos));

          }

          //System.out.println(retval);

        } catch (SQLException e) {
          e.printStackTrace();
          context.setError("Problems confirming table columns: " + e.getMessage());
        } finally {
          if (rs != null) {
            try {
              rs.close();
            } catch (SQLException ignore) {
              //ignore.printStackTrace();
            }
          }
        }

      }

    }
    return retval;
  }

}
