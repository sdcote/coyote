/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.web;

/**
 * Enumeration of HTTP methods.
 */
public enum Method {
  GET( "GET"), POST( "POST"), HEAD( "HEAD"), OPTIONS( "OPTIONS"), PUT( "PUT"), DELETE( "DELETE"), TRACE( "TRACE");

  private String name;




  private Method( String s ) {
    name = s;
  }




  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }




  public static Method getMethodByName( String name ) {
    if ( name != null ) {
      for ( Method method : Method.values() ) {
        if ( name.equalsIgnoreCase( method.toString() ) ) {
          return method;
        }
      }
    }
    return null;
  }

}
