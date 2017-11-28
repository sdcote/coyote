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
public class BasicAuthTest {

  /**
   * @return
   */
  private HttpMessage getRequest() {
    HttpPost retval = new HttpPost("https://nowhere.io/api/method");
    return retval;
  }




  @Test
  public void test() {
    String expectedData = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    Config cfg = new Config();
    cfg.put(ConfigTag.USERNAME, "Aladdin");
    cfg.put(ConfigTag.PASSWORD, "open sesame");

    try {
      BasicAuth decorator = new BasicAuth();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(BasicAuth.DEFAULT_HEADER);
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
  public void specifyHeader() {
    String headerName = "MyAuth";
    String expectedData = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    Config cfg = new Config();
    cfg.put(ConfigTag.USERNAME, "Aladdin");
    cfg.put(ConfigTag.PASSWORD, "open sesame");
    cfg.put(ConfigTag.HEADER, headerName);

    try {
      BasicAuth decorator = new BasicAuth();
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
  public void encryptedUsername() {
    String expectedData = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    Config cfg = new Config();
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME, "JHhGjEJwi1HQYyJ4SG0gH1ERo0FqHorb");
    cfg.put(ConfigTag.PASSWORD, "open sesame");

    try {
      BasicAuth decorator = new BasicAuth();
      decorator.setConfiguration(cfg);
      
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(BasicAuth.DEFAULT_HEADER);
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
  public void encryptedPassword() {
    String expectedData = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    Config cfg = new Config();
    cfg.put(ConfigTag.USERNAME, "Aladdin");
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD, "3nFJqavKpY7Gx+XX8cEgqnQtmzdytnsG3Z+9gKEYaHw=");

    try {
      BasicAuth decorator = new BasicAuth();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(BasicAuth.DEFAULT_HEADER);
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
  public void encryptedBoth() {
    String expectedData = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    Config cfg = new Config();
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME, "JHhGjEJwi1HQYyJ4SG0gH1ERo0FqHorb");
    cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD, "3nFJqavKpY7Gx+XX8cEgqnQtmzdytnsG3Z+9gKEYaHw=");

    try {
      BasicAuth decorator = new BasicAuth();
      decorator.setConfiguration(cfg);
      HttpMessage request = getRequest();
      decorator.process(request);

      Header[] headers = request.getHeaders(BasicAuth.DEFAULT_HEADER);
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
