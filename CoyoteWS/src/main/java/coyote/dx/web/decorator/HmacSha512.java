/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.web.decorator;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;

import coyote.commons.CipherUtil;
import coyote.commons.StreamUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.loader.Loader;
import coyote.loader.log.Log;


/**
 * This generates a SHA-512 message authentication code for the body of the 
 * request message.
 * 
 * <p>If a data configuration attribute is given, the MAC will be generated 
 * for that string instead of the body. This helps when the service does not 
 * parse the body as expected or if only a portion of the body is to be signed.
 */
public class HmacSha512 extends HeaderDecorator implements RequestDecorator {

  /**
   * @return the secret used to initialize the hash function
   */
  public String getSecret() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.SECRET)) {
      retval = configuration.getString(ConfigTag.SECRET);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.SECRET)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.SECRET));
    }
    return retval;
  }




  /**
   * @param secret the secret used to initialize the hash function
   */
  public void setSecret(String secret) {
    configuration.set(ConfigTag.SECRET, secret);
  }




  /**
   * @return the data to be hashed
   */
  public String getData() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.DATA)) {
      retval = configuration.getString(ConfigTag.DATA);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.DATA)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.DATA));
    }
    return retval;
  }




  /**
   * @param text the data to be hashed
   */
  public void setData(String text) {
    configuration.set(ConfigTag.DATA, text);
  }




  /**
   * @see coyote.dx.web.decorator.AbstractDecorator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration(DataFrame frame) {
    super.setConfiguration(frame);

    // Make sure we have a private key
    if (StringUtil.isBlank(getSecret())) {
      throw new IllegalArgumentException(getClass().getSimpleName() + " decorator must contain a '" + ConfigTag.SECRET + "' configuration element");
    }

    // If no data value is provided, the entire request body will be signed with the secret
    if (StringUtil.isBlank(getData())) {
      Log.debug(getClass().getSimpleName() + " decorator will only sign messages which contain a body");
    }

    // Make sure we have a header defined
    if (StringUtil.isBlank(getHeaderName())) {
      throw new IllegalArgumentException(getClass().getSimpleName() + " decorator must contain a header name to populate");
    }

  }




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process(HttpMessage request) {

    // TODO: If there is no data config element, get the body of the message and sign the whole thing

    try {
      Mac shaMac = Mac.getInstance("HmacSHA512");
      SecretKeySpec keySpec = new SecretKeySpec(getSecret().getBytes(), "HmacSHA512");
      shaMac.init(keySpec);
      final byte[] macData;
      if (StringUtil.isBlank(getData())) {
        if (request instanceof HttpEntityEnclosingRequest) {
          HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest)request;
          HttpEntity body = req.getEntity();
          if (body != null) {
            try {
              macData = shaMac.doFinal(StreamUtil.loadBytes(body.getContent()));
            } catch (UnsupportedOperationException | IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
              return;
            }
          } else {
            Log.error("The HTTP message did not contain a body to sign");
            macData = new byte[0];
          }
        } else {
          Log.error("No data specified for decorator and could not retrieve body entity to sign from: " + request.getClass().getName());
          return;
        }
      } else {
        macData = shaMac.doFinal(getData().getBytes());
      }

      String sign = Hex.encodeHexString(macData);
      request.setHeader(getHeaderName(), sign);
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
