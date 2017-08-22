/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader;

import coyote.dx.CDX;


/**
 * Class containing CLI help text.
 */
public class LoaderHelp {

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append("Coyote Data Exchange Toolkit v");
    b.append(CDX.VERSION.toString());
    b.append("\n\nCall with the name of a data transfer file to execute that data transfer.");
    b.append("\n    cdx [filename]");
    b.append("\n\nThe following parameters are supported:");
    b.append("\n  -d  Enable debugging messages.");
    b.append("\n  -v  Enable verbose (informational) messages.");
    b.append("\n  -help  This screen is displayed.");
    b.append("\n  -version  Displays the version of all modules on the class path.");
    b.append("\n  encrypt  Encrypt the given string.");
    b.append("\n           This parameter takes the following positional parameters:");
    b.append("\n    encrypt \"string to encrypt\" [key] [xtea|blowfish]");
    return b.toString();
  }

}
