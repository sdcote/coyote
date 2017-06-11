/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.security;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.junit.AfterClass;
import org.junit.Test;

import coyote.commons.ByteUtil;
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
    //System.out.println( "key:" + new String( CipherUtil.encode( key ) ) );
    cipher.init( secret.getBytes( "UTF8" ) ); // use UTF16 for larger character sets

    // Encrypt the text with the UTF16 encoded bytes our our clear text string 
    byte[] cipherdata = cipher.encrypt( cleartext.getBytes( "UTF8" ) );

    //System.out.println( "Data has been converted into " + cipherdata.length + " bytes of data" + new String( CipherUtil.encode( cipherdata ) ) );

    // - - - Decrypt the data - - - 

    // Create a new instance of the Cipher to decrypt our data
    Cipher cipher2 = new BlowfishCipher();

    // Initialize the second cipher with our secret key
    cipher2.init( secret.getBytes( "UTF8" ) );

    // Decrypt the data  
    byte[] cleardata = cipher2.decrypt( cipherdata );

    String newtext = new String( cleardata, "UTF8" );

    //System.out.println( cleartext );
    //System.out.println( newtext );
    assertEquals( cleartext, newtext );

  }




  @Test
  public void testAgainstInternal() throws Exception {
    String cleartext = new Date().toString();
    String secret = "S3cRetKey";
    byte[] key = secret.getBytes( "UTF8" );
    byte[] plainData = cleartext.getBytes( "UTF8" );

    // Do our encryption
    Cipher cipher = new BlowfishCipher();
    cipher.init( key );
    byte[] cipherdata = cipher.encrypt( plainData );
    //System.out.println( "Data has been converted into " + cipherdata.length + " bytes of data" + new String( CipherUtil.encode( cipherdata ) ) );

    // the internal data should be the same length
    byte[] encryptedData = new byte[cipherdata.length];
    encrypt( plainData, encryptedData, key );

  }




  @Test
  public void testInternal() throws Exception {
    byte[] key = { 0x11, 0x22, 0x33, 0x44 };
    byte[] plainData = { 0x55, (byte)0xaa, 0x12, 0x34, 0x56, 0x78, (byte)0x9a, (byte)0xbc };
    //System.out.println(ByteUtil.dump( plainData ));

    // we use 16 bytes to allow for PKCS5 padding
    byte[] encryptedData = new byte[16];
    encrypt( plainData, encryptedData, key );
    //System.out.println(ByteUtil.dump( encryptedData ));
    
    byte[] decryptedData = new byte[8];
    decrypt( encryptedData, decryptedData, key );
    //System.out.println(ByteUtil.dump( decryptedData ));

    if ( !Arrays.equals( plainData, decryptedData ) ){
      fail( "Decrypted data not equal." );
    }
  }




  private static void encrypt( byte[] input, byte[] output, byte[] key ) throws Exception {
    crypt( javax.crypto.Cipher.ENCRYPT_MODE, input, output, key );
  }




  private static void decrypt( byte[] input, byte[] output, byte[] key ) throws Exception {
    crypt( javax.crypto.Cipher.DECRYPT_MODE, input, output, key );
  }




  private static void crypt( int opmode, byte[] input, byte[] output, byte[] key ) throws Exception {
    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance( "Blowfish/ECB/PKCS5Padding" );
    SecretKeySpec keySpec = new SecretKeySpec( key, "Blowfish" );
    cipher.init( opmode, keySpec );
    cipher.doFinal( input, 0, input.length, output );
  }

}
