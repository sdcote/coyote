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
package coyote.batch;

import coyote.commons.Version;
import coyote.commons.security.BlowfishCipher;


/**
 * 
 */
public class Batch {

  public static final Version VERSION = new Version( 0, 2, 0, Version.EXPERIMENTAL );
  public static final String NAME = "Batch";
  public static final String CIPHER_KEY = "CoyoteBatch";
  public static final String CIPHER_NAME = BlowfishCipher.CIPHER_NAME;

}
