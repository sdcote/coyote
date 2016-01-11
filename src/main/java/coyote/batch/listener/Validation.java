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

import coyote.batch.ConfigurationException;
import coyote.batch.OperationalContext;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;


/**
 * This writes the record with all the errors for later processing.
 */
public class Validation extends FileRecorder {

  /**
   * @see coyote.batch.listener.ContextRecorder#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // check for any other options to set here...like format, whether to include the error message...

  }




  /**
   * @see coyote.batch.listener.AbstractListener#onValidationFailed(coyote.batch.OperationalContext, java.lang.String)
   */
  @Override
  public void onValidationFailed( OperationalContext context, String errorMessage ) {

    // write the record out with the errors

    StringBuffer b = new StringBuffer();

    if ( context instanceof TransactionContext || context instanceof TransformContext ) {
      b.append( context.getRow() );
      b.append( ": " );
    } else {
      b.append( "Operational validation failure: " );
    }

    b.append( errorMessage );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );
  }

}
