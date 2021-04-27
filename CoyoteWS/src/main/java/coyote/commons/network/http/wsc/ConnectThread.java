package coyote.commons.network.http.wsc;

class ConnectThread extends WebSocketThread {

  public ConnectThread(final WebSocket ws) {
    super("ConnectThread", ws, ThreadType.CONNECT_THREAD);
  }




  private void handleError(final WebSocketException cause) {
    final ListenerManager manager = webSocket.getListenerManager();
    manager.callOnError(cause);
    manager.callOnConnectError(cause);
  }




  @Override
  public void runMain() {
    try {
      webSocket.connect();
    } catch (final WebSocketException e) {
      handleError(e);
    }
  }

}
