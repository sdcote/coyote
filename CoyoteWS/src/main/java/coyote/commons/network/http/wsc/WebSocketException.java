package coyote.commons.network.http.wsc;

/**
 * WebSocket exception.
 */
public class WebSocketException extends Exception {
  private static final long serialVersionUID = -7198308065813844747L;
  private final WebSocketError errorMessage;




  public WebSocketException(final WebSocketError error) {
    this.errorMessage = error;
  }




  public WebSocketException(final WebSocketError error, final String message) {
    super(message);
    this.errorMessage = error;
  }




  public WebSocketException(final WebSocketError error, final String message, final Throwable cause) {
    super(message, cause);
    this.errorMessage = error;
  }




  public WebSocketException(final WebSocketError error, final Throwable cause) {
    super(cause);
    this.errorMessage = error;
  }




  public WebSocketError getError() {
    return errorMessage;
  }
}
