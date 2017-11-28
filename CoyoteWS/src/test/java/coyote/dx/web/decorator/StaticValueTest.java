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
public class StaticValueTest {

  /**
   * @return
   */
  private HttpMessage getRequest() {
    HttpPost retval = new HttpPost("https://nowhere.io/api/method");
    return retval;
  }




  @Test
  public void test() {
    String expectedData = "1234-5678-9012-3456";
    String expectedHeader = "APIKEY";
    Config cfg = new Config();
    cfg.put(ConfigTag.VALUE, expectedData);
    cfg.put(ConfigTag.HEADER, expectedHeader);

    try {
      StaticValue decorator = new StaticValue();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(expectedHeader);
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
  public void encryptedValue() {
    String expectedData = "1234-5678-9012-3456";
    String expectedHeader = "APIKEY";
    Config cfg = new Config();
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.VALUE, "ac3m45iJ5P4z88xVGyIJ7Ayqdyjp4kuFGCimb2lAqfsZUqtzQwauqxouiUOqa3qN");
    cfg.put(ConfigTag.HEADER, expectedHeader);

    try {
      StaticValue decorator = new StaticValue();
      decorator.setConfiguration(cfg);

      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(expectedHeader);
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
    cfg.put(ConfigTag.VALUE, "1234-5678-9012-3456");

    try {
      StaticValue decorator = new StaticValue();
      decorator.setConfiguration(cfg);
      fail("Should not allow missing header in configuration");
    } catch (Exception e) {
      // ignore
    }
  }

}
