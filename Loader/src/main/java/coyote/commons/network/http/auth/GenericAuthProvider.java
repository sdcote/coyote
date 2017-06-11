/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.commons.network.http.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import coyote.commons.ByteUtil;
import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTP;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * This is the default authentication and authorization component for the HTTP 
 * service.
 *
 * <p>Passwords are read and stored in memory as a multi-round MD5 digest. The
 * number of rounds is chosen pseudo-randomly. This is done to protect
 * passwords from malicious components, core dumps and follows recommended 
 * safe coding practices for password handling.
 *
 * <p><b>NOTE:</b> User and Group name matching is case sensitive. It is
 * recommended to normalize all user and group names by hand. (e.g. always
 * specify names in lower case.)
 */
public class GenericAuthProvider implements AuthProvider {
  /**
  * Class to hold user data
  */
  class User {
    private String name = null;
    private byte[] pass = null;
    private final List<String> groups = new ArrayList<String>();




    void addGroup( final String groupname ) {
      groups.add( groupname );
    }




    List<String> getGroups() {
      return groups;
    }




    String getName() {
      return name;
    }




    byte[] getPassword() {
      return pass;
    }




    public boolean memberOf( final String group ) {
      for ( final String groupname : groups ) {
        if ( groupname.equals( group ) ) {
          return true;
        }
      }
      return false;
    }




    boolean passwordMatches( final byte[] data ) {
      return Arrays.equals( data, pass );
    }




    void setName( final String name ) {
      this.name = name;
    }




    void setPassword( final byte[] pass ) {
      this.pass = pass;
    }

  }

  public static final String AUTH_SECTION = "Auth";
  public static final String USER_SECTION = "Users";
  public static final String NAME = "Name";
  public static final String PASSWORD = "Password";
  public static final String GROUPS = "Groups";
  public static final String ENCRYPTED = "Encrypted";

  public static final String ALLOW_NO_SSL = "AllowUnsecuredConnections";
  public static final String SEND_AUTH_ON_FAILURE = "SendAuthRequestOnFailure";

  private static final String MD5 = "MD5";

  private static final String UTF8 = "UTF8";

  static {
    try {
      MessageDigest.getInstance( MD5 );
    } catch ( final NoSuchAlgorithmException e ) {
      e.printStackTrace();
    }
    try {
      UTF8.getBytes( UTF8 );
    } catch ( final UnsupportedEncodingException e ) {
      e.printStackTrace();
    }
  }
  public final List<User> userList = new ArrayList<User>();

  private boolean allowNoSSL = false;
  private boolean sendAuthRequest = false;

  private int digestRounds = 1;




  public GenericAuthProvider() {
    final Random rand = new Random();
    digestRounds = rand.nextInt( ( 5 - 1 ) + 1 ) + 1;
  }




  /**
   * @param cfg
   */
  public GenericAuthProvider( final Config cfg ) {
    if ( cfg != null ) {
      for ( final DataField field : cfg.getFields() ) {
        if ( GenericAuthProvider.USER_SECTION.equalsIgnoreCase( field.getName() ) && field.isFrame() ) {
          configUsers( (DataFrame)field.getObjectValue() );
        }
      }

      try {
        if ( cfg.getAsBoolean( ALLOW_NO_SSL ) ) {
          Log.append( HTTPD.EVENT, "NOTICE: SSL checks will be ignored in this server instance. Sensitive data may traverse unencrypted connections." );
          allowNoSSL = true;
        }
      } catch ( final DataFrameException e ) {}

      try {
        if ( cfg.getAsBoolean( SEND_AUTH_ON_FAILURE ) ) {
          Log.append( HTTPD.EVENT, "NOTICE: The WWW-Authenticate header will be sent on failed authentication requests. This allows the user to enter basic auth credentials on most browsers. This may be used as an attack vector (Brute Force & DoS)." );
          sendAuthRequest = true;
        }
      } catch ( final DataFrameException e ) {}
    }

  }




  /**
   * @param cfg a configuration for a list of users
   */
  private void configUsers( final DataFrame cfg ) {
    for ( final DataField field : cfg.getFields() ) {
      if ( field.isFrame() ) {
        final DataFrame userframe = (DataFrame)field.getObjectValue();
        final User user = new User();

        for ( final DataField userfield : userframe.getFields() ) {
          if ( userfield.getName() != null ) {
            if ( userfield.getName().endsWith( NAME ) ) {
              if ( userfield.getName().startsWith( ENCRYPTED ) ) {
                user.setName( CipherUtil.decryptString( userfield.getStringValue() ) );
              } else {
                user.setName( userfield.getStringValue() );
              }
            } else if ( userfield.getName().endsWith( PASSWORD ) ) {
              String passwd;
              if ( userfield.getName().startsWith( ENCRYPTED ) ) {
                passwd = CipherUtil.decryptString( userfield.getStringValue() );
              } else {
                passwd = userfield.getStringValue();
              }
              try {
                user.setPassword( digest( passwd.getBytes( UTF8 ) ) );
              } catch ( final UnsupportedEncodingException e ) {}
            } else if ( userfield.getName().endsWith( GROUPS ) ) {
              String groups;
              if ( userfield.getName().startsWith( ENCRYPTED ) ) {
                groups = CipherUtil.decryptString( userfield.getStringValue() );
              } else {
                groups = userfield.getStringValue();
              }
              final String[] tokens = groups.split( "," );
              for ( final String token : tokens ) {
                user.addGroup( token.trim() );
              }
            }
          }
        }

        userList.add( user );
      }
    }
  }




  /**
   * Perform multi-round MD5 digest of given bytes.
   *
   * @param data the bytes to digest
   *
   * @return digest of the given data
   */
  byte[] digest( final byte[] data ) {
    byte[] val = data;
    if ( digestRounds > 0 ) {
      MessageDigest md = null;
      try {
        md = MessageDigest.getInstance( MD5 );
      } catch ( final NoSuchAlgorithmException e ) {}

      if ( md != null ) {
        for ( int x = 0; x < digestRounds; x++ ) {
          val = md.digest( val );
        }
      }
    }
    return val;
  }




  /**
   * @return the number of digest rounds
   */
  int getDigestRounds() {
    return digestRounds;
  }




  /**
   * Get a user record by its name.
   *
   * @param name The name of the user to retrieve.
   *
   * @return the first user with the given name or null if not found.
   */
  User getUser( final String name ) {
    if ( StringUtil.isNotEmpty( name ) ) {
      for ( final User user : userList ) {
        if ( name.equals( user.getName() ) ) {
          return user;
        }
      }
    }
    return null;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthenticated(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isAuthenticated( final IHTTPSession session ) {

    // all headers are stored in lower case since browsers use different case
    final String authHeader = session.getRequestHeaders().get( HTTP.HDR_AUTHORIZATION.toLowerCase() );
    if ( StringUtil.isNotBlank( authHeader ) ) {
      String[] tokens = authHeader.split( " " );
      if ( ( tokens != null ) && ( tokens.length > 0 ) ) {
        final String authType = tokens[0];
        Log.append( HTTPD.EVENT, "Received Auth Type of '" + authType + "'" );

        // Assume Basic Auth
        if ( tokens.length > 1 ) {
          final byte[] data = ByteUtil.fromBase64( tokens[1].trim() );
          final String authPair = new String( data );
          tokens = authPair.split( ":" );

          String username;
          String password;

          if ( tokens != null ) {
            if ( tokens.length > 0 ) {
              username = tokens[0];
            } else {
              username = null;
            }
            if ( tokens.length > 1 ) {
              password = tokens[1];
            } else {
              password = null;
            }

            // find the user with the given name
            final User user = getUser( username );
            if ( user != null ) {
              Log.append( HTTPD.EVENT, "Successful authentication for '" + username + "'" );
              // we found a user
              if ( StringUtil.isNotBlank( password ) ) {
                try {
                  // digest the given password
                  final byte[] barray = digest( password.getBytes( UTF8 ) );

                  if ( user.passwordMatches( barray ) ) {
                    // add the user and groups to the session
                    session.setUserName( user.getName() );
                    session.setUserGroups( user.getGroups() );
                    return true;
                  }
                } catch ( final UnsupportedEncodingException e ) {
                  e.printStackTrace(); // should never happen, tested in static init
                }

              } // null, empty or blank passwords are not supported/allowed

            } // we found a user with that name

          } // null tokens???
        } else {
          Log.append( HTTPD.EVENT, "No authentication data received for '" + authType + "' from " + session.getRemoteIpAddress() + ":" + session.getRemoteIpPort() );
        }
      } else {
        // No auth header
      }
    }

    if ( sendAuthRequest ) {
      session.getResponseHeaders().put( HTTP.HDR_WWW_AUTHENTICATE, HTTP.BASIC + " realm=\"Generic Realm\"" );
    }

    return false;

  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthorized(coyote.commons.network.http.IHTTPSession, java.lang.String)
   */
  @Override
  public boolean isAuthorized( final IHTTPSession session, final String groups ) {
    final String username = session.getUserName();
    final User user = getUser( username );
    if ( user != null ) {
      final String[] tokens = groups.split( "," );
      for ( final String token : tokens ) {
        if ( user.memberOf( token.trim() ) ) {
          return true;
        }
      }
    }

    return false;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isSecureConnection(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isSecureConnection( final IHTTPSession session ) {
    // we can configure the auth provider to ignore the SSL check on many of
    // the responders when the deployment does not have valid SSL certificates,
    // such as in development and testing.
    if ( allowNoSSL ) {
      return true;
    }

    return session.isSecure();
  }




  /**
   * @param rounds
   */
  void setDigestRounds( final int rounds ) {
    digestRounds = rounds;
  }

}
