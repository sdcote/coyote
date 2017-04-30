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

import java.util.UUID;


/**
 * All it takes to generate data is extending the AbstractGenerator and 
 * overriding/implementing one method.
 */
public class GuidGenerator extends AbstractGenerator {

  /**
   * @see coyote.testdata.AbstractGenerator#generateData(java.lang.String, coyote.testdata.Row)
   */
  @Override
  public void generateData( String name, Row row ) {
    row.set( name, UUID.randomUUID().toString() );
  }

}
