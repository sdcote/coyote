/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
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

  private final InputStream in;

  private OutputStream out;

  private WebSocketFrame.OpCode continuousOpCode = null;

  private final List<WebSocketFrame> continuousFrames = new LinkedList<WebSocketFrame>();

  private State state = State.UNCONNECTED;

  private final IHTTPSession handshakeRequest;

  private final Response handshakeResponse = new Response( Status.SWITCH_PROTOCOL, null, (InputStream)null, 0 ) {

    @Override
    protected void send( OutputStream out ) {
      WebSocket.this.out = out;
      WebSocket.this.state = State.CONNECTING;
      super.send( out );
      WebSocket.this.state = State.OPEN;
      WebSocket.this.onOpen();
      readWebsocket();
    }
  };




  public WebSocket( IHTTPSession handshakeRequest ) {
    this.handshakeRequest = handshakeRequest;
    this.in = handshakeRequest.getInputStream();

    this.handshakeResponse.addHeader( WebSocketDaemon.HEADER_UPGRADE, WebSocketDaemon.HEADER_UPGRADE_VALUE );
    this.handshakeResponse.addHeader( WebSocketDaemon.HEADER_CONNECTION, WebSocketDaemon.HEADER_CONNECTION_VALUE );
  }




  public boolean isOpen() {
    return state == State.OPEN;
  }




  protected abstract void onOpen();




  protected abstract void onClose( CloseCode code, String reason, boolean initiatedByRemote );




  protected abstract void onMessage( WebSocketFrame message );




  protected abstract void onPong( WebSocketFrame pong );




  protected abstract void onException( IOException exception );




  /**
   * Debug method. <b>Do not Override unless for debug purposes!</b>
   * 
   * @param frame The received WebSocket Frame.
   */
  protected void debugFrameReceived( WebSocketFrame frame ) {}




  /**
   * Debug method. <b>Do not Override unless for debug purposes!</b><br>
   * This method is called before actually sending the frame.
   * 
   * @param frame The sent WebSocket Frame.
   */
  protected void debugFrameSent( WebSocketFrame frame ) {}




  public void close( CloseCode code, String reason, boolean initiatedByRemote ) throws IOException {
    State oldState = this.state;
    this.state = State.CLOSING;
    if ( oldState == State.OPEN ) {
      sendFrame( new CloseFrame( code, reason ) );
    } else {
      doClose( code, reason, initiatedByRemote );
    }
  }




  private void doClose( CloseCode code, String reason, boolean initiatedByRemote ) {
    if ( this.state == State.CLOSED ) {
      return;
    }
    if ( this.in != null ) {
      try {
        this.in.close();
      } catch ( IOException e ) {
        WebSocketDaemon.LOG.log( Level.FINE, "close failed", e );
      }
    }
    if ( this.out != null ) {
      try {
        this.out.close();
      } catch ( IOException e ) {
        WebSocketDaemon.LOG.log( Level.FINE, "close failed", e );
      }
    }
    this.state = State.CLOSED;
    onClose( code, reason, initiatedByRemote );
  }




  public IHTTPSession getHandshakeRequest() {
    return this.handshakeRequest;
  }




  public Response getHandshakeResponse() {
    return this.handshakeResponse;
  }




  private void handleCloseFrame( WebSocketFrame frame ) throws IOException {
    CloseCode code = CloseCode.NormalClosure;
    String reason = "";
    if ( frame instanceof CloseFrame ) {
      code = ( (CloseFrame)frame ).getCloseCode();
      reason = ( (CloseFrame)frame ).getCloseReason();
    }
    if ( this.state == State.CLOSING ) {
      doClose( code, reason, false );
    } else {
      close( code, reason, true );
    }
  }




  private void handleFrameFragment( WebSocketFrame frame ) throws IOException {
    if ( frame.getOpCode() != OpCode.Continuation ) {
      if ( this.continuousOpCode != null ) {
        throw new WebSocketException( CloseCode.ProtocolError, "Previous continuous frame sequence not completed." );
      }
      this.continuousOpCode = frame.getOpCode();
      this.continuousFrames.clear();
      this.continuousFrames.add( frame );
    } else if ( frame.isFin() ) {
      if ( this.continuousOpCode == null ) {
        throw new WebSocketException( CloseCode.ProtocolError, "Continuous frame sequence was not started." );
      }
      onMessage( new WebSocketFrame( this.continuousOpCode, this.continuousFrames ) );
      this.continuousOpCode = null;
      this.continuousFrames.clear();
    } else if ( this.continuousOpCode == null ) {
      throw new WebSocketException( CloseCode.ProtocolError, "Continuous frame sequence was not started." );
    } else {
      this.continuousFrames.add( frame );
    }
  }




  private void handleWebsocketFrame( WebSocketFrame frame ) throws IOException {
    debugFrameReceived( frame );
    if ( frame.getOpCode() == OpCode.Close ) {
      handleCloseFrame( frame );
    } else if ( frame.getOpCode() == OpCode.Ping ) {
      sendFrame( new WebSocketFrame( OpCode.Pong, true, frame.getBinaryPayload() ) );
    } else if ( frame.getOpCode() == OpCode.Pong ) {
      onPong( frame );
    } else if ( !frame.isFin() || frame.getOpCode() == OpCode.Continuation ) {
      handleFrameFragment( frame );
    } else if ( this.continuousOpCode != null ) {
      throw new WebSocketException( CloseCode.ProtocolError, "Continuous frame sequence not completed." );
    } else if ( frame.getOpCode() == OpCode.Text || frame.getOpCode() == OpCode.Binary ) {
      onMessage( frame );
    } else {
      throw new WebSocketException( CloseCode.ProtocolError, "Non control or continuous frame expected." );
    }
  }




  public void ping( byte[] payload ) throws IOException {
    sendFrame( new WebSocketFrame( OpCode.Ping, true, payload ) );
  }




  private void readWebsocket() {
    try {
      while ( this.state == State.OPEN ) {
        handleWebsocketFrame( WebSocketFrame.read( this.in ) );
      }
    } catch ( CharacterCodingException e ) {
      onException( e );
      doClose( CloseCode.InvalidFramePayloadData, e.toString(), false );
    } catch ( IOException e ) {
      onException( e );
      if ( e instanceof WebSocketException ) {
        doClose( ( (WebSocketException)e ).getCode(), ( (WebSocketException)e ).getReason(), false );
      }
    }
    finally {
      doClose( CloseCode.InternalServerError, "Responder terminated without closing the connection.", false );
    }
  }




  public void send( byte[] payload ) throws IOException {
    sendFrame( new WebSocketFrame( OpCode.Binary, true, payload ) );
  }




  public void send( String payload ) throws IOException {
    sendFrame( new WebSocketFrame( OpCode.Text, true, payload ) );
  }




  public synchronized void sendFrame( WebSocketFrame frame ) throws IOException {
    debugFrameSent( frame );
    frame.write( this.out );
  }
}