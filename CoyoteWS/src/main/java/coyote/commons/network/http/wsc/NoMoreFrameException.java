package coyote.commons.network.http.wsc;

class NoMoreFrameException extends WebSocketException {
  private static final long serialVersionUID = -4689561441057480212L;




  public NoMoreFrameException() {
    super(WebSocketError.NO_MORE_FRAME, "No more WebSocket frame from the server.");
  }

}
