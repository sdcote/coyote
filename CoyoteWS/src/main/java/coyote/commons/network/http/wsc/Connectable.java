package coyote.commons.network.http.wsc;

import java.util.concurrent.Callable;


/**
 * An implementation of {@link Callable} interface that calls
 * {@link WebSocket#connect()}.
 */
class Connectable implements Callable<WebSocket> {
  private final WebSocket webSocket;




  public Connectable(final WebSocket ws) {
    webSocket = ws;
  }




  @Override
  public WebSocket call() throws WebSocketException {
    return webSocket.connect();
  }
  
}
