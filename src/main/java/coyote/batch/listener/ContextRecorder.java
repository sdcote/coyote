/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.listener;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurationException;
import coyote.batch.ContextListener;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Base class for context listeners sending output to some target
 */
public abstract class ContextRecorder extends AbstractListener implements ContextListener {

  protected boolean onRead = false;
  protected boolean onWrite = false;




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return configuration.getAsString( ConfigTag.TARGET );
  }




  /**
   * Set the URI to where the write will write its data.
   * 
   * @param value the URI to where the writer should write its data
   */
  public void setTarget( final String value ) {
    configuration.put( ConfigTag.TARGET, value );
  }




  /**
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    if ( frame.contains( ConfigTag.READ ) ) {
      try {
        onRead = frame.getAsBoolean( ConfigTag.READ );
        Log.debug( LogMsg.createMsg( Batch.MSG, "ContextRecorder.read_flag_set_as", onRead ) );
      } catch ( DataFrameException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "ContextRecorder.read_flag_not_valid", e.getMessage() ) );
        onRead = false;
      }
    } else {
      Log.debug( LogMsg.createMsg( Batch.MSG, "ContextRecorder.no_read_flag" ) );
    }

    if ( frame.contains( ConfigTag.WRITE ) ) {
      try {
        onWrite = frame.getAsBoolean( ConfigTag.WRITE );
        Log.debug( LogMsg.createMsg( Batch.MSG, "ContextRecorder.write_flag_set_as", onWrite ) );
      } catch ( DataFrameException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "ContextRecorder.write_flag_not_valid", e.getMessage() ) );
        onWrite = false;
      }
    } else {
      Log.debug( LogMsg.createMsg( Batch.MSG, "ContextRecorder.no_write_flag" ) );
    }

  }
}
