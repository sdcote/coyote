/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.azure;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class SASTokenTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  @Test
  public void test() {

    String scope = "CoyoteIoT.azure-devices.net/devices/george";
    long expiry = 1470067491;
    String key = "6LdAO46ea+1oYydWj2ZSoA==";
    String test = "SharedAccessSignature sig=xL98QBjQRcRIX%2FFvT%2F8Orjf3YqIt8rm8boGw%2B9Am%2B6c%3D&se=1470067491&sr=CoyoteIoT.azure-devices.net/devices/george";

    SASToken token = new SASToken( scope, key, expiry );
    assertNotNull( token );
    String subject = token.toString();
    assertNotNull( subject );
    assertEquals( test, subject );

    //System.out.println( token.toString());

  }




  @Test
  public void testBuild() {
    String hostname = "CoyoteIoT.azure-devices.net";
    String deviceId = "george";
    String scope = IotHubUri.getResourceUri( hostname, deviceId );
    //System.out.println( scope );

    long expiry = 1470067491;
    String key = "6LdAO46ea+1oYydWj2ZSoA==";
    String test = "SharedAccessSignature sig=xL98QBjQRcRIX%2FFvT%2F8Orjf3YqIt8rm8boGw%2B9Am%2B6c%3D&se=1470067491&sr=CoyoteIoT.azure-devices.net/devices/george";

    SASToken token = new SASToken( scope, key, expiry );
    assertNotNull( token );
    String subject = token.toString();
    assertNotNull( subject );
    assertEquals( test, subject );

  }

  
  @Test
  public void testBuildOther() {

    String hostname = "CoyoteIoT.azure-devices.net";
    String deviceId = "device-fcbd127a";
    String scope = IotHubUri.getResourceUri( hostname, deviceId );

    long expiry = 1501598806;
    String key = "ypZ2F76vfOkYYHKsRbQOYP6SKW7/TOo4maD9GmqYMII=";

    SASToken token = new SASToken( scope, key, expiry );
    assertNotNull( token );
    String subject = token.toString();
    assertNotNull( subject );
    System.out.println( subject );

  }
}
