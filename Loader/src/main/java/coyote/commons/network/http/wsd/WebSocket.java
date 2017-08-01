package coyote.commons.network.http.wsd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.CharacterCodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.wsd.WebSocketDaemon.State;
import coyote.commons.network.http.wsd.WebSocketFrame.CloseCode;
import coyote.commons.network.http.wsd.WebSocketFrame.CloseFrame;
import coyote.commons.network.http.wsd.WebSocketFrame.OpCode;


public abstract class WebSocket {

  private final List<WebSocketFrame> continuousFrames = new LinkedList<WebSocketFrame>();

  private WebSocketFrame.OpCode continuousOpCode = null;

  private final IHTTPSession handshakeRequest;

  private final Response handshakeResponse = new Response(Status.SWITCH_PROTOCOL, null, (InputStream)null, 0) {

    @Override
    protected void send(final OutputStream out) {
      WebSocket.this.out = out;
      state = State.CONNECTING;
      super.send(out);
      state = State.OPEN;
      WebSocket.this.onOpen();
      readWebsocket();
    }
  };

  private final InputStream in;

  private OutputStream out;

  private State state = State.UNCONNECTED;




  public WebSocket(final IHTTPSession handshakeRequest) {
    this.handshakeRequest = handshakeRequest;
    in = handshakeRequest.getInputStream();

    handshakeResponse.addHeader(WebSocketDaemon.HEADER_UPGRADE, WebSocketDaemon.HEADER_UPGRADE_VALUE);
    handshakeResponse.addHeader(WebSocketDaemon.HEADER_CONNECTION, WebSocketDaemon.HEADER_CONNECTION_VALUE);
  }




  public void close(final CloseCode code, final String reason, final boolean initiatedByRemote) throws IOException {
    final State oldState = state;
    state = State.CLOSING;
    if (oldState == State.OPEN) {
      sendFrame(new CloseFrame(code, reason));
    } else {
      doClose(code, reason, initiatedByRemote);
    }
  }




  public IHTTPSession getHandshakeRequest() {
    return handshakeRequest;
  }




  public Response getHandshakeResponse() {
    return handshakeResponse;
  }




  public boolean isOpen() {
    return state == State.OPEN;
  }




  public void ping(final byte[] payload) throws IOException {
    sendFrame(new WebSocketFrame(OpCode.Ping, true, payload));
  }




  public void send(final byte[] payload) throws IOException {
    sendFrame(new WebSocketFrame(OpCode.Binary, true, payload));
  }




  public void send(final String payload) throws IOException {
    sendFrame(new WebSocketFrame(OpCode.Text, true, payload));
  }




  public synchronized void sendFrame(final WebSocketFrame frame) throws IOException {
    debugFrameSent(frame);
    frame.write(out);
  }




  private void doClose(final CloseCode code, final String reason, final boolean initiatedByRemote) {
    if (state == State.CLOSED) {
      return;
    }
    if (in != null) {
      try {
        in.close();
      } catch (final IOException e) {
        WebSocketDaemon.LOG.log(Level.FINE, "close failed", e);
      }
    }
    if (out != null) {
      try {
        out.close();
      } catch (final IOException e) {
        WebSocketDaemon.LOG.log(Level.FINE, "close failed", e);
      }
    }
    state = State.CLOSED;
    onClose(code, reason, initiatedByRemote);
  }




  private void handleCloseFrame(final WebSocketFrame frame) throws IOException {
    CloseCode code = CloseCode.NormalClosure;
    String reason = "";
    if (frame instanceof CloseFrame) {
      code = ((CloseFrame)frame).getCloseCode();
      reason = ((CloseFrame)frame).getCloseReason();
    }
    if (state == State.CLOSING) {
      doClose(code, reason, false);
    } else {
      close(code, reason, true);
    }
  }




  private void handleFrameFragment(final WebSocketFrame frame) throws IOException {
    if (frame.getOpCode() != OpCode.Continuation) {
      if (continuousOpCode != null) {
        throw new WebSocketException(CloseCode.ProtocolError, "Previous continuous frame sequence not completed.");
      }
      continuousOpCode = frame.getOpCode();
      continuousFrames.clear();
      continuousFrames.add(frame);
    } else if (frame.isFin()) {
      if (continuousOpCode == null) {
        throw new WebSocketException(CloseCode.ProtocolError, "Continuous frame sequence was not started.");
      }
      onMessage(new WebSocketFrame(continuousOpCode, continuousFrames));
      continuousOpCode = null;
      continuousFrames.clear();
    } else if (continuousOpCode == null) {
      throw new WebSocketException(CloseCode.ProtocolError, "Continuous frame sequence was not started.");
    } else {
      continuousFrames.add(frame);
    }
  }




  private void handleWebsocketFrame(final WebSocketFrame frame) throws IOException {
    debugFrameReceived(frame);
    if (frame.getOpCode() == OpCode.Close) {
      handleCloseFrame(frame);
    } else if (frame.getOpCode() == OpCode.Ping) {
      sendFrame(new WebSocketFrame(OpCode.Pong, true, frame.getBinaryPayload()));
    } else if (frame.getOpCode() == OpCode.Pong) {
      onPong(frame);
    } else if (!frame.isFin() || (frame.getOpCode() == OpCode.Continuation)) {
      handleFrameFragment(frame);
    } else if (continuousOpCode != null) {
      throw new WebSocketException(CloseCode.ProtocolError, "Continuous frame sequence not completed.");
    } else if ((frame.getOpCode() == OpCode.Text) || (frame.getOpCode() == OpCode.Binary)) {
      onMessage(frame);
    } else {
      throw new WebSocketException(CloseCode.ProtocolError, "Non control or continuous frame expected.");
    }
  }




  private void readWebsocket() {
    try {
      while (state == State.OPEN) {
        handleWebsocketFrame(WebSocketFrame.read(in));
      }
    } catch (final CharacterCodingException e) {
      onException(e);
      doClose(CloseCode.InvalidFramePayloadData, e.toString(), false);
    } catch (final IOException e) {
      onException(e);
      if (e instanceof WebSocketException) {
        doClose(((WebSocketException)e).getCode(), ((WebSocketException)e).getReason(), false);
      }
    }
    finally {
      doClose(CloseCode.InternalServerError, "Responder terminated without closing the connection.", false);
    }
  }




  /**
   * Debug method. <b>Do not Override unless for debug purposes!</b>
   *
   * @param frame The received WebSocket Frame.
   */
  protected void debugFrameReceived(final WebSocketFrame frame) {}




  /**
   * Debug method. <b>Do not Override unless for debug purposes!</b><br>
   * This method is called before actually sending the frame.
   *
   * @param frame The sent WebSocket Frame.
   */
  protected void debugFrameSent(final WebSocketFrame frame) {}




  protected abstract void onClose(CloseCode code, String reason, boolean initiatedByRemote);




  protected abstract void onException(IOException exception);




  protected abstract void onMessage(WebSocketFrame message);




  protected abstract void onOpen();




  protected abstract void onPong(WebSocketFrame pong);
}