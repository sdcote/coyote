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
package coyote.batch.validate;

import coyote.batch.FrameValidator;
import coyote.batch.TransactionContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * There must be a value and it must not be an empty string or all whitespace.
 * 
 *<pre>"NotEmpty" : { "field" : "model",  "desc" : "Model cannot be empty"  },</pre>
 *
 */
public class NotEmpty extends AbstractValidator implements FrameValidator {
  public NotEmpty() {
    description = "Field must not be empty";
  }




  /**
   * @see coyote.batch.FrameValidator#process(coyote.batch.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) throws ValidationException {

    // get the field from the working frame of the given context
    DataFrame frame = context.getWorkingFrame();

    if ( frame != null ) {
      DataField field = frame.getField( fieldName );
      if ( field != null ) {

        // get the value
        String value = field.getStringValue();

        // check the value
        if ( StringUtil.isBlank( value ) ) {
          fail( context, fieldName );
          return false;
        }

      } else {
        // fail
        fail( context, fieldName );
        return false;
      }
    } else {
      // fail && error
      context.setError( "There is no working frame" );
      return false;
    }

    return true;
  }

}
