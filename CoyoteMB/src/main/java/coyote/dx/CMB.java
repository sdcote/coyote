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
package coyote.batch;

import coyote.commons.Version;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * Batch Mail Tools fixture
 */
public class BatchMT {
  public static final Version VERSION = new Version( 0, 0, 1, Version.EXPERIMENTAL );
  public static final String NAME = "CMB";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "CmbMsg" );
  }
}
