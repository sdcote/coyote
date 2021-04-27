package coyote.commons.network.http.wsc;

import static coyote.commons.network.http.wsc.WebSocketOpcode.BINARY;
import static coyote.commons.network.http.wsc.WebSocketOpcode.CLOSE;
import static coyote.commons.network.http.wsc.WebSocketOpcode.CONTINUATION;
import static coyote.commons.network.http.wsc.WebSocketOpcode.PING;
import static coyote.commons.network.http.wsc.WebSocketOpcode.PONG;
import static coyote.commons.network.http.wsc.WebSocketOpcode.TEXT;
import static coyote.commons.network.http.wsc.WebSocketState.CLOSED;
import static coyote.commons.network.http.wsc.WebSocketState.CLOSING;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import coyote.commons.network.http.wsc.StateManager.CloseInitiator;


class ReadingThread extends WebSocketThread {
  private class CloseTask extends TimerTask {
    @Override
    public void run() {
      try {
        final Socket socket = webSocket.getSocket();
        socket.close();
      } catch (final Throwable t) {
        // Ignore.
      }
    }
  }

  private boolean stopRequested;
  private WebSocketFrame closeFrame;
  private final List<WebSocketFrame> continuation = new ArrayList<WebSocketFrame>();
  private final PerMessageCompressionExtension pmce;
  private final Object closeMutex = new Object();
  private Timer closeTimer;
  private CloseTask closeTask;
  private long closeDelay;
  private boolean noWaitForCloseFrame;




  public ReadingThread(final WebSocket websocket) {
    super("ReadingThread", websocket, ThreadType.READING_THREAD);
    pmce = websocket.getPerMessageCompressionExtension();
  }




  /**
   * Call {@link WebSocketListener#onBinaryFrame(WebSocket, WebSocketFrame)
   * onBinaryFrame} method of the listeners.
   */
  private void callOnBinaryFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnBinaryFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onBinaryMessage(WebSocket, String)
   * onBinaryMessage} method of the listeners.
   */
  private void callOnBinaryMessage(final byte[] message) {
    webSocket.getListenerManager().callOnBinaryMessage(message);
  }




  /**
   * Call {@link WebSocketListener#onCloseFrame(WebSocket, WebSocketFrame)
   * onCloseFrame} method of the listeners.
   */
  private void callOnCloseFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnCloseFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onContinuationFrame(WebSocket, WebSocketFrame)
   * onContinuationFrame} method of the listeners.
   */
  private void callOnContinuationFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnContinuationFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onError(WebSocket, WebSocketException)
   * onError} method of the listeners.
   */
  private void callOnError(final WebSocketException cause) {
    webSocket.getListenerManager().callOnError(cause);
  }




  /**
   * Call {@link WebSocketListener#onFrame(WebSocket, WebSocketFrame) onFrame}
   * method of the listeners.
   */
  private void callOnFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onFrameError(WebSocket,
   * WebSocketException, WebSocketFrame) onFrameError} method of the listeners.
   */
  private void callOnFrameError(final WebSocketException cause, final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnFrameError(cause, frame);
  }




  /**
   * Call {@link WebSocketListener#onMessageDecompressionError(WebSocket, WebSocketException, byte[])
   * onMessageDecompressionError} method of the listeners.
   */
  private void callOnMessageDecompressionError(final WebSocketException cause, final byte[] compressed) {
    webSocket.getListenerManager().callOnMessageDecompressionError(cause, compressed);
  }




  /**
   * Call {@link WebSocketListener#onMessageError(WebSocket, WebSocketException, List)
   * onMessageError} method of the listeners.
   */
  private void callOnMessageError(final WebSocketException cause, final List<WebSocketFrame> frames) {
    webSocket.getListenerManager().callOnMessageError(cause, frames);
  }




  /**
   * Call {@link WebSocketListener#onPingFrame(WebSocket, WebSocketFrame)
   * onPingFrame} method of the listeners.
   */
  private void callOnPingFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnPingFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onPongFrame(WebSocket, WebSocketFrame)
   * onPongFrame} method of the listeners.
   */
  private void callOnPongFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnPongFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onTextFrame(WebSocket, WebSocketFrame)
   * onTextFrame} method of the listeners.
   */
  private void callOnTextFrame(final WebSocketFrame frame) {
    webSocket.getListenerManager().callOnTextFrame(frame);
  }




  /**
   * Call {@link WebSocketListener#onTextMessage(WebSocket, String)
   * onTextMessage} method of the listeners.
   */
  private void callOnTextMessage(final byte[] data) {
    try {
      // Interpret the byte array as a string.
      // OutOfMemoryError may happen when the size of data is too big.
      final String message = WebSocketUtil.toStringUTF8(data);

      // Call onTextMessage() method of the listeners.
      callOnTextMessage(message);
    } catch (final Throwable t) {
      // Failed to convert payload data into a string.
      final WebSocketException wse = new WebSocketException(WebSocketError.TEXT_MESSAGE_CONSTRUCTION_ERROR, "Failed to convert payload data into a string: " + t.getMessage(), t);

      // Notify the listeners that text message construction failed.
      callOnError(wse);
      callOnTextMessageError(wse, data);
    }
  }




  /**
   * Call {@link WebSocketListener#onTextMessage(WebSocket, String)
   * onTextMessage} method of the listeners.
   */
  private void callOnTextMessage(final String message) {
    webSocket.getListenerManager().callOnTextMessage(message);
  }




  /**
   * Call {@link WebSocketListener#onTextMessageError(WebSocket, WebSocketException, byte[])
   * onTextMessageError} method of the listeners.
   */
  private void callOnTextMessageError(final WebSocketException cause, final byte[] data) {
    webSocket.getListenerManager().callOnTextMessageError(cause, data);
  }




  private void cancelClose() {
    synchronized (closeMutex) {
      cancelCloseTask();
    }
  }




  private void cancelCloseTask() {
    if (closeTimer != null) {
      closeTimer.cancel();
      closeTimer = null;
    }

    if (closeTask != null) {
      closeTask.cancel();
      closeTask = null;
    }
  }




  private byte[] concatenatePayloads(final List<WebSocketFrame> frames) {
    Throwable cause;

    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      // For each web socket frame.
      for (final WebSocketFrame frame : frames) {
        // Get the payload of the frame.
        final byte[] payload = frame.getPayload();

        // If the payload is null or empty.
        if (payload == null || payload.length == 0) {
          continue;
        }

        // Append the payload.
        baos.write(payload);
      }

      // Return the concatenated byte array.
      return baos.toByteArray();
    } catch (final IOException e) {
      cause = e;
    } catch (final OutOfMemoryError e) {
      cause = e;
    }

    // Create a WebSocketException which has a cause.
    final WebSocketException wse = new WebSocketException(WebSocketError.MESSAGE_CONSTRUCTION_ERROR, "Failed to concatenate payloads of multiple frames to construct a message: " + cause.getMessage(), cause);

    // Notify the listeners that message construction failed.
    callOnError(wse);
    callOnMessageError(wse, frames);

    // Create a close frame with a close code of 1009 which
    // indicates that the message is too big to process.
    final WebSocketFrame frame = WebSocketFrame.createCloseFrame(WebSocketCloseCode.OVERSIZE, wse.getMessage());

    // Send the close frame.
    webSocket.sendFrame(frame);

    // Failed to construct a message.
    return null;
  }




  private WebSocketFrame createCloseFrame(final WebSocketException wse) {
    int closeCode;

    switch (wse.getError()) {
      // In WebSocketInputStream.readFrame()

      case INSUFFICENT_DATA:
      case INVALID_PAYLOAD_LENGTH:
      case NO_MORE_FRAME:
        closeCode = WebSocketCloseCode.UNCONFORMED;
        break;

      case TOO_LONG_PAYLOAD:
      case INSUFFICIENT_MEMORY_FOR_PAYLOAD:
        closeCode = WebSocketCloseCode.OVERSIZE;
        break;

      // In this.verifyFrame(WebSocketFrame)

      case NON_ZERO_RESERVED_BITS:
      case UNEXPECTED_RESERVED_BIT:
      case UNKNOWN_OPCODE:
      case FRAME_MASKED:
      case FRAGMENTED_CONTROL_FRAME:
      case UNEXPECTED_CONTINUATION_FRAME:
      case CONTINUATION_NOT_CLOSED:
      case TOO_LONG_CONTROL_FRAME_PAYLOAD:
        closeCode = WebSocketCloseCode.UNCONFORMED;
        break;

      // In this.readFrame()

      case INTERRUPTED_IN_READING:
      case IO_ERROR_IN_READING:
        closeCode = WebSocketCloseCode.VIOLATED;
        break;

      // Others (unexpected)

      default:
        closeCode = WebSocketCloseCode.VIOLATED;
        break;
    }

    return WebSocketFrame.createCloseFrame(closeCode, wse.getMessage());
  }




  private byte[] decompress(final byte[] input) {
    WebSocketException wse;

    try {
      // Decompress the message.
      return pmce.decompress(input);
    } catch (final WebSocketException e) {
      wse = e;
    }

    // Notify the listeners that decompression failed.
    callOnError(wse);
    callOnMessageDecompressionError(wse, input);

    // Create a close frame with a close code of 1003 which
    // indicates that the message cannot be accepted.
    final WebSocketFrame frame = WebSocketFrame.createCloseFrame(WebSocketCloseCode.UNACCEPTABLE, wse.getMessage());

    // Send the close frame.
    webSocket.sendFrame(frame);

    // Failed to construct a message.
    return null;
  }




  private byte[] getMessage(final List<WebSocketFrame> frames) {
    // Concatenate payloads of the frames.
    byte[] data = concatenatePayloads(continuation);

    // If the concatenation failed.
    if (data == null) {
      // Stop reading.
      return null;
    }

    // If a per-message compression extension is enabled and
    // the Per-Message Compressed bit of the first frame is set.
    if (pmce != null && frames.get(0).getRsv1()) {
      // Decompress the data.
      data = decompress(data);
    }

    return data;
  }




  private byte[] getMessage(final WebSocketFrame frame) {
    // The raw payload of the frame.
    byte[] payload = frame.getPayload();

    // If a per-message compression extension is enabled and
    // the Per-Message Compressed bit of the frame is set.
    if (pmce != null && frame.getRsv1()) {
      // Decompress the payload.
      payload = decompress(payload);
    }

    return payload;
  }




  private boolean handleBinaryFrame(final WebSocketFrame frame) {
    // Notify the listeners that a binary frame was received.
    callOnBinaryFrame(frame);

    // If the frame indicates the start of fragmentation.
    if (frame.getFin() == false) {
      // Start a continuation sequence.
      continuation.add(frame);

      // Keep reading.
      return true;
    }

    // Get the payload of the frame. Decompression is performed
    // when necessary.
    final byte[] payload = getMessage(frame);

    // Notify the listeners that a binary message was received.
    callOnBinaryMessage(payload);

    // Keep reading.
    return true;
  }




  private boolean handleCloseFrame(final WebSocketFrame frame) {
    // Get the manager which manages the state of the web socket.
    final StateManager manager = webSocket.getStateManager();

    // The close frame sent from the server.
    closeFrame = frame;

    boolean stateChanged = false;

    synchronized (manager) {
      // The current state of the web socket.
      final WebSocketState state = manager.getState();

      // If the current state is neither CLOSING nor CLOSED.
      if (state != CLOSING && state != CLOSED) {
        // Change the state to CLOSING.
        manager.changeToClosing(CloseInitiator.SERVER);

        // This web socket has not sent a close frame yet,
        // so schedule sending a close frame.

        // RFC 6455, 5.5.1. Close
        //
        //   When sending a Close frame in response, the endpoint
        //   typically echos the status code it received.
        //

        // Simply reuse the frame.
        webSocket.sendFrame(frame);

        stateChanged = true;
      }
    }

    if (stateChanged) {
      // Notify the listeners of the state change.
      webSocket.getListenerManager().callOnStateChanged(CLOSING);
    }

    // Notify the listeners that a close frame was received.
    callOnCloseFrame(frame);

    // Stop reading.
    return false;
  }




  private boolean handleContinuationFrame(final WebSocketFrame frame) {
    // Notify the listeners that a continuation frame was received.
    callOnContinuationFrame(frame);

    // Append the continuation frame to the existing continuation sequence.
    continuation.add(frame);

    // If the frame is not the last one for the continuation.
    if (frame.getFin() == false) {
      // Keep reading.
      return true;
    }

    // Concatenate payloads of the frames. Decompression is performed
    // when necessary.
    final byte[] data = getMessage(continuation);

    // If the concatenation failed.
    if (data == null) {
      // Stop reading.
      return false;
    }

    // If the continuation forms a text message.
    if (continuation.get(0).isTextFrame()) {
      // Notify the listeners that a text message was received.
      callOnTextMessage(data);
    } else {
      // Notify the listeners that a binary message was received.
      callOnBinaryMessage(data);
    }

    // Clear the continuation.
    continuation.clear();

    // Keep reading.
    return true;
  }




  private boolean handleFrame(final WebSocketFrame frame) {
    // Notify the listeners that a frame was received.
    callOnFrame(frame);

    // Dispatch based on the opcode.
    switch (frame.getOpcode()) {
      case CONTINUATION:
        return handleContinuationFrame(frame);

      case TEXT:
        return handleTextFrame(frame);

      case BINARY:
        return handleBinaryFrame(frame);

      case CLOSE:
        return handleCloseFrame(frame);

      case PING:
        return handlePingFrame(frame);

      case PONG:
        return handlePongFrame(frame);

      default:
        // Ignore the frame whose opcode is unknown. Keep reading.
        return true;
    }
  }




  private boolean handlePingFrame(final WebSocketFrame frame) {
    // Notify the listeners that a ping frame was received.
    callOnPingFrame(frame);

    // RFC 6455, 5.5.3. Pong
    //
    //   A Pong frame sent in response to a Ping frame must
    //   have identical "Application data" as found in the
    //   message body of the Ping frame being replied to.

    // Create a pong frame which has the same payload as
    // the ping frame.
    final WebSocketFrame pong = WebSocketFrame.createPongFrame(frame.getPayload());

    // Send the pong frame to the server.
    webSocket.sendFrame(pong);

    // Keep reading.
    return true;
  }




  private boolean handlePongFrame(final WebSocketFrame frame) {
    // Notify the listeners that a pong frame was received.
    callOnPongFrame(frame);

    // Keep reading.
    return true;
  }




  private boolean handleTextFrame(final WebSocketFrame frame) {
    // Notify the listeners that a text frame was received.
    callOnTextFrame(frame);

    // If the frame indicates the start of fragmentation.
    if (frame.getFin() == false) {
      // Start a continuation sequence.
      continuation.add(frame);

      // Keep reading.
      return true;
    }

    // Get the payload of the frame. Decompression is performed
    // when necessary.
    final byte[] payload = getMessage(frame);

    // Notify the listeners that a text message was received.
    callOnTextMessage(payload);

    // Keep reading.
    return true;
  }




  private void main() {
    webSocket.onReadingThreadStarted();

    while (true) {
      synchronized (this) {
        if (stopRequested) {
          break;
        }
      }

      // Receive a frame from the server.
      final WebSocketFrame frame = readFrame();

      if (frame == null) {
        // Something unexpected happened.
        break;
      }

      // Handle the frame.
      final boolean keepReading = handleFrame(frame);

      if (keepReading == false) {
        break;
      }
    }

    // Wait for a close frame if one has not been received yet.
    waitForCloseFrame();

    // Cancel a task which calls Socket.close() if running.
    cancelClose();
  }




  private void notifyFinished() {
    webSocket.onReadingThreadFinished(closeFrame);
  }




  private WebSocketFrame readFrame() {
    WebSocketFrame frame = null;
    WebSocketException wse = null;

    try {
      // Receive a frame from the server.
      frame = webSocket.getInput().readFrame();

      // Verify the frame. If invalid, WebSocketException is thrown.
      verifyFrame(frame);

      // Return the verified frame.
      return frame;
    } catch (final InterruptedIOException e) {
      if (stopRequested) {
        // Thread.interrupt() interrupted a blocking socket read operation.
        // This thread has been interrupted intentionally.
        return null;
      } else {
        // Interruption occurred while a frame was being read from the web socket.
        wse = new WebSocketException(WebSocketError.INTERRUPTED_IN_READING, "Interruption occurred while a frame was being read from the web socket: " + e.getMessage(), e);
      }
    } catch (final IOException e) {
      if (stopRequested && isInterrupted()) {
        // Socket.close() interrupted a blocking socket read operation.
        // This thread has been interrupted intentionally.
        return null;
      } else {
        // An I/O error occurred while a frame was being read from the web socket.
        wse = new WebSocketException(WebSocketError.IO_ERROR_IN_READING, "An I/O error occurred while a frame was being read from the web socket: " + e.getMessage(), e);
      }
    } catch (final WebSocketException e) {
      // A protocol error.
      wse = e;
    }

    boolean error = true;

    // If the input stream of the WebSocket connection has reached the end
    // without receiving a close frame from the server.
    if (wse instanceof NoMoreFrameException) {
      // Not wait for a close frame in waitForCloseFrame() which will be called later.
      noWaitForCloseFrame = true;

      // If the configuration of the WebSocket instance allows the behavior.
      if (webSocket.isMissingCloseFrameAllowed()) {
        error = false;
      }
    }

    if (error) {
      // Notify the listeners that an error occurred while a frame was being read.
      callOnError(wse);
      callOnFrameError(wse, frame);
    }

    // Create a close frame.
    final WebSocketFrame closeFrame = createCloseFrame(wse);

    // Send the close frame.
    webSocket.sendFrame(closeFrame);

    // No WebSocket frame is available.
    return null;
  }




  void requestStop(final long closeDelay) {
    synchronized (this) {
      if (stopRequested) {
        return;
      }

      stopRequested = true;
    }

    // interrupt() may not interrupt a blocking socket read(), so calling
    // interrupt() here may not work. interrupt() in Java is different
    // from signal-based interruption in C which unblocks a read() system
    // call. Anyway, let's mark this thread as interrupted.
    interrupt();

    // To surely unblock a read() call, Socket.close() needs to be called.
    // Or, shutdownInterrupt() may work, but it is not explicitly stated
    // in the JavaDoc. In either case, interruption should not be executed
    // now because a close frame from the server should be waited for.
    //
    // So, let's schedule a task with some delay which calls Socket.close().
    // However, in normal cases, a close frame will arrive from the server
    // before the task calls Socket.close().
    this.closeDelay = closeDelay;
    scheduleClose();
  }




  @Override
  public void runMain() {
    try {
      main();
    } catch (final Throwable t) {
      // An uncaught throwable was detected in the reading thread.
      final WebSocketException cause = new WebSocketException(WebSocketError.UNEXPECTED_ERROR_IN_READING_THREAD, "An uncaught throwable was detected in the reading thread: " + t.getMessage(), t);

      // Notify the listeners.
      final ListenerManager manager = webSocket.getListenerManager();
      manager.callOnError(cause);
      manager.callOnUnexpectedError(cause);
    }

    // Notify this reading thread finished.
    notifyFinished();
  }




  private void scheduleClose() {
    synchronized (closeMutex) {
      cancelCloseTask();
      scheduleCloseTask();
    }
  }




  private void scheduleCloseTask() {
    closeTask = new CloseTask();
    closeTimer = new Timer("ReadingThreadCloseTimer");
    closeTimer.schedule(closeTask, closeDelay);
  }




  private void verifyFrame(final WebSocketFrame frame) throws WebSocketException {
    // Verify RSV1, RSV2 and RSV3.
    verifyReservedBits(frame);

    // The opcode of the frame must be known.
    verifyFrameOpcode(frame);

    // Frames from the server must not be masked.
    verifyFrameMask(frame);

    // Verify fragmentation conditions.
    verifyFrameFragmentation(frame);

    // Verify the size of the payload.
    verifyFrameSize(frame);
  }




  private void verifyFrameFragmentation(final WebSocketFrame frame) throws WebSocketException {
    // Control frames (see Section 5.5) MAY be injected in the
    // middle of a fragmented message. Control frames themselves
    // MUST NOT be fragmented.
    if (frame.isControlFrame()) {
      // If fragmented.
      if (frame.getFin() == false) {
        // A control frame is fragmented.
        throw new WebSocketException(WebSocketError.FRAGMENTED_CONTROL_FRAME, "A control frame is fragmented.");
      }

      // No more requirements on a control frame.
      return;
    }

    // True if a continuation has already started.
    final boolean continuationExists = (continuation.size() != 0);

    // If the frame is a continuation frame.
    if (frame.isContinuationFrame()) {
      // There must already exist a continuation sequence.
      if (continuationExists == false) {
        // A continuation frame was detected although a continuation had not started.
        throw new WebSocketException(WebSocketError.UNEXPECTED_CONTINUATION_FRAME, "A continuation frame was detected although a continuation had not started.");
      }

      // No more requirements on a continuation frame.
      return;
    }

    // A data frame.

    if (continuationExists) {
      // A non-control frame was detected although the existing continuation had not been closed.
      throw new WebSocketException(WebSocketError.CONTINUATION_NOT_CLOSED, "A non-control frame was detected although the existing continuation had not been closed.");
    }
  }




  /**
   * Ensure that the given frame is not masked.
   *
   * <blockquote>
   * <p>From RFC 6455, 5.1. Overview:
   * <p><i>A server MUST NOT mask any frames that it sends to the client. A 
   * client MUST close a connection if it detects a masked frame.</i>
   * </blockquote>
   */
  private void verifyFrameMask(final WebSocketFrame frame) throws WebSocketException {
    // If the frame is masked.
    if (frame.getMask()) {
      // A frame from the server is masked.
      throw new WebSocketException(WebSocketError.FRAME_MASKED, "A frame from the server is masked.");
    }
  }




  /**
   * Ensure that the opcode of the give frame is a known one.
   *
   * <blockquote>
   * <p>From RFC 6455, 5.2. Base Framing Protocol
   * <p><i>If an unknown opcode is received, the receiving endpoint MUST Fail 
   * the WebSocket Connection.</i>
   * </blockquote>
   */
  private void verifyFrameOpcode(final WebSocketFrame frame) throws WebSocketException {
    switch (frame.getOpcode()) {
      case CONTINUATION:
      case TEXT:
      case BINARY:
      case CLOSE:
      case PING:
      case PONG:
        // Known opcode
        return;

      default:
        break;
    }

    // If extended use of web socket frames is allowed.
    if (webSocket.isExtended()) {
      // Allow the unknown opcode.
      return;
    }

    // A frame has an unknown opcode.
    throw new WebSocketException(WebSocketError.UNKNOWN_OPCODE, "A frame has an unknown opcode: 0x" + Integer.toHexString(frame.getOpcode()));
  }




  private void verifyFrameSize(final WebSocketFrame frame) throws WebSocketException {
    // If the frame is not a control frame.
    if (frame.isControlFrame() == false) {
      // Nothing to check.
      return;
    }

    // RFC 6455, 5.5. Control Frames.
    //
    //   All control frames MUST have a payload length of 125 bytes or less
    //   and MUST NOT be fragmented.
    //

    final byte[] payload = frame.getPayload();

    if (payload == null) {
      // The frame does not have payload.
      return;
    }

    if (125 < payload.length) {
      // The payload size of a control frame exceeds the maximum size (125 bytes).
      throw new WebSocketException(WebSocketError.TOO_LONG_CONTROL_FRAME_PAYLOAD, "The payload size of a control frame exceeds the maximum size (125 bytes): " + payload.length);
    }
  }




  /**
   * Verify the RSV1 bit of a frame.
   */
  private void verifyReservedBit1(final WebSocketFrame frame) throws WebSocketException {
    // If a per-message compression extension has been agreed.
    if (pmce != null) {
      // Verify the RSV1 bit using the rule described in RFC 7692.
      final boolean verified = verifyReservedBit1ForPMCE(frame);

      if (verified) {
        return;
      }
    }

    if (frame.getRsv1() == false) {
      // No problem.
      return;
    }

    // The RSV1 bit of a frame is set unexpectedly.
    throw new WebSocketException(WebSocketError.UNEXPECTED_RESERVED_BIT, "The RSV1 bit of a frame is set unexpectedly.");
  }




  /**
   * Verify the RSV1 bit of a frame using the rule described in RFC 7692.
   * See <a href="https://tools.ietf.org/html/rfc7692#section-6">6. Framing</a>
   * in <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a> for details.
   */
  private boolean verifyReservedBit1ForPMCE(final WebSocketFrame frame) throws WebSocketException {
    if (frame.isTextFrame() || frame.isBinaryFrame()) {
      // The RSV1 of the first frame of a message is called
      // "Per-Message Compressed" bit. It can be either 0 or 1.
      // In other words, any value is okay.
      return true;
    }

    // Further checking is required.
    return false;
  }




  /**
   * Verify the RSV2 bit of a frame.
   */
  private void verifyReservedBit2(final WebSocketFrame frame) throws WebSocketException {
    if (frame.getRsv2() == false) {
      // No problem.
      return;
    }

    // The RSV2 bit of a frame is set unexpectedly.
    throw new WebSocketException(WebSocketError.UNEXPECTED_RESERVED_BIT, "The RSV2 bit of a frame is set unexpectedly.");
  }




  /**
   * Verify the RSV3 bit of a frame.
   */
  private void verifyReservedBit3(final WebSocketFrame frame) throws WebSocketException {
    if (frame.getRsv3() == false) {
      // No problem.
      return;
    }

    // The RSV3 bit of a frame is set unexpectedly.
    throw new WebSocketException(WebSocketError.UNEXPECTED_RESERVED_BIT, "The RSV3 bit of a frame is set unexpectedly.");
  }




  private void verifyReservedBits(final WebSocketFrame frame) throws WebSocketException {
    // If extended use of web socket frames is allowed.
    if (webSocket.isExtended()) {
      // Do not check RSV1/RSV2/RSV3 bits.
      return;
    }

    // The specification requires that these bits "be 0 unless an extension
    // is negotiated that defines meanings for non-zero values".

    verifyReservedBit1(frame);
    verifyReservedBit2(frame);
    verifyReservedBit3(frame);
  }




  private void waitForCloseFrame() {
    if (noWaitForCloseFrame) {
      return;
    }

    // If a close frame has already been received.
    if (closeFrame != null) {
      return;
    }

    WebSocketFrame frame = null;

    // Schedule a task which calls Socket.close() to prevent the code below 
    // from looping forever.
    scheduleClose();

    while (true) {
      try {
        // Read a frame from the server.
        frame = webSocket.getInput().readFrame();
      } catch (final Throwable t) {
        // Give up receiving a close frame.
        break;
      }

      // If it is a close frame.
      if (frame.isCloseFrame()) {
        // Received a close frame. Finished.
        closeFrame = frame;
        break;
      }

      if (isInterrupted()) {
        break;
      }
    }
  }

}
