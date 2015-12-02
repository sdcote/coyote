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
package coyote.commons.eval;

/**
 * A method is a special type of function which takes String arguments a 
 * performs busine logic processing to generate a numeric or boolean value. 
 */
public class Method extends Function {

  /**
   * This constructor builds a method with a fixed argument count.
   * 
   * @param name The methods name
   * @param argumentCount The methods argument count.
   * 
   * @throws IllegalArgumentException if argumentCount is lower than 0 or if the method name is null or empty.
   */
  public Method( final String name, final int argumentCount ) {
    super( name, argumentCount, argumentCount );
  }




  /**
   * This constructor builds a method with a variable argument count.
   * 
   * <p>For instance, a parse method may have at least one argument.</p>
   * 
   * @param name The methods name
   * @param minArgumentCount The methods minimum argument count.
   * @param maxArgumentCount The methods maximum argument count (Integer.MAX_VALUE to specify no upper limit).
   * 
   * @throws IllegalArgumentException if minArgumentCount is less than 0 or greater than maxArgumentCount or if the function name is null or empty.
   */
  public Method( String name, int minArgumentCount, int maxArgumentCount ) {
    super( name, minArgumentCount, maxArgumentCount );
  }

}
