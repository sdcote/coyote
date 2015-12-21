package coyote.commons.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** 
 * A String tokenizer that accepts delimiters that are greater than one 
 * character.
 */
public class Tokenizer {
  
  
  /**
   * Iterator over the tokens in an expression
   */
  private class StringTokenizerIterator implements Iterator<String> {
    private final StringTokenizer tokens;

    private String nextToken = null;




    /**
     * @param tokens The StringTokenizer on which is based this instance.
     */
    public StringTokenizerIterator( final StringTokenizer tokens ) {
      this.tokens = tokens;
    }




    private boolean buildNextToken() {
      while ( ( nextToken == null ) && tokens.hasMoreTokens() ) {
        nextToken = tokens.nextToken();
        if ( trimTokens ) {
          nextToken = nextToken.trim();
        }
        if ( nextToken.isEmpty() ) {
          nextToken = null;
        }
      }
      return nextToken != null;
    }




    @Override
    public boolean hasNext() {
      return buildNextToken();
    }




    @Override
    public String next() {
      if ( !buildNextToken() ) {
        throw new NoSuchElementException();
      }
      final String token = nextToken;
      nextToken = null;
      return token;
    }




    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }




  private static Pattern delimitersToRegexp( final List<String> delimiters ) {
    // First, create a regular expression that match the union of the 
    // delimiters. Be aware that, in case of delimiters containing others (e.g. 
    // example && and &), the longer may be before the shorter (&& should be 
    // before &) or the regexpr parser will recognize && as two &.
    Collections.sort( delimiters, new Comparator<String>() {
      @Override
      public int compare( final String o1, final String o2 ) {
        return -o1.compareTo( o2 );
      }
    } );
    // Build a string that will contain the regular expression
    final StringBuilder result = new StringBuilder();
    result.append( '(' );
    for ( final String delim : delimiters ) {
      // For each delimiter
      if ( result.length() != 1 ) {
        // Add it to the union
        result.append( '|' );
      }
      // Quote the delimiter as it could contain some regexpr reserved characters
      result.append( "\\Q" ).append( delim ).append( "\\E" );
    }
    result.append( ')' );
    return Pattern.compile( result.toString() );
  }

  private Pattern pattern;

  private String tokenDelimiters;

  private boolean trimTokens;




  /**
   * Constructs a tokenizer that trims all the tokens.
   * 
   * @param delimiters the delimiters of the tokenizer, usually, the operators 
   *        symbols, the brackets and the function argument separator are used 
   *        as delimiter in the string.
   */
  public Tokenizer( final List<String> delimiters ) {
    if ( onlyOneChar( delimiters ) ) {
      final StringBuilder builder = new StringBuilder();
      for ( final String delimiter : delimiters ) {
        builder.append( delimiter );
      }
      tokenDelimiters = builder.toString();
    } else {
      pattern = delimitersToRegexp( delimiters );
    }
    trimTokens = true;
  }




  private void addToTokens( final List<String> tokens, String token ) {
    if ( trimTokens ) {
      token = token.trim();
    }
    if ( !token.isEmpty() ) {
      tokens.add( token );
    }
  }




  /**
   * Tests whether this tokens trims the tokens returned by 
   * {@link #tokenize(String)} method.
   *  
   * @return true if tokens are trimmed.
   */
  public boolean isTrimTokens() {
    return trimTokens;
  }




  /** 
   * Tests whether a String list contains only 1 character length elements.
   * 
   * @param delimiters The list to test
   * 
   * @return true if it contains only one char length elements (or no elements) 
   */
  private boolean onlyOneChar( final List<String> delimiters ) {
    for ( final String delimiter : delimiters ) {
      if ( delimiter.length() != 1 ) {
        return false;
      }
    }
    return true;
  }




  /**
   * Sets the trimTokens attribute.
   * 
   * <p>Note that empty tokens are always omitted by this class.</p>
   * 
   * @param trimTokens true to have the tokens returned by 
   *        {@link #tokenize(String)} method trimmed.
   */
  public void setTrimTokens( final boolean trimTokens ) {
    this.trimTokens = trimTokens;
  }




  /** 
   * Converts a string into tokens.
   * 
   * <p>Example: The result for the expression "{@code -1+min(10,3)}" evaluated 
   * for a DoubleEvaluator is an iterator on "-", "1", "+", "min", "(", "10", 
   * ",", "3", ")".</p>
   * 
   * @param string The string to be split into tokens
   * 
   * @return The tokens
   */
  public Iterator<String> tokenize( final String string ) {
    if ( pattern != null ) {
      final List<String> res = new ArrayList<String>();
      final Matcher m = pattern.matcher( string );
      int pos = 0;
      while ( m.find() ) {
        // While there's a delimiter in the string
        if ( pos != m.start() ) {
          // If there's something between the current and the previous delimiter
          // Add to the tokens list
          addToTokens( res, string.substring( pos, m.start() ) );
        }
        addToTokens( res, m.group() ); // add the delimiter
        pos = m.end(); // Remember end of delimiter
      }
      if ( pos != string.length() ) {
        // If it remains some characters in the string after last delimiter
        addToTokens( res, string.substring( pos ) );
      }
      // Return the result
      return res.iterator();
    } else {
      return new StringTokenizerIterator( new StringTokenizer( string, tokenDelimiters, true ) );
    }
  }

}