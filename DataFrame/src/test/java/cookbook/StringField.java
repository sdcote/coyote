/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package cookbook;

import java.io.Console;

import coyote.commons.ByteUtil;
import coyote.dataframe.DataFrame;

/**
 * 
 */
public class StringField {

  /**
   * @param args
   */
  public static void main( String[] args ) {
    DataFrame frame = new DataFrame("MSG", "Hello World!");
    byte[] bytes = frame.getBytes();
    System.out.println( ByteUtil.dump(bytes));
  }

}
