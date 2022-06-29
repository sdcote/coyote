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
    b.append("Coyote Data Exchange v");
    b.append(CDX.VERSION.toString());
    b.append("\n\nCall with the name of a data transfer file to execute.");
    b.append("\n\n    cdx [filename]");
    b.append("\n\nIf the first parameter is \"encrypt\", the loader will encrypt the given");
    b.append("\nquoted string using the defaults:");
    b.append("\n\n    cdx encrypt \"string to encrypt\"");
    b.append("\n\nYou can specify the key and algorithm using positional parameters:");
    b.append("\n\n    cdx encrypt \"string to encrypt\" [key] [xtea|blowfish|null]");
    b.append("\n\nThe following parameters are also supported:");
    b.append("\n  -d  | --debug    Enable debugging messages.");
    b.append("\n  -v  | --verbose  Enable verbose (informational) messages.");
    b.append("\n  -t  | --trace    Enable (program flow) trace messages.");
    b.append("\n  -owd             Override the work directory with the current directory.");
    b.append("\n  --help           Show this screen.");
    b.append("\n  --version        Displays the version of all known modules on the class path.");
    b.append("\n");
    return b.toString();
  }

}
