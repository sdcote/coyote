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
package coyote.commons;

import java.util.regex.Pattern;

/**
 * 
 */
public class RegexUtil {
  
  /**
   * Turn a *-wildcard style glob ("*-all.jar") into a regular expression
   * (^.*\-all\.jar$)
   * 
   * @param glob
   * @param extended true To match bash-like globs, eg. ? for any single-character match, [a-z] for character ranges, and {*.txt, *.csv} for multiple alternatives
   * @param global true to match anywhere false to anchor the match to the beginning and end
   * @param flags additional flags to pass to the pattern compiler
   * 
   * @return a Pattern ready for matching
   */
  public static Pattern globToRegex( String glob, boolean extended, boolean global, int flags ) {
    // the buffer we use to build our regex
    StringBuffer b = new StringBuffer();

    // If we are doing extended matching, this boolean is true when we are inside
    // a group (eg {*.csv,*.txt}), and false otherwise.
    boolean inGroup = false;

    for ( int i = 0; i < glob.length(); i++ ) {
      char c = glob.charAt( i );

      switch ( c ) {
        case '\\':
        case '/':
        case '$':
        case '^':
        case '+':
        case '.':
        case '(':
        case ')':
        case '=':
        case '!':
        case '|':
          b.append( '\\' );
          b.append( c );
          break;

        case '?':
          if ( extended ) {
            b.append( '.' );
            break;
          }

        case '[':
        case ']':
          if ( extended ) {
            b.append( c );
            break;
          }

        case '{':
          if ( extended ) {
            inGroup = true;
            b.append( '(' );
            break;
          }

        case '}':
          if ( extended ) {
            inGroup = false;
            b.append( ')' );
            break;
          }

        case ',':
          if ( inGroup ) {
            b.append( '|' );
            break;
          }
          b.append( '\\' );
          b.append( c );
          break;

        case '*':
          b.append( ".*" );
          break;

        default:
          b.append( c );
      }
    }

    // If not global, constrain the regular expression with ^ & $
    if ( !global ) {
      b.insert( 0, '^' );
      b.append( '$' );
    }

    // create the regex pattern
    return Pattern.compile( b.toString(), flags );
  }


}
