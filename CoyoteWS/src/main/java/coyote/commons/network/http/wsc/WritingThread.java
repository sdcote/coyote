package coyote.commons.network.http.wsc;

import static coyote.commons.network.http.wsc.WebSocketState.CLOSED;
import static coyote.commons.network.http.wsc.WebSocketState.CLOSING;

import java.io.IOException;
import java.util.LinkedList;

import coyote.commons.network.http.wsc.StateManager.CloseInitiator;


class WritingThread extends WebSocketThread {
  private static final int SHOULD_SEND = 0;
  private static final int SHOULD_STOP = 1;
  private static final int SHOULD_CONTINUE = 2;
  private static final int SHOULD_FLUSH = 3;
  private static final int FLUSH_THRESHOLD = 1000;
  private final LinkedList<WebSocketFrame> frames;
  private final PerMessageCompressionExtension pmce;
  private boolean stopRequested;
  private WebSocketFrame closeFrame;
  private boolean flushNeeded;
  private boolean stopped;




  private static boolean isHighPriorityFrame(final WebSocketFrame frame) {
    return (frame.isPingFrame() || frame.isPongFrame());
  }




  public WritingThread(final WebSocket websocket) {
    super("WritingThread", websocket, ThreadType.WRITING_THREAD);

    frames = new LinkedList<WebSocketFrame>();
    pmce = websocket.getPerMessageCompressionExtension();
  }




  private void addHighPriorityFrame(final WebSocketFrame frame) {
    int index = 0;

    // Determine the index at which the frame is added.
    // Among high priority frames, the order is kept in insertion order.
    for (final WebSocketFrame f : frames) {
      // If a non high-priority frame was found.
      if (isHighPriorityFrame(f) == false) {
        break;
      }

      ++index;
    }

    frames.add(index, frame);
  }




  private void changeToClosing() {
    final StateManager manager = webSocket.getStateManager();

    boolean stateChanged = false;

    synchronized (manager) {
      // The current state of the web socket.
      final WebSocketState state = manager.getState();

      // If the current state is neither CLOSING nor CLOSED.
      if (state != CLOSING && state != CLOSED) {
        // Change the state to CLOSING.
        manager.changeToClosing(CloseInitiator.CLIENT);

        stateChanged = true;
      }
    }

    if (stateChanged) {
      // Notify the listeners of the state change.
      webSocket.getListenerManager().callOnStateChanged(CLOSING);
    }
  }




  private void doFlush() throws WebSocketException {
    try {
      // Flush
      flush();

      synchronized (this) {
        flushNeeded = false;
      }
    } catch (final IOException e) {
      // Flushing frames to the server failed.
      final WebSocketException cause = new WebSocketException(WebSocketError.FLUSH_ERROR, "Flushing frames to the server failed: " + e.getMessage(), e);

      // Notify the listeners.
      final ListenerManager manager = webSocket.getListenerManager();
      manager.callOnError(cause);
      manager.callOnSendError(cause, null);

      throw cause;
    }
  }




  private void flush() throws IOException {
    webSocket.getOutput().flush();
  }




  private long flushIfLongInterval(final long lastFlushAt) throws WebSocketException {
    // The current timestamp.
    final long current = System.currentTimeMillis();

    // If sending frames has taken too much time since the last flush.
    if (FLUSH_THRESHOLD < (current - lastFlushAt)) {
      // Flush without waiting for remaining frames to be processed.
      doFlush();

      // Update the timestamp at which the last flush was executed.
      return current;
    } else {
      // Flush is not needed now.
      return lastFlushAt;
    }
  }




  private void flushIgnoreError() {
    try {
      flush();
    } catch (final IOException e) {}
  }




  private boolean isFlushNeeded(final boolean last) {
    return (last || webSocket.isAutoFlush() || flushNeeded || closeFrame != null);
  }




  private void main() {
    webSocket.onWritingThreadStarted();

    while (true) {
      // Wait for frames to be queued.
      final int result = waitForFrames();

      if (result == SHOULD_STOP) {
        break;
      } else if (result == SHOULD_FLUSH) {
        flushIgnoreError();
        continue;
      } else if (result == SHOULD_CONTINUE) {
        continue;
      }

      try {
        // Send frames.
        sendFrames(false);
      } catch (final WebSocketException e) {
        // An I/O error occurred.
        break;
      }
    }

    try {
      // Send remaining frames, if any.
      sendFrames(true);
    } catch (final WebSocketException e) {
      // An I/O error occurred.
    }
  }




  private void notifyFinished() {
    webSocket.onWritingThreadFinished(closeFrame);
  }




  public void queueFlush() {
    synchronized (this) {
      flushNeeded = true;

      // Wake up this thread.
      notifyAll();
    }
  }




  public boolean queueFrame(final WebSocketFrame frame) {
    synchronized (this) {
      while (true) {
        // If this thread has already stopped.
        if (stopped) {
          // Frames won't be sent any more. Not queued.
          return false;
        }

        // If this thread has been requested to stop or has sent a
        // close frame to the server.
        if (stopRequested || closeFrame != null) {
          // Don't wait. Process the remaining task without delay.
          break;
        }

        // If the frame is a control frame.
        if (frame.isControlFrame()) {
          // Queue the frame without blocking.
          break;
        }

        // Get the upper limit of the queue size.
        final int queueSize = webSocket.getFrameQueueSize();

        // If the upper limit is not set.
        if (queueSize == 0) {
          // Add the frame to mFrames unconditionally.
          break;
        }

        // If the current queue size has not reached the upper limit.
        if (frames.size() < queueSize) {
          // Add the frame.
          break;
        }

        try {
          // Wait until the queue gets spaces.
          wait();
        } catch (final InterruptedException e) {}
      }

      // Add the frame to the queue.
      if (isHighPriorityFrame(frame)) {
        // Add the frame at the first position so that it can be sent immediately.
        addHighPriorityFrame(frame);
      } else {
        // Add the frame at the last position.
        frames.addLast(frame);
      }

      // Wake up this thread.
      notifyAll();
    }

    // Queued.
    return true;
  }




  public void requestStop() {
    synchronized (this) {
      // Schedule stopping.
      stopRequested = true;

      // Wake up this thread.
      notifyAll();
    }
  }




  @Override
  public void runMain() {
    try {
      main();
    } catch (final Throwable t) {
      // An uncaught throwable was detected in the writing thread.
      final WebSocketException cause = new WebSocketException(WebSocketError.UNEXPECTED_ERROR_IN_WRITING_THREAD, "An uncaught throwable was detected in the writing thread: " + t.getMessage(), t);

      // Notify the listeners.
      final ListenerManager manager = webSocket.getListenerManager();
      manager.callOnError(cause);
      manager.callOnUnexpectedError(cause);
    }

    synchronized (this) {
      // Mainly for queueFrame().
      stopped = true;
      notifyAll();
    }

    // Notify this writing thread finished.
    notifyFinished();
  }




  private void sendFrame(WebSocketFrame frame) throws WebSocketException {
    // Compress the frame if appropriate.
    frame = WebSocketFrame.compressFrame(frame, pmce);

    // Notify the listeners that the frame is about to be sent.
    webSocket.getListenerManager().callOnSendingFrame(frame);

    boolean unsent = false;

    // If a close frame has already been sent.
    if (closeFrame != null) {
      // Frames should not be sent to the server.
      unsent = true;
    }
    // If the frame is a close frame.
    else if (frame.isCloseFrame()) {
      closeFrame = frame;
    }

    if (unsent) {
      // Notify the listeners that the frame was not sent.
      webSocket.getListenerManager().callOnFrameUnsent(frame);
      return;
    }

    // If the frame is a close frame.
    if (frame.isCloseFrame()) {
      // Change the state to closing if its current value is
      // neither CLOSING nor CLOSED.
      changeToClosing();
    }

    try {
      // Send the frame to the server.
      webSocket.getOutput().write(frame);
    } catch (final IOException e) {
      // An I/O error occurred when a frame was tried to be sent.
      final WebSocketException cause = new WebSocketException(WebSocketError.IO_ERROR_IN_WRITING, "An I/O error occurred when a frame was tried to be sent: " + e.getMessage(), e);

      // Notify the listeners.
      final ListenerManager manager = webSocket.getListenerManager();
      manager.callOnError(cause);
      manager.callOnSendError(cause, frame);

      throw cause;
    }

    // Notify the listeners that the frame was sent.
    webSocket.getListenerManager().callOnFrameSent(frame);
  }




  private void sendFrames(final boolean last) throws WebSocketException {
    // The timestamp at which the last flush was executed.
    long lastFlushAt = System.currentTimeMillis();

    while (true) {
      WebSocketFrame frame;

      synchronized (this) {
        // Pick up one frame from the queue.
        frame = frames.poll();

        // Mainly for queueFrame().
        notifyAll();

        // If the queue is empty.
        if (frame == null) {
          // No frame to process.
          break;
        }
      }

      // Send the frame to the server.
      sendFrame(frame);

      // If the frame is PING or PONG.
      if (frame.isPingFrame() || frame.isPongFrame()) {
        // Deliver the frame to the server immediately.
        doFlush();
        lastFlushAt = System.currentTimeMillis();
        continue;
      }

      // If flush is not needed.
      if (isFlushNeeded(last) == false) {
        // Try to consume the next frame without flush.
        continue;
      }

      // Flush if long time has passed since the last flush.
      lastFlushAt = flushIfLongInterval(lastFlushAt);
    }

    if (isFlushNeeded(last)) {
      doFlush();
    }
  }




  private int waitForFrames() {
    synchronized (this) {
      // If this thread has been requested to stop.
      if (stopRequested) {
        return SHOULD_STOP;
      }

      // If a close frame has already been sent.
      if (closeFrame != null) {
        return SHOULD_STOP;
      }

      // If the list of web socket frames to be sent is empty.
      if (frames.size() == 0) {
        // Check mFlushNeeded before calling wait().
        if (flushNeeded) {
          flushNeeded = false;
          return SHOULD_FLUSH;
        }

        try {
          // Wait until a new frame is added to the list
          // or this thread is requested to stop.
          wait();
        } catch (final InterruptedException e) {}
      }

      if (stopRequested) {
        return SHOULD_STOP;
      }

      if (frames.size() == 0) {
        if (flushNeeded) {
          flushNeeded = false;
          return SHOULD_FLUSH;
        }

        // Spurious wakeup.
        return SHOULD_CONTINUE;
      }
    }

    return SHOULD_SEND;
  }
}
