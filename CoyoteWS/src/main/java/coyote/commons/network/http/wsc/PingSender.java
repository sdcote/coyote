package coyote.commons.network.http.wsc;

/**
 * Regularly send Ping frames.
 */
class PingSender extends AbstractFrameSender {
  private static final String TIMER_NAME = "PingSender";




  public PingSender(final WebSocket webSocket, final PayloadGenerator generator) {
    super(webSocket, TIMER_NAME, generator);
  }




  @Override
  protected WebSocketFrame createFrame(final byte[] payload) {
    return WebSocketFrame.createPingFrame(payload);
  }

}
