/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.validate;

import java.util.HashMap;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameValidator;
import coyote.dx.context.TransactionContext;


/**
 * The value of this field must not match any other instances of this field.
 * 
 *<pre>"Distinct" : { "field" : "model",  "desc" : "Optional description value"  },</pre>
 *
 */
public class Distinct extends AbstractValidator implements FrameValidator {

  private final Map<String, Integer> values = new HashMap<String, Integer>();
  long emptycount = 0;
  long missingcount = 0;




  public Distinct() {
    description = "Field must contain a distinct value";
  }




  /**
   * @see coyote.dx.FrameValidator#process(coyote.dx.context.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) throws ValidationException {

    // get the field from the working frame of the given context
    DataFrame frame = context.getWorkingFrame();

    if ( frame != null ) {
      DataField field = frame.getField( fieldName );
      if ( field != null ) {

        // get the value
        String key = field.getStringValue();
        if ( StringUtil.isNotBlank( key ) ) {
          if ( values.containsKey( key ) ) {
            int count = (Integer)values.get( key );
            count++;
            values.put( key, count );
            fail( context, fieldName, fieldName + ": value of '" + key + "' has occured " + count + " times" );
            return false;
          } else {
            values.put( key, 1 );
          }
        } else {
          fail( context, fieldName, "Empty value for " + fieldName + " count: " + ++emptycount );
        }
      } else {
        fail( context, fieldName, "Missing field for " + fieldName + " count: " + ++missingcount );
      }
    } else {
      // fail && error
      context.setError( "There is no working frame" );
      return false;
    }

    return true;
  }

}
