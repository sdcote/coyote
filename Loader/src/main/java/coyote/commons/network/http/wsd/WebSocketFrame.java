/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http.wsd;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


public class WebSocketFrame {
  public static final Charset TEXT_CHARSET = Charset.forName( "UTF-8" );
  private WebSocketFrame.OpCode opCode;
  private boolean fin;
  private byte[] maskingKey;
  private byte[] payload;
  private transient int _payloadLength;
  private transient String _payloadString;


  /**
   * 
   */
  public static enum CloseCode {
    NormalClosure( 1000), 
    GoingAway( 1001), 
    ProtocolError( 1002), 
    UnsupportedData( 1003), 
    NoStatusRcvd( 1005), 
    AbnormalClosure( 1006), 
    InvalidFramePayloadData( 1007), 
    PolicyViolation( 1008), 
    MessageTooBig( 1009), 
    MandatoryExt( 1010), 
    InternalServerError( 1011), 
    TLSHandshake( 1015);

    public static WebSocketFrame.CloseCode find( int value ) {
      for ( WebSocketFrame.CloseCode code : values() ) {
        if ( code.getValue() == value ) {
          return code;
        }
      }
      return null;
    }

    private final int code;




    private CloseCode( int code ) {
      this.code = code;
    }




    public int getValue() {
      return this.code;
    }
  }


  /**
   * 
   */
  public static class CloseFrame extends WebSocketFrame {

    private static byte[] generatePayload( WebSocketFrame.CloseCode code, String closeReason ) throws CharacterCodingException {
      if ( code != null ) {
        byte[] reasonBytes = text2Binary( closeReason );
        byte[] payload = new byte[reasonBytes.length + 2];
        payload[0] = (byte)( code.getValue() >> 8 & 0xFF );
        payload[1] = (byte)( code.getValue() & 0xFF );
        System.arraycopy( reasonBytes, 0, payload, 2, reasonBytes.length );
        return payload;
      } else {
        return new byte[0];
      }
    }

    private WebSocketFrame.CloseCode _closeCode;

    private String _closeReason;




    public CloseFrame( WebSocketFrame.CloseCode code, String closeReason ) throws CharacterCodingException {
      super( OpCode.Close, true, generatePayload( code, closeReason ) );
    }




    private CloseFrame( WebSocketFrame wrap ) throws CharacterCodingException {
      super( wrap );
      assert wrap.getOpCode() == OpCode.Close;
      if ( wrap.getBinaryPayload().length >= 2 ) {
        this._closeCode = CloseCode.find( ( wrap.getBinaryPayload()[0] & 0xFF ) << 8 | wrap.getBinaryPayload()[1] & 0xFF );
        this._closeReason = binary2Text( getBinaryPayload(), 2, getBinaryPayload().length - 2 );
      }
    }




    public WebSocketFrame.CloseCode getCloseCode() {
      return this._closeCode;
    }




    public String getCloseReason() {
      return this._closeReason;
    }
  }

  /**
   * 
   */
  public static enum OpCode {
    Continuation(0), 
    Text(1), 
    Binary(2), 
    Close(8), 
    Ping(9), 
    Pong(10);

    public static WebSocketFrame.OpCode find( byte value ) {
      for ( WebSocketFrame.OpCode opcode : values() ) {
        if ( opcode.getValue() == value ) {
          return opcode;
        }
      }
      return null;
    }

    private final byte code;




    private OpCode( int code ) {
      this.code = (byte)code;
    }




    public byte getValue() {
      return this.code;
    }




    public boolean isControlFrame() {
      return this == Close || this == Ping || this == Pong;
    }
  }





  public static String binary2Text( byte[] payload ) throws CharacterCodingException {
    return new String( payload, WebSocketFrame.TEXT_CHARSET );
  }




  public static String binary2Text( byte[] payload, int offset, int length ) throws CharacterCodingException {
    return new String( payload, offset, length, WebSocketFrame.TEXT_CHARSET );
  }




  private static int checkedRead( int read ) throws IOException {
    if ( read < 0 ) {
      throw new EOFException();
    }
    return read;
  }




  public static WebSocketFrame read( InputStream in ) throws IOException {
    byte head = (byte)checkedRead( in.read() );
    boolean fin = ( head & 0x80 ) != 0;
    WebSocketFrame.OpCode opCode = OpCode.find( (byte)( head & 0x0F ) );
    if ( ( head & 0x70 ) != 0 ) {
      throw new WebSocketException( CloseCode.ProtocolError, "The reserved bits (" + Integer.toBinaryString( head & 0x70 ) + ") must be 0." );
    }
    if ( opCode == null ) {
      throw new WebSocketException( CloseCode.ProtocolError, "Received frame with reserved/unknown opcode " + ( head & 0x0F ) + "." );
    } else if ( opCode.isControlFrame() && !fin ) {
      throw new WebSocketException( CloseCode.ProtocolError, "Fragmented control frame." );
    }

    WebSocketFrame frame = new WebSocketFrame( opCode, fin );
    frame.readPayloadInfo( in );
    frame.readPayload( in );
    if ( frame.getOpCode() == OpCode.Close ) {
      return new CloseFrame( frame );
    } else {
      return frame;
    }
  }




  public static byte[] text2Binary( String payload ) throws CharacterCodingException {
    return payload.getBytes( WebSocketFrame.TEXT_CHARSET );
  }





  private WebSocketFrame( WebSocketFrame.OpCode opCode, boolean fin ) {
    setOpCode( opCode );
    setFin( fin );
  }




  public WebSocketFrame( WebSocketFrame.OpCode opCode, boolean fin, byte[] payload ) {
    this( opCode, fin, payload, null );
  }




  public WebSocketFrame( WebSocketFrame.OpCode opCode, boolean fin, byte[] payload, byte[] maskingKey ) {
    this( opCode, fin );
    setMaskingKey( maskingKey );
    setBinaryPayload( payload );
  }




  public WebSocketFrame( WebSocketFrame.OpCode opCode, boolean fin, String payload ) throws CharacterCodingException {
    this( opCode, fin, payload, null );
  }




  public WebSocketFrame( WebSocketFrame.OpCode opCode, boolean fin, String payload, byte[] maskingKey ) throws CharacterCodingException {
    this( opCode, fin );
    setMaskingKey( maskingKey );
    setTextPayload( payload );
  }




  public WebSocketFrame( WebSocketFrame.OpCode opCode, List<WebSocketFrame> fragments ) throws WebSocketException {
    setOpCode( opCode );
    setFin( true );

    long _payloadLength = 0;
    for ( WebSocketFrame inter : fragments ) {
      _payloadLength += inter.getBinaryPayload().length;
    }
    if ( _payloadLength < 0 || _payloadLength > Integer.MAX_VALUE ) {
      throw new WebSocketException( CloseCode.MessageTooBig, "Max frame length has been exceeded." );
    }
    this._payloadLength = (int)_payloadLength;
    byte[] payload = new byte[this._payloadLength];
    int offset = 0;
    for ( WebSocketFrame inter : fragments ) {
      System.arraycopy( inter.getBinaryPayload(), 0, payload, offset, inter.getBinaryPayload().length );
      offset += inter.getBinaryPayload().length;
    }
    setBinaryPayload( payload );
  }




  public WebSocketFrame( WebSocketFrame clone ) {
    setOpCode( clone.getOpCode() );
    setFin( clone.isFin() );
    setBinaryPayload( clone.getBinaryPayload() );
    setMaskingKey( clone.getMaskingKey() );
  }




  public byte[] getBinaryPayload() {
    return this.payload;
  }




  public byte[] getMaskingKey() {
    return this.maskingKey;
  }




  public WebSocketFrame.OpCode getOpCode() {
    return this.opCode;
  }




  public String getTextPayload() {
    if ( this._payloadString == null ) {
      try {
        this._payloadString = binary2Text( getBinaryPayload() );
      } catch ( CharacterCodingException e ) {
        throw new RuntimeException( "Undetected CharacterCodingException", e );
      }
    }
    return this._payloadString;
  }




  public boolean isFin() {
    return this.fin;
  }




  public boolean isMasked() {
    return this.maskingKey != null && this.maskingKey.length == 4;
  }




  private String payloadToString() {
    if ( this.payload == null ) {
      return "null";
    } else {
      final StringBuilder sb = new StringBuilder();
      sb.append( '[' ).append( this.payload.length ).append( "b] " );
      if ( getOpCode() == OpCode.Text ) {
        String text = getTextPayload();
        if ( text.length() > 100 ) {
          sb.append( text.substring( 0, 100 ) ).append( "..." );
        } else {
          sb.append( text );
        }
      } else {
        sb.append( "0x" );
        for ( int i = 0; i < Math.min( this.payload.length, 50 ); ++i ) {
          sb.append( Integer.toHexString( this.payload[i] & 0xFF ) );
        }
        if ( this.payload.length > 50 ) {
          sb.append( "..." );
        }
      }
      return sb.toString();
    }
  }




  private void readPayload( InputStream in ) throws IOException {
    this.payload = new byte[this._payloadLength];
    int read = 0;
    while ( read < this._payloadLength ) {
      read += checkedRead( in.read( this.payload, read, this._payloadLength - read ) );
    }

    if ( isMasked() ) {
      for ( int i = 0; i < this.payload.length; i++ ) {
        this.payload[i] ^= this.maskingKey[i % 4];
      }
    }

    // Test for Unicode errors
    if ( getOpCode() == OpCode.Text ) {
      this._payloadString = binary2Text( getBinaryPayload() );
    }
  }




  private void readPayloadInfo( InputStream in ) throws IOException {
    byte b = (byte)checkedRead( in.read() );
    boolean masked = ( b & 0x80 ) != 0;

    this._payloadLength = (byte)( 0x7F & b );
    if ( this._payloadLength == 126 ) {
      // checkedRead must return int for this to work
      this._payloadLength = ( checkedRead( in.read() ) << 8 | checkedRead( in.read() ) ) & 0xFFFF;
      if ( this._payloadLength < 126 ) {
        throw new WebSocketException( CloseCode.ProtocolError, "Invalid data frame 2byte length. (not using minimal length encoding)" );
      }
    } else if ( this._payloadLength == 127 ) {
      long _payloadLength = (long)checkedRead( in.read() ) << 56 | (long)checkedRead( in.read() ) << 48 | (long)checkedRead( in.read() ) << 40 | (long)checkedRead( in.read() ) << 32 | checkedRead( in.read() ) << 24 | checkedRead( in.read() ) << 16 | checkedRead( in.read() ) << 8 | checkedRead( in.read() );
      if ( _payloadLength < 65536 ) {
        throw new WebSocketException( CloseCode.ProtocolError, "Invalid data frame 4byte length. (not using minimal length encoding)" );
      }
      if ( _payloadLength < 0 || _payloadLength > Integer.MAX_VALUE ) {
        throw new WebSocketException( CloseCode.MessageTooBig, "Max frame length has been exceeded." );
      }
      this._payloadLength = (int)_payloadLength;
    }

    if ( this.opCode.isControlFrame() ) {
      if ( this._payloadLength > 125 ) {
        throw new WebSocketException( CloseCode.ProtocolError, "Control frame with payload length > 125 bytes." );
      }
      if ( this.opCode == OpCode.Close && this._payloadLength == 1 ) {
        throw new WebSocketException( CloseCode.ProtocolError, "Received close frame with payload len 1." );
      }
    }

    if ( masked ) {
      this.maskingKey = new byte[4];
      int read = 0;
      while ( read < this.maskingKey.length ) {
        read += checkedRead( in.read( this.maskingKey, read, this.maskingKey.length - read ) );
      }
    }
  }




  public void setBinaryPayload( byte[] payload ) {
    this.payload = payload;
    this._payloadLength = payload.length;
    this._payloadString = null;
  }




  public void setFin( boolean fin ) {
    this.fin = fin;
  }




  public void setMaskingKey( byte[] maskingKey ) {
    if ( maskingKey != null && maskingKey.length != 4 ) {
      throw new IllegalArgumentException( "MaskingKey " + Arrays.toString( maskingKey ) + " hasn't length 4" );
    }
    this.maskingKey = maskingKey;
  }




  public void setOpCode( WebSocketFrame.OpCode opcode ) {
    this.opCode = opcode;
  }




  public void setTextPayload( String payload ) throws CharacterCodingException {
    this.payload = text2Binary( payload );
    this._payloadLength = payload.length();
    this._payloadString = payload;
  }




  public void setUnmasked() {
    setMaskingKey( null );
  }




  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder( "WS[" );
    sb.append( getOpCode() );
    sb.append( ", " ).append( isFin() ? "fin" : "inter" );
    sb.append( ", " ).append( isMasked() ? "masked" : "unmasked" );
    sb.append( ", " ).append( payloadToString() );
    sb.append( ']' );
    return sb.toString();
  }




  public void write( OutputStream out ) throws IOException {
    byte header = 0;
    if ( this.fin ) {
      header |= 0x80;
    }
    header |= this.opCode.getValue() & 0x0F;
    out.write( header );

    this._payloadLength = getBinaryPayload().length;
    if ( this._payloadLength <= 125 ) {
      out.write( isMasked() ? 0x80 | (byte)this._payloadLength : (byte)this._payloadLength );
    } else if ( this._payloadLength <= 0xFFFF ) {
      out.write( isMasked() ? 0xFE : 126 );
      out.write( this._payloadLength >>> 8 );
      out.write( this._payloadLength );
    } else {
      out.write( isMasked() ? 0xFF : 127 );
      out.write( this._payloadLength >>> 56 & 0 ); 
      out.write( this._payloadLength >>> 48 & 0 );
      out.write( this._payloadLength >>> 40 & 0 );
      out.write( this._payloadLength >>> 32 & 0 );
      out.write( this._payloadLength >>> 24 );
      out.write( this._payloadLength >>> 16 );
      out.write( this._payloadLength >>> 8 );
      out.write( this._payloadLength );
    }

    if ( isMasked() ) {
      out.write( this.maskingKey );
      for ( int i = 0; i < this._payloadLength; i++ ) {
        out.write( getBinaryPayload()[i] ^ this.maskingKey[i % 4] );
      }
    } else {
      out.write( getBinaryPayload() );
    }
    out.flush();
  }

}