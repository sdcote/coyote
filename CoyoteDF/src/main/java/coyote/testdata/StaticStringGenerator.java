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
 * 
 */
public class StaticStringGenerator extends AbstractGenerator {

  private String[] values = null;




  /**
   * Constructor with the value(s) to use for all generation requests.
   * 
   * @param string The value to use for all generation requests.
   */
  public StaticStringGenerator( final String... string ) {
    values = string;
  }




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

    // Generate random data
    Object retval = values[random.nextInt( values.length )];

    // Transform it
    retval = applyTransforms( retval );

    // return the generated value
    return retval;
  }

}
