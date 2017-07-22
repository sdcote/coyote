/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.lang.reflect.Array;


/**
 * Class used to match and filter class names, JMS topics and such.
 * 
 * TODO Change getments via StringUtil.split('.')
 * TODO Allow for different rules delimiters, segment lengths, etc.
 */
public class SegmentFilter {

  /**
   * The segments of the filter text in order of their occurrence without the
   * dots.
   */
  private String[] segments = null;




  /**
   * Constructor SegmentFilter
   *
   * @param pattern The text pattern, with optional wildcards, to be used to
   *          compare against other text names.
   */
  public SegmentFilter( String pattern ) {
    segments = getSegments( pattern );

    if ( segments == null ) {
      throw new IllegalArgumentException( "Filter pattern is not legal" );
    }

  }




  /**
   * Return the segments of the text filter.
   *
   * @return The array of Strings that represent the filter text passed to the
   *         constructor.
   */
  public String[] getSegments() {
    return segments;
  }




  /**
   * Break the given text into an array of segments for easy comparison.
   *
   * @param text The string representing the text name to break into segments.
   *
   * @return An array of Strings containing each segment of the text, or null
   *         if the text is not a valid segmented text string.
   */
  public static String[] getSegments( String text ) {
    if ( text == null ) {
      return null;
    }

    // Length check
    int len = text.length();

    if ( ( len == 0 ) || ( len > 250 ) ) {
      throw new IllegalArgumentException( "Pattern length of '" + text.length() + "' is illegal (1-250)" );
    }

    int fromIndex = 0;

    String[] retval = new String[0];

    do {
      int dotPosition = text.indexOf( '.', fromIndex );

      // no dot was found, the entire string is the first segment
      if ( dotPosition < 0 ) {
        // if this is the first time through...
        if ( fromIndex == 0 ) {
          // ...no dot was found so the whole text is the (first) segment
          int length = Array.getLength( retval );
          String[] newarray = new String[length + 1];
          System.arraycopy( retval, 0, newarray, 0, length );
          Array.set( newarray, length, text );
          retval = newarray;
        } else {
          // ...the rest of the string is the (last) segment
          int length = Array.getLength( retval );
          String[] newarray = new String[length + 1];
          System.arraycopy( retval, 0, newarray, 0, length );
          Array.set( newarray, length, text.substring( fromIndex ) );
          retval = newarray;
        }

        break;
      }

      // Segments cannot be longer than 128 characters
      if ( dotPosition - fromIndex > 127 ) {
        throw new IllegalArgumentException( "Segment at position '" + fromIndex + "' is > 128 characters" );
      }

      // get the segment
      String segment = text.substring( fromIndex, dotPosition );

      // Make sure that if the segment is larger than one character, that it
      // does not contain the '>' character like "my.topic.ste>" or "my.su*"
      if ( ( segment.length() > 1 ) && ( ( segment.indexOf( '>' ) > 1 ) || ( segment.indexOf( '*' ) > 1 ) ) ) {
        throw new IllegalArgumentException( "Malformed wildcard in segment at position '" + fromIndex + "'" );
      }

      int length = Array.getLength( retval );
      String[] newarray = new String[length + 1];
      System.arraycopy( retval, 0, newarray, 0, length );
      Array.set( newarray, length, segment );
      retval = newarray;

      if ( ( segment.length() == 1 ) && ( segment.charAt( 0 ) == '>' ) ) {
        // nothing comes after the '>' wildcard
      }

      // set the from index to the point just past the location of the last dot
      fromIndex = dotPosition + 1;
    }
    while ( true );

    return retval;
  }




  /**
   * See if the filter matches the given text.
   *
   * @param text the text name to check
   *
   * @return true if the filter matches the text, false otherwise. False will
   *         also be returned if the given text is not a valid segmented name.
   */
  public boolean matches( String text ) {
    if ( ( text != null ) && ( segments != null ) ) {
      return matches( getSegments( text ) );
    }

    return false;
  }




  /**
   * See if the filter segments match the text segments
   *
   * @param subSegments the text segments to match.
   *
   * @return True if the segments match our segments false otherwise.
   */
  public boolean matches( String[] subSegments ) {
    // if there are fewer text segments than filter segments, then all the
    // segments of the filter could not possibly be satisfied
    if ( subSegments.length < segments.length ) {
      return false;
    }

    if ( ( ( segments != null ) && ( subSegments != null ) ) && ( subSegments.length >= segments.length ) ) {
      for ( int i = 0; i < segments.length; i++ ) {
        if ( segments[i].equals( ">" ) ) {
          return true; // EarlyExit wildcard
        }

        if ( !segments[i].equals( "*" ) ) {
          if ( !segments[i].equals( subSegments[i] ) ) {
            return false; // no match
          }
        }
      } // for each segment

      // check for too many subSegments to match our filter
      if ( subSegments.length > segments.length ) {
        return false;
      }

    } else {
      return false; // no match
    }

    // Made it all the way through, all segments match
    return true;
  }




  /**
   * Return the original filter string given in the constructor.
   *
   * @return the original filter string given in the constructor.
   */
  public String toString() {
    if ( segments.length > 0 ) {
      if ( segments.length > 1 ) {
        StringBuffer retval = new StringBuffer();

        for ( int i = 0; i < segments.length; i++ ) {
          retval.append( segments[i] );

          if ( i + 1 < segments.length ) {
            retval.append( '.' );
          }
        }

        return retval.toString();
      } else {
        return segments[0];
      }
    }

    return super.toString();
  }
}
