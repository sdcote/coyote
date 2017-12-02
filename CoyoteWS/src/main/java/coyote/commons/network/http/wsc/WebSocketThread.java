package coyote.commons.network.http.wsc;

abstract class WebSocketThread extends Thread {
  protected final WebSocket webSocket;
  private final ThreadType threadType;




  WebSocketThread(final String name, final WebSocket ws, final ThreadType type) {
    super(name);
    webSocket = ws;
    threadType = type;
  }




  public void callOnThreadCreated() {
    final ListenerManager lm = webSocket.getListenerManager();

    if (lm != null) {
      lm.callOnThreadCreated(threadType, this);
    }
  }




  @Override
  public void run() {
    final ListenerManager lm = webSocket.getListenerManager();

    if (lm != null) {
      // Execute onThreadStarted() of the listeners.
      lm.callOnThreadStarted(threadType, this);
    }

    runMain();

    if (lm != null) {
      // Execute onThreadStopping() of the listeners.
      lm.callOnThreadStopping(threadType, this);
    }
  }




  protected abstract void runMain();
}
