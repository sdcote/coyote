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

import coyote.dx.ConfigTag;


/**
 * 
 */
public abstract class HeaderDecorator extends AbstractDecorator implements RequestDecorator {

  /**
   * @return the headerName
   */
  public String getHeaderName() {
    String retval = null;
    if (configuration.containsIgnoreCase(ConfigTag.HEADER)) {
      retval = configuration.getString(ConfigTag.HEADER);
    }
    return retval;
  }




  /**
   * @param headerName the headerName to set
   */
  public void setHeaderName(String headerName) {
    configuration.set(ConfigTag.HEADER, headerName);
  }

}
