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
package coyote.testdata.format;

import coyote.testdata.DataSet;


/**
 * 
 */
public interface Writer {

  public void close();




  public void open();




  /**
   * Write formatted data to the given output stream.
   * 
   * <p>This method is used to generate large datasets while avoiding the 
   * formatted output from residing in memory. The goal is to write the 
   * formatted data to a file system or network connection and avoid storing 
   * the data in memory for later output.</p>
   * 
   * @param dataset The data to write
   */
  public void write( DataSet dataset );

}
