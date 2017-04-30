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
package coyote.testdata;

/**
 * This this generates M or F.
 */
public class GenderGenerator extends AbstractGenerator {
  private static final String MALE = "M";
  private static final String FEMALE = "F";




  /**
   * @see coyote.testdata.AbstractGenerator#generateData(java.lang.String, coyote.testdata.Row)
   */
  @Override
  public void generateData( final String name, final Row row ) {
    row.set( name, getData() );
  }




  /**
   * @return
   */
  private Object getData() {

    Object retval = null;

    // Generate random data
    final int choice = random.nextInt( 2 );
    if ( choice == 1 ) {
      retval = MALE;
    } else {
      retval = FEMALE;
    }

    // Transform it
    retval = applyTransforms( retval );

    // return the generated value
    return retval;
  }

}
