package coyote.commons.network.http.wsc;

import static coyote.commons.network.http.wsc.WebSocketOpcode.BINARY;
import static coyote.commons.network.http.wsc.WebSocketOpcode.CLOSE;
import static coyote.commons.network.http.wsc.WebSocketOpcode.CONTINUATION;
import static coyote.commons.network.http.wsc.WebSocketOpcode.PING;
import static coyote.commons.network.http.wsc.WebSocketOpcode.PONG;
import static coyote.commons.network.http.wsc.WebSocketOpcode.TEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * WebSocket frame.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5"
 *      >RFC 6455, 5. Data Framing</a>
 */
public class WebSocketFrame {
  private boolean fin;
  private boolean rsv1;
  private boolean rsv2;
  private boolean rsv3;
  private int opCode;
  private boolean mask;
  private byte[] payload;




  private static byte[] compress(final byte[] data, final PerMessageCompressionExtension pmce) {
    try {
      // Compress the data.
      return pmce.compress(data);
    } catch (final WebSocketException e) {
      // Failed to compress the data. Ignore this error and use the plain 
      // original data. The current implementation does not call any listener 
      // callback method for this error.
      return data;
    }
  }




  static WebSocketFrame compressFrame(final WebSocketFrame frame, final PerMessageCompressionExtension pmce) {
    // If Per-Message Compression is not enabled.
    if (pmce == null) {
      // No compression.
      return frame;
    }

    // If the frame is neither a TEXT frame nor a BINARY frame.
    if (frame.isTextFrame() == false && frame.isBinaryFrame() == false) {
      // No compression.
      return frame;
    }

    // If the frame is not the final frame.
    if (frame.getFin() == false) {
      // The compression must be applied to this frame and
      // all the subsequent continuation frames, but the
      // current implementation does not support the behavior.
      return frame;
    }

    // If the RSV1 bit is set.
    if (frame.getRsv1()) {
      // In the current implementation, RSV1=true is allowed
      // only as Per-Message Compressed Bit (See RFC 7692,
      // 6. Framing). Therefore, RSV1=true here is regarded
      // as "already compressed".
      return frame;
    }

    // The plain payload before compression.
    final byte[] payload = frame.getPayload();

    // If the payload is empty.
    if (payload == null || payload.length == 0) {
      // No compression.
      return frame;
    }

    // Compress the payload.
    final byte[] compressed = compress(payload, pmce);

    // If the length of the compressed data is not less than
    // that of the original plain payload.
    if (payload.length <= compressed.length) {
      // It's better not to compress the payload.
      return frame;
    }

    // Replace the plain payload with the compressed data.
    frame.setPayload(compressed);

    // Set Per-Message Compressed Bit (See RFC 7692, 6. Framing).
    frame.setRsv1(true);

    return frame;
  }




  /**
   * Create a binary frame.
   *
   * @param payload The payload for a newly created frame.
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is {@link 
   *         WebSocketOpcode#BINARY BINARY} and payload is the given one.
   */
  public static WebSocketFrame createBinaryFrame(final byte[] payload) {
    return new WebSocketFrame().setFin(true).setOpcode(BINARY).setPayload(payload);
  }




  /**
   * Create a close frame.
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is {@link 
   *         WebSocketOpcode#CLOSE CLOSE} and payload is {@code null}.
   */
  public static WebSocketFrame createCloseFrame() {
    return new WebSocketFrame().setFin(true).setOpcode(CLOSE);
  }




  /**
   * Create a close frame.
   *
   * @param closeCode The close code.
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is {@link 
   *         WebSocketOpcode#CLOSE CLOSE} and payload contains a close code.
   *
   * @see WebSocketCloseCode
   */
  public static WebSocketFrame createCloseFrame(final int closeCode) {
    return createCloseFrame().setCloseFramePayload(closeCode, null);
  }




  /**
   * Create a close frame.
   *
   * @param closeCode The close code.
   *
   * @param reason The close reason. Note that a control frame's payload 
   *        length must be 125 bytes or less (RFC 6455, 
   *        <a href="https://tools.ietf.org/html/rfc6455#section-5.5">5.5. 
   *        Control Frames</a>).
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is {@link 
   *         WebSocketOpcode#CLOSE CLOSE} and payload contains a close 
   *         code and a close reason.
   *
   * @see WebSocketCloseCode
   */
  public static WebSocketFrame createCloseFrame(final int closeCode, final String reason) {
    return createCloseFrame().setCloseFramePayload(closeCode, reason);
  }




  /**
   * Create a continuation frame. Note that the FIN bit of the
   * returned frame is false.
   *
   * @return A WebSocket frame whose FIN bit is false, opcode is {@link 
   *         WebSocketOpcode#CONTINUATION CONTINUATION} and payload is 
   *         {@code null}.
   */
  public static WebSocketFrame createContinuationFrame() {
    return new WebSocketFrame().setOpcode(CONTINUATION);
  }




  /**
   * Create a continuation frame. Note that the FIN bit of the
   * returned frame is false.
   *
   * @param payload The payload for a newly create frame.
   *
   * @return A WebSocket frame whose FIN bit is false, opcode is
   *         {@link WebSocketOpcode#CONTINUATION CONTINUATION} and
   *         payload is the given one.
   */
  public static WebSocketFrame createContinuationFrame(final byte[] payload) {
    return createContinuationFrame().setPayload(payload);
  }




  /**
   * Create a continuation frame. Note that the FIN bit of the
   * returned frame is false.
   *
   * @param payload The payload for a newly create frame.
   *
   * @return A WebSocket frame whose FIN bit is false, opcode is
   *         {@link WebSocketOpcode#CONTINUATION CONTINUATION} and
   *         payload is the given one.
   */
  public static WebSocketFrame createContinuationFrame(final String payload) {
    return createContinuationFrame().setPayload(payload);
  }




  /**
   * Create a ping frame.
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *         {@link WebSocketOpcode#PING PING} and payload is
   *         {@code null}.
   */
  public static WebSocketFrame createPingFrame() {
    return new WebSocketFrame().setFin(true).setOpcode(PING);
  }




  /**
   * Create a ping frame.
   *
   * @param payload The payload for a newly created frame.
   *        Note that a control frame's payload length must be 125 bytes or less
   *        (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
   *        >5.5. Control Frames</a>).
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *         {@link WebSocketOpcode#PING PING} and payload is
   *         the given one.
   */
  public static WebSocketFrame createPingFrame(final byte[] payload) {
    return createPingFrame().setPayload(payload);
  }




  /**
   * Create a ping frame.
   *
   * @param payload The payload for a newly created frame.
   *        Note that a control frame's payload length must be 125 bytes or less
   *        (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
   *        >5.5. Control Frames</a>).
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *         {@link WebSocketOpcode#PING PING} and payload is
   *         the given one.
   */
  public static WebSocketFrame createPingFrame(final String payload) {
    return createPingFrame().setPayload(payload);
  }




  /**
   * Create a pong frame.
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *        {@link WebSocketOpcode#PONG PONG} and payload is
   *        {@code null}.
   */
  public static WebSocketFrame createPongFrame() {
    return new WebSocketFrame().setFin(true).setOpcode(PONG);
  }




  /**
   * Create a pong frame.
   *
   * @param payload The payload for a newly created frame.
   *        Note that a control frame's payload length must be 125 bytes or less
   *        (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
   *        >5.5. Control Frames</a>).
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *         {@link WebSocketOpcode#PONG PONG} and payload is
   *         the given one.
   */
  public static WebSocketFrame createPongFrame(final byte[] payload) {
    return createPongFrame().setPayload(payload);
  }




  /**
   * Create a pong frame.
   *
   * @param payload The payload for a newly created frame.
   *        Note that a control frame's payload length must be 125 bytes or less
   *        (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
   *        >5.5. Control Frames</a>).
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *         {@link WebSocketOpcode#PONG PONG} and payload is
   *         the given one.
   */
  public static WebSocketFrame createPongFrame(final String payload) {
    return createPongFrame().setPayload(payload);
  }




  /**
   * Create a text frame.
   *
   * @param payload The payload for a newly created frame.
   *
   * @return A WebSocket frame whose FIN bit is true, opcode is
   *         {@link WebSocketOpcode#TEXT TEXT} and payload is
   *         the given one.
   */
  public static WebSocketFrame createTextFrame(final String payload) {
    return new WebSocketFrame().setFin(true).setOpcode(TEXT).setPayload(payload);
  }




  /**
   * Mask/unmask payload.
   *
   * <p>The logic of masking/unmasking is described in "<a href=
   * "http://tools.ietf.org/html/rfc6455#section-5.3">5.3. Client-to-Server 
   * Masking</a>" in RFC 6455.
   *
   * @param maskingKey The masking key. If {@code null} is given or the length
   *        of the masking key is less than 4, nothing is performed.
   *
   * @param payload Payload to be masked/unmasked.
   *
   * @return {@code payload}.
   *
   * @see <a href="http://tools.ietf.org/html/rfc6455#section-5.3">5.3. Client-to-Server Masking</a>
   */
  static byte[] mask(final byte[] maskingKey, final byte[] payload) {
    if (maskingKey == null || maskingKey.length < 4 || payload == null) {
      return payload;
    }

    for (int i = 0; i < payload.length; ++i) {
      payload[i] ^= maskingKey[i % 4];
    }

    return payload;
  }




  private static List<WebSocketFrame> split(final WebSocketFrame frame, final int maxPayloadSize) {
    // The original payload and the original FIN bit.
    final byte[] originalPayload = frame.getPayload();
    final boolean originalFin = frame.getFin();

    final List<WebSocketFrame> frames = new ArrayList<WebSocketFrame>();

    // Generate the first frame using the existing WebSocketFrame instance.
    // Note that the reserved bit 1 and the opcode are untouched.
    byte[] payload = Arrays.copyOf(originalPayload, maxPayloadSize);
    frame.setFin(false).setPayload(payload);
    frames.add(frame);

    for (int from = maxPayloadSize; from < originalPayload.length; from += maxPayloadSize) {
      // Prepare the payload of the next continuation frame.
      final int to = Math.min(from + maxPayloadSize, originalPayload.length);
      payload = Arrays.copyOfRange(originalPayload, from, to);

      // Create a continuation frame.
      final WebSocketFrame cont = WebSocketFrame.createContinuationFrame(payload);
      frames.add(cont);
    }

    if (originalFin) {
      // Set the FIN bit of the last frame.
      frames.get(frames.size() - 1).setFin(true);
    }

    return frames;
  }




  static List<WebSocketFrame> splitIfNecessary(WebSocketFrame frame, final int maxPayloadSize, final PerMessageCompressionExtension pmce) {
    // If the maximum payload size is not specified.
    if (maxPayloadSize == 0) {
      // Not split.
      return null;
    }

    // If the total length of the payload is equal to or
    // less than the maximum payload size.
    if (frame.getPayloadLength() <= maxPayloadSize) {
      // Not split.
      return null;
    }

    // If the frame is a binary frame or a text frame.
    if (frame.isBinaryFrame() || frame.isTextFrame()) {
      // Try to compress the frame. In the current implementation, binary
      // frames and text frames with the FIN bit true can be compressed.
      // The compressFrame() method may change the payload and the RSV1
      // bit of the given frame.
      frame = compressFrame(frame, pmce);

      // If the payload length of the frame has become equal to or less
      // than the maximum payload size as a result of the compression.
      if (frame.getPayloadLength() <= maxPayloadSize) {
        // Not split. (Note that the frame has been compressed)
        return null;
      }
    } else if (frame.isContinuationFrame() == false) {
      // Control frames (Close/Ping/Pong) are not split.
      return null;
    }

    // Split the frame.
    return split(frame, maxPayloadSize);
  }




  private void appendPayloadBinary(final StringBuilder builder) {
    if (appendPayloadCommon(builder)) {
      // Nothing more to append.
      return;
    }

    for (final byte element : payload) {
      builder.append(String.format("%02X ", (0xFF & element)));
    }

    if (payload.length != 0) {
      // Remove the last space.
      builder.setLength(builder.length() - 1);
    }
  }




  private void appendPayloadClose(final StringBuilder builder) {
    builder.append(",CloseCode=").append(getCloseCode()).append(",Reason=");

    final String reason = getCloseReason();

    if (reason == null) {
      builder.append("null");
    } else {
      builder.append("\"").append(reason).append("\"");
    }
  }




  private boolean appendPayloadCommon(final StringBuilder builder) {
    builder.append(",Payload=");

    if (payload == null) {
      builder.append("null");

      // Nothing more to append.
      return true;
    }

    if (rsv1) {
      // In the current implementation, mRsv1=true is allowed
      // only when Per-Message Compression is applied.
      builder.append("compressed");

      // Nothing more to append.
      return true;
    }

    // Continue.
    return false;
  }




  private void appendPayloadText(final StringBuilder builder) {
    if (appendPayloadCommon(builder)) {
      // Nothing more to append.
      return;
    }

    builder.append("\"");
    builder.append(getPayloadText());
    builder.append("\"");
  }




  /**
   * Parse the first two bytes of the payload as a close code.
   *
   * <p>If any payload is not set or the length of the payload is less than 2,
   * this method returns 1005 ({@link WebSocketCloseCode#NONE}).
   *
   * <p>The value returned from this method is meaningless if this frame is 
   * not a close frame.
   *
   * @return The close code.
   *
   * @see <a href="http://tools.ietf.org/html/rfc6455#section-5.5.1"
   *      >RFC 6455, 5.5.1. Close</a>
   *
   * @see WebSocketCloseCode
   */
  public int getCloseCode() {
    if (payload == null || payload.length < 2) {
      return WebSocketCloseCode.NONE;
    }

    // A close code is encoded in network byte order.
    final int closeCode = (((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF));

    return closeCode;
  }




  /**
   * Parse the third and subsequent bytes of the payload as a close reason.
   *
   * <p>If any payload is not set or the length of the payload is less than 3,
   * this method returns {@code null}.
   *
   * <p>The value returned from this method is meaningless if this frame is 
   * not a close frame.
   *
   * @return The close reason.
   */
  public String getCloseReason() {
    if (payload == null || payload.length < 3) {
      return null;
    }

    return WebSocketUtil.toStringUTF8(payload, 2, payload.length - 2);
  }




  /**
   * Get the value of FIN bit.
   *
   * @return The value of FIN bit.
   */
  public boolean getFin() {
    return fin;
  }




  /**
   * Get the value of MASK bit.
   *
   * @return The value of MASK bit.
   */
  boolean getMask() {
    return mask;
  }




  /**
   * Get the opcode.
   *
   * <table border="1" cellpadding="5" style="table-collapse: collapse;">
   *   <caption>WebSocket opcode</caption>
   *   <thead>
   *     <tr>
   *       <th>Value</th>
   *       <th>Description</th>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td>0x0</td>
   *       <td>Frame continuation</td>
   *     </tr>
   *     <tr>
   *       <td>0x1</td>
   *       <td>Text frame</td>
   *     </tr>
   *     <tr>
   *       <td>0x2</td>
   *       <td>Binary frame</td>
   *     </tr>
   *     <tr>
   *       <td>0x3-0x7</td>
   *       <td>Reserved</td>
   *     </tr>
   *     <tr>
   *       <td>0x8</td>
   *       <td>Connection close</td>
   *     </tr>
   *     <tr>
   *       <td>0x9</td>
   *       <td>Ping</td>
   *     </tr>
   *     <tr>
   *       <td>0xA</td>
   *       <td>Pong</td>
   *     </tr>
   *     <tr>
   *       <td>0xB-0xF</td>
   *       <td>Reserved</td>
   *     </tr>
   *   </tbody>
   * </table>
   *
   * @return The opcode.
   *
   * @see WebSocketOpcode
   */
  public int getOpcode() {
    return opCode;
  }




  /**
   * Get the unmasked payload.
   *
   * @return The unmasked payload. {@code null} may be returned.
   */
  public byte[] getPayload() {
    return payload;
  }




  /**
   * Get the payload length.
   *
   * @return The payload length.
   */
  public int getPayloadLength() {
    if (payload == null) {
      return 0;
    }

    return payload.length;
  }




  /**
   * Get the unmasked payload as a text.
   *
   * @return A string constructed by interrupting the payload as a UTF-8 bytes.
   */
  public String getPayloadText() {
    if (payload == null) {
      return null;
    }

    return WebSocketUtil.toStringUTF8(payload);
  }




  /**
   * Get the value of RSV1 bit.
   *
   * @return The value of RSV1 bit.
   */
  public boolean getRsv1() {
    return rsv1;
  }




  /**
   * Get the value of RSV2 bit.
   *
   * @return The value of RSV2 bit.
   */
  public boolean getRsv2() {
    return rsv2;
  }




  /**
   * Get the value of RSV3 bit.
   *
   * @return The value of RSV3 bit.
   */
  public boolean getRsv3() {
    return rsv3;
  }




  /**
   * Check if this frame has payload.
   *
   * @return {@code true} if this frame has payload.
   */
  public boolean hasPayload() {
    return payload != null;
  }




  /**
   * Check if this frame is a binary frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is 0x2 
   * ({@link WebSocketOpcode#BINARY}).
   *
   * @return {@code true} if this frame is a binary frame (= if the opcode is 
   *         0x2).
   */
  public boolean isBinaryFrame() {
    return (opCode == BINARY);
  }




  /**
   * Check if this frame is a close frame.
   *
   * <p> This method returns {@code true} when the value of the opcode is 0x8 
   * ({@link WebSocketOpcode#CLOSE}).
   *
   * @return {@code true} if this frame is a close frame (= if the opcode is 
   *         0x8).
   */
  public boolean isCloseFrame() {
    return (opCode == CLOSE);
  }




  /**
   * Check if this frame is a continuation frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is 0x0 
   * ({@link WebSocketOpcode#CONTINUATION}).
   *
   * @return {@code true} if this frame is a continuation frame (= if the 
   *         opcode is 0x0).
   */
  public boolean isContinuationFrame() {
    return (opCode == CONTINUATION);
  }




  /**
   * Check if this frame is a control frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is in 
   * between 0x8 and 0xF.
   *
   * @return {@code true} if this frame is a control frame (= if the opcode is 
   *         in between 0x8 and 0xF).
   */
  public boolean isControlFrame() {
    return (0x8 <= opCode && opCode <= 0xF);
  }




  /**
   * Check if this frame is a data frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is in 
   * between 0x1 and 0x7.
   *
   * @return {@code true} if this frame is a data frame (= if the opcode is in 
   *         between 0x1 and 0x7).
   */
  public boolean isDataFrame() {
    return (0x1 <= opCode && opCode <= 0x7);
  }




  /**
   * Check if this frame is a ping frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is 0x9 
   * ({@link WebSocketOpcode#PING}).
   *
   * @return {@code true} if this frame is a ping frame (= if the opcode is 
   *         0x9).
   */
  public boolean isPingFrame() {
    return (opCode == PING);
  }




  /**
   * Check if this frame is a pong frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is 0xA 
   * ({@link WebSocketOpcode#PONG}).
   *
   * @return {@code true} if this frame is a pong frame (= if the opcode is 
   *         0xA).
   */
  public boolean isPongFrame() {
    return (opCode == PONG);
  }




  /**
   * Check if this frame is a text frame.
   *
   * <p>This method returns {@code true} when the value of the opcode is 0x1 
   * ({@link WebSocketOpcode#TEXT}).
   *
   * @return {@code true} if this frame is a text frame (= if the opcode is 
   *         0x1).
   */
  public boolean isTextFrame() {
    return (opCode == TEXT);
  }




  /**
   * Set the payload that conforms to the payload format of close frames.
   *
   * <p>The given parameters are encoded based on the rules described in
   * "<a href="http://tools.ietf.org/html/rfc6455#section-5.5.1">5.5.1. 
   * Close</a>" of RFC 6455.
   *
   * <p>Note that the reason should not be too long because the payload length 
   * of a <a href="http://tools.ietf.org/html/rfc6455#section-5.5">control 
   * frame</a> must be 125 bytes or less.
   *
   * @param closeCode The close code.
   *
   * @param reason The reason. {@code null} is accepted. An empty string is 
   *        treated in the same way as {@code null}.
   *
   * @return {@code this} object.
   *
   * @see <a href="http://tools.ietf.org/html/rfc6455#section-5.5.1"
   *      >RFC 6455, 5.5.1. Close</a>
   *
   * @see WebSocketCloseCode
   */
  public WebSocketFrame setCloseFramePayload(final int closeCode, final String reason) {
    // Convert the close code to a 2-byte unsigned integer
    // in network byte order.
    final byte[] encodedCloseCode = new byte[]{(byte)((closeCode >> 8) & 0xFF), (byte)((closeCode) & 0xFF)};

    // If a reason string is not given.
    if (reason == null || reason.length() == 0) {
      // Use the close code only.
      return setPayload(encodedCloseCode);
    }

    // Convert the reason into a byte array.
    final byte[] encodedReason = WebSocketUtil.getBytesUTF8(reason);

    // Concatenate the close code and the reason.
    final byte[] payload = new byte[2 + encodedReason.length];
    System.arraycopy(encodedCloseCode, 0, payload, 0, 2);
    System.arraycopy(encodedReason, 0, payload, 2, encodedReason.length);

    // Use the concatenated string.
    return setPayload(payload);
  }




  /**
   * Set the value of FIN bit.
   *
   * @param fin The value of FIN bit.
   *
   * @return {@code this} object.
   */
  public WebSocketFrame setFin(final boolean fin) {
    this.fin = fin;
    return this;
  }




  /**
   * Set the value of MASK bit.
   *
   * @param mask The value of MASK bit.
   *
   * @return {@code this} object.
   */
  WebSocketFrame setMask(final boolean mask) {
    this.mask = mask;
    return this;
  }




  /**
   * Set the opcode
   *
   * @param opcode The opcode.
   *
   * @return {@code this} object.
   *
   * @see WebSocketOpcode
   */
  public WebSocketFrame setOpcode(final int opcode) {
    this.opCode = opcode;
    return this;
  }




  /**
   * Set the unmasked payload.
   *
   * <p>Note that the payload length of a 
   * <a href="http://tools.ietf.org/html/rfc6455#section-5.5">control frame</a> 
   * must be 125 bytes or less.
   *
   * @param payload The unmasked payload. {@code null} is accepted. An empty 
   *        byte array is treated in the same way as {@code null}.
   *
   * @return {@code this} object.
   */
  public WebSocketFrame setPayload(byte[] payload) {
    if (payload != null && payload.length == 0) {
      payload = null;
    }

    this.payload = payload;
    return this;
  }




  /**
   * Set the payload. The given string is converted to a byte array
   * in UTF-8 encoding.
   *
   * <p>Note that the payload length of a 
   * <a href="http://tools.ietf.org/html/rfc6455#section-5.5">control frame</a> 
   * must be 125 bytes or less.
   *
   * @param payload The unmasked payload. {@code null} is accepted. An empty 
   *        string is treated in the same way as {@code null}.
   *
   * @return {@code this} object.
   */
  public WebSocketFrame setPayload(final String payload) {
    if (payload == null || payload.length() == 0) {
      return setPayload((byte[])null);
    }

    return setPayload(WebSocketUtil.getBytesUTF8(payload));
  }




  /**
   * Set the value of RSV1 bit.
   *
   * @param rsv1 The value of RSV1 bit.
   *
   * @return {@code this} object.
   */
  public WebSocketFrame setRsv1(final boolean rsv1) {
    this.rsv1 = rsv1;
    return this;
  }




  /**
   * Set the value of RSV2 bit.
   *
   * @param rsv2 The value of RSV2 bit.
   *
   * @return {@code this} object.
   */
  public WebSocketFrame setRsv2(final boolean rsv2) {
    this.rsv2 = rsv2;
    return this;
  }




  /**
   * Set the value of RSV3 bit.
   *
   * @param rsv3 The value of RSV3 bit.
   *
   * @return {@code this} object.
   */
  public WebSocketFrame setRsv3(final boolean rsv3) {
    this.rsv3 = rsv3;
    return this;
  }




  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder().append("WebSocketFrame(FIN=").append(fin ? "1" : "0").append(",RSV1=").append(rsv1 ? "1" : "0").append(",RSV2=").append(rsv2 ? "1" : "0").append(",RSV3=").append(rsv3 ? "1" : "0").append(",Opcode=").append(WebSocketUtil.toOpcodeName(opCode)).append(",Length=").append(getPayloadLength());

    switch (opCode) {
      case TEXT:
        appendPayloadText(builder);
        break;

      case BINARY:
        appendPayloadBinary(builder);
        break;

      case CLOSE:
        appendPayloadClose(builder);
        break;
    }

    return builder.append(")").toString();
  }

}
