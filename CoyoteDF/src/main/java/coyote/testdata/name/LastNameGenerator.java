/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.testdata.name;

import coyote.testdata.AbstractGenerator;
import coyote.testdata.transform.Capitalize;


/**
 * 
 */
public class LastNameGenerator extends AbstractGenerator {

  public LastNameGenerator() {

    super.addStrategy( new LastNameFileStrategy() );

    super.addTransform( new Capitalize() );
  }

}
