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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dx.ConfigTag;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class HmacSha512Test {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  /**
   * @return a test message
   */
  private HttpMessage getRequest() {
    HttpPost retval = new HttpPost("https://nowhere.io/api/method");

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("command", "GetMyData"));
    params.add(new BasicNameValuePair("nonce", "9s84bhd83gwnx8"));

    try {
      retval.setEntity(new UrlEncodedFormEntity(params));
    } catch (UnsupportedEncodingException ignore) {
      // should not happen with test data
    }

    return retval;
  }




  @Test
  public void basicTest() {
    String headerName = "Sign";
    String expectedData = "a627c51a0756fecc1d181010d2f0203082292f72d212835d48f241c8a5a47313a5398543263b974ed74ed1bd4be45b7c3bfefffbd1950ff509abb6c1ef6383f0";
    Config cfg = new Config();
    cfg.put(ConfigTag.DATA, "This is a test message");
    cfg.put(ConfigTag.SECRET, "ThirtyDirtyBirds");
    cfg.put(ConfigTag.HEADER, headerName);
    //System.out.println(cfg);

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
    String headerName = "Sign";
    String expectedData = "8857660909891df1c5bc6022d13dd2f3ef7fde60d1f726845516a479f9b4cb4e701f688b0fab4133c747aa5e9f31e0b912ec6e145457f4946dff15ec6ddaa2e8";
    Config cfg = new Config();
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
