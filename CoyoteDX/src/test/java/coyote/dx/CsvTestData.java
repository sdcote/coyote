/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

/**
 * Creates various types of CSV data to test CSV readers and parsers.
 */
public class CsvTestData {

  public static String simpleNumericData() {
    StringBuilder b = new StringBuilder();
    b.append("Rate,Amount,Minimum,Maximum");
    b.append((char)13);
    b.append((char)10);
    b.append("0.00845,1.21013800,2,2");
    b.append((char)13);
    b.append((char)10);
    b.append("0.008757,0.02472024,2,2");
    b.append((char)13);
    b.append((char)10);
    return b.toString();
  }

}
