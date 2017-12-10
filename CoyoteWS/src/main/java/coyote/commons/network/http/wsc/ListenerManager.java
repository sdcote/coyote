package coyote.commons.network.http.wsc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class ListenerManager {
  private final WebSocket mWebSocket;
  private final List<WebSocketListener> mListeners = new ArrayList<WebSocketListener>();
  private boolean mSyncNeeded = true;
  private List<WebSocketListener> mCopiedListeners;




  public ListenerManager(final WebSocket websocket) {
    mWebSocket = websocket;
  }




  public void addListener(final WebSocketListener listener) {
    if (listener == null) {
      return;
    }

    synchronized (mListeners) {
      mListeners.add(listener);
      mSyncNeeded = true;
    }
  }




  public void addListeners(final List<WebSocketListener> listeners) {
    if (listeners == null) {
      return;
    }

    synchronized (mListeners) {
      for (final WebSocketListener listener : listeners) {
        if (listener == null) {
          continue;
        }

        mListeners.add(listener);
        mSyncNeeded = true;
      }
    }
  }




  private void callHandleCallbackError(final WebSocketListener listener, final Throwable cause) {
    try {
      listener.handleCallbackError(mWebSocket, cause);
    } catch (final Throwable t) {}
  }




  public void callOnBinaryFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onBinaryFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnBinaryMessage(final byte[] message) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onBinaryMessage(mWebSocket, message);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnCloseFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onCloseFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnConnected(final Map<String, List<String>> headers) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onConnected(mWebSocket, headers);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnConnectError(final WebSocketException cause) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onConnectError(mWebSocket, cause);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnContinuationFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onContinuationFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnDisconnected(final WebSocketFrame serverCloseFrame, final WebSocketFrame clientCloseFrame, final boolean closedByServer) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onDisconnected(mWebSocket, serverCloseFrame, clientCloseFrame, closedByServer);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnError(final WebSocketException cause) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onError(mWebSocket, cause);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnFrameError(final WebSocketException cause, final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onFrameError(mWebSocket, cause, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnFrameSent(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onFrameSent(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnFrameUnsent(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onFrameUnsent(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnMessageDecompressionError(final WebSocketException cause, final byte[] compressed) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onMessageDecompressionError(mWebSocket, cause, compressed);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnMessageError(final WebSocketException cause, final List<WebSocketFrame> frames) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onMessageError(mWebSocket, cause, frames);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnPingFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onPingFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnPongFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onPongFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnSendError(final WebSocketException cause, final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onSendError(mWebSocket, cause, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnSendingFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onSendingFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnSendingHandshake(final String requestLine, final List<String[]> headers) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onSendingHandshake(mWebSocket, requestLine, headers);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnStateChanged(final WebSocketState newState) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onStateChanged(mWebSocket, newState);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnTextFrame(final WebSocketFrame frame) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onTextFrame(mWebSocket, frame);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnTextMessage(final String message) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onTextMessage(mWebSocket, message);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnTextMessageError(final WebSocketException cause, final byte[] data) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onTextMessageError(mWebSocket, cause, data);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnThreadCreated(final ThreadType threadType, final Thread thread) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onThreadCreated(mWebSocket, threadType, thread);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnThreadStarted(final ThreadType threadType, final Thread thread) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onThreadStarted(mWebSocket, threadType, thread);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnThreadStopping(final ThreadType threadType, final Thread thread) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onThreadStopping(mWebSocket, threadType, thread);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void callOnUnexpectedError(final WebSocketException cause) {
    for (final WebSocketListener listener : getSynchronizedListeners()) {
      try {
        listener.onUnexpectedError(mWebSocket, cause);
      } catch (final Throwable t) {
        callHandleCallbackError(listener, t);
      }
    }
  }




  public void clearListeners() {
    synchronized (mListeners) {
      if (mListeners.size() == 0) {
        return;
      }

      mListeners.clear();
      mSyncNeeded = true;
    }
  }




  public List<WebSocketListener> getListeners() {
    return mListeners;
  }




  private List<WebSocketListener> getSynchronizedListeners() {
    synchronized (mListeners) {
      if (mSyncNeeded == false) {
        return mCopiedListeners;
      }

      // Copy mListeners to copiedListeners.
      final List<WebSocketListener> copiedListeners = new ArrayList<WebSocketListener>(mListeners.size());

      for (final WebSocketListener listener : mListeners) {
        copiedListeners.add(listener);
      }

      // Synchronize.
      mCopiedListeners = copiedListeners;
      mSyncNeeded = false;

      return copiedListeners;
    }
  }




  public void removeListener(final WebSocketListener listener) {
    if (listener == null) {
      return;
    }

    synchronized (mListeners) {
      if (mListeners.remove(listener)) {
        mSyncNeeded = true;
      }
    }
  }




  public void removeListeners(final List<WebSocketListener> listeners) {
    if (listeners == null) {
      return;
    }

    synchronized (mListeners) {
      for (final WebSocketListener listener : listeners) {
        if (listener == null) {
          continue;
        }

        if (mListeners.remove(listener)) {
          mSyncNeeded = true;
        }
      }
    }
  }

}
