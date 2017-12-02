package coyote.commons.network.http.wsc;

/**
 * Opcode.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.2"
 *      >RFC 6455, 5.2. Base Framing Protocol</a>
 */
public class WebSocketOpcode {
  /** Opcode for "frame continuation" (0x0). */
  public static final int CONTINUATION = 0x0;

  /** Opcode for "text frame" (0x1). */
  public static final int TEXT = 0x1;

  /** Opcode for "binary frame" (0x2). */
  public static final int BINARY = 0x2;

  /** Opcode for "connection close" (0x8). */
  public static final int CLOSE = 0x8;

  /** Opcode for "ping" (0x9). */
  public static final int PING = 0x9;

  /** Opcode for "pong" (0xA). */
  public static final int PONG = 0xA;

}
