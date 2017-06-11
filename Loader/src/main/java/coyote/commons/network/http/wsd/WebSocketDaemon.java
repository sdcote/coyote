package coyote.commons.network.http.wsd;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Logger;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SecurityResponseException;
import coyote.commons.network.http.Status;


/**
 * Web Socket Daemon
 */
public abstract class WebSocketDaemon extends HTTPD {

  static final Logger LOG = Logger.getLogger( WebSocketDaemon.class.getName() );

  public static final String HEADER_UPGRADE = "upgrade";
  public static final String HEADER_UPGRADE_VALUE = "websocket";
  public static final String HEADER_CONNECTION = "connection";
  public static final String HEADER_CONNECTION_VALUE = "Upgrade";
  public static final String HEADER_WEBSOCKET_VERSION = "sec-websocket-version";
  public static final String HEADER_WEBSOCKET_VERSION_VALUE = "13";
  public static final String HEADER_WEBSOCKET_KEY = "sec-websocket-key";
  public static final String HEADER_WEBSOCKET_ACCEPT = "sec-websocket-accept";
  public static final String HEADER_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
  private final static String WEBSOCKET_KEY_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();


  public static enum State {
    UNCONNECTED, 
    CONNECTING, 
    OPEN, 
    CLOSING, 
    CLOSED
  }




  /**
   * Translates the specified byte array into Base64 string.
   * 
   * @param buf the byte array (not null)
   * 
   * @return the translated Base64 string (not null)
   */
  private static String encodeBase64( final byte[] buf ) {
    final int size = buf.length;
    final char[] ar = new char[( ( size + 2 ) / 3 ) * 4];
    int a = 0;
    int i = 0;
    while ( i < size ) {
      final byte b0 = buf[i++];
      final byte b1 = i < size ? buf[i++] : 0;
      final byte b2 = i < size ? buf[i++] : 0;

      final int mask = 0x3F;
      ar[a++] = WebSocketDaemon.ALPHABET[( b0 >> 2 ) & mask];
      ar[a++] = WebSocketDaemon.ALPHABET[( ( b0 << 4 ) | ( ( b1 & 0xFF ) >> 4 ) ) & mask];
      ar[a++] = WebSocketDaemon.ALPHABET[( ( b1 << 2 ) | ( ( b2 & 0xFF ) >> 6 ) ) & mask];
      ar[a++] = WebSocketDaemon.ALPHABET[b2 & mask];
    }
    switch ( size % 3 ) {
      case 1:
        ar[--a] = '=';
      case 2:
        ar[--a] = '=';
    }
    return new String( ar );
  }




  public static String makeAcceptKey( final String key ) throws NoSuchAlgorithmException {
    final MessageDigest md = MessageDigest.getInstance( "SHA-1" );
    final String text = key + WebSocketDaemon.WEBSOCKET_KEY_MAGIC;
    md.update( text.getBytes(), 0, text.length() );
    final byte[] sha1hash = md.digest();
    return encodeBase64( sha1hash );
  }




  public WebSocketDaemon( final int port ) {
    super( port );
  }




  public WebSocketDaemon( final String hostname, final int port ) {
    super( hostname, port );
  }




  private boolean isWebSocketConnectionHeader( final Map<String, String> headers ) {
    final String connection = headers.get( WebSocketDaemon.HEADER_CONNECTION );
    return ( connection != null ) && connection.toLowerCase().contains( WebSocketDaemon.HEADER_CONNECTION_VALUE.toLowerCase() );
  }




  protected boolean isWebsocketRequested( final IHTTPSession session ) {
    final Map<String, String> headers = session.getRequestHeaders();
    final String upgrade = headers.get( WebSocketDaemon.HEADER_UPGRADE );
    final boolean isCorrectConnection = isWebSocketConnectionHeader( headers );
    final boolean isUpgrade = WebSocketDaemon.HEADER_UPGRADE_VALUE.equalsIgnoreCase( upgrade );
    return isUpgrade && isCorrectConnection;
  }




  protected abstract WebSocket openWebSocket( IHTTPSession handshake );




  @Override
  public Response serve( final IHTTPSession session ) throws SecurityResponseException {
    final Map<String, String> headers = session.getRequestHeaders();
    if ( isWebsocketRequested( session ) ) {
      if ( !WebSocketDaemon.HEADER_WEBSOCKET_VERSION_VALUE.equalsIgnoreCase( headers.get( WebSocketDaemon.HEADER_WEBSOCKET_VERSION ) ) ) {
        return Response.createFixedLengthResponse( Status.BAD_REQUEST, MimeType.TEXT.getType(), "Invalid Websocket-Version " + headers.get( WebSocketDaemon.HEADER_WEBSOCKET_VERSION ) );
      }

      if ( !headers.containsKey( WebSocketDaemon.HEADER_WEBSOCKET_KEY ) ) {
        return Response.createFixedLengthResponse( Status.BAD_REQUEST, MimeType.TEXT.getType(), "Missing Websocket-Key" );
      }

      final WebSocket webSocket = openWebSocket( session );
      final Response handshakeResponse = webSocket.getHandshakeResponse();
      try {
        handshakeResponse.addHeader( WebSocketDaemon.HEADER_WEBSOCKET_ACCEPT, makeAcceptKey( headers.get( WebSocketDaemon.HEADER_WEBSOCKET_KEY ) ) );
      } catch ( final NoSuchAlgorithmException e ) {
        return Response.createFixedLengthResponse( Status.INTERNAL_ERROR, MimeType.TEXT.getType(), "The SHA-1 Algorithm required for websockets is not available on the server." );
      }

      if ( headers.containsKey( WebSocketDaemon.HEADER_WEBSOCKET_PROTOCOL ) ) {
        handshakeResponse.addHeader( WebSocketDaemon.HEADER_WEBSOCKET_PROTOCOL, headers.get( WebSocketDaemon.HEADER_WEBSOCKET_PROTOCOL ).split( "," )[0] );
      }

      return handshakeResponse;
    } else {
      return serveHttp( session );
    }
  }




  protected Response serveHttp( final IHTTPSession session ) throws SecurityResponseException {
    return super.serve( session );
  }




  /**
   * not all websockets implementations accept gzip compression.
   */
  @Override
  protected boolean useGzipWhenAccepted( final Response r ) {
    return false;
  }

}
