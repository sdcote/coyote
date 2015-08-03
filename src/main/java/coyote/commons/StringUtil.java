/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.commons;

/**
 * 
 */
public class StringUtil {

  /** CarriageReturn and LineFeed character sequence */
  public static final String CRLF = "\r\n";

  /** CarriageReturn string */
  public static final String CR = "\r";

  /** NewLine string */
  public static final String NL = "\n";

  /** Hard tab string */
  public static final String HT = "\t";

  /** Platform specific path separator (default = "/") */
  public static final String PATH_SEPARATOR = System.getProperty( "path.separator", "/" );

  /** Platform specific path separator (default = ":") */
  public static final String FILE_SEPARATOR = System.getProperty( "file.separator", ":" );

  /** Platform specific line separator (default = CRLF) */
  public static final String LINE_FEED = System.getProperty( "line.separator", "\r\n" );

  /** CharEncodingISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1. */
  public static final String ISO_8859_1 = "ISO-8859-1";

  /** Seven-bit ASCII, also known as ISO646-US, also known as the Basic Latin block of the Unicode character set. */
  public static final String US_ASCII = "US-ASCII";

  /** Sixteen-bit Unicode Transformation Format, The byte order specified by a mandatory initial byte-order mark (either order accepted on input, big-endian used on output) */
  public static final String UTF_16 = "UTF-16";

  /** Sixteen-bit Unicode Transformation Format, big-endian byte order. */
  public static final String UTF_16BE = "UTF-16BE";

  /** Sixteen-bit Unicode Transformation Format, little-endian byte order. */
  public static final String UTF_16LE = "UTF-16LE";

  /** Eight-bit Unicode Transformation Format. */
  public static final String UTF_8 = "UTF-8";

  /** Field ISO8859_1 */
  public static String ISO8859_1;
  static {
    final String iso = System.getProperty( "ISO_8859_1" );
    if ( iso != null ) {
      StringUtil.ISO8859_1 = iso;
    } else {
      try {
        new String( new byte[] { (byte)20 }, "ISO-8859-1" );

        StringUtil.ISO8859_1 = "ISO-8859-1";
      } catch ( final java.io.UnsupportedEncodingException e ) {
        StringUtil.ISO8859_1 = "ISO8859_1";
      }
    }
  }




  /**
   * Count the occurrences of the substring in string s.
   * 
   * @param str string to search in. Return 0 if this is null.
   * @param sub string to search for. Return 0 if this is null.
   */
  public static int countOccurrencesOf( String str, String sub ) {
    if ( str == null || sub == null || str.length() == 0 || sub.length() == 0 ) {
      return 0;
    }
    int count = 0;
    int pos = 0;
    int idx;
    while ( ( idx = str.indexOf( sub, pos ) ) != -1 ) {
      ++count;
      pos = idx + sub.length();
    }
    return count;
  }




  /**
   * Make sure a string is not null.
   * 
   * @param arg Any string, possibly null
   * 
   * @return An empty string if the original was null, else the original
   */
  public static final String notNull( final String arg ) {
    if ( arg == null ) {
      return new String( "" );
    }

    return arg;
  }




  /**
   * Checks if a string is not null, empty ("") and not only whitespace.
   * 
   * <p>This is a convenience wrapper around isBlank(String) to make code 
   * slightly more readable.</p>
   * 
   * @param str the String to check, may be null
   * 
   * @return <code>true</code> if the String is not empty and not null and not
   *         whitespace
   * 
   * @see #isBlank(String)
   */
  public static boolean isNotBlank( String str ) {
    return !StringUtil.isBlank( str );
  }




  /**
   * Checks if a string is not null, empty ("") and not only whitespace.
   * 
   * @param str the String to check, may be null
   * 
   * @return <code>true</code> if the String is not empty and not null and not
   *         whitespace
   */
  public static boolean isBlank( String str ) {
    int strLen;
    if ( str == null || ( strLen = str.length() ) == 0 ) {
      return true;
    }
    for ( int i = 0; i < strLen; i++ ) {
      if ( ( Character.isWhitespace( str.charAt( i ) ) == false ) ) {
        return false;
      }
    }
    return true;
  }




  /**
   * Add zeros to the beginning of the given number to make it the requested 
   * length.
   * 
   * <p>The requested string may be longer than requested if the string 
   * representation of the number is longer than the requested length. In 
   * effect, the size argument is therefore a requested minimum size and no 
   * truncation of the given number will occur.</p>
   * 
   * @param num the number to represent
   * @param size the requested length of the string to be returned
   * 
   * @return a string representing the given number padded with zeros to the 
   *         requested length.
   */
  public static String zeropad( final short num, final int size ) {
    return StringUtil.zeropad( (long)num, size );
  }




  /**
   * Add zeros to the beginning of the given number to make it the requested 
   * length.
   * 
   * <p>The requested string may be longer than requested if the string 
   * representation of the number is longer than the requested length. In 
   * effect, the size argument is therefore a requested minimum size and no 
   * truncation of the given number will occur.</p>
   * 
   * @param num the number to represent
   * @param size the requested length of the string to be returned
   * 
   * @return a string representing the given number padded with zeros to the 
   *         requested length.
   */
  public static String zeropad( final int num, final int size ) {
    return StringUtil.zeropad( (long)num, size );
  }




  /**
   * Add zeros to the beginning of the given number to make it the requested 
   * length.
   * 
   * <p>The requested string may be longer than requested if the string 
   * representation of the number is longer than the requested length. In 
   * effect, the size argument is therefore a requested minimum size and no 
   * truncation of the given number will occur.</p>
   * 
   * @param num the number to represent
   * @param size the requested length of the string to be returned
   * 
   * @return a string representing the given number padded with zeros to the 
   *         requested length.
   */
  public static String zeropad( final long num, final int size ) {
    final String value = Long.toString( num );

    if ( value.length() >= size ) {
      return value;
    }

    final StringBuffer buf = new StringBuffer( size );
    for ( int indx = 0; indx++ < ( size - value.length() ); buf.append( '0' ) ) {
      ;
    }

    buf.append( value );

    return buf.toString();
  }




  /**
   * Check that the given CharSequence is neither {@code null} nor of length 0.
   * Note: Will return {@code true} for a CharSequence that purely consists of whitespace.
   * <p><pre class="code">
   * StringUtil.hasLength(null) = false
   * StringUtil.hasLength("") = false
   * StringUtil.hasLength(" ") = true
   * StringUtil.hasLength("Hello") = true
   * </pre>
   * @param str the CharSequence to check (may be {@code null})
   * 
   * @return {@code true} if the CharSequence is not null and has length
   */
  public static boolean hasLength( CharSequence str ) {
    return ( str != null && str.length() > 0 );
  }




  /**
   * Check that the given String is neither {@code null} nor of length 0.
   * 
   * <p><strong>Note:</strong> This will return {@code true} for a String that 
   * consists entirely of whitespace.</p>
   * 
   * @param str the String to check (may be {@code null})
   * 
   * @return {@code true} if the String is not null and has length
   */
  public static boolean hasLength( String str ) {
    return hasLength( (CharSequence)str );
  }




  /**
   * Check whether the given CharSequence has actual text.
   * 
   * <p>Returns {@code true} if the string not {@code null}, its length is 
   * greater than 0, and it contains at least one non-whitespace character.</p>
   * 
   * <pre class="code">
   * StringUtils.hasText(null) = false
   * StringUtils.hasText("") = false
   * StringUtils.hasText(" ") = false
   * StringUtils.hasText("12345") = true
   * StringUtils.hasText(" 12345 ") = true
   * </pre>
   * 
   * @param str the CharSequence to check (may be {@code null})
   * 
   * @return {@code true} if the CharSequence is not {@code null}, its length is greater than 0, and it does not contain whitespace only
   */
  public static boolean hasText( CharSequence str ) {
    if ( !hasLength( str ) ) {
      return false;
    }
    int strLen = str.length();
    for ( int i = 0; i < strLen; i++ ) {
      if ( !Character.isWhitespace( str.charAt( i ) ) ) {
        return true;
      }
    }
    return false;
  }




  /**
   * Check whether the given String has actual text.
   * 
   * <p>Returns {@code true} if the string not {@code null}, its length is 
   * greater than 0, and it contains at least one non-whitespace character.</p>
   * 
   * @param str the String to check (may be {@code null})
   * 
   * @return {@code true} if the String is not {@code null}, its length is greater than 0, and it does not contain whitespace only
   */
  public static boolean hasText( String str ) {
    return hasText( (CharSequence)str );
  }




  /**
   * Return the given text in a string of the given length, aligned in the 
   * given manner and padded with the space (' ') character.
   * 
   * <p>Note: if the given text is larger than the requested string, a portion 
   * of the text will be lost. Which portion depends on the alignment. Right 
   * justified text will result in the beginning (left side) of the text being 
   * lost. Left justified text will result in the end of the text being lost 
   * and centered text will result in both sides of the text being lost.</p>
   * 
   * @param text the text to represent
   * @param length the length of the string to return
   * @param alignment 0 (or less) left justified, 1 = right and 2 (or greater) right justified.
   * 
   * @return A string of exactly the given length with the text aligned within as specified,
   */
  public static String fixedLength( String text, int length, int alignment ) {
    return fixedLength( text, length, alignment, ' ' );
  }




  /**
   * Return the given text in a string of the given length, aligned in the 
   * given manner and padded with the given character.
   * 
   * <p>Note: if the given text is larger than the requested string, a portion 
   * of the text will be lost. Which portion depends on the alignment. Right 
   * justified text will result in the beginning (left side) of the text being 
   * lost. Left justified text will result in the end of the text being lost 
   * and centered text will result in both sides of the text being lost.</p>
   * 
   * @param text the text to represent
   * @param length the length of the string to return
   * @param alignment 0 (or less) left justified, 1 = right and 2 (or greater) right justified.
   * @param padChar the character with which to pad the string.
   * 
   * @return A string of exactly the given length with the text aligned within as specified,
   */
  public static String fixedLength( String text, int length, int alignment, char padChar ) {
    int textLength = text.length();
    int padLength = length - textLength;

    StringBuffer b = new StringBuffer();

    if ( alignment < 1 ) {
      // left justification
      if ( padLength > 0 ) {
        b.append( text );
        for ( int i = 0; i < padLength; i++ ) {
          b.append( padChar );
        }
      } else if ( padLength < 0 ) {
        b.append( text.substring( 0, length ) );
      } else {
        b.append( text );
      }
    } else if ( alignment > 1 ) {
      // right justification
      if ( padLength > 0 ) {
        for ( int i = 0; i < padLength; i++ ) {
          b.append( padChar );
        }
        b.append( text );
      } else if ( padLength < 0 ) {
        b.append( text.substring( Math.abs( padLength ) ) );
      } else {
        b.append( text );
      }
    } else {
      // centered alignment
      if ( padLength > 0 ) {
        int lpad = padLength / 2;
        for ( int i = 0; i < lpad; i++ ) {
          b.append( padChar );
        }
        b.append( text );
        while ( b.length() < length ) {
          b.append( padChar );
        }
      } else if ( padLength < 0 ) {
        int lpad = padLength / 2;
        if ( Math.abs( padLength ) % 2 == 0 )
          b.append( text.substring( Math.abs( lpad ), length + 1 ) );
        else
          b.append( text.substring( Math.abs( lpad ), length ) );
      } else {
        b.append( text );
      }
    }

    return b.toString();
  }

}
