package coyote.commons.network.http.wsc;

/**
 * Close code.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6455#section-7.4.1"
 *      >RFC 6455, 7.4.1. Defined Status Codes</a>
 */
public class WebSocketCloseCode {
  /**
   * 1000 indicates a normal closure, meaning that the purpose for
   * which the connection was established has been fulfilled.
   */
  public static final int NORMAL = 1000;

  /**
   * 1001 indicates that an endpoint is "going away", such as a server
   * going down or a browser having navigated away from a page.
   */
  public static final int AWAY = 1001;

  /**
   * 1002 indicates that an endpoint is terminating the connection due
   * to a protocol error.
   */
  public static final int UNCONFORMED = 1002;

  /**
   * 1003 indicates that an endpoint is terminating the connection
   * because it has received a type of data it cannot accept
   * (e&#46;g&#46;, an endpoint that understands only text data MAY
   * send this if it receives a binary message).
   */
  public static final int UNACCEPTABLE = 1003;

  /**
   * 1005 is a reserved value and MUST NOT be set as a status code in a
   * Close control frame by an endpoint&#46;  It is designated for use in
   * applications expecting a status code to indicate that no status
   * code was actually present.
   */
  public static final int NONE = 1005;

  /**
   * 1006 is a reserved value and MUST NOT be set as a status code in a
   * Close control frame by an endpoint&#46;  It is designated for use in
   * applications expecting a status code to indicate that the
   * connection was closed abnormally, e&#46;g&#46;, without sending or
   * receiving a Close control frame.
   */
  public static final int ABNORMAL = 1006;

  /**
   * 1007 indicates that an endpoint is terminating the connection
   * because it has received data within a message that was not
   * consistent with the type of the message (e&#46;g&#46;, non-UTF-8
   * [<a href="http://tools.ietf.org/html/rfc3629">RFC3629</a>] data
   * within a text message).
   */
  public static final int INCONSISTENT = 1007;

  /**
   * 1008 indicates that an endpoint is terminating the connection
   * because it has received a message that violates its policy&#46;
   * This is a generic status code that can be returned when there
   * is no other more suitable status code (e&#46;g&#46;, 1003 or 1009)
   * or if there is a need to hide specific details about the policy.
   */
  public static final int VIOLATED = 1008;

  /**
   * 1009 indicates that an endpoint is terminating the connection
   * because it has received a message that is too big for it to
   * process.
   */
  public static final int OVERSIZE = 1009;

  /**
   * 1010 indicates that an endpoint (client) is terminating the
   * connection because it has expected the server to negotiate
   * one or more extension, but the server didn't return them in
   * the response message of the WebSocket handshake&#46;  The
   * list of extensions that are needed SHOULD appear in the
   * /reason/ part of the Close frame&#46; Note that this status
   * code is not used by the server, because it can fail the
   * WebSocket handshake instead.
   */
  public static final int UNEXTENDED = 1010;

  /**
   * 1011 indicates that a server is terminating the connection because
   * it encountered an unexpected condition that prevented it from
   * fulfilling the request.
   */
  public static final int UNEXPECTED = 1011;

  /**
   * 1015 is a reserved value and MUST NOT be set as a status code in a
   * Close control frame by an endpoint&#46;  It is designated for use in
   * applications expecting a status code to indicate that the
   * connection was closed due to a failure to perform a TLS handshake
   * (e&#46;g&#46;, the server certificate can't be verified).
   */
  public static final int INSECURE = 1015;

}
