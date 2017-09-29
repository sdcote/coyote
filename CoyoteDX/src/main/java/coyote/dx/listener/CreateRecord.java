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
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
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
 * <p>This has 2 modes of operation. Simple, the fastest, just serializes the 
 * data frame as a JSON string and places it in a row. The other mode (not 
 * simple?) saves each field in a separate row, preserving order and 
 * hierarchy. This is slower as it generates a batch of inserts for each of 
 * the fields and then sends the batch to the database. Simple mode can be 
 * used for 90% of applications since this type of data transfer is normally 
 * the first stage of integration. THe "non-simple" mode is only used when the 
 * data frames are stored in their final location and there is a need to edit 
 * fields of the frame individually.  
 *  
 * <p>The structure of the table is a normalized attribute table. This allows 
 * any structure to be modeled and enables the table to support different 
 * record types. Each key-value pair is assigned a parent to which it belongs.
 * All pairs should have a parent as all attributes belong to a data frame.
 */
public class CreateRecord extends AbstractDatabaseListener implements ContextListener {

  private static final String SIMPLE_MODE = "SimpleMode";




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
    Log.info("Create Record Listener handling target frame of " + cntxt.getTargetFrame());
    DataFrame frame = cntxt.getWorkingFrame();
    String guid;
    if (isSimpleMode()) {
      guid = saveEntireFrame(frame);
    } else {
      guid = saveFrame(frame, 0, null);
    }
    cntxt.setProcessingResult(guid);
  }




  /**
   * This saves the entire frame as a JSON string in the value field.
   * 
   * @param frame the frame to save.
   * 
   * @return the system identifier for the inserted record.
   */
  private String saveEntireFrame(DataFrame frame) {
    String sysid = GUID.randomGUID().toString();
    Log.info("Frame Id:" + sysid + " - " + frame);
    return sysid;
  }




  /**
   * @return true if this is to serialize the entire frame into the value 
   *         field, false to store each field in a separate row.
   */
  private boolean isSimpleMode() {
    boolean retval = true;
    try {
      retval = configuration.getAsBoolean(SIMPLE_MODE);
    } catch (final DataFrameException ignore) {
      // must not be set or is invalid boolean value
    }

    return retval;
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
