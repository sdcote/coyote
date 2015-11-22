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

import coyote.commons.CipherUtil;
import coyote.commons.Version;
import coyote.loader.Loader;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class Batch {

  public static final Version VERSION = new Version( 0, 2, 0, Version.EXPERIMENTAL );
  public static final String NAME = "Batch";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "BatchMsg" );
  }




  /**
   * Common utility to encrypt data.
   * 
   * <p>Note that this uses the libraries from the Loader package and is 
   * intended to use the default key and encryption algorithm therein.</p>
   * 
   * @param cleartext the text to encrypt
   * 
   * @return encrypted text
   */
  public static String encrypt( String cleartext ) {
    String retval = null;
    String key = System.getProperty( ConfigTag.CIPHER_KEY, CipherUtil.getKey( Loader.CIPHER_KEY ) );
    String cipherName = System.getProperty( ConfigTag.CIPHER_NAME, Loader.CIPHER_NAME );
    retval = CipherUtil.encipher( cleartext, cipherName, key );
    return retval;
  }




  /**
   * Common utility to decrypt data
   * 
   * <p>Note that this uses the libraries from the Loader package and is 
   * intended to use the default key and encryption algorithm therein.</p>
   * 
   * @param ciphertext encrypted text
   * 
   * @return decrypted text
   */
  public static String decrypt( String ciphertext ) {
    String retval = null;
    String key = System.getProperty( ConfigTag.CIPHER_KEY, CipherUtil.getKey( Loader.CIPHER_KEY ) );
    String cipherName = System.getProperty( ConfigTag.CIPHER_NAME, Loader.CIPHER_NAME );
    retval = CipherUtil.decipher( ciphertext, cipherName, key );
    return retval;
  }

}
