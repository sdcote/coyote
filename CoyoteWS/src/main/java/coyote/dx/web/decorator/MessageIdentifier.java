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

import java.util.UUID;

import org.apache.http.HttpMessage;


/**
 * Generates a random UUID as the message identifier.
 * 
 * <p>Useful for auditing in web service infrastructures.</p>
 */
public class MessageIdentifier extends HeaderDecorator implements RequestDecorator {
  private static final String DEFAULT_HEADER = "X-Message-Id";




  public MessageIdentifier() {
    setHeaderName(DEFAULT_HEADER);
  }




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process(HttpMessage request) {
    request.setHeader(getHeaderName(), UUID.randomUUID().toString());
  }

}
