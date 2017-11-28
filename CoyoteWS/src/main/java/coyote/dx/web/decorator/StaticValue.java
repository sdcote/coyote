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

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.loader.Loader;


/**
 * Generates a random UUID as the message identifier.
 * 
 * <p>Useful for auditing in web service infrastructures.</p>
 */
public class StaticValue extends HeaderDecorator implements RequestDecorator {

  /**
   * Constructor used for testing
   */
  public StaticValue() {
    // used for testing
  }




  /**
   * @see coyote.dx.web.decorator.AbstractDecorator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration(DataFrame frame) {
    super.setConfiguration(frame);

    // Make sure we have a a header defined
    if (StringUtil.isBlank(getHeaderName())) {
      throw new IllegalArgumentException(getClass().getSimpleName() + " decorator must contain a header name to populate");
    }

  }




  /**
   * @return the value to be placed in the header
   */
  public String getValue() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.VALUE)) {
      retval = configuration.getString(ConfigTag.VALUE);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.VALUE)) {
      retval = CipherUtil.decryptString(configuration.getAsString(Loader.ENCRYPT_PREFIX + ConfigTag.VALUE));
    }
    return retval;
  }




  /**
   * @param text the value to be placed in the header
   */
  public void setValue(String text) {
    configuration.set(ConfigTag.VALUE, text);
  }




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process(HttpMessage request) {
    request.setHeader(getHeaderName(), getValue());
  }

}
