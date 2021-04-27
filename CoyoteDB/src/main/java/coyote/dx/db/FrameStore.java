/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import coyote.commons.ExceptionUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.ColumnDefinition;
import coyote.commons.jdbc.ColumnType;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.jdbc.TableDefinition;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.log.Log;


/**
 * This is a static class which contains methods to create, retrieve, update 
 * and delete (CRUD) data frames in a JDBC database.
 * 
 * <p>Each field is stored in its own record. Since a dataframe is simply a 
 * collection of fields, the use of parent identifiers enable the hirarchical 
 * representation of the data. The root frames are represented by a sysid 
 * only, that is all the fields with the same sysid are part of that frame 
 * and if there is no field with that sysid, that sysid represents the root 
 * frame. More simply, if a sysid in a parent column does not point to a 
 * field, that sysid represents the root data frame.
 * 
 * <p>This class uses FieldSlots to hold the field data for later assembley 
 * into frames or creation of batch insert or update commands. This 
 * intermediate format allows for recursive SQL calls over the same 
 * connection but outside the result set. This also allows the system to 
 * create batches of insert and update commands in contrast with creating a 
 * command for each field. One batch insert can create the entire frame at 
 * once. The same is true for updates and deletions. 
 */
public abstract class FrameStore {

  public static final String SYSID = "SysId";
  public static final String ACTIVE = "Active";
  public static final String PARENT = "Parent";
  public static final String SEQUENCE = "Sequence";
  public static final String NAME = "Name";
  public static final String VALUE = "Value";
  public static final String TYPE = "Type";
  public static final String CREATED_BY = "CreatedBy";
  public static final String CREATED_ON = "CreatedOn";
  public static final String MODIFIED_BY = "ModifiedBy";
  public static final String MODIFIED_ON = "ModifiedOn";

  // Size of the batch of inserts and updates to send at once
  private static final int BATCH_SIZE = 250;




  /**
   * Private constructor
   */
  private FrameStore() {
    // no instances, all static methods
  }




  /**
   * Store the given data frame in the database.
   * 
   * <p>This performs a batch insert of all the fields in the given frame and 
   * its children. If there are too many fields in the frame, multiple batches 
   * will be executed.
   * 
   * @param frame the frame to store in the database
   * @param conn the JDBC connection to the database
   * @param entity the entity creating the dataframe
   * @param schema the schema in the database where the data is stored
   * @param table the table in the schema where the data is stored
   * @param dialect the database product being used, if null, the connection 
   *        metadata will be queried
   * 
   * @return the system identifier of the new frame
   */
  @SuppressWarnings("unchecked")
  public static String create(DataFrame frame, Connection conn, String entity, String schema, String table, String dialect) {
    String retval = GUID.randomSecureGUID().toString();
    Log.info("Creating Frame Id:" + retval);
    String databaseProduct = dialect;
    if (StringUtil.isBlank(databaseProduct)) {
      databaseProduct = DatabaseUtil.getProduct(conn);
    }

    List<FieldSlot> slots = getSlots(frame, retval);

    final SymbolTable sqlsymbols = new SymbolTable();
    sqlsymbols.put(DatabaseDialect.DB_SCHEMA_SYM, schema);
    sqlsymbols.put(DatabaseDialect.TABLE_NAME_SYM, table);
    sqlsymbols.put(DatabaseDialect.FIELD_NAMES_SYM, SYSID + ", " + ACTIVE + ", " + PARENT + ", " + SEQUENCE + ", " + NAME + ", " + VALUE + ", " + TYPE + ", " + CREATED_BY + ", " + CREATED_ON + ", " + MODIFIED_BY + ", " + MODIFIED_ON);
    sqlsymbols.put(DatabaseDialect.FIELD_VALUES_SYM, "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
    final String sql = DatabaseDialect.getSQL(databaseProduct, DatabaseDialect.INSERT, sqlsymbols);
    if (sql != null) {
      PreparedStatement preparedStatement = null;
      try {
        conn.setAutoCommit(false);
        preparedStatement = conn.prepareStatement(sql);
        int count = 0;
        for (FieldSlot slot : slots) {
          preparedStatement.setString(1, slot.getSysId());
          preparedStatement.setBoolean(2, slot.isActive());
          preparedStatement.setString(3, slot.getParent());
          preparedStatement.setInt(4, slot.getSequence());
          preparedStatement.setString(5, slot.getName());
          preparedStatement.setString(6, slot.getValue());
          preparedStatement.setShort(7, slot.getType());
          preparedStatement.setString(8, entity);
          preparedStatement.setTimestamp(9, new java.sql.Timestamp(new Date().getTime()));
          preparedStatement.setString(10, entity);
          preparedStatement.setTimestamp(11, new java.sql.Timestamp(new Date().getTime()));
          preparedStatement.addBatch();

          count++;
          if (count % BATCH_SIZE == 0) {
            int[] result = preparedStatement.executeBatch();
            conn.commit();
            Log.debug("Commited a batch of " + result.length+" rows");
            count = 0;
          }
        }

        if (count > 0) {
          int[] result = preparedStatement.executeBatch();
          Log.debug("Commited batch of " + result.length+" rows");
          conn.commit();
        }

      } catch (final SQLException e) {
        Log.fatal(ExceptionUtil.toString(e));
        Log.debug(ExceptionUtil.stackTrace(e));
        try {
          conn.rollback();
        } catch (SQLException e1) {
          Log.warn("Could not roll-back inserts: " + e1.getMessage());
        }
      } finally {
        if (preparedStatement != null)
          DatabaseUtil.closeQuietly(preparedStatement);
      }
    } else {
      Log.error("Cannot support " + databaseProduct + " database product");
    }

    return retval;
  }




  /**
   * Retrieve a dataframe from the database with the given system identifier.
   * 
   * <p>This is a naive recursion into the database for 2 reasons, the varing 
   * capabilities of different databases make comming up with a good recursive 
   * SQL statement difficult and the type of data used in the framework tends 
   * not to be hierarchical so the number of times this will become a 
   * performance issues should be very small. For these reasons this method
   * performs multiple select calls to build the frame hierarchy. 
   * 
   * @param sysid the system identifier of the frame to retrieve
   * @param conn the JDBC connection to the database
   * @param entity the entity reading the dataframe
   * @param schema the schema in the database where the data is stored
   * @param table the table in the schema where the data is stored
   * @param dialect the database product being used, if null, the connection 
   *        metadata will be queried
   * 
   * @return the dataframe with that system identifier or null if not found
   */
  public static DataFrame read(String sysid, Connection conn, String entity, String schema, String table, String dialect) {
    DataFrame retval = null;
    String query = "SELECT * FROM " + schema + "." + table + " WHERE parent = '" + sysid + "' ORDER BY " + SEQUENCE + " ASC";
    ResultSet result = null;
    List<FieldSlot> slots = new ArrayList<FieldSlot>();

    Log.debug(String.format("Executing query: '%s'", query));

    if (conn != null) {
      try {
        Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        result = statement.executeQuery(query);

        if (result != null) {
          try {
            while (result.next()) {
              slots.add(new FieldSlot(result.getString(SYSID), result.getString(PARENT), result.getInt(SEQUENCE), result.getBoolean(ACTIVE), result.getString(NAME), result.getShort(TYPE), result.getString(VALUE)));
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } catch (SQLException e) {
        String emsg = String.format("Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query);
        Log.error(emsg);
      }
    }

    if (slots.size() > 0) {
      retval = assembleFrame(slots, conn, entity, schema, table, dialect);
    }

    return retval;
  }




  /**
   * Assemble the given list of field slots into a data frame.
   * 
   * <p>With the exception of {@code slots}, all the arguments are used when
   * calling {@link #read(String, Connection, String, String, String, String)}
   * to retrieve a filed that itself contains other fields (i.e. a DataFrame).
   * 
   * @param slots the list of filed slots representing the fields in the frame
   * @param sysid the system identifier of the frame to retrieve
   * @param conn the JDBC connection to the database
   * @param entity the entity reading the dataframe
   * @param schema the schema in the database where the data is stored
   * @param table the table in the schema where the data is stored
   * @param dialect the database product being used, if null, the connection 
   *        metadata will be queried
   *
   * @return a data frame comprised of the data from the field slots 
   */
  private static DataFrame assembleFrame(List<FieldSlot> slots, Connection conn, String entity, String schema, String table, String dialect) {
    DataFrame retval = new DataFrame();
    for (FieldSlot slot : slots) {
      if (slot.getType() == DataField.FRAMETYPE) {
        DataFrame childframe = read(slot.getSysId(), conn, entity, schema, table, dialect);
        retval.add(slot.getName(), childframe);
      } else {
        final Object contextValue = DataField.parse(slot.getValue(), slot.getType());
        retval.add(slot.getName(), contextValue);
      }
    }
    return retval;
  }




  /**
   * Update the data frame with the given identifier with the given data frame.
   * 
   * @param sysid the system identifier of the data frame to update
   * @param frame the data frame representing the end state of the update
   * @param conn the JDBC connection to the database
   * @param entity the entity updating the dataframe
   * @param schema the schema in the database where the data is stored
   * @param table the table in the schema where the data is stored
   * 
   * @return true if the update was successful, false if the update failed
   */
  public static boolean update(String sysid, DataFrame frame, Connection conn, String entity, String schema, String table) {
    boolean retval = false;

    return retval;
  }




  /**
   * Delete the data frame with the given identifier.
   * 
   * <p>In actuality, the fields are only marked as inactive and not removed 
   * from the system. It is expected that data will be purged if necessary by
   * selecting all inactive fields with a modified date older than some cut-off.
   * 
   * @param sysid the system identifier of the data frame to delete
   * @param conn the JDBC connection to the database
   * @param entity the entity creating the dataframe
   * @param schema the schema in the database where the data is stored
   * @param table the table in the schema where the data is stored
   * 
   * @return true if the delete was successful, false if the deletion failed
   */
  public static boolean delete(String sysid, Connection conn, String entity, String schema, String table) {
    boolean retval = false;

    return retval;
  }




  /**
   * Retrieve a list of slots representing the fields comprising this frame.
   * 
   * <p>Each slot is assigned a random GUID so it can be uniquely identifier 
   * in the database
   * 
   * <p>This is a recursive method, meaning when a field contains a data frame 
   * this method will call itself to get the list of fields / slots comprising 
   * that frame.
   * 
   * @param frame the frame to process
   * @param parent the system identifier of the parent data frame to which 
   *        this frame belongs
   *
   * @return a list of FieldSlots representing the fields comprising the given 
   *         frame. The list may be empty but never null.
   */
  protected static List<FieldSlot> getSlots(DataFrame frame, String parent) {
    List<FieldSlot> retval = new ArrayList<FieldSlot>();
    int seq = 0;
    for (DataField field : frame.getFields()) {
      String sysid = GUID.randomSecureGUID().toString();
      if (field.isFrame()) {
        retval.add(new FieldSlot(sysid, parent, seq++, true, field.getName(), field.getType(), null));
        retval.addAll(getSlots((DataFrame)field.getObjectValue(), sysid));
      } else {
        retval.add(new FieldSlot(sysid, parent, seq++, true, field.getName(), field.getType(), field.getStringValue()));
      }
    }
    return retval;
  }




  /**
   * Get a table definition for storing data frame fields in a table.
   * 
   * @param table the name of the table
   * @param schema name of the schema
   * 
   * @return a table schema this field store uses to store data frame fields
   */
  public static TableDefinition getTableSchema(String table, String schema) {
    TableDefinition retval = new TableDefinition(table);
    retval.setSchemaName(schema);
    retval.addColumn(new ColumnDefinition(SYSID, ColumnType.STRING).setLength(36).setPrimaryKey(true));
    retval.addColumn(new ColumnDefinition(ACTIVE, ColumnType.BOOLEAN));
    retval.addColumn(new ColumnDefinition(PARENT, ColumnType.STRING).setLength(36).setNullable(true));
    retval.addColumn(new ColumnDefinition(SEQUENCE, ColumnType.INT).setNullable(true));
    retval.addColumn(new ColumnDefinition(NAME, ColumnType.STRING).setLength(64).setNullable(true));
    retval.addColumn(new ColumnDefinition(VALUE, ColumnType.STRING).setLength(4096).setNullable(true));
    retval.addColumn(new ColumnDefinition(TYPE, ColumnType.SHORT).setNullable(true));
    retval.addColumn(new ColumnDefinition(CREATED_BY, ColumnType.STRING).setLength(36));
    retval.addColumn(new ColumnDefinition(CREATED_ON, ColumnType.DATE));
    retval.addColumn(new ColumnDefinition(MODIFIED_BY, ColumnType.STRING).setLength(36));
    retval.addColumn(new ColumnDefinition(MODIFIED_ON, ColumnType.DATE));
    return retval;
  }
}
