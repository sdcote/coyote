/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
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

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;


/**
 * 
 */
public class StringUtil {

  /** CarriageReturn and LineFeed character sequence */
  public static final String CRLF = "\r\n";

  /** CarriageReturn string */
  public static final String CR = "\r";

  /** The space string */
  public static final String SP = " ";

  /** Hard tab string */
  public static final String HT = "\t";

  /** NewLine string */
  public static final String NL = "\n";

  /** Platform specific line separator (default = CRLF) */
  public static final String LINE_FEED = System.getProperty( "line.separator", "\r\n" );

  /** Length of the platform specific LineFeed sequence */
  public static final int LINE_FEED_LEN = StringUtil.LINE_FEED.length();

  /** Platform specific path separator (default = "/") */
  public static final String PATH_SEPARATOR = System.getProperty( "path.separator", "/" );

  /** Platform specific path separator (default = ":") */
  public static final String FILE_SEPARATOR = System.getProperty( "file.separator", ":" );

  /**
   * An "XML Safe" string require thats certain strings (or more correctly,
   * characters) be substituted for others. See page 257 of "XML by Example".
   * <ul> 
   * <li>&amp; - &amp;&amp;</li> 
   * <li>&lt; - &amp;&lt;</li>
   * <li>&gt; - &amp;&gt;</li>
   * <li>&quot; - &amp;&quot;</li> 
   * <li>&#39; - &amp;&#39;</li>
   * </ul>
   */
  public static final String XML_ENTITYREFS[] = { "&", "&amp;", "<", "&lt;", ">", "&gt;", "\"", "&quot;", "'", "&apos;", "\n", "&#xa;", "\r", "&#xd;", "\t", "&#x9;" };

  /** Same as XML but there is no entity reference for an apostrophe. */
  public static final String HTML_ENTITYREFS[] = { "&", "&amp;", "<", "&lt;", ">", "&gt;", "\"", "&quot;", "\n", "&#xa;", "\r", "&#xd;", "\t", "&#x9;" };

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

  /** Left Alignment value ({@value}) for {@code fixedLength} method */
  public static final int LEFT_ALIGNMENT = 0;
  /** Center Alignment value ({@value}) for {@code fixedLength} method */
  public static final int CENTER_ALIGNMENT = 1;
  /** Right Alignment value ({@value}) for {@code fixedLength} method */
  public static final int RIGHT_ALIGNMENT = 2;

  /** Double Quote character */
  public static final char DOUBLE_QUOTE = '"';

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
   * Checks the given character set name for validity in this runtime.
   * 
   * @param name the name of the character set to check
   * 
   * @return true if this runtime supports this character set, false otherwise
   */
  public static boolean checkCharacterSetName( String name ) {
    try {
      new String( new byte[] { (byte)20 }, name );
      return true;
    } catch ( final java.io.UnsupportedEncodingException e ) {
      return false;
    }

  }




  /**
   * Convert the given string into ISO 8859-1 encoding.
   * 
   * <p>This is the defacto standard for string encoding on the Internet</p>
   * 
   * @param text the text to encode
   * 
   * @return the bytes representing the encoded text or null if the text is null
   */
  public static byte[] getBytes( String text ) {
    byte[] retval = null;
    if ( text != null ) {
      try {
        retval = text.getBytes( StringUtil.ISO8859_1 );
      } catch ( final Exception ex ) {}
    }
    return retval;
  }




  /**
   * Convert the given byte array into a string using  ISO 8859-1 encoding.
   *  
   * @param bytes array of bytes to decode into a string
   * 
   * @return the decoded string or null if the passed data was null.
   */
  public static String getString( byte[] bytes ) {
    String retval = null;
    try {
      retval = new String( bytes, StringUtil.ISO8859_1 );
    } catch ( UnsupportedEncodingException e ) {
      e.printStackTrace();
    }
    return retval;
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
   * Checks if a string is null or empty ("").
   * 
   * @param str the String to check, may be null
   * 
   * @return <code>true</code> if the String is empty or null, false otherwise
   * 
   * @see #isNotEmpty(String)
   */
  public static boolean isEmpty( String str ) {
    if ( str == null || str.length() == 0 ) {
      return true;
    }
    return false;
  }




  /**
   * Checks if a string is not null and not empty ("").
   * 
   * <p>Whitespace is allowed.</p>
   * 
   * <p>This is a convenience wrapper around {@code isEmpty(String)} to make 
   * code slightly more readable.</p>
   * 
   * @param str the String to check, may be null
   * 
   * @return <code>true</code> if the String is not empty and not null, false othersize
   * 
   * @see #isEmpty(String)
   */
  public static boolean isNotEmpty( String str ) {
    return !StringUtil.isEmpty( str );
  }




  /**
   * Checks if a string is null, empty ("") or only whitespace.
   * 
   * @param str the String to check, may be null
   * 
   * @return {@code true} if the argument is empty or null or only whitespace
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
   * @param alignment 0 (or less) left justified, 1 = center and 2 (or greater) right justified.
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
   * @param alignment 0 (or less) left justified, 1 = center and 2 (or greater) right justified.
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




  /**
   * Strip off the namespace from a fully qualified class name.
   *
   * @param classname the fully qualified class name name to parse (without the ".class"
   *
   * @return just the local portion of the class name (everything after the last '.'.
   */
  public static String getLocalJavaName( String classname ) {
    return tail( classname, '.' );
  }




  /**
   * Strip off the path from a fully qualified file name.
   *
   * @param file the fully qualified file name to parse
   *
   * @return just the local portion of the file name (everything after the last '/'.
   */
  public static String getLocalFileName( String file ) {
    return tail( file, '/' );
  }




  /**
   * Return the string after the last occurrence of the given character in the
   * given string.
   *
   * <p>Useful for getting extensions from filenames. Also used to retrieve the
   * last segment of an IP address.</p>
   *
   * @param text the string to parse
   * @param ch the sentinel character
   *
   * @return the string after the last occurrence of the given character in the
   * given string
   */
  public static String tail( String text, char ch ) {
    int indx = text.lastIndexOf( ch );
    return ( indx != -1 ) ? text.substring( indx + 1 ) : text;
  }




  /**
   * Return the string before the last occurrence of the given character in
   * the given string.
   * 
   * <p>Useful for getting the body of a filename.</p>
   * 
   * @param text the string to parse
   * @param ch the sentinel character
   * 
   * @return the string before the last occurrence of the given character in
   * the given string.
   */
  public static String head( final String text, final char ch ) {
    final int indx = text.lastIndexOf( ch );
    return ( indx != -1 ) ? text.substring( 0, indx ) : text;
  }




  /**
   * Retrieve the value between the first and last double-quote characters.
   * 
   * <p>If there are no double-quotes or only one double-quote, the returned 
   * value is null.</p>
   * 
   * <p>It is possible the returned value itself contains double-quotes. This 
   * is because this method starts from the outside edges of the text and 
   * returns values within the outside pairings of double-quotes.</p>
   * 
   * @param text the text string to parse
   * 
   * @return the value between the outside pairing of double-quotes or null if 
   *         no parings were found.
   */
  public static String getQuotedValue( String text ) {
    String retval = null;
    int start = text.indexOf( DOUBLE_QUOTE );
    int end = text.lastIndexOf( DOUBLE_QUOTE );

    if ( start > -1 && end > start ) {
      retval = text.substring( start + 1, end );
    }

    return retval;
  }




  /**
   * Make a string safe to send as part of an XML message.
   * 
   * @param string the string to convert
   * 
   * @return Restored string.
   */
  public static final String StringToXML( final String string ) {
    return StringUtil.tokenSubst( StringUtil.XML_ENTITYREFS, StringUtil.notNull( string ), true );
  }




  /**
   * Make a string safe to send as part of an HTML message.
   * 
   * @param string
   * @return Restored string.
   */
  public static final String StringToHTML( final String string ) {
    return StringUtil.tokenSubst( StringUtil.HTML_ENTITYREFS, StringUtil.notNull( string ), true );
  }




  /**
   * Replace a list of tokens in a string.
   * 
   * <p>String {@code 2i} is replaced with String {@code 2i+1}. Order is very 
   * important. If you want to convert &lt; to &amp;&lt; and you also want to 
   * convert &amp; to &amp;$amp; then it is important that you first convert 
   * &amp; to &amp;&amp; before converting &lt; to &amp;&lt;. If you do not, 
   * then the &amp; in &amp;&lt; will be converted to &amp;&amp;&lt;.</p>
   * 
   * @param tokens is an array of strings such that string {@code 2i} is
   *            replaced with string {@code 2i+1}.
   * @param string is the string to be searched.
   * @param fromStart If true, the substitution will be performed from the
   *            beginning , otherwise the replacement will begin from the end
   *            of the array resulting in a reverse substitution
   * 
   * @return string with tokens replaced.
   */
  public static final String tokenSubst( final String[] tokens, final String string, final boolean fromStart ) {
    String temps = ( string == null ) ? "" : string;

    if ( temps.length() > 0 ) {
      final int delta = ( fromStart ) ? 2 : -2;
      int i_old = ( fromStart ) ? 0 : tokens.length - 1;
      int i_new = i_old + delta / 2;
      final int num_to_do = tokens.length / 2;
      int cnt;

      for ( cnt = 0; cnt < num_to_do; ++cnt ) {
        StringBuffer buf = null;
        int last_pos = 0;
        final String tok_string = tokens[i_old];
        final int tok_len = tok_string.length();
        int tok_pos = temps.indexOf( tok_string, last_pos );

        while ( tok_pos >= 0 ) {
          if ( buf == null ) {
            buf = new StringBuffer();
          }

          if ( last_pos != tok_pos ) {
            buf.append( temps.substring( last_pos, tok_pos ) );
          }

          buf.append( tokens[i_new] );

          last_pos = tok_pos + tok_len;
          tok_pos = temps.indexOf( tok_string, last_pos );
        }

        if ( ( last_pos < temps.length() ) && ( buf != null ) ) {
          buf.append( temps.substring( last_pos ) );
        }

        if ( buf != null ) {
          temps = buf.toString();
        }

        i_old += delta;
        i_new += delta;
      }
    }

    return temps;
  }




  /**
   * Replace one character with another in a string.
   * 
   * @param target the target character to replace
   * @param desired the desired character
   * @param string is the string to be searched.
   * 
   * @return string with tokens replaced.
   */
  public static final String charSubst( final char target, final char desired, final String string ) {
    final StringBuffer buf = new StringBuffer( ( string == null ) ? "" : string );

    for ( int indx = buf.length() - 1; indx >= 0; --indx ) {
      if ( buf.charAt( indx ) == target ) {
        buf.setCharAt( indx, desired );
      }
    }

    return buf.toString();
  }




  /**
   * Return a string made XML safe back to its original condition.
   * 
   * @param string the string to convert
   * 
   * @return XML String converted to an XML safe string.
   */
  public static final String XMLToString( final String string ) {
    return StringUtil.tokenSubst( StringUtil.XML_ENTITYREFS, string, false );
  }




  /**
   * Makes a string safe to place in HTML
   * 
   * @param string the string to convert
   * 
   * @return a new string with HTML characters replaces with HTML entities.
   */
  public static final String HTMLToString( final String string ) {
    return StringUtil.replace( StringUtil.tokenSubst( StringUtil.XML_ENTITYREFS, string, false ), "&nbsp;", "" );
  }




  /**
   * Tokenize the given string and return the string at given column index.
   * 
   * <p>Often text will be returned as a set of text separated by whitespace, 
   * this offers a quick method to return the token at a given location.</p>
   * 
   * @param text the line of text to tokenize
   * @param col the index of the
   * 
   * @return the token at the given column location or an empty string if the 
   *         column index was greater than the number of tokens in the text. 
   *         This will never return null.
   */
  public static String readColumn( final String text, final int col ) {
    final StringTokenizer stringtokenizer = new StringTokenizer( text );

    if ( stringtokenizer.countTokens() >= col ) {
      for ( int indx = 1; indx <= col; indx++ ) {
        final String retval = stringtokenizer.nextToken();

        if ( indx == col ) {
          return retval;
        }
      }
    }

    return "";
  }




  /**
   * Replace substrings within string.
   * 
   * @param text the text to scan
   * @param target the string to replace in the text
   * @param desired the string to put in place of the text
   * 
   * @return a string with all the occurrences of the target strings replaced
   *         with the desired strings
   */
  public static final String replace( final String text, final String target, final String desired ) {
    int ch = 0;
    int indx = text.indexOf( target, ch );
    if ( indx == -1 ) {
      return text;
    }

    final StringBuffer buf = new StringBuffer( text.length() + desired.length() );
    do {
      buf.append( text.substring( ch, indx ) );
      buf.append( desired );

      ch = indx + target.length();
    }
    while ( ( indx = text.indexOf( target, ch ) ) != -1 );

    if ( ch < text.length() ) {
      buf.append( text.substring( ch, text.length() ) );
    }

    return buf.toString();
  }




  /**
   * Checks if the CharSequence contains any character in the given set of characters.
   * 
   * @param cs the CharSequence to check, may be null
   * @param searchChars the chars to search for, may be null
   * 
   * @return the {@code true} if any of the chars are found, {@code false} if no match or null input
   */
  public static boolean containsAny( final CharSequence cs, final char[] searchChars ) {
    if ( isEmpty( cs ) || isEmpty( searchChars ) ) {
      return false;
    }
    final int csLength = cs.length();
    final int searchLength = searchChars.length;
    final int csLast = csLength - 1;
    final int searchLast = searchLength - 1;
    for ( int i = 0; i < csLength; i++ ) {
      final char ch = cs.charAt( i );
      for ( int j = 0; j < searchLength; j++ ) {
        if ( searchChars[j] == ch ) {
          if ( Character.isHighSurrogate( ch ) ) {
            if ( j == searchLast ) {
              // missing low surrogate, fine, like String.indexOf(String)
              return true;
            }
            if ( ( i < csLast ) && ( searchChars[j + 1] == cs.charAt( i + 1 ) ) ) {
              return true;
            }
          } else {
            // ch is in the Basic Multilingual Plane
            return true;
          }
        }
      }
    }
    return false;
  }




  /**
   * Checks if the CharSequence contains any character in the given set of characters.
   * 
   * @param cs the CharSequence to check, may be null
   * @param searchChars the chars to search for, may be null
   * 
   * @return the {@code true} if any of the chars are found, {@code false} if no match or null input
   */
  public static boolean containsAny( final CharSequence cs, final CharSequence searchChars ) {
    if ( searchChars == null ) {
      return false;
    }
    return containsAny( cs, toCharArray( searchChars ) );
  }




  /**
   * Counts how many times the substring appears in the larger string.
   * 
   * @param str the CharSequence to check, may be null
   * @param sub the substring to count, may be null
   * 
   * @return the number of occurrences, 0 if either CharSequence is {@code null}
   */
  public static int countMatches( final CharSequence str, final CharSequence sub ) {
    if ( isEmpty( str ) || isEmpty( sub ) ) {
      return 0;
    }
    int count = 0;
    int idx = 0;
    while ( ( idx = indexOf( str, sub, idx ) ) != -1 ) {
      count++;
      idx += sub.length();
    }
    return count;
  }




  /**
   * Checks if the CharSequence equals any character in the given set of characters.
   * 
   * @param cs the CharSequence to check
   * @param strs the CharSequence against which to check
   * 
   * @return true if equals any
   */
  public static boolean equalsAny( final CharSequence cs, final CharSequence[] strs ) {
    boolean eq = false;
    if ( cs == null ) {
      eq = strs == null;
    }

    if ( strs != null ) {
      for ( final CharSequence str : strs ) {
        eq = eq || str.equals( cs );
      }
    }

    return eq;
  }




  /**
   * Used by the indexOf(CharSequence methods) as a green implementation of indexOf.
   * 
   * @param cs the {@code CharSequence} to be processed
   * @param searchChar the {@code CharSequence} to be searched for
   * @param start the start index
   * 
   * @return the index where the search sequence was found
   */
  private static int indexOf( final CharSequence cs, final CharSequence searchChar, final int start ) {
    return cs.toString().indexOf( searchChar.toString(), start );
  }




  /**
   * @param array
   */
  private static boolean isEmpty( final char[] array ) {
    return ( array == null ) || ( array.length == 0 );
  }




  /**
   * Checks if a CharSequence is empty ("") or null.
   * 
   * @param cs the CharSequence to check, may be null
   * 
   * @return {@code true} if the CharSequence is empty or null
   */
  public static boolean isEmpty( final CharSequence cs ) {
    return ( cs == null ) || ( cs.length() == 0 );
  }




  /**
   * Green implementation of toCharArray.
   * 
   * @param cs the {@code CharSequence} to be processed
   * 
   * @return the resulting char array
   */
  private static char[] toCharArray( final CharSequence cs ) {
    if ( cs instanceof String ) {
      return ( (String)cs ).toCharArray();
    } else {
      final int sz = cs.length();
      final char[] array = new char[cs.length()];
      for ( int i = 0; i < sz; i++ ) {
        array[i] = cs.charAt( i );
      }
      return array;
    }
  }




  /**
   * Remove all whitespace from the given string.
   * 
   * @param text The text from which the whitespace is to be removed.
   * 
   * @return a copy of the given text string with no whitespace or null of the
   *         passed text was null.
   */
  public static final String removeWhitespace( final String text ) {
    String retval = null;
    if ( text != null ) {
      final char[] chars = new char[text.length()];
      int mrk = 0;

      for ( int i = 0; i < text.length(); i++ ) {
        final char c = text.charAt( i );
        if ( !Character.isWhitespace( c ) ) {
          chars[mrk++] = c;
        }
      }

      if ( mrk > 0 ) {
        final char[] data = new char[mrk];
        for ( int i = 0; i < mrk; data[i] = chars[i++] ) {
          ;
        }

        retval = new String( data );
      } else {
        retval = new String();
      }
    }

    return retval;
  }




  /**
   * @return null if string is null or empty
   */
  public static String getNullIfEmpty( final String text ) {
    return StringUtil.isBlank( text ) ? null : text.trim();
  }

}
