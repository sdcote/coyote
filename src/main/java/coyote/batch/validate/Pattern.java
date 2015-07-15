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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.FrameValidator;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.batch.ValidationException;


/**
 * "Pattern" : { "field" : "src1",  "avoid" : "^(\\d{3}-?\\d{2}-?\\d{4})$", "desc" : "SSN", "halt" : false  },
 * "Pattern" : { "field" : "src2",  "avoid" : "^4[0-9]{12}(?:[0-9]{3})?$", "desc" : "Visa Card", "halt" : false  },
 * "Pattern" : { "field" : "src3",  "avoid" : "^5[1-5][0-9]{14}$", "desc" : "MasterCard", "halt" : false  },
 * "Pattern" : { "field" : "src4",  "avoid" : "^3[47][0-9]{13}$", "desc" : "Amex", "halt" : false  },
 * "Pattern" : { "field" : "src5",  "avoid" : "^3(?:0[0-5]|[68][0-9])[0-9]{11}$", "desc" : "Diners Club", "halt" : false  },
 * "Pattern" : { "field" : "src6",  "avoid" : "^6(?:011|5[0-9]{2})[0-9]{12}$", "desc" : "Discover", "halt" : false  },
 * "Pattern" : { "field" : "src7",  "match" : "^(?:2131|1800|35\\d{3})\\d{11}$", "desc" : "JCB", "halt" : false  },
 *    
 * Use named field
 * Support field wildcards
 * Support regex in field names
 * Support template resolution of field names
 * 
 * match pattern: must match to return true; false if no match
 * avoid pattern: must not match to return true; false if a match is found (PII)
 */
public class Pattern extends AbstractValidator implements FrameValidator {

  

  
  /**
   * @see coyote.batch.validate.AbstractValidator#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    
  }

  /**
   * @see coyote.batch.FrameValidator#process(coyote.batch.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) throws ValidationException {
    
log.info( "Checking..." );    
    // context.fireValidationFailed( "This is the error message why the validation failed" );
    // return false;
    
    
    return true;
  }


}
