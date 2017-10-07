/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.ColumnDefinition;
import coyote.commons.jdbc.ColumnType;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.jdbc.TableDefinition;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FrameSet;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.Symbols;
import coyote.dx.db.Database;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is an operational context backed by a database which allows values to
 * be persisted on remote systems.
 *
 * <p>The data in the context can be managed externally without regard to
 * where the transform is being run, allowing transforms to be run on many
 * different hosts without having to manage locally persisted context data.
 *
 * <p>Key value pairs specified in the fields section are used to reset the
 * field values in the database context at the start of the job. Their values
 * will be persisted when the context is closed. Other jobs using the context
 * will then have access to these values unless they reset them in a similar
 * fashion.
 *
 * <p>The primary use case is the running of transform jobs in a pool of
 * distributed instances in the cloud. A particular instance will run one on
 * one host and run another time on a different host. Another use case is
 * running jobs in virtual machines with ephemeral file systems such as Heroku.
 * The VM is restarted at least daily with a fresh file system and all local
 * files are lost. This context allows persistent data to be stored remotely
 * so that local file access is not required.
 *
 * <p>Unlike a writer, this component deals with fields of a dataframe not the
 * dataframe itself. Reach field is a record in the table differentiated by
 * the field name and the name of the job to which it belongs.
 */
public class DatabaseContext extends PersistentContext {

  private static final String TABLE_NAME = "Context";
  private static final String SCHEMA_NAME = "DX";

  /** The JDBC connection used by this context to interact with the database */
  protected Connection connection;

  /** Component we use to handle connection creation */
  private Database database = null;

  /** Our identity to record in the context on inserts and update operations */
  String identity = null;

  /** The list of existing fields in the database for this job */
  FrameSet existingFields = null;

  /** Name of the database product being used */
  String databaseProduct = null;




  /**
   * @see coyote.dx.context.TransformContext#close()
   */
  @Override
  public void close() {
    final DataFrame frame = new DataFrame();
    for (final String key : properties.keySet()) {
      try {
        frame.add(key, properties.get(key));
      } catch (final Exception e) {
        Log.debug("Cannot persist property '" + key + "' - " + e.getMessage());
      }
    }

    frame.put(Symbols.RUN_COUNT, runcount);

    final Object rundate = get(Symbols.DATETIME);
    if (rundate != null) {
      if (rundate instanceof Date) {
        frame.put(Symbols.PREVIOUS_RUN_DATETIME, rundate);
      } else {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Context.run_date_reset", rundate));
      }
    }

    upsertFields(connection, TABLE_NAME, frame);

    super.close();
  }




  /**
   * Create the table necessary to store named values for a named job.
   *
   * <p>This should not be the final table; it should be reviewed and altered
   * by the DBA to meet the needs of the application. This is to help ensure
   * quick deployment and operation only, not production use.
   */
  private void createTables() {

    String sql = DatabaseDialect.getCreateSchema(databaseProduct, SCHEMA_NAME, database.getUserName());
    Log.debug("Creating table in database...");
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(sql);
      Log.debug("Schema created.");
    } catch (final SQLException e) {
      Log.error("Schema creation failed!");
      e.printStackTrace();
    }

    final TableDefinition tdef = new TableDefinition(TABLE_NAME);
    tdef.setSchemaName(SCHEMA_NAME);
    tdef.addColumn(new ColumnDefinition("SysId", ColumnType.STRING).setLength(36).setPrimaryKey(true));
    tdef.addColumn(new ColumnDefinition("Job", ColumnType.STRING).setLength(64));
    tdef.addColumn(new ColumnDefinition("Name", ColumnType.STRING).setLength(64));
    tdef.addColumn(new ColumnDefinition("Value", ColumnType.STRING).setLength(255).setNullable(true));
    tdef.addColumn(new ColumnDefinition("Type", ColumnType.SHORT).setNullable(true));
    tdef.addColumn(new ColumnDefinition("CreatedBy", ColumnType.STRING).setLength(32));
    tdef.addColumn(new ColumnDefinition("CreatedOn", ColumnType.DATE));
    tdef.addColumn(new ColumnDefinition("ModifiedBy", ColumnType.STRING).setLength(32));
    tdef.addColumn(new ColumnDefinition("ModifiedOn", ColumnType.DATE));

    sql = DatabaseDialect.getCreate(databaseProduct, tdef);

    Log.debug("Creating table in database...");
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(sql);
      Log.debug("Table created.");
    } catch (final SQLException e) {
      Log.error("Table creation failed!");
      e.printStackTrace();
    }
  }




  /**
   * Attempt to determine what database user this context is running as so the
   * identity of insert and update or records can be recorded.
   */
  private void determineIdentity() {
    identity = getIdentity();
    if (identity == null) {
      if (StringUtil.isNotBlank(database.getUserName())) {
        identity = database.getUserName();
      } else if (StringUtil.isNotBlank(database.getConnectedUser(connection))) {
        identity = database.getConnectedUser(connection);
      } else {
        identity = this.getClass().getSimpleName();
      }
    }
  }




  public String getIdentity() {
    return configuration.getString(ConfigTag.IDENTITY);
  }




  @SuppressWarnings("unchecked")
  private void insertField(final DataField field, final SymbolTable sqlsymbols) {
    sqlsymbols.put(DatabaseDialect.FIELD_NAMES_SYM, "SysId, Job, Name, Value, Type, CreatedBy, CreatedOn, ModifiedBy, ModifiedOn");
    sqlsymbols.put(DatabaseDialect.FIELD_VALUES_SYM, "?, ?, ?, ?, ?, ?, ?, ?, ?");
    final String sql = DatabaseDialect.getSQL(databaseProduct, DatabaseDialect.INSERT, sqlsymbols);
    if (sql != null) {
      try {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, UUID.randomUUID().toString());
        preparedStatement.setString(2, getEngine().getName());
        preparedStatement.setString(3, field.getName());
        preparedStatement.setString(4, field.getStringValue());
        preparedStatement.setInt(5, field.getType());
        preparedStatement.setString(6, identity);
        preparedStatement.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
        preparedStatement.setString(8, identity);
        preparedStatement.setTimestamp(9, new java.sql.Timestamp(new Date().getTime()));
        preparedStatement.executeUpdate();
      } catch (final SQLException e) {
        Log.fatal(ExceptionUtil.toString(e));
        Log.debug(ExceptionUtil.stackTrace(e));
      }
    } else {
      Log.error("Cannot support " + databaseProduct + " database product");
    }
  }




  private boolean isAutoCreate() {
    return configuration.getBoolean(ConfigTag.AUTO_CREATE);
  }




  /**
   * <p>The context is one of the first components opened because all the rest
   * of the components need an initialized/opened context to perform their
   * functions and to share data.
   *
   * @see coyote.dx.context.TransformContext#open()
   */
  @Override
  public void open() {

    if (configuration != null) {

      String name = null;

      // TODO: Optionally get database from context e.g. "database":"Oracle5"

      if (getEngine() == null) {
        Log.fatal("Context is not connected to a transform engine!");
        setError("No engine set in context");
        return;
      } else {
        name = getEngine().getName();
      }

      if (StringUtil.isNotBlank(name)) {
        database = new Database();
        try {
          database.setConfiguration(configuration);
          database.open(null);
          connection = database.getConnection();

          try {
            if (!connection.isValid(10)) {
              Log.fatal("Database connection is not valid");
              setError("Database connection is not valid");
              return;
            }
            databaseProduct = database.getProductName(connection);
            Log.debug("Context is using a " + databaseProduct + " database");
          } catch (final SQLException e) {
            Log.fatal("Database connection is not valid");
            setError("Database connection is not valid");
            return;
          }

          determineIdentity();
          verifyTables();
          readfields(name);
          incrementRunCount();
          setPreviousRunDate();
          updateSymbols();
          updateFields();
        } catch (final ConfigurationException e) {
          e.printStackTrace();
        }

      }
    }

  }




  /**
   * Read in all the context fields from the database with the given name.
   *
   * @param name the name of the context (i.e. job name) to query.
   */
  private void readfields(final String name) {
    Log.debug("Reading fields for context '" + name + "' on " + databaseProduct);
    existingFields = DatabaseUtil.readAllRecords(connection, "select * from " + SCHEMA_NAME + "." + TABLE_NAME + " where Job = '" + name + "'");
    for (final DataFrame frame : existingFields.getRows()) {
      Log.debug("Read in context variable:" + frame.toString());
      final DataField keyField = frame.getFieldIgnoreCase("Name");
      if ((keyField != null) && StringUtil.isNotBlank(keyField.getStringValue())) {
        final DataField valueField = frame.getFieldIgnoreCase("Value");
        if ((valueField != null) && valueField.isNotNull()) {
          final DataField typeField = frame.getFieldIgnoreCase("Type");
          if ((typeField != null) && typeField.isNotNull()) {
            final Object contextValue = DataField.parse(valueField.getStringValue(), (short)typeField.getObjectValue());
            if (contextValue != null) {
              set(keyField.getStringValue(), contextValue);
            } else {
              set(keyField.getStringValue(), valueField.getStringValue());
            }
          } else {
            set(keyField.getStringValue(), valueField.getStringValue());
          }
        }
      }
    }
  }




  @SuppressWarnings("unchecked")
  private void upsertFields(final Connection conn, final String tableName, final DataFrame frame) {
    final SymbolTable sqlsymbols = new SymbolTable();
    sqlsymbols.put(DatabaseDialect.DB_SCHEMA_SYM, SCHEMA_NAME);
    sqlsymbols.put(DatabaseDialect.TABLE_NAME_SYM, TABLE_NAME);

    String sql = null;

    for (final DataField field : frame.getFields()) {
      final DataFrame existingFrame = existingFields.getFrameByColumnValue("Name", field.getName());
      if (existingFrame != null) {
        final DataField sysIdField = existingFrame.getFieldIgnoreCase("SysId");
        if (sysIdField != null) {
          String existingValue = null;
          final DataField valueField = existingFrame.getFieldIgnoreCase("Value");
          if (valueField != null) {
            existingValue = valueField.getStringValue();
          }
          // Only update if the value is different
          if (!field.getStringValue().equals(existingValue)) {
            Log.debug("Field:" + field.getName() + " was '" + existingValue + "' and now is '" + field.getStringValue() + "'");

            sqlsymbols.put(DatabaseDialect.FIELD_MAP_SYM, "Value=?, Type=?, ModifiedBy=?, ModifiedOn=?");
            sqlsymbols.put(DatabaseDialect.SYS_ID_SYM, sysIdField.getStringValue());
            sql = DatabaseDialect.getSQL(databaseProduct, DatabaseDialect.UPDATE, sqlsymbols);

            if (sql != null) {
              try {
                final PreparedStatement preparedStatement = conn.prepareStatement(sql);
                if (field.getType() == DataField.DATE) {
                  preparedStatement.setString(1, new SimpleDateFormat(CDX.DEFAULT_DATETIME_FORMAT).format((Date)field.getObjectValue()));
                } else {
                  preparedStatement.setString(1, field.getStringValue());
                }
                preparedStatement.setInt(2, field.getType());
                preparedStatement.setString(3, identity);
                preparedStatement.setTimestamp(4, new java.sql.Timestamp(new Date().getTime()));
                preparedStatement.executeUpdate();
              } catch (final SQLException e) {
                Log.fatal(ExceptionUtil.toString(e));
              }
            } else {
              Log.error("Cannot support " + databaseProduct + " database product");
            }

          }
        } else {
          Log.error("Existing field does not contain a sysid: " + existingFrame.toString());
          insertField(field, sqlsymbols);
        }
      } else {
        insertField(field, sqlsymbols);
      }
    }
  }




  /**
   * Make sure the tables exist.
   */
  private void verifyTables() {
    if (!DatabaseUtil.tableExists(TABLE_NAME, connection)) {
      if (isAutoCreate()) {
        createTables();
      }
    }
  }

}
