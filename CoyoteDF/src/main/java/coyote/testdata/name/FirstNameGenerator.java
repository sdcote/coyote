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
package coyote.testdata.name;

import coyote.testdata.AbstractGenerator;
import coyote.testdata.GenerationStrategy;
import coyote.testdata.transform.Capitalize;


/**
 * 
 */
public class FirstNameGenerator extends AbstractGenerator {

  public FirstNameGenerator() {

    final GenerationStrategy strategy = new FirstNameFileStrategy();
    super.addStrategy( strategy );

    // Now use a simple initial capitalization transform
    super.addTransform( new Capitalize() );
  }

}
