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
import coyote.dx.ConfigTag;
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
    
    String guid = FrameStore.create(cntxt.getTargetFrame(), conn, getIdentity(), determineSchema(), getTable(), getDatabaseProduct());
    
    cntxt.setProcessingResult(new DataFrame().set(ConfigTag.ID, guid));

    if (getConnector().isPooled()) {
      try {
        conn.close();
      } catch (SQLException e) {
        Log.warn(this.getClass().getName() + " experienced problems closing the database connection: " + e.getMessage());
      }
    }

  }

}
