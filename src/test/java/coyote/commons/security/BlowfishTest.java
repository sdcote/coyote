/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.commons.security;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.Test;

import coyote.commons.CipherUtil;


/**
 * 
 */
public class BlowfishTest {

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * This shows the basic use case for Blowfish ciphering.
   */
  @Test
  public void testBasicUseCase() throws Exception {
    // The text we need to protect
    String cleartext = new Date().toString();
    String secret = "S3cRetKey";

    // - - - Encrypt the data - - - 

    // Create a new instance of the Cipher
    Cipher cipher = new BlowfishCipher();

    // Initialize the cipher with our secret key here we just use the UTF16 
    // encoding of our key string
    byte[] key = secret.getBytes( "UTF8" );
    System.out.println("key:"+new String (CipherUtil.encode( key )));
    cipher.init( secret.getBytes( "UTF8" ) ); // use UTF16 for larger character sets

    // Encrypt the text with the UTF16 encoded bytes our our clear text string 
    byte[] cipherdata = cipher.encrypt( cleartext.getBytes( "UTF8" ) );

    System.out.println("Data has been converted into "+cipherdata.length+" bytes of data"+ new String(CipherUtil.encode( cipherdata )));

    // - - - Decrypt the data - - - 

    // Create a new instance of the Cipher to decrypt our data
    Cipher cipher2 = new BlowfishCipher();

    // Initialize the second cipher with our secret key
    cipher2.init( secret.getBytes( "UTF8" ) );

    // Decrypt the data  
    byte[] cleardata = cipher2.decrypt( cipherdata );

    String newtext = new String( cleardata, "UTF8" );

    System.out.println( cleartext );
    System.out.println( newtext );
    assertEquals( cleartext, newtext );

  }

}
