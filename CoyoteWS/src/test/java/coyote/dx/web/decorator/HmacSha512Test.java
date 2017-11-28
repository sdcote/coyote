/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.web.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

import coyote.dx.ConfigTag;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;


/**
 * 
 */
public class HmacSha512Test {

  @Test
  public void test() {
    String headerName = "Sign";
    String expectedData = "a627c51a0756fecc1d181010d2f0203082292f72d212835d48f241c8a5a47313a5398543263b974ed74ed1bd4be45b7c3bfefffbd1950ff509abb6c1ef6383f0";
    Config cfg = new Config();
    cfg.put(ConfigTag.DATA, "This is a test message");
    cfg.put(ConfigTag.SECRET, "ThirtyDirtyBirds");
    cfg.put(ConfigTag.HEADER, headerName);
    System.out.println(cfg);

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(headerName);
      assertNotNull(headers);
      assertTrue(headers.length == 1);
      Header header = headers[0];
      assertNotNull(header);
      assertEquals(expectedData, header.getValue());

    } catch (Exception e) {
      fail(e.getMessage());
    }

  }




  @Test
  public void noHeader() {
    Config cfg = new Config();
    cfg.put(ConfigTag.DATA, "This is a test message");
    cfg.put(ConfigTag.SECRET, "ThirtyDirtyBirds");

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      fail("Should not allow missing header in configuration");
    } catch (Exception e) {
      // ignore
    }
  }




  /**
   * @return
   */
  private HttpMessage getRequest() {
    HttpPost retval = new HttpPost("https://nowhere.io/api/method");
    return retval;
  }




  @Test
  public void noSecret() {
    Config cfg = new Config();
    cfg.put(ConfigTag.DATA, "This is a test message");
    cfg.put(ConfigTag.HEADER, "Sign");

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      fail("Should not allow missing secret in configuration");
    } catch (Exception e) {
      // ignore
    }
  }




  @Test
  public void noData() {
    Config cfg = new Config();
    cfg.put(ConfigTag.SECRET, "ThirtyDirtyBirds");
    cfg.put(ConfigTag.HEADER, "Sign");

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      fail("Should not allow missing data in configuration");
    } catch (Exception e) {
      // ignore
    }
  }




  @Test
  public void encryptedSecret() {
    String headerName = "Sign";
    String expectedData = "a627c51a0756fecc1d181010d2f0203082292f72d212835d48f241c8a5a47313a5398543263b974ed74ed1bd4be45b7c3bfefffbd1950ff509abb6c1ef6383f0";
    Config cfg = new Config();
    cfg.put(ConfigTag.DATA, "This is a test message");
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.SECRET, "oM99uAKBZTHVB/MkWuv0rHD5htIiE34DrM4Kqx7vo/WJW9/asNT24Q==");
    cfg.put(ConfigTag.HEADER, headerName);

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(headerName);
      assertNotNull(headers);
      assertTrue(headers.length == 1);
      Header header = headers[0];
      assertNotNull(header);
      assertEquals(expectedData, header.getValue());

    } catch (Exception e) {
      fail(e.getMessage());
    }

  }




  @Test
  public void encryptedData() {
    String headerName = "Sign";
    String expectedData = "a627c51a0756fecc1d181010d2f0203082292f72d212835d48f241c8a5a47313a5398543263b974ed74ed1bd4be45b7c3bfefffbd1950ff509abb6c1ef6383f0";
    Config cfg = new Config();
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.DATA, "vJUZblVWfM3F7OESe0z/SbAr8DEf8ReFLfuKG/87yxKjEwCgkrgb7Ucy369bVUwMOhgtQThTqIc=");
    cfg.put(ConfigTag.SECRET, "ThirtyDirtyBirds");
    cfg.put(ConfigTag.HEADER, headerName);

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(headerName);
      assertNotNull(headers);
      assertTrue(headers.length == 1);
      Header header = headers[0];
      assertNotNull(header);
      assertEquals(expectedData, header.getValue());

    } catch (Exception e) {
      fail(e.getMessage());
    }

  }




  @Test
  public void encryptedDataAndSecret() {
    String headerName = "Sign";
    String expectedData = "a627c51a0756fecc1d181010d2f0203082292f72d212835d48f241c8a5a47313a5398543263b974ed74ed1bd4be45b7c3bfefffbd1950ff509abb6c1ef6383f0";
    Config cfg = new Config();
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.DATA, "vJUZblVWfM3F7OESe0z/SbAr8DEf8ReFLfuKG/87yxKjEwCgkrgb7Ucy369bVUwMOhgtQThTqIc=");
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.SECRET, "oM99uAKBZTHVB/MkWuv0rHD5htIiE34DrM4Kqx7vo/WJW9/asNT24Q==");
    cfg.put(ConfigTag.HEADER, headerName);

    try {
      HmacSha512 decorator = new HmacSha512();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(headerName);
      assertNotNull(headers);
      assertTrue(headers.length == 1);
      Header header = headers[0];
      assertNotNull(header);
      assertEquals(expectedData, header.getValue());

    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

}
