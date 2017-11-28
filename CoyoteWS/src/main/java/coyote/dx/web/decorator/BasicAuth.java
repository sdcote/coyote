/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web.decorator;

import org.apache.http.HttpMessage;

import coyote.commons.ByteUtil;
import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.loader.Loader;


/**
 * When the user agent wants to send the server authentication credentials it 
 * may use the Authorization field.
 * 
 * The Authorization field is constructed as follows:<ul>
 * <li>Username and password are combined into a string "username:password". 
 * Note that username cannot contain the ":" character.</li>
 * <li>The resulting string is then encoded using the RFC2045-MIME variant of 
 * Base64, except not limited to 76 char/line</li>
 * <li>The authorization method and a space i.e. "Basic " is then put before 
 * the encoded string.</li></ul>
 * 
 * <p>For example, if the user agent uses 'Aladdin' as the username and 
 * 'open sesame' as the password then the field is formed as follows:<pre>
 * Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==</pre>
 * 
 */
public class BasicAuth extends HeaderDecorator implements RequestDecorator {
  protected static final String BASIC = "Basic";
  protected static final String DEFAULT_HEADER = "Authorization";




  public BasicAuth(String user, String pswd) {
    setUsername(user);
    setPassword(pswd);
  }




  /**
   * Constructor used for testing
   */
  public BasicAuth() {
    // used for testing
  }




  /**
   * 
   */
  void calculateHeaderData() {
    if (StringUtil.isNotBlank(getUsername()) || StringUtil.isNotBlank(getPassword())) {
      StringBuffer b = new StringBuffer();

      if (StringUtil.isNotBlank(getUsername())) {
        b.append(getUsername());
      }
      b.append(":");

      if (StringUtil.isNotBlank(getPassword())) {
        b.append(getPassword());
      }

      setHeaderData(BASIC + " " + ByteUtil.toBase64(StringUtil.getBytes(b.toString())));
    }
  }




  /**
   * @return the headerData
   */
  public String getHeaderData() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.DATA)) {
      retval = configuration.getString(ConfigTag.DATA);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.DATA)) {
      retval = configuration.getString(Loader.ENCRYPT_PREFIX + ConfigTag.DATA);
    }
    return retval;
  }




  /**
   * @param headerData the headerData to set
   */
  public void setHeaderData(String headerData) {
    configuration.set(ConfigTag.DATA, headerData);
  }




  /**
   * @return the username
   */
  public String getUsername() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.USERNAME)) {
      retval = configuration.getString(ConfigTag.USERNAME);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));
    }
    return retval;
  }




  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    configuration.set(ConfigTag.USERNAME, username);
    setHeaderData(null); // force recalculation of data
  }




  /**
   * @return the password
   */
  public String getPassword() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.PASSWORD)) {
      retval = configuration.getString(ConfigTag.PASSWORD);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));
    }
    return retval;
  }




  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    configuration.set(ConfigTag.PASSWORD, password);
    setHeaderData(null); // force recalculation of data
  }




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process(HttpMessage request) {

    // if we don't have any data to place in the header...
    if (StringUtil.isBlank(getHeaderData())) {
      // ... calculate it from the username and password
      calculateHeaderData();
    }

    // Set the header if data exists
    if (StringUtil.isNotBlank(getHeaderData())) {
      request.setHeader(getHeaderName(), getHeaderData());
    }
  }




  /**
   * @see coyote.dx.web.decorator.HeaderDecorator#getHeaderName()
   */
  @Override
  public String getHeaderName() {
    String retval = super.getHeaderName();
    if (StringUtil.isBlank(retval)) {
      setHeaderName(DEFAULT_HEADER);
      retval = DEFAULT_HEADER;
    }
    return retval;
  }

}
