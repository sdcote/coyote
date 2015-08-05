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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import coyote.commons.security.BlowfishCipher;
import coyote.commons.security.Cipher;
import coyote.commons.security.NullCipher;
import coyote.commons.security.XTEACipher;


/**
 * Very basic cipher utilities to assist with privacy.
 */
public class CipherUtil {

  // The amount of salt we add to the data we encrypt
  private static final int SALT_SIZE = 4;

  // name of a mandatory character set in all Java environments
  private static final String UTF_16 = "UTF-16";
  private static final String UTF_8 = "UTF-8";

  // The default initialization vector
  static final byte[] DEFAULT_IV = new byte[] { -90, 4, 13, 127, 54, 1, -34, -5, 8, 1, 2, -32, 44, 77, 98, 32 };

  // Something to generate salt for our data
  private static Random random = new Random( System.currentTimeMillis() );

  public static final String DEFAULT_CIPHER = BlowfishCipher.CIPHER_NAME;
  private static final Map<String, Cipher> cipherMap = new HashMap<String, Cipher>();
  static {
    register( new BlowfishCipher() );
    register( new XTEACipher() );
    register( new NullCipher() );
  }

  //The line separator string of the operating system.
  private static final String LINE_FEED = System.getProperty( "line.separator" );

  // 6-bit nibbles to Base64 characters.
  private static final char[] charMap = new char[64];
  static {
    int i = 0;
    for ( char c = 'A'; c <= 'Z'; c++ ) {
      charMap[i++] = c;
    }
    for ( char c = 'a'; c <= 'z'; c++ ) {
      charMap[i++] = c;
    }
    for ( char c = '0'; c <= '9'; c++ ) {
      charMap[i++] = c;
    }
    charMap[i++] = '+';
    charMap[i++] = '/';
  }

  // Base64 characters to 6-bit nibbles.
  private static final byte[] niblMap = new byte[128];
  static {
    for ( int i = 0; i < niblMap.length; i++ ) {
      niblMap[i] = -1;
    }
    for ( int i = 0; i < 64; i++ ) {
      niblMap[charMap[i]] = (byte)i;
    }
  }




  /**
   * Use the defaults to decipher the text.
   * 
   * <p>There are a few steps and options for deciphering text and this method 
   * uses a set of defaults:<ol>
   * <li>Decode the cipher data into bytes using Base64 encoding.</li>
   * <li>Use the default (Blowfish) cipher and initialize it with a default 16 
   * octet (128 bit) key and decrypt the data.</li>
   * <li>Four bytes of random salt is assumed to prepend the data so those 4 
   * bytes are removed.</li>
   * <li>The bytes are converted into a string using UTF-16 encoding.</li></ol>
   * </p>
   * 
   * @param cipherText the text to decipher
   * 
   * @return the deciphered string, null if the deciphering failed.
   * 
   * @see #encipher(String)
   */
  public static String decipher( final String cipherText ) {
    return decipher( cipherText, getCipher(), 4, DEFAULT_IV );
  }




  /**
   * Decipher the given text using the given cipher, key and assume the data is 
   * prepended with the given number of bytes of random data.
   * 
   * @param cipherText the text to decipher
   * @param cipher the cipher to use
   * @param saltSize number of bytes (of salt) to remove from the front of the cipherdata
   * @param key the key (initialization vector) to use with the cipher
   * 
   * @return The original text 
   */
  public static String decipher( final String cipherText, final Cipher cipher, final int saltSize, final byte[] key ) {

    // Base64 decode
    final byte[] cipherdata = decode( cipherText );

    if ( ( key != null ) && ( key.length > 0 ) ) {
      cipher.init( key );
    } else {
      // use the default key
      cipher.init( DEFAULT_IV );
    }

    // decrypt the bytes
    final byte[] saltedData = cipher.decrypt( cipherdata );

    byte[] data = null;
    if ( saltSize > 0 ) {
      // copy data without the salt
      data = new byte[saltedData.length - SALT_SIZE];
      System.arraycopy( saltedData, SALT_SIZE, data, 0, data.length );
    } else {
      data = saltedData;
    }

    String retval = null;
    try {
      retval = new String( data, UTF_16 );
    } catch ( final UnsupportedEncodingException e ) {
      e.printStackTrace();
    }

    return retval;

  }




  /**
   * Decipher the given text using the given named cipher with the given cipher 
   * key.
   * 
   * @param cipherText The enciphered data in Base64 encoding to decipher
   * @param cipherName the name of the cipher algorithm to use
   * @param key the secret key (initialization vector) to use with the cipher
   * 
   * @return the deciphered text
   * 
   * @see #encipher(String, String, String)
   */
  public static String decipher( final String cipherText, final String cipherName, final String key ) {

    byte[] iv = null;

    if ( ( key != null ) && ( key.length() > 0 ) ) {
      iv = decode( key );
    } else {
      iv = CipherUtil.DEFAULT_IV;
    }

    Cipher cipher = null;
    if ( ( cipherName != null ) && ( cipherName.trim().length() > 0 ) ) {
      cipher = getCipher( cipherName );
    } else {
      cipher = getCipher();
    }
    return decipher( cipherText, cipher, 4, iv );
  }




  /**
   * Decodes a byte array from Base64 format.
   * 
   * <p>No blanks or line breaks are allowed within the Base64 encoded input 
   * data.</p>
   * 
   * @param in A character array containing the Base64 encoded data.
   * 
   * @return An array containing the decoded data bytes.
   * 
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
  */
  public static byte[] decode( final char[] in ) {
    return decode( in, 0, in.length );
  }




  /**
   * Decodes a byte array from Base64 format.
   * 
   * <p>No blanks or line breaks are allowed within the Base64 encoded input 
   * data.</p>
   * 
   * @param in A character array containing the Base64 encoded data.
   * @param iOff Offset of the first character in {@code in} to be processed.
   * @param iLen Number of characters to process in {@code in}, starting at {@code iOff}.
   * 
   * @return An array containing the decoded data bytes.
   * 
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
  */
  public static byte[] decode( final char[] in, final int iOff, int iLen ) {
    if ( ( iLen % 4 ) != 0 ) {
      throw new IllegalArgumentException( "Length of Base64 encoded input string is not a multiple of 4." );
    }
    while ( ( iLen > 0 ) && ( in[( iOff + iLen ) - 1] == '=' ) ) {
      iLen--;
    }
    final int oLen = ( iLen * 3 ) / 4;
    final byte[] out = new byte[oLen];
    int ip = iOff;
    final int iEnd = iOff + iLen;
    int op = 0;
    while ( ip < iEnd ) {
      final int i0 = in[ip++];
      final int i1 = in[ip++];
      final int i2 = ip < iEnd ? in[ip++] : 'A';
      final int i3 = ip < iEnd ? in[ip++] : 'A';
      if ( ( i0 > 127 ) || ( i1 > 127 ) || ( i2 > 127 ) || ( i3 > 127 ) ) {
        throw new IllegalArgumentException( "Illegal character in Base64 encoded data." );
      }
      final int b0 = niblMap[i0];
      final int b1 = niblMap[i1];
      final int b2 = niblMap[i2];
      final int b3 = niblMap[i3];
      if ( ( b0 < 0 ) || ( b1 < 0 ) || ( b2 < 0 ) || ( b3 < 0 ) ) {
        throw new IllegalArgumentException( "Illegal character in Base64 encoded data." );
      }
      final int o0 = ( b0 << 2 ) | ( b1 >>> 4 );
      final int o1 = ( ( b1 & 0xf ) << 4 ) | ( b2 >>> 2 );
      final int o2 = ( ( b2 & 3 ) << 6 ) | b3;
      out[op++] = (byte)o0;
      if ( op < oLen ) {
        out[op++] = (byte)o1;
      }
      if ( op < oLen ) {
        out[op++] = (byte)o2;
      }
    }
    return out;
  }




  /**
   * Decodes a byte array from Base64 format.
   * 
   * <p>No blanks or line breaks are allowed within the Base64 encoded input 
   * data.</p>
   * 
   * @param s A Base64 String to be decoded.
   * 
   * @return An array containing the decoded data bytes.
   * 
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode( final String s ) {
    return decode( s.toCharArray() );
  }




  /**
   * Decodes a byte array from Base64 format and ignores line separators, tabs 
   * and blanks.
   * 
   * <p>CR, LF, Tab and Space characters are ignored in the input data.</p>
   * 
   * @param s A Base64 String to be decoded.
   * 
   * @return An array containing the decoded data bytes.
   * 
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decodeLines( final String s ) {
    final char[] buf = new char[s.length()];
    int p = 0;
    for ( int ip = 0; ip < s.length(); ip++ ) {
      final char c = s.charAt( ip );
      if ( ( c != ' ' ) && ( c != '\r' ) && ( c != '\n' ) && ( c != '\t' ) ) {
        buf[p++] = c;
      }
    }
    return decode( buf, 0, p );
  }




  /**
   * Decodes a string from Base64 format.
   * 
   * <p>No blanks or line breaks are allowed within the Base64 encoded input 
   * data.</p>
   * 
   * @param s A Base64 String to be decoded.
   * 
   * @return A String containing the decoded data.
   * 
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static String decodeString( final String s ) {
    return new String( decode( s ) );
  }




  /**
   * Encrypt the text and return a string that can be later decrypted.
   * 
   * <p>There are a few steps and options for enciphering text and this method 
   * uses a set of defaults:<ol>
   * <li>The string is converted into bytes using UTF-16 encoding. This 
   * should be available on all platforms.</li>
   * <li>Four bytes of random salt is added to the beginning of the encoded 
   * data.</li>
   * <li>Use the default (Blowfish) cipher and initialize it with a default 16 
   * octet (128 bit) key and encrypt the salted data.</li>
   * <li>Encode the cipher data into a string using Base64 encoding.</li></ol>
   * </p>
   * 
   * <p>The result is a text string which can be sent or stored on any media 
   * which supports strings.</p>
   * 
   * @param text The text to encrypt
   * 
   * @return enciphered string suitable for sending in the clear
   * 
   * @see #decipher(String)
   */
  public static String encipher( final String text ) {

    // generate 4 random bytes as a salt
    final byte[] saltParam = new byte[SALT_SIZE];
    random.nextBytes( saltParam );

    return CipherUtil.encipher( text, getCipher(), saltParam, DEFAULT_IV );
  }




  /**
   * This enciphers the given text using the given cipher with the given key 
   * adding the given salt (if provided) to randomize the data.
   * 
   * @param text The text to encipher
   * @param cipher the instance of the cipher to use
   * @param salt the salt to prepend to the data to be enciphered
   * @param key the key to use with the cipher to encipher the data
   * 
   * @return Base64 encoding of the enciphered data
   */
  public static String encipher( final String text, final Cipher cipher, final byte[] salt, final byte[] key ) {

    byte[] textbytes = null;

    // First get the UTF-16 encoding of the data
    if ( text != null ) {
      try {
        textbytes = text.getBytes( UTF_16 );
      } catch ( final UnsupportedEncodingException e ) {
        e.printStackTrace();
      }
    } else {
      textbytes = new byte[0];
    }

    byte[] data = null;
    if ( ( salt != null ) && ( salt.length > 0 ) ) {
      data = new byte[textbytes.length + salt.length];
      System.arraycopy( salt, 0, data, 0, salt.length );
      System.arraycopy( textbytes, 0, data, salt.length, textbytes.length );
    } else {
      data = textbytes;
    }

    if ( ( key != null ) && ( key.length > 0 ) ) {
      cipher.init( key );
    } else {
      // use the default key
      cipher.init( DEFAULT_IV );
    }

    final byte[] cipherdata = cipher.encrypt( data );

    // Now we use base64 encoding of the encoded data to generate our return value 
    final char[] retval = encode( cipherdata );

    return new String( retval );
  }




  /**
   * Encipher the given text using the given named cipher with the given cipher 
   * key.
   * 
   * @param plainText The text to encode
   * @param cipherName The name of the cipher to use
   * @param key Base64 encoding of the bytes to use as the initialization 
   *        vector (i.e. key) for the cipher
   *        
   * @return Base64 encoding of the encrypted data representing the given plain text
   * 
   * @see #decipher(String, String, String)
   */
  public static String encipher( final String plainText, final String cipherName, final String key ) {
    byte[] iv = null;

    if ( ( key != null ) && ( key.length() > 0 ) ) {
      iv = decode( key );
    } else {
      iv = CipherUtil.DEFAULT_IV;
    }

    // generate 4 random bytes as a salt
    final byte[] saltParam = new byte[SALT_SIZE];
    random.nextBytes( saltParam );

    Cipher cipher = null;
    if ( ( cipherName != null ) && ( cipherName.trim().length() > 0 ) ) {
      cipher = getCipher( cipherName );
    } else {
      cipher = getCipher();
    }

    return CipherUtil.encipher( plainText, cipher, saltParam, iv );
  }




  /**
   * Encodes a byte array into Base64 format.
   * 
   * @param in An array containing the data bytes to be encoded.
   * 
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode( final byte[] in ) {
    return encode( in, 0, in.length );
  }




  /**
   * Encodes a byte array into Base64 format.
   * 
   * @param in An array containing the data bytes to be encoded.
   * @param iLen Number of bytes to process in {@code in}.
   * 
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode( final byte[] in, final int iLen ) {
    return encode( in, 0, iLen );
  }




  /**
   * Encodes a byte array into Base64 format.
   * 
   * @param in An array containing the data bytes to be encoded.
   * @param iOff Offset of the first byte in {@code in} to be processed.
   * @param iLen Number of bytes to process in {@code in}, starting at {@code iOff}.
   * 
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode( final byte[] in, final int iOff, final int iLen ) {
    final int oDataLen = ( ( iLen * 4 ) + 2 ) / 3; // output length without padding
    final int oLen = ( ( iLen + 2 ) / 3 ) * 4; // output length including padding
    final char[] out = new char[oLen];
    int ip = iOff;
    final int iEnd = iOff + iLen;
    int op = 0;
    while ( ip < iEnd ) {
      final int i0 = in[ip++] & 0xff;
      final int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
      final int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
      final int o0 = i0 >>> 2;
      final int o1 = ( ( i0 & 3 ) << 4 ) | ( i1 >>> 4 );
      final int o2 = ( ( i1 & 0xf ) << 2 ) | ( i2 >>> 6 );
      final int o3 = i2 & 0x3F;
      out[op++] = charMap[o0];
      out[op++] = charMap[o1];
      out[op] = op < oDataLen ? charMap[o2] : '=';
      op++;
      out[op] = op < oDataLen ? charMap[o3] : '=';
      op++;
    }
    return out;
  }




  /**
   * Encodes a byte array into Base 64 format and breaks the output into lines 
   * of 76 characters.
   * 
   * @param in An array containing the data bytes to be encoded.
   * 
   * @return A String containing the Base64 encoded data, broken into lines.
   */
  public static String encodeLines( final byte[] in ) {
    return encodeLines( in, 0, in.length, 76, LINE_FEED );
  }




  /**
   * Encodes a byte array into Base 64 format and breaks the output into lines.
   * 
   * @param in An array containing the data bytes to be encoded.
    * @param iOff Offset of the first byte in {@code in} to be processed.
   * @param iLen Number of bytes to be processed in {@code in}, starting at {@code iOff}.
   * @param lineLen Line length for the output data. Should be a multiple of 4.
   * @param lineSeparator The line separator to be used to separate the output lines.
   * 
   * @return A String containing the Base64 encoded data, broken into lines.
   */
  public static String encodeLines( final byte[] in, final int iOff, final int iLen, final int lineLen, final String lineSeparator ) {
    final int blockLen = ( lineLen * 3 ) / 4;
    if ( blockLen <= 0 ) {
      throw new IllegalArgumentException();
    }
    final int lines = ( ( iLen + blockLen ) - 1 ) / blockLen;
    final int bufLen = ( ( ( iLen + 2 ) / 3 ) * 4 ) + ( lines * lineSeparator.length() );
    final StringBuilder buf = new StringBuilder( bufLen );
    int ip = 0;
    while ( ip < iLen ) {
      final int l = Math.min( iLen - ip, blockLen );
      buf.append( encode( in, iOff + ip, l ) );
      buf.append( lineSeparator );
      ip += l;
    }
    return buf.toString();
  }




  /**
   * Encodes a string into Base64 format.
   * 
   * @param s A String to be encoded.
   * 
   * @return A String containing the Base64 encoded data.
   */
  public static String encodeString( final String s ) {
    return new String( encode( s.getBytes() ) );
  }




  /**
   * @return an instance of the default cipher.
   */
  public static Cipher getCipher() {
    return getCipher( DEFAULT_CIPHER );
  }




  /**
   * Return a particular cipher by its name.
   * 
   * @param name the name of the cipher to retrieve.
   * 
   * @return the cipher registered with that name or null if none by that name exists.
   */
  public static Cipher getCipher( final String name ) {
    if ( name != null ) {
      final Cipher retval = cipherMap.get( name.toLowerCase() );
      if ( retval != null ) {
        return retval.getInstance();
      }
    }
    return null;
  }




  /**
   * Generate a string representation of a cipher key based on the given text.
   * 
   * <p>This method is useful for generating a cipher key based on text the 
   * user may provide. The result is a base64 encoding of the bytes which can 
   * be placed in configuration files.</p>
   * 
   * @param text The text from which the bytes are generated
   * 
   * @return Base64 encoding of the bytes generated from the given text.
   */
  public static String getKey( final String text ) {
    byte[] bytes = new byte[0];

    if ( ( text != null ) && ( text.length() > 0 ) ) {
      try {
        bytes = text.getBytes( UTF_8 );
      } catch ( final UnsupportedEncodingException e ) {
        e.printStackTrace();
      }
    }

    return new String( encode( bytes ) );
  }




  /**
   * Pad the given the data to the given block size according to RFC 1423.
   * 
   * <p>First the data is padded to blocks of data using a PKCS5 DES CBC
   * encryption padding scheme described in section 1.1 of RFC-1423.</p>
   * 
   * <p>The last byte of the stream is ALWAYS the number of bytes added to the 
   * end of the data. If the data ends on a boundary, then there will be eight
   * bytes of padding:<code><pre>
   * 88888888 - all of the last block is padding.
   * X7777777 - the last seven bytes are padding.
   * XX666666 - the last six bytes are padding.
   * XXX55555 - etc.
   * XXXX4444 - etc.
   * XXXXX333 - etc.
   * XXXXXX22 - etc.
   * XXXXXXX1 - only the last byte is padding.</pre></code></p>
   * 
   * <p>According to RFC1423 section 1.1:<blockquote>The input to the DES CBC
   * encryption process shall be padded to a multiple of 8 octets, in the
   * following manner. Let n be the length in octets of the input. Pad the 
   * input by appending 8-(n mod 8) octets to the end of the message, each 
   * having the value 8-(n mod 8), the number of octets being added. In 
   * hexadecimal, the possible paddings are: 01, 0202, 030303, 04040404, 
   * 0505050505, 060606060606, 07070707070707, and 0808080808080808. All input 
   * is padded with 1 to 8 octets to produce a multiple of 8 octets in length. 
   * The padding can be removed unambiguously after decryption.</blockquote></p>
   *  
   * @param data The source data
   * 
   * @return a new array of data containing the original data and the padding
   * 
   * @see #trim(byte[])
   */
  public static byte[] pad( final byte[] data ) {
    // pad the data as necessary using a PKCS5 (or RFC1423) padding scheme
    int padding = 8 - ( data.length % 8 );

    // There is always padding even it it is not needed
    if ( padding == 0 ) {
      padding = 8;
    }

    // create the return value
    final byte[] retval = new byte[data.length + padding];

    // copy the original data
    System.arraycopy( data, 0, retval, 0, data.length );

    // add the padding
    for ( int x = data.length; x < retval.length; retval[x++] = (byte)padding ) {
      ;
    }

    return retval;
  }




  /**
   * Add the given cipher to the list of ciphers supported by this fixture.
   * 
   * <p>The cipher must return a unique name so it can be referenced and 
   * returned later.</p>
   * 
   * @param cipher the cipher to register with the fixture
   */
  public static void register( final Cipher cipher ) {
    if ( ( cipher != null ) && ( cipher.getName() != null ) ) {
      cipherMap.put( cipher.getName().toLowerCase(), cipher );
    }
  }




  /**
   * Remove padding that is at the end of the data using RFC 1423.
   * 
   * @param data the byte array to trim.
   * 
   * @return The trimmed array.
   * 
   * @see #pad(byte[])
   */
  public static byte[] trim( final byte[] data ) {
    if ( data.length > 0 ) {
      final int padding = data[data.length - 1];

      if ( ( padding > 0 ) && ( padding < 9 ) ) {
        final byte[] retval = new byte[data.length - padding];
        System.arraycopy( data, 0, retval, 0, retval.length );
        return retval;
      }
    }

    return data;
  }
}
