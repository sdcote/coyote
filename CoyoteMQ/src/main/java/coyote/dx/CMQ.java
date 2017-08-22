/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
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
public class CMQ {
  public static final Version VERSION = new Version(0, 1, 0, Version.DEVELOPMENT);
  public static final String NAME = "CDX";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName("CmqMsg");
  }




  /**
   * Called by other classes to get our version number.
   * 
   * @return a string represing our version.
   */
  public String getVersion() {
    return VERSION.toString();
  }

}
