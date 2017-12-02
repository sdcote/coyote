package coyote.commons.network.http.wsc;

class FinishThread extends WebSocketThread {
  
  public FinishThread(final WebSocket ws) {
    super("FinishThread", ws, ThreadType.FINISH_THREAD);
  }




  @Override
  public void runMain() {
    webSocket.finish();
  }
}
