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
package coyote.testdata.transform;

/**
 * This capitalizes the first letter in the string ignoring all preceding non-
 * letters such as spaces and numbers.
 */
public class Capitalize implements DataTransform {

  /**
   * Capitalize the first netter in the string.
   * 
   * <p>If the value is not a string, the original object reference will be 
   * returned.</p>
   * 
   * @see coyote.testdata.transform.DataTransform#transform(java.lang.Object)
   */
  @Override
  public Object transform( final Object value ) {
    if ( value != null ) {
      if ( value instanceof String ) {
        final String token = (String)value;
        if ( token.trim().length() > 0 ) {
          // scan for the first letter after whitespace and make sure it is capitalized.
          for ( int x = 0; x < token.length(); x++ ) {
            if ( Character.isAlphabetic( token.charAt( x ) ) ) {

              final StringBuilder buffer = new StringBuilder();
              if ( x > 0 ) {
                buffer.append( token.subSequence( 0, x ) );
              }
              buffer.append( Character.toString( token.charAt( x ) ).toUpperCase() );
              if ( x < token.length() ) {
                buffer.append( token.subSequence( x + 1, token.length() ) );
              }
              return buffer.toString();

            }

          }
        }

      }
    }
    return value;
  }

}
