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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import coyote.commons.ByteUtil;
import coyote.commons.network.http.HTTP;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class DefaultAuthProviderTest {
  private static final String AUTH_CONFIG = "{ \"Users\" : [ { \"Name\" : \"admin\", \"Password\" : \"secret\", \"Groups\" : \"sysop,devop\" },{ \"Name\" : \"sysop\", \"Password\" : \"secret\", \"Groups\" : \"sysop\" }, { \"Name\" : \"devop\", \"Password\" : \"secret\", \"Groups\" : \"devop\" }, { \"Name\" : \"user\", \"Password\" : \"secret\" } ] }";

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




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {

  }




  /**
   * Test method for {@link coyote.batch.http.BatchAuthProvider#DefaultAuthProvider()}.
   */
  @Test
  public void testDefaultAuthProvider() {
    AuthProvider provider = new BatchAuthProvider();
    assertNotNull( provider );
  }




  /**
   * Test method for {@link coyote.batch.http.BatchAuthProvider#DefaultAuthProvider(coyote.loader.cfg.Config)}.
   */
  @Test
  public void testDefaultAuthProviderConfig() {
    try {
      Config cfg = new Config( AUTH_CONFIG );

      AuthProvider provider = new BatchAuthProvider( cfg );
      assertNotNull( provider );

    } catch ( ConfigurationException e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * Test method for {@link coyote.batch.http.BatchAuthProvider#isSecureConnection(coyote.commons.network.http.IHTTPSession)}.
   */
  @Ignore
  public void testIsSecureConnection() {
    fail( "Not yet implemented" ); // TODO
  }




  @Test
  public void digestTest() {
    BatchAuthProvider provider = new BatchAuthProvider();
    String password = "secret";

    provider.setDigestRounds( 1 );
    try {
      byte[] barray = provider.digest( password.getBytes( UTF8 ) );
      String result = ByteUtil.bytesToHex( barray );
      assertEquals( "5E BE 22 94 EC D0 E0 F0 8E AB 76 90 D2 A6 EE 69", result );
    } catch ( UnsupportedEncodingException e ) {
      fail( e.getMessage() );
    }

    provider.setDigestRounds( 2 );
    try {
      byte[] barray = provider.digest( password.getBytes( UTF8 ) );
      String result = ByteUtil.bytesToHex( barray );
      assertEquals( "9E 76 90 17 C8 5F 06 49 77 FE 6A 65 8F 20 7F A6", result );
    } catch ( UnsupportedEncodingException e ) {
      fail( e.getMessage() );
    }

    provider.setDigestRounds( 3 );
    try {
      byte[] barray = provider.digest( password.getBytes( UTF8 ) );
      String result = ByteUtil.bytesToHex( barray );
      assertEquals( "09 C5 10 DF 26 46 5A EE 2F 81 E7 16 DF 44 A3 B7", result );
    } catch ( UnsupportedEncodingException e ) {
      fail( e.getMessage() );
    }

    provider.setDigestRounds( 4 );
    try {
      byte[] barray = provider.digest( password.getBytes( UTF8 ) );
      String result = ByteUtil.bytesToHex( barray );
      assertEquals( "CA 7C B2 24 AC 50 ED 0F 42 90 0D 3F BB 4E 85 90", result );
    } catch ( UnsupportedEncodingException e ) {
      fail( e.getMessage() );
    }

    provider.setDigestRounds( 5 );
    try {
      byte[] barray = provider.digest( password.getBytes( UTF8 ) );
      String result = ByteUtil.bytesToHex( barray );
      assertEquals( "93 FC B3 61 1F 8E 12 DF 71 9A 47 8A C2 EA 36 14", result );
    } catch ( UnsupportedEncodingException e ) {
      fail( e.getMessage() );
    }

  }




  /**
   * Test method for {@link coyote.batch.http.BatchAuthProvider#isAuthenticated(coyote.commons.network.http.IHTTPSession)}.
   */
  @Test
  public void testIsAuthenticated() {
    try {
      Config cfg = new Config( AUTH_CONFIG );

      BatchAuthProvider provider = new BatchAuthProvider( cfg );
      assertNotNull( provider );
      int rounds = provider.getDigestRounds();
      //System.out.println( "Provider is using "+rounds+" digest Rounds" );

      BatchAuthProvider.User user = provider.getUser( "user" );
      assertNotNull( user );
      String name = user.getName();
      byte[] barray = user.getPassword();
      String result = ByteUtil.bytesToHex( barray );
      //System.out.println( "User: "+name+" password: "+result );

      // create a mock session
      MockSession session = new MockSession();

      // Generate an Authorization header for a user in our test configuration
      String username = "user";
      String password = "secret";
      String basicAuth = TestHttpClient.calculateHeaderData( username, password );
      session.addRequestHeader( HTTP.HDR_AUTHORIZATION.toLowerCase(), basicAuth );

      // Have the provider validate this session
      assertTrue( provider.isAuthenticated( session ) );

      // the user and groups should be populate in the session
      assertNotNull( session.getUserName() );
      assertEquals( session.getUserName(), username );
      assertNotNull( session.getUserGroups() );
      assertTrue( session.getUserGroups().size() == 0 );

    } catch ( ConfigurationException e ) {
      fail( e.getMessage() );
    }
  }




  /**
   * Test method for {@link coyote.batch.http.BatchAuthProvider#isAuthorized(coyote.commons.network.http.IHTTPSession, java.lang.String)}.
   */
  @Test
  public void testIsAuthorized() {
    try {
      Config cfg = new Config( AUTH_CONFIG );

      BatchAuthProvider provider = new BatchAuthProvider( cfg );
      assertNotNull( provider );

      BatchAuthProvider.User user = provider.getUser( "user" );
      assertNotNull( user );
      String name = user.getName();
      byte[] barray = user.getPassword();

      // create a mock session
      MockSession session = new MockSession();

      // Generate an Authorization header for a user in our test configuration
      String username = "user";
      String password = "secret";
      String basicAuth = TestHttpClient.calculateHeaderData( username, password );
      session.addRequestHeader( HTTP.HDR_AUTHORIZATION.toLowerCase(), basicAuth );
      // Have the provider validate and set the username in the session
      assertTrue( provider.isAuthenticated( session ) );
      // Have the provider check role based access of this session
      assertFalse( provider.isAuthorized( session, "devop" ) );

      // Generate an Authorization header for the 'admin' user
      username = "admin";
      password = "secret";
      basicAuth = TestHttpClient.calculateHeaderData( username, password );
      session = new MockSession();
      session.addRequestHeader( HTTP.HDR_AUTHORIZATION.toLowerCase(), basicAuth );
      assertTrue( provider.isAuthenticated( session ) );
      assertTrue( provider.isAuthorized( session, "devop" ) );

    } catch ( ConfigurationException e ) {
      fail( e.getMessage() );
    }
    
  }

}
