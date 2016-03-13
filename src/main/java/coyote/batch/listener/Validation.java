/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import coyote.batch.FrameValidator;
import coyote.batch.OperationalContext;
import coyote.batch.TransactionContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.ConfigurationException;


/**
 * This writes the record with all the errors for later processing.
 */
public class Validation extends FileRecorder {

  List<String> validationErrors = new ArrayList<String>();




  /**
   * @see coyote.batch.listener.ContextRecorder#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // check for any other options to set here...like format, whether to include the error message...

  }




  /**
   * @see coyote.batch.listener.AbstractListener#onValidationFailed(coyote.batch.OperationalContext, coyote.batch.FrameValidator, java.lang.String)
   */
  @Override
  public void onValidationFailed( OperationalContext context, FrameValidator validator, String errorMessage ) {

    StringBuffer b = new StringBuffer();
    b.append( validator.getFieldName() );
    b.append( " did not pass '" );
    b.append( validator.getClass().getSimpleName() );
    b.append( "' check: " );
    b.append( validator.getDescription() );
    validationErrors.add( b.toString() );
  }




  public void onFrameValidationFailed( TransactionContext context ) {

    // write the record out with the errors

    StringBuffer b = new StringBuffer();

    b.append( context.getRow() );
    b.append( ": " );

    // show the frame which failed validation
    b.append( context.getWorkingFrame().toString() );
    b.append( ": " );

    // Show the validation errors
    for ( int x = 0; x < validationErrors.size(); x++ ) {
      b.append( validationErrors.get( x ) );
      if ( x + 1 < validationErrors.size() ) {
        b.append( ", " );
      }
    }
    b.append( StringUtil.LINE_FEED );

    // clear out the collected errors
    validationErrors.clear();

    // write out the validation failure
    write( b.toString() );

  }

}
