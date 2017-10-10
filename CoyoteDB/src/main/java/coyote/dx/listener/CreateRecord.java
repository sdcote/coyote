/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import coyote.commons.ExceptionUtil;
import coyote.commons.GUID;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.loader.log.Log;


/**
 * This inserts the target record into the configured database table.
 * 
 * <p>This listener is executed after the working frame is mapped to the 
 * target frame so that any mapping operations are included in the processing
 * of the data. Since it runs after mapping, it mimics a writer. 
 * 
 * <p>Using a listener instead of a Writer allows for more finer control of 
 * the operation. For example, records can be created when it is known the 
 * record does not exist by either a lookup of a field value or state of the 
 * context. Compare this to a Writer which will only perform upserts. The 
 * trade-off is that there is no batching of transactions, each insert is 
 * separate from the others.
 * 
 * <p>Transforms can be used to perform lookups and alter the state of the 
 * working frame to enable conditions for the listener to be run.
 * 
 * <p>This listener operates at the end of the transaction context, giving all 
 * other components a chance to process the working frame and the mapper to 
 * generate a properly formatted record for insertion into the database.
 * 
 * <p>This has 2 modes of operation. Simple, the fastest, just serializes the 
 * data frame as a JSON string and places it in a row. The other mode (not 
 * simple?) saves each field in a separate row, preserving order and 
 * hierarchy. This is slower as it generates a batch of inserts for each of 
 * the fields and then sends the batch to the database. Simple mode can be 
 * used for 90% of applications since this type of data transfer is normally 
 * the first stage of integration. The "non-simple" mode is only used when the 
 * data frames are stored in their final location and there is a need to edit 
 * fields of the frame individually.  
 *  
 * <p>The structure of the table is a normalized attribute table. This allows 
 * any structure to be modeled and enables the table to support different 
 * record types. Each key-value pair is assigned a parent to which it belongs.
 * All pairs should have a parent as all attributes belong to a data frame.
 */
public class CreateRecord extends AbstractDatabaseListener implements ContextListener {





  /**
   * @see coyote.dx.listener.AbstractDatabaseListener#execute(coyote.dx.context.TransactionContext)
   */
  @Override
  public void execute(TransactionContext cntxt) {
    Log.info("Create Record Listener handling target frame of " + cntxt.getTargetFrame());
    Connection conn = getConnector().getConnection();

    String guid;
    if (isSimpleMode()) {
      guid = saveEntireFrame(cntxt.getTargetFrame(), conn);
    } else {
      guid = saveFrame(cntxt.getTargetFrame(), 0, null);
    }

    cntxt.setProcessingResult(guid);

    // if the connector pools connections, it is safe to close the connection
    // otherwise, we should keep it open for later use by this component.
    if (getConnector().isPooled()) {
      try {
        // closing a pooled connection returns it to the pool
        conn.close();
      } catch (SQLException e) {
        Log.warn(this.getClass().getName() + " experienced problems closing the database connection: " + e.getMessage());
      }
    }

  }




  /**
   * This saves the entire frame as a JSON string in the value field.
   * 
   * @param frame the frame to save.
   * @param conn 
   * 
   * @return the system identifier for the inserted record.
   */
  @SuppressWarnings("unchecked")
  private String saveEntireFrame(DataFrame frame, Connection conn) {
    String sysid = GUID.randomGUID().toString();
    Log.info("Frame Id:" + sysid + " - " + frame);

    // serialize the frame into JSON
    String value = JSONMarshaler.marshal(frame);

    final SymbolTable sqlsymbols = new SymbolTable();
    sqlsymbols.put(DatabaseDialect.DB_SCHEMA_SYM, determineSchema());
    sqlsymbols.put(DatabaseDialect.TABLE_NAME_SYM, getTable());
    sqlsymbols.put(DatabaseDialect.FIELD_NAMES_SYM, "SysId, Active, Value, Type, CreatedBy, CreatedOn, ModifiedBy, ModifiedOn");
    sqlsymbols.put(DatabaseDialect.FIELD_VALUES_SYM, "?, ?, ?, ?, ?, ?, ?, ?");
    final String sql = DatabaseDialect.getSQL(databaseProduct, DatabaseDialect.INSERT, sqlsymbols);
    if (sql != null) {
      try {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, sysid);
        preparedStatement.setBoolean(2, true);
        preparedStatement.setString(3, value);
        preparedStatement.setShort(4, DataField.STRING);
        preparedStatement.setString(5, getIdentity());
        preparedStatement.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
        preparedStatement.setString(7, getIdentity());
        preparedStatement.setTimestamp(8, new java.sql.Timestamp(new Date().getTime()));
        preparedStatement.executeUpdate();
      } catch (final SQLException e) {
        Log.fatal(ExceptionUtil.toString(e));
        Log.debug(ExceptionUtil.stackTrace(e));
      }
    } else {
      Log.error("Cannot support " + databaseProduct + " database product");
    }

    return sysid;
  }




  //

  // This is the complicated way to save dataframes




  //

  /**
   * @param frame
   */
  private String saveFrame(DataFrame frame, int num, String parent) {
    String sysid = null;
    if (frame != null) {
      sysid = GUID.randomGUID().toString();
      Log.info("Frame  " + num + " type:FRM parent:" + parent + " SysId:" + sysid);

      int seq = 0;
      for (DataField field : frame.getFields()) {

        if (field.isFrame()) {
          saveFrame((DataFrame)field.getObjectValue(), seq++, sysid);
        } else {
          saveField(field, seq++, sysid);
        }
      }
    }
    return sysid;
  }




  /**
   * @param field
   */
  private void saveField(DataField field, int seq, String parent) {
    String sysid = GUID.randomGUID().toString();
    Log.info("Field " + seq + " '" + field.getName() + "' type:" + field.getTypeName() + " Frame:" + parent + " SysId:" + sysid);
  }

}
