package coyote.commons.network.mqtt.network.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;


public class WebSocketFrame {

  public static final int frameLengthOverhead = 6;




  /**
   * Appends the Fin flag and the OpCode
   * @param buffer
   * @param opcode
   * @param fin
   */
  public static void appendFinAndOpCode( final ByteBuffer buffer, final byte opcode, final boolean fin ) {
    byte b = 0x00;
    // Add Fin flag
    if ( fin ) {
      b |= 0x80;
    }
    //RSV 1,2,3 aren't important

    // Add opcode
    b |= opcode & 0x0F;
    buffer.put( b );
  }




  /**
   * Appends the Length of the payload to the buffer
   * @param buffer
   * @param length
   * @param b
   */
  private static void appendLength( final ByteBuffer buffer, final int length, final boolean masked ) {

    if ( length < 0 ) {
      throw new IllegalArgumentException( "Length cannot be negative" );
    }

    final byte b = ( masked ? (byte)0x80 : 0x00 );
    if ( length > 0xFFFF ) {
      buffer.put( (byte)( b | 0x7F ) );
      buffer.put( (byte)0x00 );
      buffer.put( (byte)0x00 );
      buffer.put( (byte)0x00 );
      buffer.put( (byte)0x00 );
      buffer.put( (byte)( ( length >> 24 ) & 0xFF ) );
      buffer.put( (byte)( ( length >> 16 ) & 0xFF ) );
      buffer.put( (byte)( ( length >> 8 ) & 0xFF ) );
      buffer.put( (byte)( length & 0xFF ) );
    } else if ( length >= 0x7E ) {
      buffer.put( (byte)( b | 0x7E ) );
      buffer.put( (byte)( length >> 8 ) );
      buffer.put( (byte)( length & 0xFF ) );
    } else {
      buffer.put( (byte)( b | length ) );
    }
  }




  /**
   * Appends the Length and Mask to the buffer
   * @param buffer
   * @param length
   * @param mask
   */
  public static void appendLengthAndMask( final ByteBuffer buffer, final int length, final byte mask[] ) {
    if ( mask != null ) {
      appendLength( buffer, length, true );
      buffer.put( mask );
    } else {
      appendLength( buffer, length, false );
    }
  }




  /**
   * Generates a random masking key
   * Nothing super secure, but enough
   * for websockets.
   * @return ByteArray containing the key;
   */
  public static byte[] generateMaskingKey() {
    final Random randomGenerator = new Random();
    final int a = randomGenerator.nextInt( 255 );
    final int b = randomGenerator.nextInt( 255 );
    final int c = randomGenerator.nextInt( 255 );
    final int d = randomGenerator.nextInt( 255 );
    return new byte[] { (byte)a, (byte)b, (byte)c, (byte)d };
  }

  private byte opcode;

  private boolean fin;

  private byte payload[];

  private boolean closeFlag = false;




  /**
   * Initialize a new WebSocketFrame
   * @param opcode
   * @param fin
   * @param payload
   */
  public WebSocketFrame( final byte opcode, final boolean fin, final byte[] payload ) {
    this.opcode = opcode;
    this.fin = fin;
    this.payload = payload;
  }




  /**
   * Initialize WebSocketFrame from raw Data
   * @param rawFrame
   */
  public WebSocketFrame( final byte[] rawFrame ) {

    final ByteBuffer buffer = ByteBuffer.wrap( rawFrame );

    // First Byte: Fin, Reserved, Opcode
    byte b = buffer.get();
    setFinAndOpCode( b );

    // Second Byte Masked & Initial Length
    b = buffer.get();
    final boolean masked = ( ( b & 0x80 ) != 0 );
    int payloadLength = (byte)( 0x7F & b );
    int byteCount = 0;
    if ( payloadLength == 0X7F ) {
      // 8 Byte Extended payload length
      byteCount = 8;
    } else if ( payloadLength == 0X7E ) {
      // 2 bytes extended payload length
      byteCount = 2;
    }

    // Decode the extended payload length
    while ( --byteCount > 0 ) {
      b = buffer.get();
      payloadLength |= ( b & 0xFF ) << ( 8 * byteCount );
    }

    // Get the Masking key if masked
    byte maskingKey[] = null;
    if ( masked ) {
      maskingKey = new byte[4];
      buffer.get( maskingKey, 0, 4 );
    }
    payload = new byte[payloadLength];
    buffer.get( payload, 0, payloadLength );

    // Demask payload if needed
    if ( masked ) {
      for ( int i = 0; i < payload.length; i++ ) {
        payload[i] ^= maskingKey[i % 4];
      }
    }
    return;
  }




  /**
   * Takes an input stream and parses it into a Websocket frame.
   * @param input
   * @throws IOException
   */
  public WebSocketFrame( final InputStream input ) throws IOException {
    final byte firstByte = (byte)input.read();
    setFinAndOpCode( firstByte );
    if ( opcode == 2 ) {
      byte maskLengthByte = (byte)input.read();
      final boolean masked = ( ( maskLengthByte & 0x80 ) != 0 );
      int payloadLength = (byte)( 0x7F & maskLengthByte );
      int byteCount = 0;
      if ( payloadLength == 0X7F ) {
        // 8 Byte Extended payload length
        byteCount = 8;
      } else if ( payloadLength == 0X7E ) {
        // 2 bytes extended payload length
        byteCount = 2;
      }

      // Decode the payload length
      if ( byteCount > 0 ) {
        payloadLength = 0;
      }
      while ( --byteCount >= 0 ) {
        maskLengthByte = (byte)input.read();
        payloadLength |= ( maskLengthByte & 0xFF ) << ( 8 * byteCount );
      }

      // Get the masking key
      byte maskingKey[] = null;
      if ( masked ) {
        maskingKey = new byte[4];
        input.read( maskingKey, 0, 4 );
      }

      payload = new byte[payloadLength];
      int offsetIndex = 0;
      int tempLength = payloadLength;
      int bytesRead = 0;
      while ( offsetIndex != payloadLength ) {
        bytesRead = input.read( payload, offsetIndex, tempLength );
        offsetIndex += bytesRead;
        tempLength -= bytesRead;
      }

      // Demask if needed
      if ( masked ) {
        for ( int i = 0; i < payload.length; i++ ) {
          payload[i] ^= maskingKey[i % 4];
        }
      }
      return;
    } else if ( opcode == 8 ) {
      // Closing connection with server
      closeFlag = true;
    } else {
      throw new IOException( "Invalid Frame: Opcode: " + opcode );
    }

  }




  /**
   * Encodes the this WebSocketFrame into a byte array.
   * @return byte array
   */
  public byte[] encodeFrame() {
    int length = payload.length + frameLengthOverhead;
    // Calculating overhead
    if ( payload.length > 65535 ) {
      length += 8;
    } else if ( payload.length >= 126 ) {
      length += 2;
    }

    final ByteBuffer buffer = ByteBuffer.allocate( length );
    appendFinAndOpCode( buffer, opcode, fin );
    final byte mask[] = generateMaskingKey();
    appendLengthAndMask( buffer, payload.length, mask );

    for ( int i = 0; i < payload.length; i++ ) {
      buffer.put( payload[i] ^= mask[i % 4] );
    }

    buffer.flip();
    return buffer.array();
  }




  public byte getOpcode() {
    return opcode;
  }




  public byte[] getPayload() {
    return payload;
  }




  public boolean isCloseFlag() {
    return closeFlag;
  }




  public boolean isFin() {
    return fin;
  }




  /**
   * Sets the frames Fin flag and opcode.
   * @param incomingByte
   */
  private void setFinAndOpCode( final byte incomingByte ) {
    fin = ( ( incomingByte & 0x80 ) != 0 );
    // Reserved bits, unused right now.
    // boolean rsv1 = ((incomingByte & 0x40) != 0);
    // boolean rsv2 = ((incomingByte & 0x20) != 0);
    // boolean rsv3 = ((incomingByte & 0x10) != 0);
    opcode = (byte)( incomingByte & 0x0F );

  }

}
