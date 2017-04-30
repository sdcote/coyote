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
package coyote.testdata.format;

import coyote.testdata.DataSet;


/**
 * 
 */
public abstract class AbstractFormatter implements Formatter {

  /**
   * @see coyote.testdata.format.Formatter#format(coyote.testdata.DataSet)
   */
  @Override
  public abstract String format( DataSet dataset );

}
