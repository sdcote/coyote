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

import coyote.testdata.LetterGenerator;
import coyote.testdata.transform.Capitalize;


/**
 * This is a specialization of the LetterGenerator which simply adds a 
 * capitalization transform to the process.
 */
public class MiddleInitialGenerator extends LetterGenerator {

  public MiddleInitialGenerator() {
    // Use a simple initial capitalization transform on the generated letters
    super.addTransform( new Capitalize() );

  }

}
