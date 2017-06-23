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
package coyote.dx;

import coyote.commons.Version;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * Coyote Mail Tools fixture.
 * 
 * Constants are placed here along with any static method useful across 
 * multiple classes in the project. 
 */
public class CMT {
  public static final Version VERSION = new Version( 0, 1, 1, Version.EXPERIMENTAL );
  public static final String NAME = "CMT";

  public static final String SENDER = "sender";
  public static final String RECEIVER = "receiver";
  public static final String ATTACH = "attach";
  public static final String SUBJECT = "subject";
  public static final String BODY = "body";

  public static final BundleBaseName MSG;

  static {
    MSG = new BundleBaseName( "CMTMsg" );
  }

}
