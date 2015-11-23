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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
  public static final DateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
  public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
  public static final DateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat( "HH:mm:ss" );

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
