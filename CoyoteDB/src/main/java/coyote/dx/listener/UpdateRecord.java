/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import coyote.dx.CDX;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This updates the configured database table based on the values in the 
 * working record.
 * 
 * <p>Using a listener instead of a Writer allows for more finer control of 
 * the operation. For example, records can be updated when it is known the 
 * record does already exists by either a lookup of a field value or state of 
 * the context. Compare this to a Writer which will only perform upserts.
 * 
 * <p>Transforms can be used to perform lookups and alter the state of the 
 * working frame to enable conditions for the listener to be run.
 * 
 * <p>This listener operates at the end of the transaction context, giving all 
 * other components a chance to process the working frame and the mapper to 
 * generate a properly formatted record for updating in the database.
 */
public class UpdateRecord extends AbstractDatabaseListener implements ContextListener {

  /**
   * @see coyote.dx.listener.AbstractListener#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd(OperationalContext context) {
    if (context instanceof TransactionContext) {
      TransactionContext cntxt = (TransactionContext)context;
      if (isEnabled()) {
        if (getCondition() != null) {
          try {
            if (evaluator.evaluateBoolean(getCondition())) {
              performCreate(cntxt);
            } else {
              if (Log.isLogging(Log.DEBUG_EVENTS)) {
                Log.debug(LogMsg.createMsg(CDX.MSG, "Listener.boolean_evaluation_false", getCondition()));
              }
            }
          } catch (final IllegalArgumentException e) {
            Log.error(LogMsg.createMsg(CDX.MSG, "Listener.boolean_evaluation_error", getCondition(), e.getMessage()));
          }
        } else {
          performCreate(cntxt);
        }
      }
    }
  }




  /**
   * @param cntxt
   */
  private void performCreate(TransactionContext cntxt) {
    Log.info("Update Record Listener handling target frame of " + cntxt.getTargetFrame());
  }

}
