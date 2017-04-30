/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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
public class LongGenerater extends AbstractGenerator {
  int min = 0;
  int max = 1;




  public LongGenerater( int start, int end ) {
    min = start;
    max = end;
  }




  /**
   * @see coyote.testdata.AbstractGenerator#generateData(java.lang.String, coyote.testdata.Row)
   */
  @Override
  public void generateData( final String name, final Row row ) {
    row.set( name, ( ( Math.abs( random.nextLong() ) % ( max - min + 1 ) ) + min ) );
  }

}
