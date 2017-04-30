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
 * This generates letters in the range of a to z.
 */
public class LetterGenerator extends AbstractGenerator {

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

    // Generate a random letter
    Object retval = String.valueOf( (char)( random.nextInt( 26 ) + 'a' ) );

    // Transform it
    retval = applyTransforms( retval );

    // return the generated value
    return retval;
  }

}
