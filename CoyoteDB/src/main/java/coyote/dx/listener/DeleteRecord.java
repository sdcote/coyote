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
import coyote.loader.log.Log;


/**
 * This deletes a record from a table using a specific field as the key.
 * 
 * <p>Using a listener instead of a Writer allows for more finer control of 
 * the operation.
 * 
 * <p>Transforms can be used to generate the appropriate key values.
 * 
 * <p>This listener operates at the end of the transaction context, giving all 
 * other components a chance to process the working frame.
 */
public class DeleteRecord extends AbstractDatabaseListener implements ContextListener {

  @Override
  public void execute(TransactionContext cntxt) {
    Log.info("Delete Record Listener handling target frame of " + cntxt.getTargetFrame());
    Connection conn = getConnector().getConnection();

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Deletes are simply a matter of flagging the record as in-active.
    // The idea is that in-active records will be purged at a later date.
    // This gives the system the ability to maintain historical context
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

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
