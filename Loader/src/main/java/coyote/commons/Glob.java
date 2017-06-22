/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial implementation
 */
package coyote.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * The Glob class provides filename globbing.
 *
 * <p>"to glob" means to substitute a filename for a wildcard pattern. Lazy
 * import statements like <b><code>import java.io.*;</code></b> is an example
 * of globbing.
 *
 * <p>Glob can be used for matching files against a <em>globbing
 * expression</em>. It can also be used for very simple string matching.
 *
 * <p>For instance, "*.class" would match all class files and "*.java" would
 * match all Java source files. You could get text files numbered between 10
 * and 99 with "??.txt" or more specifically with "[0-9][0-9].txt".
 *
 * Recognized wildcards:
 * <ul>
 * <li><strong>*</strong> - Matches any number of any characters
 * <li><strong>?</strong> - Matches one of any characters
 * <li><strong>[]</strong> - Matches any of enclosed characters, ranges (e.g.,
 * [a-z]) are supported.
 * </ul>
 */
public class Glob extends Object {
  private Vector specs = null;




  /**
   * Constructs a new Glob object, with no match list.
   */
  public Glob() {}




  /**
   * Constructs a new Glob object, setting the match list to that specified by
   * the parameter.
   *
   * @param default_spec The default match specs.
   */
  public Glob( final String default_spec ) {
    setMatchSpec( default_spec );
  }




  /**
   * Adds the glob matching specifications from the file to the current list of
   * matching specifications.
   *
   * <p>One matching specification per line.
   *
   * @param matchFile file containing match specifications.
   */
  public void addMatchFile( final File matchFile ) {
    String line;
    boolean ok = true;
    BufferedReader in = null;

    if ( specs == null ) {
      specs = new Vector();
    }

    try {
      in = new BufferedReader( new FileReader( matchFile ) );
    } catch ( final IOException ex ) {
      in = null;
      ok = false;
    }

    for ( ; ok; ) {
      try {
        line = in.readLine();
      } catch ( final IOException ex ) {
        line = null;
      }

      if ( line == null ) {
        break;
      }

      addMatchSpec( line );
    }

    if ( in != null ) {
      try {
        in.close();
      } catch ( final IOException ex ) {}
    }
  }




  /**
   * Adds the glob matching specifications to the current list of matching
   * specifications.
   *
   * @param spec The string listing the specs to add.
   */
  public void addMatchSpec( final String spec ) {
    String toke;
    int i, count;

    if ( specs == null ) {
      specs = new Vector();
    }

    final StringTokenizer toker = new StringTokenizer( spec );

    count = toker.countTokens();

    for ( i = 0; i < count; ++i ) {
      try {
        toke = toker.nextToken();
      } catch ( final NoSuchElementException ex ) {
        break;
      }

      if ( toke.equals( "!" ) ) {
        specs = new Vector();
      } else {
        specs.addElement( toke );
      }
    }
  }




  /**
   * Dump the match specifications.
   * 
   * @param message a message to prefix the dump
   */
  public String dumpMatchSpecs( final String message ) {
    final StringBuffer b = new StringBuffer( message );
    if ( ( message != null ) && ( message.length() > 0 ) ) {
      b.append( "\n" );
    }

    if ( ( specs == null ) || ( specs.size() == 0 ) ) {
      b.append( "   No match specifications." );
      b.toString();
    }

    for ( int i = 0; i < specs.size(); ++i ) {
      b.append( "   [" );
      b.append( i );
      b.append( "] '" );
      b.append( (String)specs.elementAt( i ) );
      b.append( "'\n" );
    }
    return b.toString();
  }




  /**
   * Determines if a filename matches a single expression.
   *
   * @param fileName The name of the file to check.
   * @param matchExpr The expression to check against.
   *
   * @return  true if the file name matches the expression, false otherwise.
   */
  private boolean fileMatchesExpr( final String fileName, final String matchExpr ) {
    return matchExprRecursor( fileName, matchExpr, 0, 0 );
  }




  /**
   * Determines if a file is to be matched.
   *
   * @param name The name of the file to check.
   *
   * @return true if the file is matched, false otherwise.
   */
  public boolean isFileMatched( final String name ) {
    if ( ( specs == null ) || ( specs.size() == 0 ) ) {
      return false;
    }

    for ( int i = 0; i < specs.size(); ++i ) {
      final String spec = (String)specs.elementAt( i );

      if ( fileMatchesExpr( name, spec ) ) {
        return true;
      }
    }

    return false;
  }




  /**
   * An internal routine to implement expression matching. 
   * 
   * @param string The string to be compared.
   * @param pattern The expression to compare <em>string</em> to.
   * @param sIdx The index of where we are in <em>string</em>.
   * @param pIdx The index of where we are in <em>pattern</em>.
   *
   * @return True if <em>string</em> matched pattern, else false.
   */
  private boolean matchExprRecursor( final String string, final String pattern, int sIdx, int pIdx ) {
    final int pLen = pattern.length();
    final int sLen = string.length();

    for ( ;; ) {
      if ( pIdx >= pLen ) {
        if ( sIdx >= sLen ) {
          return true;
        } else {
          return false;
        }
      }

      if ( ( sIdx >= sLen ) && ( pattern.charAt( pIdx ) != '*' ) ) {
        return false;
      }

      // Check for a '*' as the next pattern char. This is handled by a
      //recursive call for each postfix of the name.
      if ( pattern.charAt( pIdx ) == '*' ) {
        if ( ++pIdx >= pLen ) {
          return true;
        }

        for ( ;; ) {
          if ( matchExprRecursor( string, pattern, sIdx, pIdx ) ) {
            return true;
          }

          if ( sIdx >= sLen ) {
            return false;
          }

          ++sIdx;
        }
      }

      // Check for '?' as the next pattern char.
      // This matches the current character.
      if ( pattern.charAt( pIdx ) == '?' ) {
        ++pIdx;
        ++sIdx;

        continue;
      }

      // Check for '[' as the next pattern char. This is a list of acceptable
      // characters, which can include character ranges.
      if ( pattern.charAt( pIdx ) == '[' ) {
        for ( ++pIdx;; ++pIdx ) {
          if ( ( pIdx >= pLen ) || ( pattern.charAt( pIdx ) == ']' ) ) {
            return false;
          }

          if ( pattern.charAt( pIdx ) == string.charAt( sIdx ) ) {
            break;
          }

          if ( ( pIdx < ( pLen - 1 ) ) && ( pattern.charAt( pIdx + 1 ) == '-' ) ) {
            if ( pIdx >= ( pLen - 2 ) ) {
              return false;
            }

            final char chStr = string.charAt( sIdx );
            final char chPtn = pattern.charAt( pIdx );
            final char chPtn2 = pattern.charAt( pIdx + 2 );

            if ( ( chPtn <= chStr ) && ( chPtn2 >= chStr ) ) {
              break;
            }

            if ( ( chPtn >= chStr ) && ( chPtn2 <= chStr ) ) {
              break;
            }

            pIdx += 2;
          }
        }

        for ( ; pattern.charAt( pIdx ) != ']'; ++pIdx ) {
          if ( pIdx >= pLen ) {
            --pIdx;

            break;
          }
        }

        ++pIdx;
        ++sIdx;

        continue;
      }

      // Check for backslash escapes. Just skip over to match the next char
      if ( pattern.charAt( pIdx ) == '\\' ) {
        if ( ++pIdx >= pLen ) {
          return false;
        }
      }

      if ( ( pIdx < pLen ) && ( sIdx < sLen ) ) {
        if ( pattern.charAt( pIdx ) != string.charAt( sIdx ) ) {
          return false;
        }
      }

      ++pIdx;
      ++sIdx;
    }
  }




  /**
   * Replaces all current match specs with those passed in.
   *
   * <p>The given string will be split into different match specifications
   * using the <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code> delimiters.
   *
   * @param spec The string listing the specs to replace with.
   */
  public void setMatchSpec( final String spec ) {
    if ( specs != null ) {
      specs.removeAllElements();
    }

    addMatchSpec( spec );
  }




  /**
   * @return the size (number) of the match specifications
   */
  public int size() {
    return specs.size();
  }
}