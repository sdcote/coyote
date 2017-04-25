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
public class UserNameGenerator extends AbstractGenerator implements Generator {

  public UserNameGenerator() {
    addStrategy( new UsernameNameStrategy() );
  }

}
