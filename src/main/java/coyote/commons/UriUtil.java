/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * Several Static methods for working with URI
 */
public class UriUtil {

  /**
   * Provide a no-exception throwing URI creation convenience method.
   *
   * <p>This is handy for use in declarations and other static contexts where a
   * null check is fine for checking for a valid URI or URI syntax.</p>
   *
   * <p> Consider:<br><tt>
   * <pre>
   * if( UriUtil.parse( text ) != null )
   * {
   *   System.out.println( &quot;The text '&quot; + text + &quot;' represents a valid URI&quot; );
   * }
   * </pre><br></tt> As opposed to: <br>
   * <tt>
   * <pre>
   * try
   * {
   *   new URI( text );
   *   System.out.println( &quot;The text '&quot; + text + &quot;' represents a valid URI&quot; );
   * }
   * catch( URISyntaxException use )
   * {
   *   // ignore
   * }
   * </pre><br></tt>
   * </p>
   *
   * @param text The text to parse into a URI
   *
   * @return The URI object created from the text or null if the text could not
   *         be parsed into a valid URI.
   */
  public static URI parse( final String text ) {
    URI retval = null;

    try {
      retval = new URI( text );
    } catch ( final URISyntaxException use ) {
      // Ignore and return null
    }

    return retval;
  }




  /**
   * Is the URI representing a file??
   *
   * @param uri the URI to process
   * 
   * @return true if the URI is representing a file, false otherwise
   */
  public static boolean isFile( final URI uri ) {
    if ( ( uri != null ) && ( uri.getScheme() != null ) ) {
      return uri.getScheme().equalsIgnoreCase( "file" );
    }

    return false;
  }




  /**
   * Is the URI representing a JAR file?
   *
   * @param uri the URI to process
   * 
   * @return true if the URI is representing a JAR file, false otherwise
   */
  public static boolean isJar( final URI uri ) {
    if ( ( uri != null ) && ( uri.getScheme() != null ) ) {
      return uri.getScheme().equalsIgnoreCase( "jar" );
    }

    return false;
  }




  /**
   * This returns a path suitable for the local file system
   *
   * @param uri the URI to process
   * 
   * @return a path suitable for the local file system
   */
  public static String getFilePath( final URI uri ) {
    if ( uri == null ) {
      return null;
    }

    final StringBuffer buffer = new StringBuffer();

    if ( uri.getScheme() == null ) {

    } else {

      if ( uri.getScheme().equalsIgnoreCase( "jar" ) ) {
        try {
          String retval = null;

          if ( uri.getSchemeSpecificPart().toLowerCase().startsWith( "file" ) ) {
            final URI furi = new URI( uri.getSchemeSpecificPart() );

            // recursive call to get the file path
            retval = UriUtil.getFilePath( furi );

            if ( retval != null ) {
              final int ptr = retval.indexOf( '!' );

              if ( ptr > -1 ) {
                retval = retval.substring( 0, ptr );
              }
            }

            if ( ( retval != null ) && ( retval.length() > 0 ) ) {
              return retval;
            }

            return null;
          } else {
            return new File( uri.getSchemeSpecificPart() ).getAbsolutePath();
          }
        } catch ( final Exception e ) {
          System.err.println( "File reference within jar URI is invalid: " + e.getMessage() );
        }
      } else if ( uri.getScheme().equalsIgnoreCase( "file" ) ) {
        if ( uri.getAuthority() != null ) {
          buffer.append( uri.getAuthority() );
        }
      }
    }// scheme = null 

    // get the path from the URI
    buffer.append( uri.getPath() );

    // Windows drive specifiers don't need the root '/'
    if ( ( buffer.charAt( 2 ) == ':' ) && ( buffer.charAt( 0 ) == '/' ) ) {
      buffer.delete( 0, 1 );
    }

    // return a normalize the path
    return normalizePath( buffer.toString() );

  }




  /**
   * Remove duplicate file separators, remove relation dots and correct all
   * non-platform specific file separators to those of the current platform.
   *
   * @param path The path to normalize
   *
   * @return The normalized path
   */
  public static String normalizePath( String path ) {
    path = normalizeSlashes( path );
    path = removeRelations( path );

    return path;
  }




  /**
   * Replace all the file separator characters (either '/' or '\') with the
   * proper file separator for this platform.
   *
   * @param path the path to process
   *
   * @return string representing the normalized path for this platform
   */
  public static String normalizeSlashes( String path ) {
    if ( path == null ) {
      return null;
    } else {
      path = path.replace( '/', File.separatorChar );
      path = path.replace( '\\', File.separatorChar );

      return path;
    }
  }




  /**
   * Remove the current and parent directory relation references from the given
   * path string.
   *
   * <p>Takes a string like &quot;\home\work\bin\..\lib&quot; and returns a
   * path like &quot;\home\work\lib&quot;</p>
   *
   * @param path The representative path with possible relational dot notation
   *
   * @return The representative path without the dots
   */
  public static String removeRelations( final String path ) {
    if ( path == null ) {
      return null;
    } else if ( path.length() == 0 ) {
      return path;
    } else {
      // Break the path into tokens and skip any '.' tokens
      final StringTokenizer st = new StringTokenizer( path, "/\\" );
      final String[] tokens = new String[st.countTokens()];

      int i = 0;

      while ( st.hasMoreTokens() ) {
        final String token = st.nextToken();

        if ( ( token != null ) && ( token.length() > 0 ) && !token.equals( "." ) ) {
          // if there is a reference to the parent, then just move back to the
          // previous token in the list, which is this tokens parent
          if ( token.equals( ".." ) ) {
            if ( i > 0 ) {
              tokens[--i] = null;
            }
          } else {
            tokens[i++] = token;
          }
        }
      }

      // Start building the new path from the tokens
      final StringBuffer retval = new StringBuffer();

      // If the original path started with a file separator, then make sure the
      // return value starts the same way
      if ( ( path.charAt( 0 ) == '/' ) || ( path.charAt( 0 ) == '\\' ) ) {
        retval.append( File.separatorChar );
      }

      // For each token in the path
      if ( tokens.length > 0 ) {
        for ( i = 0; i < tokens.length; i++ ) {
          if ( tokens[i] != null ) {
            retval.append( tokens[i] );
          }

          // if there is another token on the list, use the platform-specific
          // file separator as a delimiter in the return value
          if ( ( i + 1 < tokens.length ) && ( tokens[i + 1] != null ) ) {
            retval.append( File.separatorChar );
          }
        }
      }

      if ( ( path.charAt( path.length() - 1 ) == '/' ) || ( ( path.charAt( path.length() - 1 ) == '\\' ) && ( retval.charAt( retval.length() - 1 ) != File.separatorChar ) ) ) {
        retval.append( File.separatorChar );
      }

      return retval.toString();
    }
  }




  /**
   * Return a file reference for the given URI if it is a file or jar URI
   *
   * @param uri the URI to process
   *
   * @return the file path portion of the URI or null if it is not a FILE or JAR URI
   */
  public static File getFile( final URI uri ) {
    if ( uri == null ) {
      return null;
    }

    if ( UriUtil.isFile( uri ) || UriUtil.isJar( uri ) ) {
      return new File( UriUtil.getFilePath( uri ) );
    }

    return null;
  }




  /**
   * Return a file reference for the given URL
   *
   * @param url the URL to process
   *
   * @return the file path portion of the URI or null if it is not a FILE or JAR URL
   */
  public static File getFile( final URL url ) {
    try {
      return UriUtil.getFile( new URI( url.toString() ) );
    } catch ( final URISyntaxException e ) {
      // should never happen since URL is a subset of URI
    }

    return null;
  }

}
