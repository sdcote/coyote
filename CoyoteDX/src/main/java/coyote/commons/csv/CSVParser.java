package coyote.commons.csv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A CSV parser which splits a single line into fields using a field delimiter.
 */
public class CSVParser {

  private final char separator;

  private final char quotechar;

  private final char escape;

  private final boolean useStrictQuotes;

  private String pending;
  private boolean isInField = false;

  private final boolean ignoreLeadingWhitespace;

  /** The default separator to use if none is supplied to the constructor. */
  public static final char SEPARATOR = ',';

  public static final int INITIAL_READ_SIZE = 128;

  /** The default quote character to use if none is supplied to the constructor. */
  public static final char QUOTE_CHARACTER = '"';

  /** The default escape character to use if none is supplied to the constructor. */
  public static final char ESCAPE_CHARACTER = '\\';

  /** The default strict quote behavior to use if none is supplied to the constructor */
  public static final boolean STRICT_QUOTES = false;

  /** The default leading whitespace behavior to use if none is supplied to the constructor */
  public static final boolean IGNORE_LEADING_WHITESPACE = true;

  /** This is the "null" character - if a value is set to this then it is ignored. I.E. if the quote character is set to null then there is no quote character. */
  public static final char NULL_CHARACTER = '\0';




  /**
   * Constructs CSVParser.
   */
  public CSVParser() {
    this( SEPARATOR, QUOTE_CHARACTER, ESCAPE_CHARACTER );
  }




  /**
   * Constructs CSVParser.
   *
   * @param separator the delimiter to use for separating entries.
   */
  public CSVParser( final char separator ) {
    this( separator, QUOTE_CHARACTER, ESCAPE_CHARACTER );
  }




  /**
   * Constructs CSVParser.
   *
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   */
  public CSVParser( final char separator, final char quotechar ) {
    this( separator, quotechar, ESCAPE_CHARACTER );
  }




  /**
   * Constructs CSVParser.
   *
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   */
  public CSVParser( final char separator, final char quotechar, final char escape ) {
    this( separator, quotechar, escape, STRICT_QUOTES );
  }




  /**
   * Constructs CSVParser.
   *
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   * @param strictQuotes if true, characters outside the quotes are ignored
   */
  public CSVParser( final char separator, final char quotechar, final char escape, final boolean strictQuotes ) {
    this( separator, quotechar, escape, strictQuotes, IGNORE_LEADING_WHITESPACE );
  }




  /**
   * Constructs CSVParser.
   *
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   * @param strictQuotes if true, characters outside the quotes are ignored
   * @param ignoreLeadingWhiteSpace if true, white space in front of a quote in a field is ignored
   */
  public CSVParser( final char separator, final char quotechar, final char escape, final boolean strictQuotes, final boolean ignoreLeadingWhiteSpace ) {
    if ( anyCharactersAreTheSame( separator, quotechar, escape ) ) {
      throw new UnsupportedOperationException( "The separator, quote, and escape characters must be different!" );
    }
    if ( separator == NULL_CHARACTER ) {
      throw new UnsupportedOperationException( "The separator character must be defined!" );
    }
    this.separator = separator;
    this.quotechar = quotechar;
    this.escape = escape;
    this.useStrictQuotes = strictQuotes;
    this.ignoreLeadingWhitespace = ignoreLeadingWhiteSpace;
  }




  public String[] parseLineMulti( final String nextLine ) throws ParseException {
    return parseLine( nextLine, true );
  }




  public String[] parseLine( final String nextLine ) throws ParseException {
    return parseLine( nextLine, false );
  }




  /**
   * Parses an incoming String and returns an array of elements.
   *
   * @param nextLine the string to parse
   * @param multi support multiple lines in values
   * 
   * @return the comma delimited list of elements, or null if nextLine is null
   * 
   * @throws ArgumentException Un-terminated quoted field at end of CSV line
   */
  private String[] parseLine( final String nextLine, final boolean multi ) throws ParseException {

    if ( !multi && ( pending != null ) ) {
      pending = null;
    }

    if ( nextLine == null ) {
      if ( pending != null ) {
        final String s = pending;
        pending = null;
        return new String[] { s };
      } else {
        return null;
      }
    }

    final List<String> tokensOnThisLine = new ArrayList<String>();
    StringBuilder sb = new StringBuilder( INITIAL_READ_SIZE );
    boolean inQuotes = false;
    if ( pending != null ) {
      sb.append( pending );
      pending = null;
      inQuotes = true;
    }
    for ( int i = 0; i < nextLine.length(); i++ ) {

      final char c = nextLine.charAt( i );
      if ( c == this.escape ) {
        if ( isNextCharacterEscapable( nextLine, inQuotes || isInField, i ) ) {
          sb.append( nextLine.charAt( i + 1 ) );
          i++;
        }
      } else if ( c == quotechar ) {
        if ( isNextCharacterEscapedQuote( nextLine, inQuotes || isInField, i ) ) {
          sb.append( nextLine.charAt( i + 1 ) );
          i++;
        } else {
          if ( !useStrictQuotes ) {
            if ( ( i > 2 ) && ( nextLine.charAt( i - 1 ) != this.separator ) && ( nextLine.length() > ( i + 1 ) ) && ( nextLine.charAt( i + 1 ) != this.separator ) ) {

              if ( ignoreLeadingWhitespace && ( sb.length() > 0 ) && isAllWhiteSpace( sb ) ) {
                sb.setLength( 0 );
              } else {
                sb.append( c );
              }

            }
          }

          inQuotes = !inQuotes;
        }
        isInField = !isInField;
      } else if ( ( c == separator ) && !inQuotes ) {
        tokensOnThisLine.add( sb.toString() );
        sb.setLength( 0 );
        isInField = false;
      } else {
        if ( !useStrictQuotes || inQuotes ) {
          sb.append( c );
          isInField = true;
        }
      }
    }

    if ( inQuotes ) {
      if ( multi ) {

        sb.append( "\n" );
        pending = sb.toString();
        sb = null;
      } else {
        throw new ParseException( "Un-terminated quoted field at end of CSV line", -1 );
      }
    }
    if ( sb != null ) {
      tokensOnThisLine.add( sb.toString() );
    }
    return tokensOnThisLine.toArray( new String[tokensOnThisLine.size()] );

  }




  /**
   * precondition: the current character is a quote or an escape
   *
   * @param nextLine the current line
   * @param inQuotes true if the current context is quoted
   * @param i        current index in line
   * 
   * @return true if the following character is a quote
   */
  private boolean isNextCharacterEscapedQuote( final String nextLine, final boolean inQuotes, final int i ) {
    return inQuotes && ( nextLine.length() > ( i + 1 ) ) && ( nextLine.charAt( i + 1 ) == quotechar );
  }




  /**
   * precondition: the current character is an escape
   *
   * @param nextLine the current line
   * @param inQuotes true if the current context is quoted
   * @param i        current index in line
   * 
   * @return true if the following character is a quote
   */
  protected boolean isNextCharacterEscapable( final String nextLine, final boolean inQuotes, final int i ) {
    return inQuotes && ( nextLine.length() > ( i + 1 ) ) && ( ( nextLine.charAt( i + 1 ) == quotechar ) || ( nextLine.charAt( i + 1 ) == this.escape ) );
  }




  /**
   * @return true if something was left over from last call(s)
   */
  boolean isPending() {
    return pending != null;
  }




  /**
   * precondition: sb.length() &gt; 0
   *
   * @param cs A sequence of characters to examine
   * 
   * @return true if every character in the sequence is whitespace
   */
  protected boolean isAllWhiteSpace( final CharSequence cs ) {
    final boolean result = true;
    for ( int i = 0; i < cs.length(); i++ ) {
      final char c = cs.charAt( i );

      if ( !Character.isWhitespace( c ) ) {
        return false;
      }
    }
    return result;
  }




  private boolean anyCharactersAreTheSame( final char separator, final char quotechar, final char escape ) {
    return isSameCharacter( separator, quotechar ) || isSameCharacter( separator, escape ) || isSameCharacter( quotechar, escape );
  }




  private boolean isSameCharacter( final char c1, final char c2 ) {
    return ( c1 != NULL_CHARACTER ) && ( c1 == c2 );
  }

}
