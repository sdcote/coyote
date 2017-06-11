/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;


/**
 * Various utilities for manipulating data at the byte level.
 *
 * <p>"overlay" methods place data into a byte array buffer.
 *
 * <p>"retrieve" methods remove data from a byte array buffer.
 *
 * <p>"show" methods convert data into strings such as a binary or hex string.
 *
 * <p>"dump" methods convert bytes into easily viewable partitioned data.
 */
public class ByteUtil {

  /**
   * All possible chars for representing a number as a String
   */
  final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };




  /**
   * Private constructor because everything is static
   */
  private ByteUtil() {}




  /**
   * Put a long into a byte[] buffer.
   *
   * <p>This stores a binary number. Assuming that only four bytes are written
   * down here are some examples of what is written:
   * <pre>
   * -259 : 0xFF 0xFF 0xFE 0xFD
   * -258 : 0xFF 0xFF 0xFE 0xFE
   * -257 : 0xFF 0xFF 0xFE 0xFF
   * -256 : 0xFF 0xFF 0xFF 0x00
   * -255 : 0xFF 0xFF 0xFF 0x01
   *  -10 : 0xFF 0xFF 0xFF 0xF6
   *   -1 : 0xFF 0xFF 0xFF 0xFF
   *    1 : 0x00 0x00 0x00 0x01
   *   10 : 0x00 0x00 0x00 0x0A
   *  258 : 0x00 0x00 0x01 0x02
   *  259 : 0x00 0x00 0x01 0x03
   *  260 : 0x00 0x00 0x01 0x04
   * </pre>
   *
   * @param x
   * @param buf
   * @param offset at which to place the number.
   */
  public static void overlay( final long x, final byte[] buf, int offset ) {
    buf[offset++] = (byte)( x >>> 56 );
    buf[offset++] = (byte)( x >>> 48 );
    buf[offset++] = (byte)( x >>> 40 );
    buf[offset++] = (byte)( x >>> 32 );
    buf[offset++] = (byte)( x >>> 24 );
    buf[offset++] = (byte)( x >>> 16 );
    buf[offset++] = (byte)( x >>> 8 );
    buf[offset++] = (byte)( x );
  }




  /**
   * Put an unsigned integer(4-byte value) into a byte[] buffer.
   *
   * @param value the value to encode into the buffer
   * @param buf the buffer into which the encoded value is written
   * @param offset at which point in the buffer to place the encoded value.
   */
  public static void overlayUnsignedInt( final long value, final byte[] buf, int offset ) {
    buf[offset++] = (byte)( ( value >>> 24 ) & 0xFF );
    buf[offset++] = (byte)( ( value >>> 16 ) & 0xFF );
    buf[offset++] = (byte)( ( value >>> 8 ) & 0xFF );
    buf[offset++] = (byte)( ( value >>> 0 ) & 0xFF );
  }




  /**
   * Return a byte[] (2 byte value) as an unsigned short
   *
   * @param value value to convert
   *
   * @return 3 bytes representing and unsigned short
   */
  public static byte[] renderUnsignedShort( final int value ) {
    final byte[] retval = new byte[2];
    retval[0] = (byte)( ( value >>> 8 ) & 0xFF );
    retval[1] = (byte)( ( value >>> 0 ) & 0xFF );

    return retval;
  }




  /**
   * Creates a 4 byte array representing the integer in network byte order
   *
   * @param value The long representing an integer value to render
   *
   * @return bytes suitable for communicating an integer of some media
   */
  public static byte[] renderUnsignedInt( final long value ) {
    final byte[] retval = new byte[4];
    retval[0] = (byte)( ( value >>> 24 ) & 0xFF );
    retval[1] = (byte)( ( value >>> 16 ) & 0xFF );
    retval[2] = (byte)( ( value >>> 8 ) & 0xFF );
    retval[3] = (byte)( ( value >>> 0 ) & 0xFF );

    return retval;
  }




  /**
   * Put an int (4 byte value) into a byte[] buffer.
   *
   * @param value the value to encode into the buffer
   * @param buf the buffer into which the encoded value is written
   * @param offset at which point in the buffer to place the encoded value.
   */
  public static void overlay( final int value, final byte[] buf, int offset ) {
    buf[offset++] = (byte)( value >>> 24 );
    buf[offset++] = (byte)( value >>> 16 );
    buf[offset++] = (byte)( value >>> 8 );
    buf[offset++] = (byte)( value );
  }




  /**
   * Put a short (2 byte value) into a byte[] buffer.
   *
   * @param value the value to encode into the buffer
   * @param buf the buffer into which the encoded value is written
   * @param offset at which point in the buffer to place the encoded value.
   */
  public static void overlay( final short value, final byte[] buf, int offset ) {
    buf[offset++] = (byte)( value >>> 8 );
    buf[offset++] = (byte)( value );
  }




  /**
   * Put a short (2 byte value) into a byte[] buffer.
   *
   * @param value the value to encode into the buffer
   * @param buf the buffer into which the encoded value is written
   * @param offset at which point in the buffer to place the encoded value.
   */
  public static void overlayUnsignedShort( final int value, final byte[] buf, int offset ) {
    buf[offset++] = (byte)( ( value >>> 8 ) & 0xFF );
    buf[offset++] = (byte)( ( value >>> 0 ) & 0xFF );
  }




  /**
   * Find the first occurrence of a specified byte.
   *
   * @param c the byte to look for
   * @param buf the buffer to search
   * @param offset from which to start
   * @param len the limit of the search
   * @return offset of first occurrence or -1 if not found.
   */
  public static int findNextByte( final byte c, final byte[] buf, final int offset, final int len ) {
    int rc = -1;

    if ( buf != null ) {
      rc = offset;

      while ( ( rc < len ) && ( buf[rc] != c ) ) {
        ++rc;
      }

      if ( rc >= len ) {
        rc = -1;
      }
    }

    return rc;
  }




  /**
   * Find the first occurrence of a specified byte.
   *
   * @param c1 first of the two bytes for which to search
   * @param c2 second of the two bytes for which to search
   * @param buf the buffer in which to search
   * @param offset from which to start
   * @param len the limit of the search
   * @return offset of first occurrence or -1 if not found.
   */
  public static int findNextTwoBytes( final byte c1, final byte c2, final byte[] buf, final int offset, final int len ) {
    int rc = -1;

    if ( buf != null ) {
      rc = offset + 1;

      while ( ( rc < len ) && ( buf[rc] != c2 ) && ( buf[rc - 1] != c1 ) ) {
        ++rc;
      }

      if ( rc >= len ) {
        rc = -1;
      }
    }

    return rc;
  }




  /**
   * Convert a byte to a character representation.
   *
   * <p>Sorta misnamed as this method will return the character representation
   * of the given byte in the current / default encoding for the locale.
   *
   * @param b the byte to convert
   *
   * @return the ASCII character representation of the byte
   */
  public static char byteToASCII( final byte b ) {
    if ( ( b < 32 ) || ( b > 126 ) ) {
      return ' ';
    } else {
      return (char)b;
    }
  }




  /**
   * Convert a byte to a 2-character string representing its hexadecimal value,
   * with leading zero if needed.
   *
   * @param b the byte to convert
   *
   * @return hex representation of the byte
   */
  public static String byteToHex( final byte b ) {
    return ByteUtil.show( b, 4 );
  }




  /**
   * Convert a byte array to a string of 2-character hexadecimal values, with
   * leading zero if needed each separated with a single space.
   *
   * @param barray The byte array to convert
   *
   * @return the string representing the array in hex
   */
  public static String bytesToHex( final byte[] barray ) {
    return bytesToHex( barray, " " );
  }




  /**
   * Convert a byte array to a string of 2-character hexadecimal values, with
   * leading zero if needed each separated with the given string delimiter.
   * 
   * @param barray The byte array to convert
   * @param delim the string to place between each hex code
   * 
   * @return the string representing the array in hex with the given delimiter between each
   */
  public static String bytesToHex( final byte[] barray, String delim ) {
    if ( barray == null ) {
      return null;
    }

    String delimiter = "";
    if ( delim != null ) {
      delimiter = delim;
    }

    final StringBuffer result = new StringBuffer();

    for ( int i = 0; i < barray.length; i++ ) {
      result.append( ByteUtil.show( barray[i], 4 ) );
      if ( i + 1 < barray.length )
        result.append( delimiter );
    }

    return result.toString().toUpperCase();
  }




  /**
   * Convert the entered number into a string. This works based upon number of bits.
   *
   * <p>This can be used to produce many standard outputs.
   * <pre>
   *   1 bit  = binary, base two
   *   2 bits = base four
   *   3 bits = octal, base eight
   *   4 bits = hex, base sixteen
   *   5 bits = base 32
   * </pre>
   * Leading digits are included.
   *
   * @param i the number to show
   * @param num_bits number of bits to consider
   *
   * @return string representation of the number represented by the bits
   */
  public static String show( long i, final int num_bits ) {
    int num_chars = 64 / num_bits;

    if ( 64 % num_bits == 0 ) {
      --num_chars;
    }

    final char[] buf = new char[num_chars + 1];
    final int radix = 1 << num_bits;
    final long mask = radix - 1;

    for ( int charPos = num_chars; charPos >= 0; --charPos ) {
      buf[charPos] = ByteUtil.digits[(int)( i & mask )];
      i >>>= num_bits;
    }

    return new String( buf );
  }




  /**
   * Convert the entered number into a string. This works based upon number of 
   * bits.
   *
   * <p>This can be used to produce many standard outputs.
   * <pre>
   *   1 bit  = binary, base two
   *   2 bits = base four
   *   3 bits = octal, base eight
   *   4 bits = hex, base sixteen
   *   5 bits = base 32
   * </pre>
   * Leading digits are included.
   *
   * @param i the number to show
   * @param num_bits number of bits to consider
   *
   * @return string representation of the number represented by the bits
   */
  public static String show( int i, final int num_bits ) {
    int num_chars = 32 / num_bits;

    if ( 32 % num_bits == 0 ) {
      --num_chars;
    }

    final char[] buf = new char[num_chars + 1];
    final int radix = 1 << num_bits;
    final long mask = radix - 1;

    for ( int charPos = num_chars; charPos >= 0; --charPos ) {
      buf[charPos] = ByteUtil.digits[(int)( i & mask )];
      i >>>= num_bits;
    }

    return new String( buf );
  }




  /**
   * Convert the entered number into a string. This works based upon number of
   * bits.
   *
   * <p>This can be used to produce many standard outputs.
   * <pre>
   *   1 bit  = binary, base two
   *   2 bits = base four
   *   3 bits = octal, base eight
   *   4 bits = hex, base sixteen
   *   5 bits = base 32
   * </pre>
   * Leading digits are included.
   *
   * @param i the number to show
   * @param num_bits number of bits to consider
   *
   * @return string representation of the number represented by the bits
   */
  public static String show( short i, final int num_bits ) {
    int num_chars = 16 / num_bits;

    if ( 16 % num_bits == 0 ) {
      --num_chars;
    }

    final char[] buf = new char[num_chars + 1];
    final int radix = 1 << num_bits;
    final long mask = radix - 1;

    for ( int charPos = num_chars; charPos >= 0; --charPos ) {
      buf[charPos] = ByteUtil.digits[(int)( i & mask )];
      i >>>= num_bits;
    }

    return new String( buf );
  }




  /**
   * Convert the entered number into a string.
   *
   * <p>This works based upon number of bits. This can be used to produce many
   * standard outputs.
   * <pre>
   *   1 bit  = binary, base two
   *   2 bits = base four
   *   3 bits = octal, base eight
   *   4 bits = hex, base sixteen
   *   5 bits = base 32
   * </pre>
   * Leading digits are included.
   *
   * @param i the number to show
   * @param num_bits number of bits to consider
   *
   * @return string representation of the number represented by the bits
   */
  public static String show( byte i, final int num_bits ) {
    int num_chars = 8 / num_bits;

    if ( 8 % num_bits == 0 ) {
      --num_chars;
    }

    final char[] buf = new char[num_chars + 1];
    final int radix = 1 << num_bits;
    final long mask = radix - 1;

    for ( int charPos = num_chars; charPos >= 0; --charPos ) {
      buf[charPos] = ByteUtil.digits[(int)( i & mask )];
      i >>>= num_bits;
    }

    return new String( buf );
  }




  /**
   * Return this byte as a binary string.
   *
   * @param octet the byte value to represent
   *
   * @return string representation of the octet 
   */
  public static String show( final byte octet ) {
    return ByteUtil.show( octet, 1 );
  }




  /**
   * This will dump one long into a string for examination.
   *
   * <p>Dump produces three lines of output including an index, binary,
   * decimal, hex, and character output.
   *
   * <p>This is an example of the output:<pre>
   * +000:00--+001:01--+002:02--+003:03--+004:04--+005:05--+006:06--+007:07--+
   * |00000000|00000000|00000001|00000111|11100110|10010101|01100100|10101111|
   * |000:00: |000:00: |001:01: |007:07: |230:e6: |149:95: |100:64:d|175:af: |
   * +--------+--------+--------+--------+--------+--------+--------+--------+
   * </pre>
   * 
   * @param data the number to format.
   *
   * @return formatted dump of the data
   */
  public static String dump( final long data ) {
    final byte[] temp = new byte[8];
    ByteUtil.overlay( data, temp, 0 );

    return ByteUtil.dump( temp );
  }




  /**
   * This will dump one integer into a string for examination.
   *
   * <p>Dump produces three lines of output including an index, binary,
   * decimal, hex, and character output.
   *
   * <p>This is an example of the output:<pre>
   * +000:00--+001:01--+002:02--+003:03--+
   * |00000000|00010000|00100000|01000000|
   * |000:00: |016:10: |032:20: |064:40:@|
   * +--------+--------+--------+--------+
   * </pre>
   * 
   * @param data the number to format.
   *
   * @return formatted dump of the data
   */
  public static String dump( final int data ) {
    final byte[] temp = new byte[4];
    ByteUtil.overlay( data, temp, 0 );

    return ByteUtil.dump( temp );
  }




  /**
   * This will dump one short into a string for examination.
   *
   * <p>Dump produces three lines of output including an index, binary,
   * decimal, hex, and character output.
   *
   * <p>This is an example of the output:<pre>
   * +000:00--+001:01--+
   * |00000000|00010000|
   * |000:00: |016:10: |
   * +--------+--------+
   * </pre>
   * 
   * @param data the byte array to format.
   *
   * @return formatted dump of the data
   */
  public static String dump( final short data ) {
    final byte[] temp = new byte[2];
    ByteUtil.overlay( data, temp, 0 );

    return ByteUtil.dump( temp );
  }




  /**
   * This will dump one byte into a string for examination
   *
   * <p>The byte will be compartmentalized where the cell contains the offset
   * (always zero) into the byte (first line), the binary representation of
   * the byte (second line), and the decimal, hex, and corresponding character
   * (third line).
   *
   * <p>This is an example of the output:<pre>
   * +000:00--+001:01--+002:02--+003:03--+004:04--+
   * |00000000|00010000|00100000|01000000|11111111|
   * |000:00: |016:10: |032:20: |064:40:@|255:ff: |
   * +--------+--------+--------+--------+--------+
   * </pre>
   * 
   * @param data the byte array to format.
   *
   * @return formatted dump of the data
   */
  public static String dump( final byte data ) {
    final byte[] temp = new byte[1];
    temp[0] = data;

    return ByteUtil.dump( temp );
  }




  /**
   * This will dump up to 255 bytes of an array into a string for examination.
   *
   * <p>The individual bytes will be compartmentalized where each cell contains
   * the offset into the byte array (first line), the binary representation of
   * the byte (second line), and the decimal, hex, and corresponding character
   * (third line).
   *
   * <p>This is an example of the output:<pre>
   * +000:00--+001:01--+002:02--+003:03--+004:04--+
   * |00000000|00010000|00100000|01000000|11111111|
   * |000:00: |016:10: |032:20: |064:40:@|255:ff: |
   * +--------+--------+--------+--------+--------+
   * </pre>
   * 
   * @param data the byte array to format.
   *
   * @return formatted dump of the data
   */
  public static String dump( final byte[] data ) {
    if ( ( data == null ) || ( data.length == 0 ) ) {
      return "";
    }

    return ByteUtil.dump( data, ( data.length > 256 ) ? 256 : data.length );
  }




  /**
   * This will dump bytes of an array into a string for examination.
   *
   * <p>The individual bytes will be compartmentalized where each cell contains
   * the offset into the byte array (first line), the binary representation of
   * the byte (second line), and the decimal, hex, and corresponding character
   * (third line).
   *
   * <p>This is an example of the output:<pre>
   * +000:00--+001:01--+002:02--+003:03--+004:04--+
   * |00000000|00010000|00100000|01000000|11111111|
   * |000:00: |016:10: |032:20: |064:40:@|255:ff: |
   * +--------+--------+--------+--------+--------+
   * </pre>
   * 
   * @param data the byte array to format.
   * @param size
   *
   * @return formatted dump of the data
   */
  public static String dump( final byte[] data, int size ) {
    // First, a little error checking
    if ( ( data == null ) || ( data.length == 0 ) ) {
      return "";
    }

    if ( data.length < size ) {
      size = data.length;
    }

    final java.text.DecimalFormat pf = new java.text.DecimalFormat( "000" );
    final StringBuffer result = new StringBuffer();
    final StringBuffer line1 = new StringBuffer( "+" );
    final StringBuffer line2 = new StringBuffer( "|" );
    final StringBuffer line3 = new StringBuffer( "|" );
    final StringBuffer line4 = new StringBuffer( "+" );
    int mark = 0;

    while ( mark < size ) {
      // Get the unsigned value of this byte
      final int value = ( data[mark] & 0xFF );

      // Print the little box that represents this byte
      line1.append( pf.format( mark ) + ":" + ByteUtil.byteToHex( (byte)mark ) + "--+" );
      line2.append( ByteUtil.show( data[mark] ) + "|" );
      line3.append( pf.format( value ) + ":" + ByteUtil.byteToHex( data[mark] ) + ":" + ByteUtil.byteToASCII( data[mark] ) + "|" );
      line4.append( "--------+" );

      mark++;

      if ( ( mark > 0 ) && ( ( mark % 8 ) == 0 ) ) {
        line1.append( "\r\n" );
        line2.append( "\r\n" );
        line3.append( "\r\n" );
        line4.append( "\r\n" );
        result.append( line1.toString() + line2.toString() + line3.toString() + line4.toString() );
        line1.delete( 0, line1.length() );
        line2.delete( 0, line2.length() );
        line3.delete( 0, line3.length() );
        line4.delete( 0, line4.length() );

        if ( mark < size ) {
          line1.append( "+" );
          line2.append( "|" );
          line3.append( "|" );
          line4.append( "+" );
        }
      }
    }

    if ( line1.length() > 0 ) {
      line1.append( "\r\n" );
      line2.append( "\r\n" );
      line3.append( "\r\n" );
      // line4.append( "\r\n" );
      result.append( line1.toString() + line2.toString() + line3.toString() + line4.toString() );
    }

    return result.toString();
  }




  /**
   * Converts a long to a little-endian four-byte array
   *
   * @param val
   *
   * @return array of byte representing little-endian encoding of the value
   */
  public final static byte[] intToLittleEndian( int val ) {
    final byte[] b = new byte[4];

    for ( int i = 0; i < 4; i++ ) {
      b[i] = (byte)( val % 256 );
      val = val / 256;
    }

    return b;
  }




  /**
   * Converts a long to a little-endian two-byte array
   *
   * @param val
   *
   * @return array of byte representing little-endian encoding of the value
   */
  public final static byte[] shortToLittleEndian( short val ) {
    final byte[] b = new byte[2];

    for ( int i = 0; i < 2; i++ ) {
      b[i] = (byte)( val % 256 );
      val = (short)( val / 256 );
    }

    return b;
  }




  /**
   * Converts a little-endian four-byte array to a long, represented as a
   * double, since long is signed.
   *
   * @param b the byte to encode
   *
   * @return array of byte representing little-endian encoding of the value
   */
  public final static double vax_to_long( final byte[] b ) {
    return ByteUtil.fixByte( b[0] ) + ( ByteUtil.fixByte( b[1] ) * 256 ) + ( ByteUtil.fixByte( b[2] ) * ( 256 ^ 2 ) ) + ( ByteUtil.fixByte( b[3] ) * ( 256 ^ 3 ) );
  }




  /**
   * Converts a little-endian four-byte array to a short, represented as an int,
   * since short is signed.
   *
   * @param b the array of bytes to encode
   *
   * @return array of byte representing little-endian encoding of the value
   */
  public final static int vax_to_short( final byte[] b ) {
    return ( ByteUtil.fixByte( b[0] ) + ( ByteUtil.fixByte( b[1] ) * 256 ) );
  }




  /**
   * bytes are signed; let's fix them...
   *
   * @param b the byte to convert
   *
   * @return the byte encoded as an unsigned value
   */
  public final static short fixByte( final byte b ) {
    if ( b < 0 ) {
      return (short)( b + 256 );
    }

    return b;
  }




  /**
   * Convert the data into base64 encoding.
   *
   * @param data the data to encode
   *
   * @return string representing base64 encoding of the data
   */
  public static String toBase64( final byte[] data ) {
    final int i = data.length / 3;
    final int j = data.length % 3;
    final byte barray[] = new byte[( ( j == 0 ) ? i : i + 1 ) * 4];
    int k = 0;
    int l = 0;

    for ( int i1 = 0; i1 < i; i1++ ) {
      barray[k++] = ByteUtil.toBase64( (byte)( data[l] >> 2 & 0x3f ) );
      barray[k++] = ByteUtil.toBase64( (byte)( ( data[l] & 3 ) << 4 | ( data[l + 1] & 0xf0 ) >> 4 & 0xf ) );
      barray[k++] = ByteUtil.toBase64( (byte)( ( data[l + 1] & 0xf ) << 2 | ( data[l + 2] & 0xc0 ) >> 6 & 3 ) );
      barray[k++] = ByteUtil.toBase64( (byte)( data[l + 2] & 0x3f ) );
      l += 3;
    }

    if ( j == 1 ) {
      barray[k++] = ByteUtil.toBase64( (byte)( data[l] >> 2 & 0x3f ) );
      barray[k++] = ByteUtil.toBase64( (byte)( ( data[l] & 3 ) << 4 ) );
      barray[k++] = 61;
      barray[k++] = 61;
    } else {
      if ( j == 2 ) {
        barray[k++] = ByteUtil.toBase64( (byte)( data[l] >> 2 & 0x3f ) );
        barray[k++] = ByteUtil.toBase64( (byte)( ( data[l] & 3 ) << 4 | ( data[l + 1] & 0xf0 ) >> 4 & 0xf ) );
        barray[k++] = ByteUtil.toBase64( (byte)( ( data[l + 1] & 0xf ) << 2 ) );
        barray[k++] = 61;
      }
    }

    return new String( barray );
  }




  /**
   * Return the length of the data expected from the given base64 encoded string.
   *
   * @param string base64 encoded data
   *
   * @return the number of data bytes expected from the given string. 
   */
  public static int getBase64Length( final String string ) {
    int len = ( string.length() / 4 ) * 3;

    if ( ( string.length() > 0 ) && ( string.charAt( string.length() - 1 ) == '=' ) ) {
      len -= ( string.charAt( string.length() - 2 ) != '=' ) ? 1 : 2;
    }

    return len;
  }




  /**
   * Decode the data from the given base64 encoded string
   *
   * @param text The string to decode
   *
   * @return the data represented by the encoded text.
   */
  public static byte[] fromBase64( final String text ) {
    final byte[] data = text.getBytes();
    int i = data.length / 4;
    int j = i * 3;
    byte octet = 0;

    if ( ( data.length > 0 ) && ( data[data.length - 1] == 61 ) ) {
      octet = ( (byte)( ( data[data.length - 2] != 61 ) ? 1 : 2 ) );
      j -= octet;

      i--;
    }

    final byte barray[] = new byte[j];
    int k = 0;
    int l = 0;

    for ( int i1 = 0; i1 < i; i1++ ) {
      final byte byte3 = ByteUtil.fromBase64( data[l++] );
      final byte byte6 = ByteUtil.fromBase64( data[l++] );
      final byte byte8 = ByteUtil.fromBase64( data[l++] );
      final byte byte9 = ByteUtil.fromBase64( data[l++] );
      barray[k++] = (byte)( byte3 << 2 | ( byte6 & 0x30 ) >> 4 );
      barray[k++] = (byte)( ( byte6 & 0xf ) << 4 | ( byte8 & 0x3c ) >> 2 );
      barray[k++] = (byte)( ( byte8 & 3 ) << 6 | byte9 & 0x3f );
    }

    if ( octet == 1 ) {
      final byte byte1 = ByteUtil.fromBase64( data[l++] );
      final byte byte4 = ByteUtil.fromBase64( data[l++] );
      final byte byte7 = ByteUtil.fromBase64( data[l++] );
      barray[k++] = (byte)( byte1 << 2 | ( byte4 & 0x30 ) >> 4 );
      barray[k++] = (byte)( ( byte4 & 0xf ) << 4 | ( byte7 & 0x3c ) >> 2 );
    } else {
      if ( octet == 2 ) {
        final byte byte2 = ByteUtil.fromBase64( data[l++] );
        final byte byte5 = ByteUtil.fromBase64( data[l++] );
        barray[k++] = (byte)( byte2 << 2 | ( byte5 & 0x30 ) >> 4 );
      }
    }

    return barray;
  }




  /**
   * convert the byte to base64 encoding
   *
   * @param octet the octet to encode
   *
   * @return the base64 encoded representation of the given byte
   */
  public static byte toBase64( final byte octet ) {
    if ( octet <= 25 ) {
      return (byte)( 65 + octet );
    }

    if ( octet <= 51 ) {
      return (byte)( ( 97 + octet ) - 26 );
    }

    if ( octet <= 61 ) {
      return (byte)( ( 48 + octet ) - 52 );
    }

    if ( octet == 62 ) {
      return 43;
    }

    return ( (byte)( ( octet != 63 ) ? 61 : 47 ) );
  }




  /**
   * Decode the base64 encoded byte
   *
   * @param octet the octet to decode
   *
   * @return the decoded byte
   */
  public static byte fromBase64( final byte octet ) {
    if ( ( octet >= 65 ) && ( octet <= 90 ) ) {
      return (byte)( octet - 65 );
    }

    if ( ( octet >= 97 ) && ( octet <= 122 ) ) {
      return (byte)( ( 26 + octet ) - 97 );
    }

    if ( ( octet >= 48 ) && ( octet <= 57 ) ) {
      return (byte)( ( 52 + octet ) - 48 );
    }

    if ( octet == 43 ) {
      return 62;
    }

    return ( (byte)( ( octet != 47 ) ? 64 : 63 ) );
  }




  /**
   * Swap bytes
   *
   * @param s the short value to swap
   * 
   * @return The byte swapped version of <code>s</code>.
   */
  public static short swapBytes( final short s ) {
    return (short)( ( s << 8 ) | ( ( s >> 8 ) & 0x00ff ) );
  }




  /**
   * Swap bytes
   *
   * @param i the integer value to swap
   * 
   * @return The byte swapped version of <code>i</code>.
   */
  public static int swapBytes( final int i ) {
    return ( i << 24 ) | ( ( i << 8 ) & 0x00ff0000 ) | ( i >>> 24 ) | ( ( i >> 8 ) & 0x0000ff00 );
  }




  /**
   * Works like String.substring()
   *
   * @param source Source of the byte array
   * @param start starting byte
   * @param length length of the array to return
   *
   * @return the portion of the byte array specified.
   */
  public static byte[] subArray( final byte[] source, final int start, final int length ) {
    if ( start < 0 ) {
      throw new ArrayIndexOutOfBoundsException( "Start index: " + start );
    }

    if ( source == null ) {
      throw new IllegalArgumentException( "Source array was null" );
    }

    if ( ( start + length ) > source.length ) {
      throw new ArrayIndexOutOfBoundsException( "length index: " + length );
    }

    final byte[] retval = new byte[length];
    System.arraycopy( source, start, retval, 0, length );

    return retval;
  }




  /**
   * Read binary from the input and write the Base64 characters to the output.
   *
   * @param inputstream That to convert
   * @param outputstream Where to send the converted bytes
   *
   * @throws IOException if anything goes wrong
   */
  public static void toBase64( final InputStream inputstream, final OutputStream outputstream ) throws IOException {
    int i = 0;
    final byte outbound[] = new byte[3];
    final byte inbound[] = new byte[1];

    do {
      if ( inputstream.read( inbound ) != 1 ) {
        break;
      }

      outbound[i++] = inbound[0];

      if ( i == 3 ) {
        final byte byte0 = ByteUtil.toBase64( (byte)( outbound[0] >> 2 & 0x3f ) );
        final byte byte3 = ByteUtil.toBase64( (byte)( ( outbound[0] & 3 ) << 4 | ( outbound[1] & 0xf0 ) >> 4 & 0xf ) );
        final byte byte6 = ByteUtil.toBase64( (byte)( ( outbound[1] & 0xf ) << 2 | ( outbound[2] & 0xc0 ) >> 6 & 3 ) );
        final byte byte8 = ByteUtil.toBase64( (byte)( outbound[2] & 0x3f ) );

        outputstream.write( new byte[] { byte0, byte3, byte6, byte8 } );

        i = 0;
      }
    }
    while ( true );

    if ( i == 1 ) {
      final byte byte1 = ByteUtil.toBase64( (byte)( outbound[0] >> 2 & 0x3f ) );
      final byte byte4 = ByteUtil.toBase64( (byte)( ( outbound[0] & 3 ) << 4 ) );

      outputstream.write( new byte[] { byte1, byte4, 61, 61 } );
    } else {
      if ( i == 2 ) {
        final byte byte2 = ByteUtil.toBase64( (byte)( outbound[0] >> 2 & 0x3f ) );
        final byte byte5 = ByteUtil.toBase64( (byte)( ( outbound[0] & 3 ) << 4 | ( outbound[1] & 0xf0 ) >> 4 & 0xf ) );
        final byte byte7 = ByteUtil.toBase64( (byte)( ( outbound[1] & 0xf ) << 2 ) );

        outputstream.write( new byte[] { byte2, byte5, byte7, 61 } );
      }
    }
  }




  /**
   * Read Base64 characters from the input and write the decoded bytes to the
   * output.
   *
   * @param inputstream
   * @param outputstream
   *
   * @throws IOException
   */
  public static void fromBase64( final InputStream inputstream, final OutputStream outputstream ) throws IOException {
    int i = 0;
    final byte inbound[] = new byte[4];
    final byte outbound[] = new byte[1];

    do {
      if ( inputstream.read( outbound ) != 1 ) {
        break;
      }

      if ( Character.isLetterOrDigit( (char)outbound[0] ) || ( (char)outbound[0] == '+' ) || ( (char)outbound[0] == '/' ) ) {
        inbound[i++] = outbound[0];
      }

      if ( (char)outbound[0] == '=' ) {
        if ( i == 2 ) {
          final byte byte0 = ByteUtil.fromBase64( inbound[0] );
          final byte byte3 = ByteUtil.fromBase64( inbound[1] );

          outputstream.write( new byte[] { (byte)( byte0 << 2 | ( byte3 & 0x30 ) >> 4 ) } );
        } else {
          if ( i == 3 ) {
            final byte byte1 = ByteUtil.fromBase64( inbound[0] );
            final byte byte4 = ByteUtil.fromBase64( inbound[1] );
            final byte byte6 = ByteUtil.fromBase64( inbound[2] );
            final byte byte8 = (byte)( byte1 << 2 | ( byte4 & 0x30 ) >> 4 );
            final byte byte10 = (byte)( ( byte4 & 0xf ) << 4 | ( byte6 & 0x3c ) >> 2 );

            outputstream.write( new byte[] { byte8, byte10 } );
          }
        }

        break;
      }

      if ( i == 4 ) {
        final byte byte2 = ByteUtil.fromBase64( inbound[0] );
        final byte byte5 = ByteUtil.fromBase64( inbound[1] );
        final byte byte7 = ByteUtil.fromBase64( inbound[2] );
        final byte byte9 = ByteUtil.fromBase64( inbound[3] );
        final byte byte11 = (byte)( byte2 << 2 | ( byte5 & 0x30 ) >> 4 );
        final byte byte12 = (byte)( ( byte5 & 0xf ) << 4 | ( byte7 & 0x3c ) >> 2 );
        final byte byte13 = (byte)( ( byte7 & 3 ) << 6 | byte9 & 0x3f );

        outputstream.write( new byte[] { byte11, byte12, byte13 } );

        i = 0;
      }
    }
    while ( true );
  }




  /**
   * Convert the hex representation to bytes of data
   *
   * @param hex the hex-encoded string to convert
   *
   * @return the byte array represented by the hex encoded string.
   */
  public static byte[] hexToBytes( final String hex ) {
    byte[] retval = null;

    if ( ( hex != null ) && ( hex.length() > 0 ) ) {
      retval = new byte[( hex.length() / 2 ) + ( hex.length() % 2 )];

      for ( int i = 0; i < hex.length(); i++ ) {

        if ( i + 1 < hex.length() ) {
          retval[( i + 1 ) / 2] = (byte)Integer.parseInt( hex.substring( i, i + 2 ), 16 );

          i++;
        } else {
          retval[( i + 1 ) / 2] = (byte)Integer.parseInt( hex.substring( i ), 16 );
        }
      }
    }

    return retval;
  }




  /**
   * Return a byte (1 octet) from an unsigned short
   *
   * @param value Short 0-255 (anything higher will wrap/overflow)
   *
   * @return a byte representing the short value to render
   */
  public static byte renderShortByte( final short value ) {
    return (byte)( ( value >>> 0 ) & 0xFF );
  }




  /**
   * Get a signed short (S8) from 1 byte in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the unsigned short number (-128 to 127) stored at the offset.
   */
  public static short retrieveShortByte( final byte[] buf, final int offset ) {
    return buf[offset];
  }




  /**
   * Get an unsigned short (U8) from 1 byte in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the unsigned short number (0 to 255) stored at the offset.
   */
  public static short retrieveUnsignedShortByte( final byte[] buf, final int offset ) {
    return (short)( buf[offset] & 0xFF );
  }




  /**
   * Return a 2-byte array from a short value.
   *
   * <p>This encodes the short in network byte order.
   *
   * @param value Short from -32,768 to 32,767 to render into the 2-byte array
   *
   * @return a 2-byte array representing the S16 short value
   */
  public static byte[] renderShort( final short value ) {
    final byte[] retval = new byte[2];
    retval[0] = (byte)( ( value >>> 8 ) & 0xFF );
    retval[1] = (byte)( ( value >>> 0 ) & 0xFF );

    return retval;
  }




  /**
   * Get a signed short (S16) from 2 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed short number (-32,768 to 32,767) stored at the offset.
   */
  public static short retrieveShort( final byte[] buf, int offset ) {
    return (short)( (short)( ( buf[offset++] & 0xff ) << 8 ) | (short)( buf[offset++] & 0xff ) );
  }




  /**
   * Get an unsigned short (U16) from 2 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the unsigned short number (0 to 65,535) stored at the offset.
   */
  public static int retrieveUnsignedShort( final byte[] buf, int offset ) {
    return ( ( ( buf[offset++] & 0xff ) << 8 ) | ( buf[offset++] & 0xff ) );
  }




  /**
   * Return a 4-byte array from an integer value.
   *
   * <p>This encodes the integer in network byte order.
   *
   * @param value integer from -2,147,483,648 to 2,147,483,647 to render into the 4-byte array
   *
   * @return a 4-byte array representing the S32 integer value
   */
  public static byte[] renderInt( final int value ) {
    final byte[] retval = new byte[4];
    retval[0] = (byte)( ( value >>> 24 ) & 0xFF );
    retval[1] = (byte)( ( value >>> 16 ) & 0xFF );
    retval[2] = (byte)( ( value >>> 8 ) & 0xFF );
    retval[3] = (byte)( ( value >>> 0 ) & 0xFF );

    return retval;
  }




  /**
   * Get a signed integer (S32) from 4 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed integer number (-2,147,483,648 to 2,147,483,647) stored
   *         at the offset.
   */
  public static int retrieveInt( final byte[] buf, int offset ) {
    return ( ( buf[offset++] & 0xff ) << 24 ) | ( ( buf[offset++] & 0xff ) << 16 ) | ( ( buf[offset++] & 0xff ) << 8 ) | ( buf[offset++] & 0xff );
  }




  /**
   * Get an unsigned integer (U32) from 4 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed integer number (0 to 4,294,967,295) stored at the
   *         offset.
   */
  public static long retrieveUnsignedInt( final byte[] buf, int offset ) {
    return ( ( (long)buf[offset++] & 0xff ) << 24 ) | ( ( (long)buf[offset++] & 0xff ) << 16 ) | ( ( (long)buf[offset++] & 0xff ) << 8 ) | ( (long)buf[offset++] & 0xff );
  }




  /**
   * Return an 8-byte array from a long value.
   *
   * <p>This encodes the long in network byte order.
   *
   * @param value long from -9,223,372,036,854,775,808 to
   *        9,223,372,036,854,775,807 to render into the 8-byte array
   *
   * @return a 8-byte array representing the S64 long value
   */
  public static byte[] renderLong( final long value ) {
    final byte[] retval = new byte[8];
    retval[0] = (byte)( ( value >>> 56 ) & 0xFF );
    retval[1] = (byte)( ( value >>> 48 ) & 0xFF );
    retval[2] = (byte)( ( value >>> 40 ) & 0xFF );
    retval[3] = (byte)( ( value >>> 32 ) & 0xFF );
    retval[4] = (byte)( ( value >>> 24 ) & 0xFF );
    retval[5] = (byte)( ( value >>> 16 ) & 0xFF );
    retval[6] = (byte)( ( value >>> 8 ) & 0xFF );
    retval[7] = (byte)( ( value >>> 0 ) & 0xFF );

    return retval;
  }




  /**
   * Get a signed long (S64) from 8 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed integer number (-9,223,372,036,854,775,808 to
   *       9,223,372,036,854,775,807) stored at the offset.
   */
  public static long retrieveLong( final byte[] buf, int offset ) {
    return ( ( (long)buf[offset++] & 0xff ) << 56 ) | ( ( (long)buf[offset++] & 0xff ) << 48 ) | ( ( (long)buf[offset++] & 0xff ) << 40 ) | ( ( (long)buf[offset++] & 0xff ) << 32 ) | ( ( (long)buf[offset++] & 0xff ) << 24 ) | ( ( (long)buf[offset++] & 0xff ) << 16 ) | ( ( (long)buf[offset++] & 0xff ) << 8 ) | ( (long)buf[offset++] & 0xff );
  }




  /**
   * Get an unsigned integer (U64) from 8 bytes in a byte[] buffer.
   *
   * <p>Range is from 0 to over 18 quintillion. Do you <em>really</em> need a
   * number that big?
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed long number (0 to 18,446,744,073,709,551,615) stored at
   *         the offset.
   */
  public static BigInteger retrieveBigInteger( final byte[] buf, final int offset ) {
    final byte[] chunk = new byte[8];
    System.arraycopy( buf, offset, chunk, 0, 8 );

    return new BigInteger( 1, chunk );
  }




  /**
   * Return an 8 byte array from a BigInteger value.
   * 
   * Only 8 bytes will be supported, enough for a U64 value of over 18 
   * quintillion.
   * 
   * @param bint the big integer object to render
   * 
   * @return an 8 byte array representing the big integer.
   */
  public static byte[] renderBigInteger( final BigInteger bint ) {
    final byte[] retval = new byte[8];
    byte[] arry = bint.toByteArray();

    if ( arry.length > retval.length )
      System.arraycopy( arry, arry.length - retval.length, retval, 0, retval.length );
    else if ( arry.length < retval.length )
      System.arraycopy( arry, 0, retval, retval.length - arry.length, arry.length );
    else
      System.arraycopy( arry, 0, retval, 0, retval.length );

    return retval;
  }




  /**
   * Return a 4-byte array from a float value.
   *
   * <p>This encodes the float in network byte order.
   *
   * @param value float from +/-1.4013e-45 to +/-3.4028e+38 to render into the
   *        4-byte array
   *
   * @return a 4-byte array representing the float value
   */
  public static byte[] renderFloat( final float value ) {
    return ByteUtil.renderInt( Float.floatToIntBits( value ) );
  }




  /**
   * Get a floating point value from 4 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed floating point number (+/-1.4013e-45 to +/-3.4028e+38)
   *       stored at the offset.
   */
  public static float retrieveFloat( final byte[] buf, final int offset ) {
    return Float.intBitsToFloat( ByteUtil.retrieveInt( buf, offset ) );
  }




  /**
   * Return an 8-byte array from a double precision value.
   *
   * <p>This encodes the long in network byte order.
   *
   * @param value double precision value from +/-4.9406e-324 to +/-1.7977e+308 to
   *        render into the 8-byte array
   * @return a 8-byte array representing the double precision value
   */
  public static byte[] renderDouble( final double value ) {
    return ByteUtil.renderLong( Double.doubleToLongBits( value ) );
  }




  /**
   * Get a double precision value from 8 bytes in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the number.
   *
   * @return the signed floating point number (+/-4.9406e-324 to +/-1.7977e+308)
   *         stored at the offset.
   */
  public static double retrieveDouble( final byte[] buf, final int offset ) {
    return Double.longBitsToDouble( ByteUtil.retrieveLong( buf, offset ) );
  }




  /**
   * Return a byte representing a boolean value.
   *
   * @param value either true or false
   *
   * @return a single byte representing 1 for true and 0 for false
   */
  public static byte renderBooleanByte( final boolean value ) {
    return (byte)( value ? 1 : 0 );
  }




  /**
   * Return a 1-byte array representing a boolean value.
   *
   * @param value either true or false
   *
   * @return a single byte representing 1 for true and 0 for false
   */
  public static byte[] renderBoolean( final boolean value ) {
    final byte[] retval = new byte[1];
    retval[0] = (byte)( value ? 1 : 0 );

    return retval;
  }




  /**
   * Get a boolean value from a single byte in a byte[] buffer.
   *
   * @param buf The buffer from which to retrieve the value
   * @param offset from which to get the value.
   *
   * @return True if the value in the byte is &gt; 0 false otherwise.
   */
  public static boolean retrieveBoolean( final byte[] buf, final int offset ) {
    if ( buf[offset] > 0 ) {
      return true;
    }

    return false;
  }




  /**
   * Return a 8-byte array representing the epoch time in milliseconds for the
   * given date relative to GMT.
   *
   * <p>This encodes the time as a long in network byte order after converting it
   * to GMT and allowing for DST.
   *
   * @param value any valid Date object
   * 
   * @return a 8-byte array representing the epoch time in milliseconds, if
   *       value is null, then the date will be encoded as all zeros.
   */
  public static byte[] renderDate( final Date value ) {
    if ( value != null ) {
      // Handy how Date handles all the GMT and DST conversions, ain't it?
      return ByteUtil.renderLong( value.getTime() );
    } else {
      return ByteUtil.renderLong( 0 );
    }
  }




  /**
   * Render 8 octets as a network byte order long value and convert it to a 
   * Date object.
   *
   * <p>Assume the value represents the number of milliseconds in epoch time in 
   * GMT.
   *
   * @param buf The source of the bytes.
   * @param offset The offset to use.
   * 
   * @return A date object representing the time in the 8-octet field.
   */
  public static Date retrieveDate( final byte[] buf, final int offset ) {
    return new Date( ByteUtil.retrieveLong( buf, offset ) );
  }




  /**
   * Render a UUID into a 16 byte array.
   * 
   * <p>This simply creates a 16 byte array, places the most significant bits 
   * into the first 8 octets and the least significant bits in the last 8.
   *  
   * @param value The UUID to encode.
   * 
   * @return the 16 bytes representing the UUID.
   */
  public static byte[] renderUUID( final UUID value ) {
    final byte[] retval = new byte[16];
    System.arraycopy( renderLong( value.getMostSignificantBits() ), 0, retval, 0, 8 );
    System.arraycopy( renderLong( value.getLeastSignificantBits() ), 0, retval, 8, 8 );
    return retval;
  }




  // TODO: FixMe! This only creates type 3 UUIDs and does not support our renderUUID method
  public static UUID retrieveUUID( final byte[] buf, final int offset ) {
    byte[] data = new byte[16];
    System.arraycopy( buf, offset, data, 0, data.length );
    UUID retval = UUID.nameUUIDFromBytes( data );
    return retval;
  }




  /**
   * Retrieve an ASCII encoded string from the given buffer.
   * 
   * @param buf The byte array from which we read.
   * @param offset The position in the byte array where the string begins.
   * @param length The length of the string in bytes.
   * 
   * @return The specified section of the given byte array decoded in ASCII.
   */
  public static String retrieveAsciiString( final byte[] buf, final int offset, final int length ) {
    final byte[] textData = new byte[length];
    System.arraycopy( buf, offset, textData, 0, textData.length );
    return new String( textData );
  }

}
