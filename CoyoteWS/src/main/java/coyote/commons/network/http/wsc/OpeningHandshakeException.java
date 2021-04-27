package coyote.commons.network.http.wsc;

import java.util.List;
import java.util.Map;


/**
 * An exception raised due to a violation against the WebSocket protocol.
 */
public class OpeningHandshakeException extends WebSocketException {
  private static final long serialVersionUID = 4771869625962042156L;
  private final StatusLine statusLine;
  private final Map<String, List<String>> headers;
  private final byte[] body;




  OpeningHandshakeException(final WebSocketError error, final String message, final StatusLine statusLine, final Map<String, List<String>> headers) {
    this(error, message, statusLine, headers, null);
  }




  OpeningHandshakeException(final WebSocketError error, final String message, final StatusLine statusLine, final Map<String, List<String>> headers, final byte[] body) {
    super(error, message);
    this.statusLine = statusLine;
    this.headers = headers;
    this.body = body;
  }




  /**
   * Get the response body contained in the WebSocket opening handshake
   * response from the server.
   *
   * <p>This method returns a non-null value only when (1) the status code is 
   * not 101 (Switching Protocols), (2) the response from the server has a 
   * response body, (3) the response has "Content-Length" header, and (4) no 
   * error occurred during reading the response body. In other cases, this 
   * method returns {@code null}.
   *
   * @return The response body.
   */
  public byte[] getBody() {
    return body;
  }




  /**
   * Get the HTTP headers contained in the WebSocket opening handshake
   * response from the server.
   *
   * @return The HTTP headers. The returned map is an instance of {@link 
   *         java.util.TreeMap TreeMap} with {@link 
   *         String#CASE_INSENSITIVE_ORDER} comparator.
   */
  public Map<String, List<String>> getHeaders() {
    return headers;
  }




  /**
   * Get the status line contained in the WebSocket opening handshake
   * response from the server.
   *
   * @return The status line.
   */
  public StatusLine getStatusLine() {
    return statusLine;
  }

}
