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
package coyote.testdata.credential;

import coyote.testdata.AbstractGenerator;
import coyote.testdata.Generator;


/**
 * 
 */
public class PasswordGenerator extends AbstractGenerator implements Generator {

  public PasswordGenerator() {
    //( final int minLength, final int maxLength, final int minLCaseCount, final int minUCaseCount, final int minNumCount, final int minSpecialCount )
    addStrategy( new PasswordStrategy(6,16,2,2,2,0) );
  }

}
