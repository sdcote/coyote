package coyote.commons.network.http.wsc;

import java.util.List;
import java.util.Map;


/**
 * An empty implementation of {@link WebSocketListener} interface.
 * 
 * <p>Extend this class to simplify your listeners. 
 *
 * @see WebSocketListener
 */
public class WebSocketAdapter implements WebSocketListener {

  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#handleCallbackError(coyote.commons.network.http.wsc.WebSocket, java.lang.Throwable)
   */
  @Override
  public void handleCallbackError(final WebSocket websocket, final Throwable cause) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onBinaryFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onBinaryFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onBinaryMessage(coyote.commons.network.http.wsc.WebSocket, byte[])
   */
  @Override
  public void onBinaryMessage(final WebSocket websocket, final byte[] binary) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onCloseFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onCloseFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onConnected(coyote.commons.network.http.wsc.WebSocket, java.util.Map)
   */
  @Override
  public void onConnected(final WebSocket websocket, final Map<String, List<String>> headers) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onConnectError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException)
   */
  @Override
  public void onConnectError(final WebSocket websocket, final WebSocketException exception) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onContinuationFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onContinuationFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onDisconnected(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame, coyote.commons.network.http.wsc.WebSocketFrame, boolean)
   */
  @Override
  public void onDisconnected(final WebSocket websocket, final WebSocketFrame serverCloseFrame, final WebSocketFrame clientCloseFrame, final boolean closedByServer) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException)
   */
  @Override
  public void onError(final WebSocket websocket, final WebSocketException cause) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onFrameError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onFrameError(final WebSocket websocket, final WebSocketException cause, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onFrameSent(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onFrameSent(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onFrameUnsent(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onFrameUnsent(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onMessageDecompressionError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException, byte[])
   */
  @Override
  public void onMessageDecompressionError(final WebSocket websocket, final WebSocketException cause, final byte[] compressed) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onMessageError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException, java.util.List)
   */
  @Override
  public void onMessageError(final WebSocket websocket, final WebSocketException cause, final List<WebSocketFrame> frames) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onPingFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onPingFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onPongFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onPongFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onSendError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onSendError(final WebSocket websocket, final WebSocketException cause, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onSendingFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onSendingFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onSendingHandshake(coyote.commons.network.http.wsc.WebSocket, java.lang.String, java.util.List)
   */
  @Override
  public void onSendingHandshake(final WebSocket websocket, final String requestLine, final List<String[]> headers) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onStateChanged(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketState)
   */
  @Override
  public void onStateChanged(final WebSocket websocket, final WebSocketState newState) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onTextFrame(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketFrame)
   */
  @Override
  public void onTextFrame(final WebSocket websocket, final WebSocketFrame frame) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onTextMessage(coyote.commons.network.http.wsc.WebSocket, java.lang.String)
   */
  @Override
  public void onTextMessage(final WebSocket websocket, final String text) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onTextMessageError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException, byte[])
   */
  @Override
  public void onTextMessageError(final WebSocket websocket, final WebSocketException cause, final byte[] data) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onThreadCreated(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.ThreadType, java.lang.Thread)
   */
  @Override
  public void onThreadCreated(final WebSocket websocket, final ThreadType threadType, final Thread thread) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onThreadStarted(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.ThreadType, java.lang.Thread)
   */
  @Override
  public void onThreadStarted(final WebSocket websocket, final ThreadType threadType, final Thread thread) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onThreadStopping(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.ThreadType, java.lang.Thread)
   */
  @Override
  public void onThreadStopping(final WebSocket websocket, final ThreadType threadType, final Thread thread) throws Exception {
    // no-op
  }




  /**
   * @see coyote.commons.network.http.wsc.WebSocketListener#onUnexpectedError(coyote.commons.network.http.wsc.WebSocket, coyote.commons.network.http.wsc.WebSocketException)
   */
  @Override
  public void onUnexpectedError(final WebSocket websocket, final WebSocketException cause) throws Exception {
    // no-op
  }
}
