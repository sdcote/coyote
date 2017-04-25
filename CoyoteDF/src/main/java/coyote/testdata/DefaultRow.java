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
package coyote.testdata;

/**
 * 
 */
public class DefaultRow extends AbstractRow {

  public DefaultRow() {

  }




  @Override
  public Object clone() {
    final Row retval = new DefaultRow();
    for ( final String column : columns.keySet() ) {
      retval.set( column, get( column ) );
    }

    return retval;
  }

}
