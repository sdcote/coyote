/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

/**
 * 
 */
public class Decrypt {

  /**
   * @param args
   */
  public static void main( String[] args ) {

    for ( int x = 0; x < args.length; x++ ) {
      String token = args[x];
      System.out.println( token + " = " + Batch.decrypt( token ) );
    }
  }

}
