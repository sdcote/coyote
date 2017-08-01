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
  public static final Charset TEXT_CHARSET = Charset.forName("UTF-8");
  private transient int _payloadLength;
  private transient String _payloadString;
  private boolean fin;
  private byte[] maskingKey;
  private WebSocketFrame.OpCode opCode;
  private byte[] payload;




  public static String binary2Text(final byte[] payload) throws CharacterCodingException {
    return new String(payload, WebSocketFrame.TEXT_CHARSET);
  }




  public static String binary2Text(final byte[] payload, final int offset, final int length) throws CharacterCodingException {
    return new String(payload, offset, length, WebSocketFrame.TEXT_CHARSET);
  }




  public static WebSocketFrame read(final InputStream in) throws IOException {
    final byte head = (byte)checkedRead(in.read());
    final boolean fin = (head & 0x80) != 0;
    final WebSocketFrame.OpCode opCode = OpCode.find((byte)(head & 0x0F));
    if ((head & 0x70) != 0) {
      throw new WebSocketException(CloseCode.ProtocolError, "The reserved bits (" + Integer.toBinaryString(head & 0x70) + ") must be 0.");
    }
    if (opCode == null) {
      throw new WebSocketException(CloseCode.ProtocolError, "Received frame with reserved/unknown opcode " + (head & 0x0F) + ".");
    } else if (opCode.isControlFrame() && !fin) {
      throw new WebSocketException(CloseCode.ProtocolError, "Fragmented control frame.");
    }

    final WebSocketFrame frame = new WebSocketFrame(opCode, fin);
    frame.readPayloadInfo(in);
    frame.readPayload(in);
    if (frame.getOpCode() == OpCode.Close) {
      return new CloseFrame(frame);
    } else {
      return frame;
    }
  }




  public static byte[] text2Binary(final String payload) throws CharacterCodingException {
    return payload.getBytes(WebSocketFrame.TEXT_CHARSET);
  }




  private static int checkedRead(final int read) throws IOException {
    if (read < 0) {
      throw new EOFException();
    }
    return read;
  }




  public WebSocketFrame(final WebSocketFrame clone) {
    setOpCode(clone.getOpCode());
    setFin(clone.isFin());
    setBinaryPayload(clone.getBinaryPayload());
    setMaskingKey(clone.getMaskingKey());
  }




  public WebSocketFrame(final WebSocketFrame.OpCode opCode, final boolean fin, final byte[] payload) {
    this(opCode, fin, payload, null);
  }




  public WebSocketFrame(final WebSocketFrame.OpCode opCode, final boolean fin, final byte[] payload, final byte[] maskingKey) {
    this(opCode, fin);
    setMaskingKey(maskingKey);
    setBinaryPayload(payload);
  }




  public WebSocketFrame(final WebSocketFrame.OpCode opCode, final boolean fin, final String payload) throws CharacterCodingException {
    this(opCode, fin, payload, null);
  }




  public WebSocketFrame(final WebSocketFrame.OpCode opCode, final boolean fin, final String payload, final byte[] maskingKey) throws CharacterCodingException {
    this(opCode, fin);
    setMaskingKey(maskingKey);
    setTextPayload(payload);
  }




  public WebSocketFrame(final WebSocketFrame.OpCode opCode, final List<WebSocketFrame> fragments) throws WebSocketException {
    setOpCode(opCode);
    setFin(true);

    long _payloadLength = 0;
    for (final WebSocketFrame inter : fragments) {
      _payloadLength += inter.getBinaryPayload().length;
    }
    if ((_payloadLength < 0) || (_payloadLength > Integer.MAX_VALUE)) {
      throw new WebSocketException(CloseCode.MessageTooBig, "Max frame length has been exceeded.");
    }
    this._payloadLength = (int)_payloadLength;
    final byte[] payload = new byte[this._payloadLength];
    int offset = 0;
    for (final WebSocketFrame inter : fragments) {
      System.arraycopy(inter.getBinaryPayload(), 0, payload, offset, inter.getBinaryPayload().length);
      offset += inter.getBinaryPayload().length;
    }
    setBinaryPayload(payload);
  }




  private WebSocketFrame(final WebSocketFrame.OpCode opCode, final boolean fin) {
    setOpCode(opCode);
    setFin(fin);
  }




  public byte[] getBinaryPayload() {
    return payload;
  }




  public byte[] getMaskingKey() {
    return maskingKey;
  }




  public WebSocketFrame.OpCode getOpCode() {
    return opCode;
  }




  public String getTextPayload() {
    if (_payloadString == null) {
      try {
        _payloadString = binary2Text(getBinaryPayload());
      } catch (final CharacterCodingException e) {
        throw new RuntimeException("Undetected CharacterCodingException", e);
      }
    }
    return _payloadString;
  }




  public boolean isFin() {
    return fin;
  }




  public boolean isMasked() {
    return (maskingKey != null) && (maskingKey.length == 4);
  }




  public void setBinaryPayload(final byte[] payload) {
    this.payload = payload;
    _payloadLength = payload.length;
    _payloadString = null;
  }




  public void setFin(final boolean fin) {
    this.fin = fin;
  }




  public void setMaskingKey(final byte[] maskingKey) {
    if ((maskingKey != null) && (maskingKey.length != 4)) {
      throw new IllegalArgumentException("MaskingKey " + Arrays.toString(maskingKey) + " hasn't length 4");
    }
    this.maskingKey = maskingKey;
  }




  public void setOpCode(final WebSocketFrame.OpCode opcode) {
    opCode = opcode;
  }




  public void setTextPayload(final String payload) throws CharacterCodingException {
    this.payload = text2Binary(payload);
    _payloadLength = payload.length();
    _payloadString = payload;
  }




  public void setUnmasked() {
    setMaskingKey(null);
  }




  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("WS[");
    sb.append(getOpCode());
    sb.append(", ").append(isFin() ? "fin" : "inter");
    sb.append(", ").append(isMasked() ? "masked" : "unmasked");
    sb.append(", ").append(payloadToString());
    sb.append(']');
    return sb.toString();
  }




  public void write(final OutputStream out) throws IOException {
    byte header = 0;
    if (fin) {
      header |= 0x80;
    }
    header |= opCode.getValue() & 0x0F;
    out.write(header);

    _payloadLength = getBinaryPayload().length;
    if (_payloadLength <= 125) {
      out.write(isMasked() ? 0x80 | (byte)_payloadLength : (byte)_payloadLength);
    } else if (_payloadLength <= 0xFFFF) {
      out.write(isMasked() ? 0xFE : 126);
      out.write(_payloadLength >>> 8);
      out.write(_payloadLength);
    } else {
      out.write(isMasked() ? 0xFF : 127);
      out.write((_payloadLength >>> 56) & 0);
      out.write((_payloadLength >>> 48) & 0);
      out.write((_payloadLength >>> 40) & 0);
      out.write((_payloadLength >>> 32) & 0);
      out.write(_payloadLength >>> 24);
      out.write(_payloadLength >>> 16);
      out.write(_payloadLength >>> 8);
      out.write(_payloadLength);
    }

    if (isMasked()) {
      out.write(maskingKey);
      for (int i = 0; i < _payloadLength; i++) {
        out.write(getBinaryPayload()[i] ^ maskingKey[i % 4]);
      }
    } else {
      out.write(getBinaryPayload());
    }
    out.flush();
  }




  private String payloadToString() {
    if (payload == null) {
      return "null";
    } else {
      final StringBuilder sb = new StringBuilder();
      sb.append('[').append(payload.length).append("b] ");
      if (getOpCode() == OpCode.Text) {
        final String text = getTextPayload();
        if (text.length() > 100) {
          sb.append(text.substring(0, 100)).append("...");
        } else {
          sb.append(text);
        }
      } else {
        sb.append("0x");
        for (int i = 0; i < Math.min(payload.length, 50); ++i) {
          sb.append(Integer.toHexString(payload[i] & 0xFF));
        }
        if (payload.length > 50) {
          sb.append("...");
        }
      }
      return sb.toString();
    }
  }




  private void readPayload(final InputStream in) throws IOException {
    payload = new byte[_payloadLength];
    int read = 0;
    while (read < _payloadLength) {
      read += checkedRead(in.read(payload, read, _payloadLength - read));
    }

    if (isMasked()) {
      for (int i = 0; i < payload.length; i++) {
        payload[i] ^= maskingKey[i % 4];
      }
    }

    // Test for Unicode errors
    if (getOpCode() == OpCode.Text) {
      _payloadString = binary2Text(getBinaryPayload());
    }
  }




  private void readPayloadInfo(final InputStream in) throws IOException {
    final byte b = (byte)checkedRead(in.read());
    final boolean masked = (b & 0x80) != 0;

    _payloadLength = (byte)(0x7F & b);
    if (_payloadLength == 126) {
      // checkedRead must return int for this to work
      _payloadLength = ((checkedRead(in.read()) << 8) | checkedRead(in.read())) & 0xFFFF;
      if (_payloadLength < 126) {
        throw new WebSocketException(CloseCode.ProtocolError, "Invalid data frame 2byte length. (not using minimal length encoding)");
      }
    } else if (_payloadLength == 127) {
      final long _payloadLength = ((long)checkedRead(in.read()) << 56) | ((long)checkedRead(in.read()) << 48) | ((long)checkedRead(in.read()) << 40) | ((long)checkedRead(in.read()) << 32) | (checkedRead(in.read()) << 24) | (checkedRead(in.read()) << 16) | (checkedRead(in.read()) << 8) | checkedRead(in.read());
      if (_payloadLength < 65536) {
        throw new WebSocketException(CloseCode.ProtocolError, "Invalid data frame 4byte length. (not using minimal length encoding)");
      }
      if ((_payloadLength < 0) || (_payloadLength > Integer.MAX_VALUE)) {
        throw new WebSocketException(CloseCode.MessageTooBig, "Max frame length has been exceeded.");
      }
      this._payloadLength = (int)_payloadLength;
    }

    if (opCode.isControlFrame()) {
      if (_payloadLength > 125) {
        throw new WebSocketException(CloseCode.ProtocolError, "Control frame with payload length > 125 bytes.");
      }
      if ((opCode == OpCode.Close) && (_payloadLength == 1)) {
        throw new WebSocketException(CloseCode.ProtocolError, "Received close frame with payload len 1.");
      }
    }

    if (masked) {
      maskingKey = new byte[4];
      int read = 0;
      while (read < maskingKey.length) {
        read += checkedRead(in.read(maskingKey, read, maskingKey.length - read));
      }
    }
  }

  /**
   *
   */
  public static enum CloseCode {
    AbnormalClosure(1006), GoingAway(1001), InternalServerError(1011), InvalidFramePayloadData(1007), MandatoryExt(1010), MessageTooBig(1009), NormalClosure(1000), NoStatusRcvd(1005), PolicyViolation(1008), ProtocolError(1002), TLSHandshake(1015), UnsupportedData(1003);

    private final int code;




    public static WebSocketFrame.CloseCode find(final int value) {
      for (final WebSocketFrame.CloseCode code : values()) {
        if (code.getValue() == value) {
          return code;
        }
      }
      return null;
    }




    private CloseCode(final int code) {
      this.code = code;
    }




    public int getValue() {
      return code;
    }
  }

  /**
   *
   */
  public static class CloseFrame extends WebSocketFrame {

    private WebSocketFrame.CloseCode _closeCode;

    private String _closeReason;




    private static byte[] generatePayload(final WebSocketFrame.CloseCode code, final String closeReason) throws CharacterCodingException {
      if (code != null) {
        final byte[] reasonBytes = text2Binary(closeReason);
        final byte[] payload = new byte[reasonBytes.length + 2];
        payload[0] = (byte)((code.getValue() >> 8) & 0xFF);
        payload[1] = (byte)(code.getValue() & 0xFF);
        System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
        return payload;
      } else {
        return new byte[0];
      }
    }




    public CloseFrame(final WebSocketFrame.CloseCode code, final String closeReason) throws CharacterCodingException {
      super(OpCode.Close, true, generatePayload(code, closeReason));
    }




    private CloseFrame(final WebSocketFrame wrap) throws CharacterCodingException {
      super(wrap);
      assert wrap.getOpCode() == OpCode.Close;
      if (wrap.getBinaryPayload().length >= 2) {
        _closeCode = CloseCode.find(((wrap.getBinaryPayload()[0] & 0xFF) << 8) | (wrap.getBinaryPayload()[1] & 0xFF));
        _closeReason = binary2Text(getBinaryPayload(), 2, getBinaryPayload().length - 2);
      }
    }




    public WebSocketFrame.CloseCode getCloseCode() {
      return _closeCode;
    }




    public String getCloseReason() {
      return _closeReason;
    }
  }

  /**
   *
   */
  public static enum OpCode {
    Binary(2), Close(8), Continuation(0), Ping(9), Pong(10), Text(1);

    private final byte code;




    public static WebSocketFrame.OpCode find(final byte value) {
      for (final WebSocketFrame.OpCode opcode : values()) {
        if (opcode.getValue() == value) {
          return opcode;
        }
      }
      return null;
    }




    private OpCode(final int code) {
      this.code = (byte)code;
    }




    public byte getValue() {
      return code;
    }




    public boolean isControlFrame() {
      return (this == Close) || (this == Ping) || (this == Pong);
    }
  }

}