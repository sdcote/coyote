/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import coyote.batch.Batch;
import coyote.commons.ByteUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTP;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * This is the default authentication and Authorization component for the HTTP service.
 * 
 * <p>Passwords are read and stored in memory as a multi-round MD5 digest. The 
 * number of rounds is chosen pseudo-randomly. This is done to protect 
 * passwords from malicious components, core dumps and follows recommended safe
 * coding practices for password handling.
 * 
 * <p><b>NOTE:</b> User and Group name matching is case sensitive. It is 
 * recommended to normalize all user and group names by hand. (e.g. always 
 * specify names in lower case.)
 */
public class BatchAuthProvider implements AuthProvider {
  public static final String AUTH_SECTION = "Auth";
  public static final String USER_SECTION = "Users";
  public static final String NAME = "Name";
  public static final String PASSWORD = "Password";
  public static final String GROUPS = "Groups";
  public static final String ENCRYPTED = "Encrypted";
  public static final String ALLOW_NO_SSL = "AllowUnsecuredConnections";

  public final List<User> userList = new ArrayList<User>();

  private boolean allowNoSSL = false;

  private int digestRounds = 1;

  private static final String MD5 = "MD5";
  private static final String UTF8 = "UTF8";

  static {
    try {
      @SuppressWarnings("unused")
      MessageDigest md = MessageDigest.getInstance( MD5 );
    } catch ( NoSuchAlgorithmException e ) {
      e.printStackTrace();
    }
    try {
      UTF8.getBytes( UTF8 );
    } catch ( UnsupportedEncodingException e ) {
      e.printStackTrace();
    }
  }




  public BatchAuthProvider() {
    Random rand = new Random();
    digestRounds = rand.nextInt( ( 5 - 1 ) + 1 ) + 1;
  }




  /**
   * @param cfg
   */
  public BatchAuthProvider( Config cfg ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        if ( BatchAuthProvider.USER_SECTION.equalsIgnoreCase( field.getName() ) && field.isFrame() ) {
          configUsers( (DataFrame)field.getObjectValue() );
        }
      }
    }

    try {
      if ( cfg.getAsBoolean( ALLOW_NO_SSL ) ) {
        Log.append( HTTPD.EVENT, "WARNING: SSL checks will be ignored in this server instance. Sensitive data will traverse unencrypted connections!" );
        Log.warn( "WARNING: SSL checks will be ignored in this server instance. Sensitive data will traverse unencrypted connections!" );
        allowNoSSL = true;
      }
    } catch ( DataFrameException e ) {}

  }




  /**
   * @param cfg a configuration for a list of users
   */
  private void configUsers( DataFrame cfg ) {
    for ( DataField field : cfg.getFields() ) {
      if ( field.isFrame() ) {
        DataFrame userframe = (DataFrame)field.getObjectValue();
        User user = new User();

        for ( DataField userfield : userframe.getFields() ) {
          if ( userfield.getName() != null ) {
            if ( userfield.getName().endsWith( NAME ) ) {
              if ( userfield.getName().startsWith( ENCRYPTED ) ) {
                user.setName( Batch.decrypt( userfield.getStringValue() ) );
              } else {
                user.setName( userfield.getStringValue() );
              }
            } else if ( userfield.getName().endsWith( PASSWORD ) ) {
              String passwd;
              if ( userfield.getName().startsWith( ENCRYPTED ) ) {
                passwd = Batch.decrypt( userfield.getStringValue() );
              } else {
                passwd = userfield.getStringValue();
              }
              try {
                user.setPassword( digest( passwd.getBytes( UTF8 ) ) );
              } catch ( UnsupportedEncodingException e ) {}
            } else if ( userfield.getName().endsWith( GROUPS ) ) {
              String groups;
              if ( userfield.getName().startsWith( ENCRYPTED ) ) {
                groups = Batch.decrypt( userfield.getStringValue() );
              } else {
                groups = userfield.getStringValue();
              }
              String[] tokens = groups.split( "," );
              for ( String token : tokens ) {
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
   * @param rounds
   */
  void setDigestRounds( int rounds ) {
    digestRounds = rounds;
  }




  /**
   * Perform multi-round MD5 digest of given bytes.
   * 
   * @param data the bytes to digest
   * 
   * @return digest of the given data
   */
  byte[] digest( byte[] data ) {
    byte[] val = data;
    if ( digestRounds > 0 ) {
      MessageDigest md = null;
      try {
        md = MessageDigest.getInstance( MD5 );
      } catch ( NoSuchAlgorithmException e ) {}

      if ( md != null ) {
        for ( int x = 0; x < digestRounds; x++ ) {
          val = md.digest( val );
        }
      }
    }
    return val;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isSecureConnection(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isSecureConnection( IHTTPSession session ) {
    session.getUri();

    // we can configure the auth provider to ignore the SSL check on many of 
    // the nuggets when the deployment does not have valid SSL certificates, 
    // such as in development.
    if ( allowNoSSL ) {
      return true;
    }

    return true;
  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthenticated(coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public boolean isAuthenticated( IHTTPSession session ) {

    // Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==

    // all headers are stored in lower case since browsers use different case
    String authHeader = session.getRequestHeaders().get( HTTP.HDR_AUTHORIZATION.toLowerCase() );
    if ( StringUtil.isNotBlank( authHeader ) ) {
      String[] tokens = authHeader.split( " " );
      if ( tokens != null && tokens.length > 0 ) {
        String authType = tokens[0];
        Log.append( HTTPD.EVENT, "Received Auth Type of '" + authType + "'" );
        // TODO: Support multiple Auth Types

        // Assume Basic Auth
        if ( tokens.length > 1 ) {
          byte[] data = ByteUtil.fromBase64( tokens[1].trim() );
          String authPair = new String( data );
          tokens = authPair.split( ":" );

          String username;
          String password;

          if ( tokens != null ) {
            if ( tokens.length > 0 ) {
              username = tokens[0];
            } else {
              username = null;
            }
            if ( tokens.length > 0 ) {
              password = tokens[0];
            } else {
              password = null;
            }

            // find the user with the given name
            User user = this.getUser( username );
            if ( user != null ) {
              // we found a user
              try {
                // digest the given password
                byte[] barray = digest( password.getBytes( UTF8 ) );

                if ( user.passwordMatches( barray ) ) {
                  // add the user and groups to the session
                  session.setUserName( user.getName() );
                  session.setUserGroups( user.getGroups() );
                  return true;
                }
              } catch ( UnsupportedEncodingException e ) {
                e.printStackTrace(); // should never happen, tested in static init
              }
            } // we found a user with that name

          } // null tokens???
        } else {
          Log.append( HTTPD.EVENT, "No authentication data received for '" + authType + "' from " + session.getRemoteIpAddress() + ":" + session.getRemoteIpPort() );
        }
      } else {
        // No auth header
      }
    }

    // TODO: Make this configurable...this can be considered a security risk allowing easier dictionary attacks
    // if no authentication header or auth failure, we can send a request for client to send one, most browsers will then pop-up a form 
    session.getResponseHeaders().put( HTTP.HDR_WWW_AUTHENTICATE, HTTP.BASIC + " realm=\"Batch Manager\"" );

    return false;

  }




  /**
   * @see coyote.commons.network.http.auth.AuthProvider#isAuthorized(coyote.commons.network.http.IHTTPSession, java.lang.String)
   */
  @Override
  public boolean isAuthorized( IHTTPSession session, String groups ) {
    String username = session.getUserName();
    User user = getUser( username );
    if ( user != null ) {
      String[] tokens = groups.split( "," );
      for ( String token : tokens ) {
        if ( user.memberOf( token.trim() ) ) {
          return true;
        }
      }
    }

    return false;
  }




  /**
   * Get a user record by its name.
   * 
   * @param name The name of the user to retrieve.
   * 
   * @return the first user with the given name or null if not found.
   */
  User getUser( String name ) {
    if ( StringUtil.isNotEmpty( name ) ) {
      for ( User user : userList ) {
        if ( name.equals( user.getName() ) ) {
          return user;
        }
      }
    }
    return null;
  }




  /**
   * @return the number of digest rounds
   */
  int getDigestRounds() {
    return digestRounds;
  }

  /**
  * Class to hold user data
  */
  class User {
    private String name = null;
    private byte[] pass = null;
    private List<String> groups = new ArrayList<String>();




    void addGroup( String groupname ) {
      groups.add( groupname );
    }




    public boolean memberOf( String group ) {
      for ( String groupname : groups ) {
        if ( groupname.equals( group ) ) {
          return true;
        }
      }
      return false;
    }




    List<String> getGroups() {
      return groups;
    }




    String getName() {
      return name;
    }




    void setName( String name ) {
      this.name = name;
    }




    byte[] getPassword() {
      return pass;
    }




    void setPassword( byte[] pass ) {
      this.pass = pass;
    }




    boolean passwordMatches( byte[] data ) {
      return Arrays.equals( data, pass );
    }

  }

}
