/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx;

import coyote.commons.Version;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class CFT {
  public static final Version VERSION = new Version( 0, 0, 1, Version.EXPERIMENTAL );
  public static final String NAME = "BatchFT";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "CFTMsg" );
  }
}
