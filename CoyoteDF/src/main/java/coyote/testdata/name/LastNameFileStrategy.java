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

import coyote.testdata.DataFileStrategy;
import coyote.testdata.Row;


/**
 * 
 */
public class LastNameFileStrategy extends DataFileStrategy {
  private static final String FILENAME = "lastname";




  public LastNameFileStrategy() {

    // Load data from a file if it exists
    super.loadData( FILENAME );

  }




  /**
   * @see coyote.testdata.GenerationStrategy#getData(coyote.testdata.Row)
   */
  @Override
  public Object getData( final Row row ) {

    // Generate random data
    final Object retval = data.get( random.nextInt( data.size() ) );

    // return the generated value
    return retval;
  }
}
