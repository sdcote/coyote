/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;


/**
 * Several Static methods for working with URI
 */
public class UriUtil {

  /** a map of scheme/protocol names to port numbers */
  private static final HashMap<String, Integer> portMap = new HashMap<String, Integer>();

  /** a map of scheme/protocol names to udp-only port numbers */
  private static final HashMap<String, Integer> udpMap = new HashMap<String, Integer>();

  static {
    // Setup the protocol lookup entries for official and well-known ports
    UriUtil.addPort( "applix", 999 ); // UDP
    UriUtil.addPort( "biff", 512 );
    UriUtil.addPort( "bootpc", 68 );
    UriUtil.addPort( "bootps", 67 );
    UriUtil.addPort( "cadlock2", 1000 ); // UDP
    UriUtil.addPort( "chargen", 19 );
    UriUtil.addPort( "courier", 530 ); // experimental
    UriUtil.addPort( "csnet-ns", 105 );
    UriUtil.addPort( "cvc", 1495 ); // Network Console
    UriUtil.addPort( "cvc_hostd", 442 ); // Network Console
    UriUtil.addPort( "cvspserver", 2401 ); // CVS pserver
    UriUtil.addPort( "daytime", 13 );
    UriUtil.addPort( "discard", 9 );
    UriUtil.addPort( "domain", 53 );
    UriUtil.addPort( "dtspc", 6112 ); // CDE subprocess control
    UriUtil.addPort( "echo", 7 );
    UriUtil.addPort( "eklogin", 2105 ); // Kerberos encrypted rlogin
    UriUtil.addPort( "exec", 512 );
    UriUtil.addPort( "finger", 79 );
    UriUtil.addPort( "fs", 7100 ); // Font server
    UriUtil.addPort( "ftp", 21 );
    UriUtil.addPort( "ftp-data", 20 );
    UriUtil.addPort( "hostnames", 101 );
    UriUtil.addPort( "http", 80 );
    UriUtil.addPort( "https", 443 );
    UriUtil.addPort( "imap", 143 );
    UriUtil.addPort( "ingreslock", 1524 );
    UriUtil.addPort( "iso-tsap", 102 );
    UriUtil.addPort( "kerberos", 750 ); // Kerberos key server
    UriUtil.addPort( "kerberos-adm", 749 ); // Kerberos V5 Administration
    UriUtil.addPort( "klogin", 543 ); // Kerberos authenticated rlogin
    UriUtil.addPort( "krb5_prop", 754 ); // Kerberos V5 KDC propogation
    UriUtil.addPort( "kshell", 544 ); // Kerberos authenticated remote shell
    UriUtil.addPort( "ldap", 389 );
    UriUtil.addPort( "ldaps", 636 );
    UriUtil.addPort( "link", 87 );
    UriUtil.addPort( "listen", 2766 ); // System V listener port
    UriUtil.addPort( "loadav", 750 ); // UDP
    UriUtil.addPort( "lockd", 4045 ); // NFS lock daemon/manager
    UriUtil.addPort( "login", 513 );
    UriUtil.addPort( "mail", 25 );
    UriUtil.addPort( "mailto", 25 );
    UriUtil.addPort( "mobile-ip", 434 ); // Mobile-IP
    UriUtil.addPort( "monitor", 561 ); // experimental
    UriUtil.addPort( "name", 42 );
    UriUtil.addPort( "netbios-dgm", 138 ); // NETBIOS Datagram Service
    UriUtil.addPort( "netbios-ns", 137 ); // NETBIOS Name Service
    UriUtil.addPort( "netbios-ssn", 139 ); // NETBIOS Session Service
    UriUtil.addPort( "netstat", 15 );
    UriUtil.addPort( "new-rwho", 550 ); // experimental
    UriUtil.addPort( "news", 144 ); // Window System
    UriUtil.addPort( "nfsd", 2049 ); // NFS server daemon (cots)
    UriUtil.addPort( "nntp", 119 ); // Network News Transfer
    UriUtil.addPort( "notify", 773 ); // UDP
    UriUtil.addPort( "ntp", 123 ); // Network Time Protocol
    UriUtil.addPort( "ock", 1000 ); // UDP
    UriUtil.addPort( "pcserver", 600 ); // ECD Integrated PC board srvr
    UriUtil.addPort( "pop", 110 ); // De-facto POP protocol of choice
    UriUtil.addPort( "pop-2", 109 ); // Post Office
    UriUtil.addPort( "pop2", 109 );
    UriUtil.addPort( "pop3", 110 );
    UriUtil.addPort( "printer", 515 ); // line printer spooler
    UriUtil.addPort( "puprouter", 999 ); // UDP
    UriUtil.addPort( "ripng", 521 );
    UriUtil.addPort( "rje", 77 );
    UriUtil.addPort( "rlogin", 513 );
    UriUtil.addPort( "rlp", 39 ); // UDP Resource Location Protocol RFC 887.
    UriUtil.addPort( "rmonitor", 560 ); // experimental
    UriUtil.addPort( "route", 520 );
    UriUtil.addPort( "rxe", 761 ); // UDP
    UriUtil.addPort( "secureid", 124 ); // UDP
    UriUtil.addPort( "shell", 514 ); // no passwords used
    UriUtil.addPort( "slp", 427 ); // Service Location Protocol, V2
    UriUtil.addPort( "smtp", 25 );
    UriUtil.addPort( "ssh", 22 ); // Secure SHell
    UriUtil.addPort( "sftp", 22 ); // Secure SHell File Transfer Protocol
    UriUtil.addPort( "scp", 22 ); // Secure Copy (uses SSH)
    UriUtil.addPort( "sunrpc", 111 );
    UriUtil.addPort( "supdup", 95 );
    UriUtil.addPort( "syslog", 514 );
    UriUtil.addPort( "systat", 11 );
    UriUtil.addPort( "talk", 517 );
    UriUtil.addPort( "tcpmux", 1 );
    UriUtil.addPort( "tell", 754 ); // UDP
    UriUtil.addPort( "telnet", 23 );
    UriUtil.addPort( "tftp", 69 );
    UriUtil.addPort( "time", 37 );
    UriUtil.addPort( "ufsd", 1008 ); // UFS-aware server
    UriUtil.addPort( "uucp", 540 ); // uucp daemon
    UriUtil.addPort( "uucp-path", 117 );
    UriUtil.addPort( "vsinet", 996 ); // UDP
    UriUtil.addPort( "who", 513 );
    UriUtil.addPort( "whois", 43 );
    UriUtil.addPort( "wins", 42 ); // UDP Host Name Server - Microsoft WINS
    UriUtil.addPort( "www", 80 );
    UriUtil.addPort( "www-ldap-gw", 1760 ); // HTTP to LDAP gateway
    UriUtil.addPort( "x400", 103 ); // ISO Mail
    UriUtil.addPort( "x400-snd", 104 );

    // Add the UDP-only protocols
    UriUtil.addUdpPort( "applix", 999 ); // UDP
    UriUtil.addUdpPort( "cadlock2", 1000 ); // UDP
    UriUtil.addUdpPort( "kerberos", 750 ); // Kerberos key server
    UriUtil.addUdpPort( "loadav", 750 ); // UDP
    UriUtil.addUdpPort( "notify", 773 ); // UDP
    UriUtil.addUdpPort( "ock", 1000 ); // UDP
    UriUtil.addUdpPort( "puprouter", 999 ); // UDP
    UriUtil.addUdpPort( "rlp", 39 ); // UDP Resource Location Protocol RFC 887.
    UriUtil.addUdpPort( "rxe", 761 ); // UDP
    UriUtil.addUdpPort( "secureid", 124 ); // UDP
    UriUtil.addUdpPort( "tell", 754 ); // UDP
    UriUtil.addUdpPort( "vsinet", 996 ); // UDP
    UriUtil.addUdpPort( "wins", 42 ); // UDP Host Name Server - Microsoft WINS

    UriUtil.addUdpPort( "mbus", 7943 ); // UDP Micro Bus 
  }




  /**
   * Adds a protocol scheme name mapping to a port.
   *
   * @param scheme the scheme portion of a URI/URL that the lookup should use
   * @param port the port number to map to that scheme
   */
  private static void addPort( final String scheme, final int port ) {
    UriUtil.portMap.put( scheme.toLowerCase(), new Integer( port ) );
  }




  /**
   * Adds a UDP-only protocol scheme name mapping to a port.
   *
   * @param scheme the scheme portion of a URI/URL that the lookup should use
   * @param port the port number to map to that scheme
   */
  private static void addUdpPort( final String scheme, final int port ) {
    UriUtil.udpMap.put( scheme.toLowerCase(), new Integer( port ) );
  }




  /**
   * Convenience method to clone a URI
   *
   * @param uri the URI to clone
   *
   * @return a separate URI 
   */
  public static URI clone( final URI uri ) {
    URI retval = null;

    try {
      retval = new URI( uri.toString() );
    } catch ( final URISyntaxException ignore ) {}

    return retval;
  }




  /**
   * Convenience method to clone a URL
   *
   * @param url the URL to clone
   *
   * @return a separate URL 
   */
  public static URL clone( final URL url ) {
    URL retval = null;

    try {
      retval = new URL( url.toString() );
    } catch ( final MalformedURLException ignore ) {}

    return retval;
  }




  /**
   * Return the given query string as a HashMap.
   *
   * @param content the string containing the encoded parameters
   * 
   * @return HashMap of query parameters. (Will never be null)
   */
  public static HashMap<String, String> decodeQuery( final String content ) {
    final HashMap<String, String> retval = new HashMap<String, String>();
    UriUtil.queryToMap( content, retval );
    return retval;
  }




  /**
   * Decode String with % encoding.
   *
   * @param encoded
   * 
   * @return Decoded string
   */
  public static String decodeString( final String encoded ) {
    final int len = encoded.length();
    final byte[] bytes = new byte[len];
    final char[] characters = encoded.toCharArray();
    int n = 0;
    boolean noDecode = true;

    for ( int i = 0; i < len; i++ ) {
      final char c = characters[i];

      if ( c > 0xff ) {
        throw new IllegalArgumentException( "illegal character at position " + i + " (>255)" );
      }

      byte b = (byte)( 0xff & c );

      if ( c == '+' ) {
        noDecode = false;
        b = (byte)' ';
      } else {
        if ( ( c == '%' ) && ( ( i + 2 ) < len ) ) {
          noDecode = false;
          b = (byte)( 0xff & Integer.parseInt( encoded.substring( i + 1, i + 3 ), 16 ) );
          i += 2;
        }
      }

      bytes[n++] = b;
    }

    if ( noDecode ) {
      return encoded;
    }

    return new String( bytes, 0, n );
  }




  /**
   * Perform URL encoding.
   *
   * @param string
   * 
   * @return encoded string.
   */
  public static String encodeString( final String string ) {
    byte[] bytes = null;
    bytes = string.getBytes();

    final int len = bytes.length;
    final byte[] encoded = new byte[bytes.length * 3];
    int n = 0;
    boolean noEncode = true;

    for ( int i = 0; i < len; i++ ) {
      final byte b = bytes[i];

      if ( b == ' ' ) {
        noEncode = false;
        encoded[n++] = (byte)'+';
      } else {
        if ( ( ( b >= 'a' ) && ( b <= 'z' ) ) || ( ( b >= 'A' ) && ( b <= 'Z' ) ) || ( ( b >= '0' ) && ( b <= '9' ) ) ) {
          encoded[n++] = b;
        } else {
          noEncode = false;
          encoded[n++] = (byte)'%';

          byte nibble = (byte)( ( b & 0xf0 ) >> 4 );

          if ( nibble >= 10 ) {
            encoded[n++] = (byte)( 'A' + nibble - 10 );
          } else {
            encoded[n++] = (byte)( '0' + nibble );
          }

          nibble = (byte)( b & 0xf );

          if ( nibble >= 10 ) {
            encoded[n++] = (byte)( 'A' + nibble - 10 );
          } else {
            encoded[n++] = (byte)( '0' + nibble );
          }
        }
      }
    }

    if ( noEncode ) {
      return string;
    }

    return new String( encoded, 0, n );
  }




  /**
   * Decoded parameters to a HashMap.
   *
   * @param query the string containing the encoded parameters
   * @param map The hashmap to populate with the query
   */
  public static void queryToMap( final String query, final HashMap<String, String> map ) {
    if ( map != null ) {
      synchronized( map ) {
        String token;
        String name;
        String value;
        final StringTokenizer tokenizer = new StringTokenizer( query, "&", false );

        while ( ( tokenizer.hasMoreTokens() ) ) {
          token = tokenizer.nextToken();

          // breaking it at the "=" sign
          int i = token.indexOf( '=' );

          if ( i < 0 ) {
            name = UriUtil.decodeString( token );
            value = "";
          } else {
            name = UriUtil.decodeString( token.substring( 0, i++ ) );

            if ( i >= token.length() ) {
              value = "";
            } else {
              value = UriUtil.decodeString( token.substring( i ) );
            }
          }

          // Add value to the map
          if ( name.length() > 0 ) {
            map.put( name, value );
          }
        }
      }
    }
  }




  /**
   * Provide a no-exception throwing URI creation convenience method.
   *
   * <p>This is handy for use in declarations and other static contexts where a
   * null check is fine for checking for a valid URI or URI syntax.</p>
   *
   * <p> Consider:<pre>
   * if( UriUtil.parse( text ) != null )
   * {
   *   System.out.println( &quot;The text '&quot; + text + &quot;' represents a valid URI&quot; );
   * }
   * </pre><br>As opposed to:<pre>
   * try
   * {
   *   new URI( text );
   *   System.out.println( &quot;The text '&quot; + text + &quot;' represents a valid URI&quot; );
   * }
   * catch( URISyntaxException use )
   * {
   *   // ignore
   * }
   * </pre>
   *
   * @param text The text to parse into a URI
   *
   * @return The URI object created from the text or null if the text could not
   *         be parsed into a valid URI.
   */
  public static URI parse( final String text ) {
    URI retval = null;

    if ( text != null ) {
      try {
        retval = new URI( text );
      } catch ( final URISyntaxException use ) {
        // Ignore and return null
      }
    }
    return retval;
  }




  /**
   * Retrieve the port number for the given scheme
   *
   * @param scheme the scheme (e.g. http) to lookup
   *
   * @return the integer representing to port number for the protocol scheme
   */
  public static int getPort( final String scheme ) {
    if ( scheme != null && scheme.trim().length() > 0 && UriUtil.portMap.containsKey( scheme.toLowerCase() ) ) {
      return ( (Integer)UriUtil.portMap.get( scheme.toLowerCase() ) ).intValue();
    }

    return -1;
  }




  /**
   * Return the port specified by the given URI.
   * 
   * <p>If there is no port specified in the URI, this code will attempt to 
   * determine the port based upon the scheme portion of the argument URI.</p>
   *
   * @param uri The URI to parse
   *
   * @return The port specified or implied by the URI, or 0 (zero) if the URI 
   *         could not be determined. Note that it is possible that the URI did 
   *         specify a port of zero, so don't assume a return value of zero 
   *         indicates no port was specified.
   */
  public static int getPort( final URI uri ) {
    if ( uri.getPort() < 0 ) {
      return UriUtil.getPort( uri.getScheme() );
    }

    return uri.getPort();
  }




  /**
   * Return the port specified by the URI as a string.
   *
   * @param uri the URI to parse
   * 
   * @return representing to port number for the protocol scheme
   */
  public static String getPortString( final URI uri ) {
    String retval = null;

    if ( uri.getPort() < 0 ) {
      return retval;
    }

    retval = Integer.toString( uri.getPort() );

    return retval;
  }




  /**
   * Determine the TCP URI to use when constructing a javax.net.SocketFactory
   * and a javax.net.ServerSocketFactory.
   *
   * <p>If the protocol of the URI is &quot;https&quot;, (case insensitive) then
   * a new URI will be returned with &quot;ssl&quot; as the protocol, the same
   * hostname and port, but with the rest blank.</p>
   *
   * @param uri the original URI
   *
   * @return a URI appropriate for making a TCP connection
   */
  public static URI getTCPURI( final URI uri ) {
    URI retval = null;

    try {
      final String scheme = uri.getScheme();

      if ( scheme != null ) {
        if ( scheme.equalsIgnoreCase( "https" ) || scheme.equalsIgnoreCase( "tls" ) ) {
          retval = new URI( "ssl://" + uri.getHost() + ":" + uri.getPort() );
        } else {
          retval = new URI( "tcp://" + uri.getHost() + ":" + uri.getPort() );
        }
      }
    } catch ( final URISyntaxException e ) {
      // e.printStackTrace();
    }

    return retval;
  }




  /**
   * Return just the host portion of the URI if applicable.
   *
   * @param uri The URI to parse
   * 
   * @return The host portion of the authority.
   */
  public static String getHost( final URI uri ) {
    String retval = uri.getHost();

    // The most common reason for getHost failure is that a port was not
    // defined in the URI making it hard for the URI class to figure out which
    // part of the authority is the host portion. We assume anything infront of
    // the colon is the host.
    if ( ( retval == null ) && ( uri.getAuthority() != null ) ) {
      // The authority is usually in the form of XXXX:999 as in "myhost:-1"
      final String text = uri.getAuthority();
      final int ptr = text.indexOf( ':' );

      if ( ptr > -1 ) {
        // just return the host portion
        retval = text.substring( 0, ptr );
      }
    }

    return retval;
  }




  /**
   * Get the address of the host.
   *
   * <p><b>NOTE</b>: This may take a long time as it will perform a DNS lookup
   * which can take several seconds!</p>
   *
   *
   * @param uri
   *
   * @return The InetAddress of the host specified in the URI. Will return null
   *         if DNS lookup fails, if the URI reference is null or if no host is
   *         specified in the URI.
   */
  public static InetAddress getHostAddress( final URI uri ) {
    if ( uri != null ) {
      final String host = uri.getHost();

      if ( host != null ) {
        try {
          return InetAddress.getByName( host );
        } catch ( final Exception exception ) {}
      }
    }

    return null;
  }




  /**
   * Get just the password from the user info portion of the URI
   *
   * @param uri the URI to parse
   *
   * @return the password portion of the user info of the URI or null if not present
   */
  public static String getPassword( final URI uri ) {
    String retval = null;

    if ( uri != null ) {
      final String userInfo = uri.getUserInfo();

      if ( ( userInfo != null ) && ( userInfo.length() > 0 ) ) {
        if ( userInfo.indexOf( ':' ) > -1 ) {
          retval = userInfo.substring( userInfo.indexOf( ':' ) + 1 );
        }

        if ( ( retval != null ) && ( retval.length() > 0 ) ) {
          return retval;
        }
      }
    }

    return retval;
  }




  /**
   * Get just the username from the user info portion of the URI
   *
   * @param uri the URI to parse
   *
   * @return the user name portion of the user info of the URI or null if not present
   */
  public static String getUser( final URI uri ) {
    String retval = null;

    if ( uri != null ) {
      final String userInfo = uri.getUserInfo();

      if ( ( userInfo != null ) && ( userInfo.length() > 0 ) ) {
        final int ptr = userInfo.indexOf( ':' );

        if ( ptr > -1 ) {
          if ( ptr > 0 ) {
            // return is everything up to the delimiter
            retval = userInfo.substring( 0, userInfo.indexOf( ':' ) );
          } else {
            // empty field - return null instead of an empty string
            return retval;
          }
        } else {
          retval = userInfo;
        }

        if ( ( retval != null ) && ( retval.length() > 0 ) ) {
          return retval;
        }
      }
    }

    return retval;
  }




  /**
   * Returns a Hashtable of name-value pairs from the query portion of the
   * given URI.
   *
   * <p>This method will always return a hashtable even if the URI does not
   * contain a query string. In such cases, the Hashtable will be empty.</p>
   *
   * @param uri The HTTP query string to parse
   *
   * @return a hashtable whose values are String[]s and keys are parameter name
   *         Strings.
   */
  public static Hashtable getParametersAsHashtable( URI uri ) {
    if ( uri.getQuery() != null ) {
      return getParametersAsHashtable( uri.getQuery() );
    }

    return new Hashtable();
  }




  /**
   * Read a query string and return a hashtable containing an array of value
   * strings keyed by parameter name.
   *
   * <p>This method will always return a hashtable even if the URI does not
   * contain a query string. In such cases, the Hashtable will be empty.</p>
   *
   * <p>Before parsing the query string, a check is made to determine if the
   * query delimiter exists in the string. If one does, only the data after the
   * LAST occurence of the '?' is parsed.</p>
   *
   * @param query The HTTP query string to parse
   *
   * @return a hashtable whose values are String[]s and keys are parameter name
   *         Strings.
   */
  public static Hashtable getParametersAsHashtable( String query ) {
    String data = query;

    // Make sure the data does not contain the Question mark
    if ( data.indexOf( '?' ) > -1 ) {
      // Just use the last query string
      data = query.substring( query.lastIndexOf( '?' ) + 1 );
    }

    // Create a new String parser
    StringParser parser = new StringParser( data, "=&" );

    Hashtable retval = new Hashtable();

    String name = null;
    String value = null;

    try {
      while ( !parser.eof() ) {
        name = decodeString( parser.readToken() ).trim();

        // Handle ?name where there is no value for the named parameter
        if ( !parser.eof() ) {
          int i = parser.readChar();

          if ( i == '=' ) {
            // check for null value
            int next = parser.peek();

            if ( next == '&' ) {
              // apparently we read PARAMETER=& so "PARAMETER" has been defined,
              // but there is no value as the next character is a name-value
              // delimiter. Assume an empty value and not a null value as the
              // name would not be given unless some kind of value is implied.
              value = "";
            } else {
              // Decode everything until the next name-value delimiter or EOF
              value = decodeString( parser.readToDelimiter( "&" ) ).trim();
            }

            // as long as we are not at the end of out data stream...
            if ( !parser.eof() ) {
              // ...read past(consume) the name-value delimiter
              parser.read();
            }
          }
        } else {
          // Oops! parameter ended prematurely, assigning empty string ''
          value = "";
        }

        // Add it to our array
        String array[] = (String[])retval.get( name );

        if ( array == null ) {
          // This is the first occurence of the named parameter
          retval.put( name, new String[] { value } );
        } else {
          // This is a subsequent occurence of the named parameter
          retval.put( name, ArrayUtil.addElement( array, value ) );
        }
      }
    } catch ( IOException ioe ) {
      // Log.warn( "Error parsing query parameters: " + ioe.getMessage() );
      // Log.debug( "Error parsing query parameters at " + parser.getPosition() );
    }

    return retval;
  }




  /**
   * Check to see if the URI represents an address on the local host/
   *
   * @param uri the URI to parse
   * 
   * @return true if the host portion represents the local host false if it is a remote host.
   */
  public static boolean hasLocalHost( final URI uri ) {
    // Get the host part of the URI
    final String host = uri.getHost();

    // No host implies a local (relative) URI
    if ( host == null ) {
      return true;
    } else {
      // Try to see if the host is one of the standard names
      if ( host.equalsIgnoreCase( "localhost" ) || host.equals( "127.0.0.1" ) || host.equals( "0.0.0.0" ) ) {
        return true;
      } else {
        // the obvious host names have been tested, now try to match addresses
        try {
          final InetAddress laddr = InetAddress.getLocalHost();
          final String localHostAddress = laddr.getHostAddress();

          if ( localHostAddress == null ) {
            // Could not determine the address of this host, assume no match
            return false;
          } else {
            // Attempt to resolve the dotted-quad address of the host
            InetAddress haddr = null;
            String hostAddress = null;

            try {
              haddr = InetAddress.getByName( host );
              hostAddress = haddr.getHostAddress();
            } catch ( final Throwable t ) {
              return false;
            }

            if ( hostAddress != null ) {
              // one last check
              if ( hostAddress.equals( "127.0.0.1" ) || host.equals( "0.0.0.0" ) ) {
                return true;
              } else {
                // Match the two IP addresses
                return hostAddress.equalsIgnoreCase( localHostAddress );
              }
            }
          }
        } catch ( final UnknownHostException ignore ) {}
      }
    }

    // Defaults to false
    return false;
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
   * Returns if the scheme can represent a TCP service.
   *
   * <p>This only returns true of is UDP returns false, as there is a very
   * short list of schemes that are UDP-only. This means that a scheme of 'dns'
   * will return true as it is not a UDP-only service and can translate to a 
   * TCP port.</p>
   *
   * @param uri the URI to query
   * 
   * @return true if the scheme is TCP based fale otherwise or could not be determined
   */
  public static boolean isTcp( final URI uri ) {
    if ( ( uri != null ) && ( uri.getScheme() != null ) ) {
      return ( UriUtil.portMap.containsKey( uri.getScheme() ) && !UriUtil.udpMap.containsKey( uri.getScheme() ) );
    }

    return false;
  }




  /**
   * Is the URI representing a UDP-only scheme?
   *
   * <p>If the URI starts with UDP or one of the other of a short list of
   * schemes that represent a UDP-only service, then this will return true.</p>
   *
   * @param uri
   *
   * @return True if the URI represents a UDP scheme, false otherwise or if URL
   *         is null.
   */
  public static boolean isUdp( final URI uri ) {
    if ( ( uri != null ) && ( uri.getScheme() != null ) ) {
      return ( uri.getScheme().equalsIgnoreCase( "udp" ) || UriUtil.udpMap.containsKey( uri.getScheme().toLowerCase() ) );
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
    } // scheme = null 

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




  /**
   * Get the base name of a URI (e.g. no path or extension)
   *
   * <p>Returns "index" from "http:\\projects\src\index.html".</p>
   *
   * @param uri The URI from which to get the base.
   *
   * @return The base file name
   */
  public static String getBase( final URI uri ) {
    if ( uri != null ) {
      String tempName = "";
      final StringTokenizer stk1 = new StringTokenizer( uri.toString(), "/\\" );

      // Cruise through the string and eat up all the tokens before the last
      // directory delimiter
      while ( stk1.hasMoreTokens() ) {
        tempName = stk1.nextToken();
      }

      final StringTokenizer stk = new StringTokenizer( tempName, "." );
      return stk.nextToken();
    }
    return null;
  }

}
