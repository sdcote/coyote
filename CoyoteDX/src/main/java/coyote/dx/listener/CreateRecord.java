/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import coyote.commons.GUID;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This inserts the working record into the configured database table.
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
 * <p>The structure of the table is a normalized attribute table. This allows 
 * any structure to be modeled and enables the table to support different 
 * record types. Each key-value pair is assigned a parent to which it belongs.
 * All pairs should have a parent as all attributes belong to a data frame.
 */
public class CreateRecord extends AbstractDatabaseListener implements ContextListener {

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
   * @see coyote.dx.listener.AbstractListener#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    // Change the name of the table based on the configuration

    // Change the schema of the table based on the configuration

    // get a connection

    // make sure the schema exists

    // make sure the table exists

    // if there are problems, place the context in error

  }




  /**
   * @param cntxt
   */
  private void performCreate(TransactionContext cntxt) {
    Log.info("Create Record Listener handling target frame of " + cntxt.getTargetFrame());
    DataFrame frame = cntxt.getWorkingFrame();
    String guid = GUID.randomGUID().toString();
    saveFrame(frame,guid,null);
    // place the GUID in the transaction context so the data frame can be retrieved later
    cntxt.setProcessingResult(guid);
  }




  /**
   * @param frame
   */
  private void saveFrame(DataFrame frame,String sysid, String parent) {
    if (frame != null) {
      for (DataField field : frame.getFields()) {
        if (field.isFrame()) {
          String guid = GUID.randomGUID().toString();
          saveFrame((DataFrame)field.getObjectValue(),guid,sysid);
        } else {
          saveField(field,sysid);
        }
      }
    }
  }




  /**
   * @param field
   */
  private void saveField(DataField field, String parent) {
    Log.info("Saving field '" + field.getName() + "' type:" + field.getTypeName()+ " Frame:"+parent);
  }

}
