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
import coyote.testdata.Row;


/**
 * 
 */
public class USNameGenerator extends AbstractGenerator {

  public USNameGenerator() {

    // Create instances of different generators

    // Use a default strategy of only 10% of the names generated should have a prefix

    // use the default strategy of only 2% of the names generate have a suffix

    // use the default strategy of 98% of the names have a middle initial

  }




  /**
   * @see coyote.testdata.AbstractGenerator#generateData(java.lang.String, coyote.testdata.Row)
   */
  @Override
  public void generateData( final String name, final Row row ) {
    // TODO Auto-generated method stub

  }

}
