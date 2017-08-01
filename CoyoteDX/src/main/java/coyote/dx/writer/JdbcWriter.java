/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import coyote.commons.JdbcUtil;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.FrameSet;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.dx.db.ColumnDefinition;
import coyote.dx.db.ColumnType;
import coyote.dx.db.DatabaseDialect;
import coyote.dx.db.MetricSchema;
import coyote.dx.db.TableDefinition;
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

  protected static final SymbolTable symbolTable = new SymbolTable();

  /** The schema of all the frames we have read in so far. */
  private MetricSchema schema = new MetricSchema();

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
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

    if (frameset.size() > 0) {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.completing_batch", getClass().getName(), frameset.size()));
      writeBatch();
      frameset.clearAll();
    }

    if (ps != null) {
      try {
        ps.close();
      } catch (final SQLException e) {
        Log.error(LogMsg.createMsg(CDX.MSG, "Writer.Could not close prepared statememt: {%s}", e.getMessage()));
      }
    }

    if (connection != null) {
      try {
        commit();
      } catch (final SQLException e) {
        Log.error(LogMsg.createMsg(CDX.MSG, "Writer.Could not commit prior to close: {%s}", e.getMessage()));
      }

      // if it looks like we created the connection ourselves (e.g. we have a 
      // configured target) close the connection
      if (StringUtil.isNotBlank(getTarget())) {
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.closing_connection", getClass().getName(), getTarget()));

        try {
          connection.close();
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
          database = product.toUpperCase();

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




  public String getDriver() {
    return configuration.getAsString(ConfigTag.DRIVER);
  }




  public String getLibrary() {
    return configuration.getAsString(ConfigTag.LIBRARY);
  }




  public String getPassword() {
    return configuration.getAsString(ConfigTag.PASSWORD);
  }




  public String getTable() {
    return configuration.getAsString(ConfigTag.TABLE);
  }




  public String getUsername() {
    return configuration.getAsString(ConfigTag.USERNAME);
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
      // get our configuration data
      setTarget(getString(ConfigTag.TARGET));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_target", getClass().getName(), getTarget()));

      setTable(getString(ConfigTag.TABLE));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_table", getClass().getName(), getTable()));

      setUsername(getString(ConfigTag.USERNAME));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_user", getClass().getName(), getUsername()));

      setPassword(getString(ConfigTag.PASSWORD));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_password", getClass().getName(), StringUtil.isBlank(getPassword()) ? 0 : getPassword().length()));

      setDriver(getString(ConfigTag.DRIVER));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_driver", getClass().getName(), getDriver()));

      setAutoCreate(getBoolean(ConfigTag.AUTO_CREATE));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.autocreate_tables", getClass().getName(), isAutoCreate()));

      setAutoAdjust(getBoolean(ConfigTag.AUTO_ADJUST));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.autoadjust_tables", getClass().getName(), isAutoAdjust()));

      setBatchSize(getInteger(ConfigTag.BATCH));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_batch_size", getClass().getName(), getBatchSize()));

      setLibrary(getString(ConfigTag.LIBRARY));
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_driver", getClass().getName(), getLibrary()));
      //TODO: try to ensure the JAR exists

    } else {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_existing_connection", getClass().getName()));
    }

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
  private void setBatchSize(final int value) {
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
            pstmt.setNull(indx, java.sql.Types.VARCHAR);
          } else {
            pstmt.setString(indx, "");
          }
          break;
        case DataField.BYTEARRAY:
          getContext().setError("Cannot add byte arrays to table");
          break;
        case DataField.STRING:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "String"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.VARCHAR);
          } else {
            pstmt.setString(indx, field.getStringValue());
          }
          break;
        case DataField.S8:
        case DataField.U8:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S8-byte"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.TINYINT);
          } else {
            pstmt.setByte(indx, (byte)field.getObjectValue());
          }
          break;
        case DataField.S16:
        case DataField.U16:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S16-Short"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.SMALLINT);
          } else {
            pstmt.setShort(indx, (Short)field.getObjectValue());
          }
          break;
        case DataField.S32:
        case DataField.U32:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S32-Integer"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.INTEGER);
          } else {
            pstmt.setInt(indx, (Integer)field.getObjectValue());
          }
          break;
        case DataField.S64:
        case DataField.U64:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S64-Long"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.BIGINT);
          } else {
            pstmt.setLong(indx, (Integer)field.getObjectValue());
          }
          break;
        case DataField.FLOAT:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Float"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.FLOAT);
          } else {
            pstmt.setFloat(indx, (Float)field.getObjectValue());
          }
          break;
        case DataField.DOUBLE:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Double"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.DOUBLE);
          } else {
            pstmt.setDouble(indx, (Double)field.getObjectValue());
          }
          break;
        case DataField.BOOLEANTYPE:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Boolean"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.BOOLEAN);
          } else {
            pstmt.setBoolean(indx, (Boolean)field.getObjectValue());
          }
          break;
        case DataField.DATE:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Timestamp"));
          if (field.isNull()) {
            pstmt.setNull(indx, java.sql.Types.TIMESTAMP);
          } else {
            final Object obj = field.getObjectValue();
            pstmt.setTimestamp(indx, JdbcUtil.getTimeStamp((Date)obj));
          }
          break;
        case DataField.URI:
          Log.debug(LogMsg.createMsg(CDX.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "String"));
          pstmt.setString(indx, field.getStringValue());
          break;
        case DataField.ARRAY:
          getContext().setError("Cannot add arrays to table field");
          break;
        default:
          // Everything else is set to null
          pstmt.setNull(indx, java.sql.Types.VARCHAR);
          break;
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }

  }




  /**
   * @param value
   */
  private void setDriver(final String value) {
    configuration.put(ConfigTag.DRIVER, value);
  }




  /**
   * @param value
   */
  private void setLibrary(final String value) {
    configuration.put(ConfigTag.LIBRARY, value);
  }




  /**
   * @param value
   */
  private void setPassword(final String value) {
    configuration.put(ConfigTag.PASSWORD, value);
  }




  /**
   * @param value
   */
  public void setTable(final String value) {
    configuration.put(ConfigTag.TABLE, value);
  }




  /**
   * @param value
   */
  public void setUsername(final String value) {
    configuration.put(ConfigTag.USERNAME, value);
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
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_sql", getClass().getName(), SQL));

        final Connection connection = getConnection();
        try {
          ps = connection.prepareStatement(SQL);
        } catch (final SQLException e) {

          getContext().setError(LogMsg.createMsg(CDX.MSG, "Writer.preparedstatement_exception", getClass().getName(), e.getMessage()).toString());
        }
      }
    }

    if (isAutoAdjust()) {
      for (final String name : frameset.getColumns()) {

        if (schema.getMetric(name).getMaximumStringLength() > tableschema.findColumn(name).getLength()) {
          // TODO: if auto adjust, check the size of the string and issue an 
          // "alter table" command to adjust the size of the column if the 
          // string is too large to fit
          System.out.println("The " + database + " table '" + tableschema.getName() + "' must be altered to fit the '" + name + "' value; table allows a size of " + tableschema.findColumn(name).getLength() + " but data requires " + schema.getMetric(name).getMaximumStringLength());
          //DatabaseDialect.alterTable()
        }
      }
    }

    // if the table check did not generate an error
    if (getContext().isNotInError()) {
      if (batchsize <= 1) {
        final DataFrame frame = frameset.get(0);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.Writing single frame {%s}", getClass().getName(), frame));

        int indx = 1;
        for (final String name : frameset.getColumns()) {
          final DataField field = frame.getField(name);
          setData(ps, indx++, field);
          if (getContext().isInError()) {
            break;
          }
        }

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.executing_sql", getClass().getName(), ps.toString()));

        try {
          ps.execute();
        } catch (final SQLException e) {
          getContext().setError("Could not insert single row: " + e.getMessage());
        }

      } else {
        // Now write a batch
        for (final DataFrame frame : frameset.getRows()) {
          Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_frame", this.getClass().getName(), frame));

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

    // check to see if the table exists
    if (!tableExists(getTable())) {

      if (isAutoCreate()) {
        Connection conn = getConnection();

        if (conn == null) {
          Log.error("Cannot get database connection");
          context.setError("Could not connect to the database");
          return false;
        }

        symbolTable.put(DatabaseDialect.TABLE_NAME_SYM, getTable());
        symbolTable.put(DatabaseDialect.DB_SCHEMA_SYM, getUsername());
        String command = DatabaseDialect.getCreate(database, schema, symbolTable);

        Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.creating_table", getClass().getName(), getTable(), command));

        Statement stmt = null;
        try {
          stmt = conn.createStatement();
          stmt.executeUpdate(command);

        } catch (Exception e) {
          Log.warn(LogMsg.createMsg(CDX.MSG, "Problems creating {} table: {}", getTable(), e.getMessage()));
        }
        finally {
          try {
            stmt.close();
          } catch (Exception e) {
            Log.warn(LogMsg.createMsg(CDX.MSG, "Problems closing create {} statement: {}", getTable(), e.getMessage()));
          }
        }

        //TODO getContext().setError( LogMsg.createMsg( CDX.MSG, "Writer.table_creation_exception", getClass().getName(), e.getMessage() ).toString() );
        // return false;
      }
    }

    // get the schema for the database table we are using
    tableschema = getTableSchema(getTable());

    return true;
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
          if (tablename.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
            tableSchemaName = rs.getString("TABLE_NAME");
            break;
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
        context.setError("Problems confirming table name: " + e.getMessage());
      }
      finally {
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

          System.out.println(retval);

        } catch (SQLException e) {
          e.printStackTrace();
          context.setError("Problems confirming table columns: " + e.getMessage());
        }
        finally {
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
   * Determine if a particular table exists in the database.
   * 
   * @param tablename The name of the table for which to query
   * 
   * @return true the named table exists, false the table does not exist.
   */
  private boolean tableExists(String tablename) {
    boolean retval = false;
    if (StringUtil.isNotBlank(tablename)) {
      Connection conn = getConnection();
      if (conn == null) {
        Log.error("Cannot get connection");
        context.setError("Could not connect to the database");
        return false;
      }

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = conn.getMetaData();

        // get all the tables so we can perform a case insensitive search
        rs = meta.getTables(null, null, "%", null);
        while (rs.next()) {
          if (tablename.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
            retval = true;
          }
        }
        return retval;

      } catch (SQLException e) {
        e.printStackTrace();
        context.setError("Problems confirming table: " + e.getMessage());
      }
      finally {
        if (rs != null) {
          try {
            rs.close();
          } catch (SQLException ignore) {
            //ignore.printStackTrace();
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
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_fields", getClass().getName(), frame.size()));
    frameset.add(frame);

    if (frameset.size() >= batchsize) {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.writing_batch", getClass().getName(), frameset.size(), batchsize));
      writeBatch();
    }

  }

}
