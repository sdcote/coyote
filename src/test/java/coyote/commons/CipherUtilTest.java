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
package coyote.commons;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;

import coyote.commons.security.BlowfishCipher;
import coyote.commons.security.Cipher;
import coyote.commons.security.NullCipher;


/**
 * 
 */
public class CipherUtilTest {

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * Test method for {@link coyote.commons.CipherUtil#pad(byte[])}.
   */
  @Test
  public void testPad() {

    byte[] data = null;
    byte[] bytes = new byte[1];
    data = CipherUtil.pad( bytes );
    assertTrue( data[7] == 7 );

    bytes = new byte[2];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 8 );
    assertTrue( data[7] == 6 );

    bytes = new byte[3];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 8 );
    assertTrue( data[7] == 5 );

    bytes = new byte[4];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 8 );
    assertTrue( data[7] == 4 );

    bytes = new byte[5];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 8 );
    assertTrue( data[7] == 3 );

    bytes = new byte[6];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 8 );
    assertTrue( data[7] == 2 );

    bytes = new byte[7];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 8 );
    assertTrue( data[7] == 1 );

    bytes = new byte[8];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 16 );
    assertTrue( data[15] == 8 );

    bytes = new byte[9];
    data = CipherUtil.pad( bytes );
    assertTrue( data.length == 16 );
    assertTrue( data[15] == 7 );

  }




  /**
   * Test method for {@link coyote.commons.CipherUtil#trim(byte[])}.
   */
  @Test
  public void testTrim() {

    byte[] data = null;
    byte[] bytes = new byte[16];
    bytes[9] = 7;
    bytes[10] = 7;
    bytes[11] = 7;
    bytes[12] = 7;
    bytes[13] = 7;
    bytes[14] = 7;
    bytes[15] = 7;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 9 );

    bytes = new byte[16];
    bytes[8] = 8;
    bytes[9] = 8;
    bytes[10] = 8;
    bytes[11] = 8;
    bytes[12] = 8;
    bytes[13] = 8;
    bytes[14] = 8;
    bytes[15] = 8;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 8 );

    bytes = new byte[8];
    bytes[7] = 1;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 7 );

    bytes = new byte[8];
    bytes[7] = 2;
    bytes[6] = 2;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 6 );

    bytes = new byte[8];
    bytes[7] = 3;
    bytes[6] = 3;
    bytes[5] = 3;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 5 );

    bytes = new byte[8];
    bytes[7] = 4;
    bytes[6] = 4;
    bytes[5] = 4;
    bytes[4] = 4;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 4 );

    bytes = new byte[8];
    bytes[7] = 5;
    bytes[6] = 5;
    bytes[5] = 5;
    bytes[4] = 5;
    bytes[3] = 5;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 3 );

    bytes = new byte[8];
    bytes[7] = 6;
    bytes[6] = 6;
    bytes[5] = 6;
    bytes[4] = 6;
    bytes[3] = 6;
    bytes[2] = 6;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 2 );

    bytes = new byte[8];
    bytes[7] = 7;
    bytes[6] = 7;
    bytes[5] = 7;
    bytes[4] = 7;
    bytes[3] = 7;
    bytes[2] = 7;
    bytes[1] = 7;
    data = CipherUtil.trim( bytes );
    assertTrue( data.length == 1 );
  }




  @Test
  public void testGetCipher() {

    // get the default cipher
    Cipher cipher = CipherUtil.getCipher( NullCipher.CIPHER_NAME );
    assertNotNull( cipher );
    assertTrue( cipher instanceof NullCipher );

    cipher = CipherUtil.getCipher( BlowfishCipher.CIPHER_NAME );
    assertNotNull( cipher );
    assertTrue( cipher instanceof BlowfishCipher );

    cipher = CipherUtil.getCipher( "biff" );
    assertNull( cipher );

    cipher = CipherUtil.getCipher( null );
    assertNull( cipher );

    cipher = CipherUtil.getCipher();
    assertNotNull( cipher );
    assertTrue( cipher instanceof BlowfishCipher );
  }




  @Test
  public void testEncryptString() {
    String originalText = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
    //String originalText = "FooBar";

    String cipherText = CipherUtil.encipher( originalText );
    //System.out.println( cipherText );
    assertNotNull( cipherText );

  }




  @Test
  public void testRoundTrip() {

    String originalText = "The doctrine of Right and Wrong, is perpetually disputed, both by Pen and the Sword: Whereas the doctrine of Lines, and Figures, is not so; because men care not, in that subject what be truth, as a thing that crosses no mans ambition, profit, or lust. For I doubt not, but if it had been a thing contrary to any mans right of dominion, or to the interest of men that have dominion, That the three Angles of a Triangle, should be equall to two Angles of a Square; that doctrine should have been, if not disputed, yet by the burning of all books of Geometry, suppressed, as far as he whom it concerned was able.";

    String cipherText = CipherUtil.encipher( originalText );

    //System.out.println("Using a key of "+ new String(CipherUtil.encode( CipherUtil.DEFAULT_IV )));

    String text = CipherUtil.decipher( cipherText );

    assertEquals( originalText, text );
  }




  @Test
  public void testgetKey() {
    String key = CipherUtil.getKey( "123abc" );
    assertNotNull( key );
    //System.out.println("key:'"+key+"'");

    key = CipherUtil.getKey( "123abcdef" );
    assertNotNull( key );

    key = CipherUtil.getKey( "1" );
    assertNotNull( key );

    key = CipherUtil.getKey( "" );
    assertNotNull( key );

    key = CipherUtil.getKey( null );
    assertNotNull( key );
  }




  @Test
  public void testGetKey() {

    String plaintext = "x9qW19]s3";

    String key = CipherUtil.getKey( plaintext );
    assertEquals( "eDlxVzE5XXMz", key );

    //System.out.println( key );
  }




  /**
   * Test the most common use case for the CipherUtil fixture
   */
  @Test
  public void testCommonCase() {
    // Pull the key from the system properties -Dsnapi.key="something"
    String key = "eDlxVzE5XXMz";

    // Pull the cipher name from the properties -Dsnapi.cipher="blowfish"
    String cipherName = "blowfish";

    // Data we want to protect
    String confidentialText = "I like Hello Kitty";

    // Encipher the given text using the given named cipher using the given key
    String cipherText = CipherUtil.encipher( confidentialText, cipherName, key );

    //System.out.println( cipherText );

    // Now decipher the text to get the original text
    String plainText = CipherUtil.decipher( cipherText, cipherName, key );

    //System.out.println( plainText );

    assertEquals( confidentialText, plainText );
  }
}
