/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.testdata.network;

import coyote.testdata.AbstractGenerator;
import coyote.testdata.Generator;


/**
 * This generates a variety of syntactically valid email addresses
 */
public class EmailGenerator extends AbstractGenerator implements Generator {

  public EmailGenerator() {
    addStrategy( new EmailFileStrategy() );
  }

}
