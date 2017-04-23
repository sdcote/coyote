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
package coyote.dx.validate;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameValidator;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;
import coyote.loader.cfg.ConfigurationException;


/**
 * "Pattern" : { "field" : "src1",  "avoid" : "^(\\d{3}-?\\d{2}-?\\d{4})$", "desc" : "SSN", "halt" : false  },
 * "Pattern" : { "field" : "src2",  "avoid" : "^4[0-9]{12}(?:[0-9]{3})?$", "desc" : "Visa Card", "halt" : false  },
 * "Pattern" : { "field" : "src3",  "avoid" : "^5[1-5][0-9]{14}$", "desc" : "MasterCard", "halt" : false  },
 * "Pattern" : { "field" : "src4",  "avoid" : "^3[47][0-9]{13}$", "desc" : "Amex", "halt" : false  },
 * "Pattern" : { "field" : "src5",  "avoid" : "^3(?:0[0-5]|[68][0-9])[0-9]{11}$", "desc" : "Diners Club", "halt" : false  },
 * "Pattern" : { "field" : "src6",  "avoid" : "^6(?:011|5[0-9]{2})[0-9]{12}$", "desc" : "Discover", "halt" : false  },
 * "Pattern" : { "field" : "src7",  "match" : "^(?:2131|1800|35\\d{3})\\d{11}$", "desc" : "JCB", "halt" : false  },
 *    
 * Support template resolution of field names
 * 
 * match pattern: must match to return true; false if no match
 * avoid pattern: must not match to return true; false if a match is found (PII)
 */
public class Pattern extends AbstractValidator implements FrameValidator {

  public Pattern() {
    description = "Field value appears to be invalid";
  }

  private String regularExpression = "";
  private boolean avoid = true;
  private java.util.regex.Pattern fieldPattern = null;
  private java.util.regex.Pattern valuePattern = null;




  /**
   * @return the avoid
   */
  public boolean isAvoiding() {
    return avoid;
  }




  /**
   * @param flag the avoid to set
   */
  public void setAvoid( boolean flag ) {
    this.avoid = flag;
  }




  /**
   * @return the pattern for field names
   */
  public java.util.regex.Pattern getFieldPattern() {
    return fieldPattern;
  }




  /**
   * @return the pattern for values
   */
  public java.util.regex.Pattern getValuePattern() {
    return valuePattern;
  }




  /**
   * @param pattern the Regular Expression pattern to set for matching or avoiding
   */
  public void setRegEx( String pattern ) {
    regularExpression = pattern;
  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    // perform base class configuration first
    super.setConfiguration( frame );

    //
    if ( frame.contains( ConfigTag.AVOID ) ) {
      setAvoid( true );
      setRegEx( frame.getAsString( ConfigTag.AVOID ) );
    } else if ( frame.contains( ConfigTag.MATCH ) ) {
      setAvoid( false );
      setRegEx( frame.getAsString( ConfigTag.MATCH ) );
    } else {
      throw new ConfigurationException( "Pattern validator must contain either '" + ConfigTag.MATCH + "' or '" + ConfigTag.AVOID + "' attribute" );

    }

  }




  /**
   * @see coyote.dx.validate.AbstractValidator#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    fieldPattern = java.util.regex.Pattern.compile( getFieldName() );
    valuePattern = java.util.regex.Pattern.compile( getValueRegEx() );
  }




  /**
   * @see coyote.dx.FrameValidator#process(coyote.dx.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) throws ValidationException {
    boolean retval = true;
    //get the working frame of the given context
    DataFrame frame = context.getWorkingFrame();

    // If we have a frame...
    if ( frame != null ) {

      String value = null;
      for ( DataField field : frame.getFields() ) {
        if ( field.getName() != null && fieldPattern.matcher( field.getName() ).matches() ) {

          // get the string value of the field
          value = field.getStringValue();

          if ( isAvoiding() ) {
            // we are avoiding a match so if there is a match, log a failure
            if ( valuePattern.matcher( value ).matches() ) {
              fail( context, fieldName );
              retval = false;
            }
          } else {
            // we are not avoiding (i.e requiring) a match so log a failure if 
            // it does not match
            if ( !valuePattern.matcher( value ).matches() ) {
              fail( context, fieldName );
              retval = false;
            }
          }// avoiding?

        }// field matches?

      } // for each field

    } else {
      // fail && error
      context.setError( "There is no working frame" );
      retval = false;
    }

    return retval;
  }




  /**
   * @return the valueRegEx
   */
  public String getValueRegEx() {
    return regularExpression;
  }

}
