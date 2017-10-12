/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.sql.Connection;
import java.sql.SQLException;

import coyote.dataframe.DataFrame;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.dx.db.FrameStore;
import coyote.loader.log.Log;


/**
 * This reads a record from a table using a specific field as the key.
 * 
 * <p>Using a listener instead of a Writer allows for more finer control of 
 * the operation.
 * 
 * <p>Transforms can be used to generate the appropriate key values.
 * 
 * <p>This listener operates at the end of the transaction context, giving all 
 * other components a chance to process the working frame.
 */
public class ReadRecord extends AbstractDatabaseListener implements ContextListener {

  /**
   * @see coyote.dx.listener.AbstractDatabaseListener#execute(coyote.dx.context.TransactionContext)
   */
  @Override
  public void execute(TransactionContext cntxt) {
    Log.info("Read Record Listener handling target frame of " + cntxt.getTargetFrame());
    Connection connection = getConnector().getConnection();

    // look for the SysId of the record to read
    DataFrame frame = cntxt.getTargetFrame();
    if (frame != null) {
      if (frame.containsIgnoreCase(FrameStore.SYSID)) {
        String sysid = frame.getFieldIgnoreCase(FrameStore.SYSID).getStringValue();
        // Use the static methods in the FrameStore to encapsulate and standardize all SQL processing
        DataFrame result = FrameStore.read(sysid, connection, getIdentity(), determineSchema(), getTable(), getDatabaseProduct());
        if (result == null) {
          Log.warn("No results for frame with a SysId of '" + sysid + "'");
        }
        cntxt.setTargetFrame(result); // replace the target frame with the results
      }
    } else {
      Log.error("No frame from which to retrive the system identifier");
    }

    // if the connector pools connections, it is safe to close the connection
    // otherwise, we should keep it open for later use by this component.
    if (getConnector().isPooled()) {
      try {
        // closing a pooled connection returns it to the pool
        connection.close();
      } catch (SQLException e) {
        Log.warn(this.getClass().getName() + " experienced problems closing the database connection: " + e.getMessage());
      }
    }

  }

}
