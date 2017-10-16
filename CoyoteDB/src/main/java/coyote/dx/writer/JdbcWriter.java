/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DISTINCT;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.commons.jdbc.ColumnDefinition;
import coyote.commons.jdbc.ColumnType;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.jdbc.TableDefinition;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.FrameSet;
import coyote.dx.CDB;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.DataSetMetrics;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.dx.db.Database;
import coyote.dx.db.DatabaseConnector;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * <p>There is an inherent loss of fidelity with this class in that not all SQL 
 * types are replicated faithfully through the writer. For example, the 
 * differences between VARCHAR, VARCHAR2, STRING are merged into a String type. 
 * NUMERIC and LONG are simply the Long type. The goal of this writer is to 
 * support the widest number of types across not only different data bases, but 
 * data storage technologies and network protocols. The core intermediate 
 * format (DataFrame) is designed to support data types common to many 
 * different technologies and therefore dictates what the framework 
 * supports.</p> 
 */
public class JdbcWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  /** The thing we use to get connections to the database */
  private DatabaseConnector connector = null;

  protected static final SymbolTable symbolTable = new SymbolTable();

  /** The schema of all the frames we have read in so far. */
  private DataSetMetrics schema = new DataSetMetrics();

  /** The database table definition */
  private TableDefinition tableschema = null;

  /** The JDBC connection used by this writer to interact with the database */
  protected Connection connection;

  /** The database product name (Oracle, H2, etc.) to which we are connected. */
  private String database = null;

  /** Cached value of the Auto-Adjust flag to alter tables if necessary. */
  private volatile boolean autoAdjust = false;

  /** The number of records we should batch before executing an UPDATE */
  protected int batchsize = 0;

  /** The collection of frames to be written; the batch of records to write. */
  protected final FrameSet frameset = new FrameSet();

  /** The SQL INSERT statement we use for writing the batch */
  protected String SQL = null;

  protected PreparedStatement ps = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    String token = getString(ConfigTag.TABLE);
    if (StringUtil.isBlank(token)) {
      throw new ConfigurationException("Invalid '" + ConfigTag.TABLE + "' configuration attribute of '" + token + "'");
    }
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

    if (frameset.size() > 0) {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.completing_batch", getClass().getSimpleName(), frameset.size()));
      writeBatch();
      frameset.clearAll();
    }

    if (connection != null) {
      try {
        commit();
      } catch (final SQLException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Writer.could_not_commit_prior_to_close", e.getMessage()));
      }
    }

    if (ps != null) {
      try {
        ps.close();
        ps = null;
      } catch (final SQLException e) {
        Log.error(LogMsg.createMsg(CDX.MSG, "Writer.Could not close prepared statememt: {%s}", e.getMessage()));
      }
    }

    if (connection != null) {
      // if it looks like we created the connection ourselves (e.g. we have a 
      // configured target) close the connection
      if (StringUtil.isNotBlank(getTarget())) {
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.closing_connection", getClass().getSimpleName(), getTarget()));

        try {
          connection.close();
          connection = null;
        } catch (final SQLException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Writer.Could not close connection cleanly: {%s}", e.getMessage()));
        }
      }
    }

    schema.clear();
    super.close();
  }




  public void commit() throws SQLException {
    connection.commit();
  }




  /**
   * @return the insert SQL appropriate for this frameset
   */
  private String generateInsertSQL() {
    final StringBuffer c = new StringBuffer("insert into ");
    final StringBuffer v = new StringBuffer();

    c.append(getSchema());
    c.append('.');
    c.append(getTable());
    c.append(" (");
    for (final String name : frameset.getColumns()) {
      c.append(name);
      c.append(", ");
      v.append("?, ");
    }
    c.delete(c.length() - 2, c.length());
    v.delete(v.length() - 2, v.length());

    c.append(") values (");
    c.append(v.toString());
    c.append(")");

    return c.toString();
  }




  public int getBatchSize() {
    try {
      return configuration.getAsInt(ConfigTag.BATCH);
    } catch (final DataFrameException ignore) {}
    return 0;
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
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.connected_to", getClass().getSimpleName(), getTarget()));
          DatabaseMetaData meta = connection.getMetaData();
          String product = meta.getDatabaseProductName();
          database = product.toUpperCase();

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
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.connected_to_product", getClass().getSimpleName(), meta.getDatabaseProductName(), meta.getDatabaseProductVersion(), meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion()));
        } else {
          getContext().setError("Connector could not get a connection to the database");
        }
      } catch (SQLException e) {
        getContext().setError("Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      }
    }
    return connection;
  }




  public String getTable() {
    return configuration.getString(ConfigTag.TABLE);
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




  public boolean isAutoAdjust() {
    return autoAdjust;
  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.setContext(context);

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
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_target", getClass().getSimpleName(), database.getTarget()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_driver", getClass().getSimpleName(), database.getDriver()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_library", getClass().getSimpleName(), database.getLibrary()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_user", getClass().getSimpleName(), database.getUserName()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_password", getClass().getSimpleName(), StringUtil.isBlank(database.getPassword()) ? 0 : database.getPassword().length()));
          }
        } catch (ConfigurationException e) {
          context.setError("Could not configure database connector: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        // if there is no schema in the configuration, set it to the same as the username
        if (StringUtil.isBlank(getString(ConfigTag.SCHEMA))) {
          getConfiguration().set(ConfigTag.SCHEMA, database.getUserName());
        }
      }
    } else {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_existing_connection", getClass().getSimpleName()));
    }

    setSchema(getString(ConfigTag.SCHEMA));
    if (StringUtil.isBlank(getString(ConfigTag.SCHEMA))) {
      context.setError("Could not determine the '" + ConfigTag.SCHEMA + "' value");
    }
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_schema", getClass().getSimpleName(), getSchema()));

    setTable(getString(ConfigTag.TABLE));
    if (StringUtil.isBlank(getString(ConfigTag.TABLE))) {
      context.setError("Could not determine the '" + ConfigTag.TABLE + "' value");
    }
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_table", getClass().getSimpleName(), getTable()));

    setAutoCreate(getBoolean(ConfigTag.AUTO_CREATE));
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.autocreate_tables", getClass().getSimpleName(), isAutoCreate()));

    setAutoAdjust(getBoolean(ConfigTag.AUTO_ADJUST));
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.autoadjust_tables", getClass().getSimpleName(), isAutoAdjust()));

    setBatchSize(getInteger(ConfigTag.BATCH));
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_batch_size", getClass().getSimpleName(), getBatchSize()));

    // validate and cache our batch size
    if (getBatchSize() < 1) {
      batchsize = 0;
    } else {
      batchsize = getBatchSize();
    }

  }




  /**
   * @param value true to automatically create the table based on the sizes of data observed so far, false to fail with an error if the table does not exist.
   */
  public void setAutoCreate(final boolean value) {
    configuration.put(ConfigTag.AUTO_CREATE, value);
  }




  /**
   * @param value true to automatically adjust column sizes to accommodate data, false to throw error
   */
  public void setAutoAdjust(final boolean value) {
    autoAdjust = value;
    configuration.put(ConfigTag.AUTO_ADJUST, value);
  }




  /**
   * @param value
   */
  public void setBatchSize(final int value) {
    batchsize = value;
    configuration.put(ConfigTag.BATCH, value);
  }




  /**
   * @param conn
   */
  public void setConnection(final Connection conn) {
    connection = conn;
  }




  /**
   * Set the given data file into the given prepared statement at the given 
   * index in the statement.
   * 
   * <p>This ensures the correct data is placed in the prepared statement with 
   * the appropriate type. This also checks for nulls.</p>
   * 
   * @param pstmt the prepared statement to which to add data
   * @param indx the index into the value set 
   * @param field the field containing the value to add
   */
  private void setData(final PreparedStatement pstmt, final int indx, final DataField field) {
    final short type = field.getType();
    try {
      switch (type) {
        case DataField.FRAMETYPE:
          getContext().setError("Cannot add complex objects to table");
          break;
        case DataField.UDEF:
          if (field.isNull()) {
            pstmt.setNull(indx, VARCHAR);
          } else {
            pstmt.setString(indx, "");
          }
          break;
        case DataField.BYTEARRAY:
          getContext().setError("Cannot add byte arrays to table");
          break;
        case DataField.STRING:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "String"));
          if (field.isNull()) {
            pstmt.setNull(indx, VARCHAR);
          } else {
            pstmt.setString(indx, field.getStringValue());
          }
          break;
        case DataField.S8:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "S8-byte"));
          if (field.isNull()) {
            pstmt.setNull(indx, TINYINT);
          } else {
            pstmt.setByte(indx, (byte)field.getObjectValue());
          }
          break;
        case DataField.U8:
        case DataField.S16:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "S16-Short"));
          if (field.isNull()) {
            pstmt.setNull(indx, SMALLINT);
          } else {
            pstmt.setShort(indx, (Short)field.getObjectValue());
          }
          break;
        case DataField.U16:
        case DataField.S32:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "S32-Integer"));
          if (field.isNull()) {
            pstmt.setNull(indx, INTEGER);
          } else {
            pstmt.setInt(indx, (Integer)field.getObjectValue());
          }
          break;
        case DataField.U32:
        case DataField.S64:
        case DataField.U64:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "S64-Long"));
          if (field.isNull()) {
            pstmt.setNull(indx, BIGINT);
          } else {
            pstmt.setLong(indx, (Integer)field.getObjectValue());
          }
          break;
        case DataField.FLOAT:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "Float"));
          if (field.isNull()) {
            pstmt.setNull(indx, FLOAT);
          } else {
            pstmt.setFloat(indx, (Float)field.getObjectValue());
          }
          break;
        case DataField.DOUBLE:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "Double"));
          if (field.isNull()) {
            pstmt.setNull(indx, DOUBLE);
          } else {
            pstmt.setDouble(indx, (Double)field.getObjectValue());
          }
          break;
        case DataField.BOOLEANTYPE:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "Boolean"));
          if (field.isNull()) {
            pstmt.setNull(indx, BOOLEAN);
          } else {
            pstmt.setBoolean(indx, (Boolean)field.getObjectValue());
          }
          break;
        case DataField.DATE:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "Timestamp"));
          if (field.isNull()) {
            pstmt.setNull(indx, TIMESTAMP);
          } else {
            final Object obj = field.getObjectValue();
            pstmt.setTimestamp(indx, DatabaseUtil.getTimeStamp((Date)obj));
          }
          break;
        case DataField.URI:
          Log.debug(LogMsg.createMsg(CDB.MSG, "Database.saving_field_as", getClass().getSimpleName(), field.getName(), indx, "String"));
          pstmt.setString(indx, field.getStringValue());
          break;
        case DataField.ARRAY:
          getContext().setError("Cannot add arrays to table field");
          break;
        default:
          // Everything else is set to null
          pstmt.setNull(indx, VARCHAR);
          break;
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }

  }




  /**
   * @param value
   */
  private void setSchema(final String value) {
    configuration.put(ConfigTag.SCHEMA, value);
  }




  /**
   * @param value
   */
  public void setTable(final String value) {
    configuration.put(ConfigTag.TABLE, value);
  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(final DataFrame frame) {

    // have the schema collect data on the frame to compile metadata on frames
    schema.sample(frame);

    // If there is a conditional expression
    if (expression != null) {

      try {
        // if the condition evaluates to true...
        if (evaluator.evaluateBoolean(expression)) {
          writeFrame(frame);
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage()));
      }
    } else {
      // Unconditionally writing frame
      writeFrame(frame);
    }

  }




  private void writeBatch() {

    if (SQL == null) {
      // Since this is the first time we have tried to write to the table, make
      // sure the table exists
      if (checkTable()) {
        SQL = generateInsertSQL();
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_sql", getClass().getSimpleName(), SQL));

        final Connection connection = getConnection();
        try {
          ps = connection.prepareStatement(SQL);
        } catch (final SQLException e) {
          getContext().setError(LogMsg.createMsg(CDX.MSG, "Writer.preparedstatement_exception", getClass().getSimpleName(), e.getMessage()).toString());
        }
      }
    }

    if (isAutoAdjust()) {
      for (final String name : frameset.getColumns()) {
        if (schema.getMetric(name).getMaximumStringLength() > tableschema.findColumn(name).getLength()) {
          // if auto adjust, check the size of the string and issue an 
          // "alter table" command to adjust the size of the column if the 
          // string is too large to fit
          Log.debug("The " + database + " table '" + tableschema.getName() + "' must be altered to fit the '" + name + "' value; table allows a size of " + tableschema.findColumn(name).getLength() + " but data requires " + schema.getMetric(name).getMaximumStringLength());

          PreparedStatement aps = null;
          String alterSql = "ALTER TABLE " + getSchema() + "." + getTable() + " ALTER COLUMN " + name + " VARCHAR2(" + schema.getMetric(name).getMaximumStringLength() + ")";
          try {
            aps = connection.prepareStatement(alterSql);
            aps.execute();
          } catch (final SQLException e) {
            getContext().setError(LogMsg.createMsg(CDX.MSG, "Writer.preparedstatement_exception", getClass().getSimpleName(), e.getMessage()).toString());
          } finally {
            if (aps != null) {
              try {
                aps.close();
              } catch (SQLException ignore) {
                // quiet
              }
            }
          }

          // alter table Employee alter column salary numeric(22,5)
          // set the size in the tableschema
          tableschema.findColumn(name).setLength(schema.getMetric(name).getMaximumStringLength());

          //DatabaseDialect.alterTable()
        }
      }
    }

    // if the table check did not generate an error
    if (getContext().isNotInError()) {
      if (batchsize <= 1) {
        final DataFrame frame = frameset.get(0);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_single_frame", getClass().getSimpleName(), frame.toString()));

        int indx = 1;
        for (final String name : frameset.getColumns()) {
          final DataField field = frame.getField(name);
          setData(ps, indx++, field);
          if (getContext().isInError()) {
            break;
          }
        }

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.executing_sql", getClass().getSimpleName(), ps.toString()));

        try {
          ps.execute();
        } catch (final SQLException e) {
          getContext().setError("Could not insert single row: " + e.getMessage());
        }

      } else {
        // Now write a batch
        for (final DataFrame frame : frameset.getRows()) {
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_frame", this.getClass().getSimpleName(), frame));

          int indx = 1;
          for (final String name : frameset.getColumns()) {
            final DataField field = frame.getField(name);
            if ((field != null) && !field.isNull()) {
              setData(ps, indx++, field);
            }
            if (getContext().isInError()) {
              break;
            }
          }

          // add this frame as a record to the batch
          try {
            ps.addBatch();
          } catch (final SQLException e) {
            getContext().setError("Could not add the record to the batch: " + e.getMessage());
          }

        }
        if (getContext().isNotInError()) {
          try {
            ps.executeBatch();
          } catch (final SQLException e) {
            getContext().setError("Could not insert batch: " + e.getMessage());
          }
        }
      }
      frameset.clearRows();
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
    if (!DatabaseUtil.tableExists(getTable(), getConnection())) {

      if (isAutoCreate()) {
        Connection conn = getConnection();

        if (conn == null) {
          Log.error("Cannot get database connection");
          context.setError("Could not connect to the database");
          return false;
        }

        symbolTable.put(DatabaseDialect.TABLE_NAME_SYM, getTable());
        symbolTable.put(DatabaseDialect.DB_SCHEMA_SYM, getSchema());

        String command = DatabaseDialect.getCreate(database, schema, symbolTable);

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.creating_table", getClass().getSimpleName(), getTable(), command));

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

        if (DatabaseUtil.tableExists(getTable(), getSchema(), getConnection())) {
          Log.debug("Table creation verified");
        } else {
          Log.error("Could not verifiy the creation of '" + getTable() + "." + getSchema() + "' table, expect subsequent database operations to fail.");
        }
      }

    }

    // get the schema for the database table we are using
    tableschema = getTableSchema(getTable());

    return true;
  }




  /**
   * 
   */
  private void checkSchema() {
    Log.debug("Looking for schema '" + getSchema() + "'");
    if (!DatabaseUtil.schemaExists(getSchema(), getConnection())) {
      if (isAutoCreate()) {
        String username = getConnector().getUserName();
        String sql = DatabaseDialect.getCreateSchema(database, getSchema(), username);
        Log.debug("Schema '" + getSchema() + "' not found in database, creating it with:\n" + sql);
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate(sql);
          Log.debug("Schema created.");

          if (DatabaseUtil.schemaExists(getSchema(), getConnection())) {
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
   * <p>Mappings are generally those suggested in the Oracle JDBC mapping guide 
   * with minor exceptions for DECIMAL and NUMERIC as BigDecimal is not 
   * supported by Data Frame at this time.</p>
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
              case TIME:
              case TIMESTAMP:
              case DATE:
                type = ColumnType.DATE;
                break;
              case BOOLEAN:
                type = ColumnType.BOOLEAN;
                break;
              case TINYINT:
                type = ColumnType.BYTE;
                break;
              case SMALLINT:
                type = ColumnType.SHORT;
                break;
              case INTEGER:
                type = ColumnType.INT;
                break;
              case FLOAT:
              case DOUBLE:
              case REAL:
                type = ColumnType.FLOAT;
                break;
              case DECIMAL:
              case NUMERIC:
                type = ColumnType.DOUBLE;
                break;
              case BIGINT:
                type = ColumnType.LONG;
                break;
              case DISTINCT:
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




  /**
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame(final DataFrame frame) {
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_fields", getClass().getSimpleName(), frame.size()));
    frameset.add(frame);

    if (frameset.size() >= batchsize) {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_batch", getClass().getSimpleName(), frameset.size(), batchsize));
      writeBatch();
    }

  }




  /**
   * Return the connector we use for 
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

}
