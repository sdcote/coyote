package coyote.commons.network.http.wsc;

class PongSender extends AbstractFrameSender {
  private static final String TIMER_NAME = "PongSender";




  public PongSender(final WebSocket webSocket, final PayloadGenerator generator) {
    super(webSocket, TIMER_NAME, generator);
  }




  @Override
  protected WebSocketFrame createFrame(final byte[] payload) {
    return WebSocketFrame.createPongFrame(payload);
  }
}
