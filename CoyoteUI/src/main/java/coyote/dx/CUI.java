/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.Version;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class CUI {
  public static final Version VERSION = new Version(0, 0, 1, Version.EXPERIMENTAL);
  public static final String NAME = "CoyoteUI";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName("CUIMsg");
  }



  public static String trimClassName(Class classref)  {
    String retval = "";
    String data = classref.toString();
    if (data.startsWith("class ")){
      data = data.substring(5);
    }
    retval = data;
    return retval;
  }


  /**
   * Called by other classes to get our version number.
   * 
   * @return a string representing our version.
   */
  public String getVersion() {
    return VERSION.toString();
  }

}
