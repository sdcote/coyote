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

import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.dx.db.FrameStore;
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

    // Use the static methods in the FrameStore to encapsulate and standardize all SQL processing
    String guid = FrameStore.create(cntxt.getTargetFrame(), conn, getIdentity(), determineSchema(), getTable(), getDatabaseProduct());

    // HttpListener uses this along with maybe other async readers
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

}
