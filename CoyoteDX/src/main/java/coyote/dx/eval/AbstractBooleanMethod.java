/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.eval;

import coyote.commons.StringUtil;


/**
 * 
 */
public abstract class AbstractBooleanMethod {

  /**
  * If the token starts and ends with a double quote, return the value 
  * contained therein.
  * 
  * @param token the text to process
  * 
  * @return just the bare token
  */
  protected static String sanitize(String token) {
    if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
      String retval = StringUtil.getQuotedValue(token);
      if (retval != null)
        return retval.trim();
      else
        return retval;
    }
    return token;
  }

}
