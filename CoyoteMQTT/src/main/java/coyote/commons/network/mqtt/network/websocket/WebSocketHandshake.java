package coyote.commons.network.mqtt.network.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Helper class to execute a WebSocket Handshake.
 */
public class WebSocketHandshake {

  // Do not change: https://tools.ietf.org/html/rfc6455#section-1.3
  private static final String ACCEPT_SALT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  private static final String SHA1_PROTOCOL = "SHA1";
  private static final String HTTP_HEADER_SEC_WEBSOCKET_ACCEPT = "sec-websocket-accept";
  private static final String HTTP_HEADER_UPGRADE = "upgrade";
  private static final String HTTP_HEADER_UPGRADE_WEBSOCKET = "websocket";
  private static final String EMPTY = "";
  private static final String LINE_SEPARATOR = "\r\n";

  private static final String HTTP_HEADER_CONNECTION = "connection";
  private static final String HTTP_HEADER_CONNECTION_VALUE = "upgrade";
  private static final String HTTP_HEADER_SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";

  InputStream input;
  OutputStream output;
  String uri;
  String host;
  int port;




  public WebSocketHandshake( final InputStream input, final OutputStream output, final String uri, final String host, final int port ) {
    this.input = input;
    this.output = output;
    this.uri = uri;
    this.host = host;
    this.port = port;
  }




  /**
   * Executes a Websocket Handshake.
   * Will throw an IOException if the handshake fails
   * @throws IOException
   */
  public void execute() throws IOException {
    final String key = "mqtt-" + ( System.currentTimeMillis() / 1000 );
    final String b64Key = Base64.encode( key );
    sendHandshakeRequest( b64Key );
    receiveHandshakeResponse( b64Key );
  }




  /**
   * Returns a Hashmap of HTTP headers
   * 
   * @param headers ArrayList<String> of headers
   * 
   * @return A Hashmap<String, String> of the headers
   */
  private Map getHeaders( final ArrayList headers ) {
    final Map headerMap = new HashMap();
    for ( int i = 1; i < headers.size(); i++ ) {
      final String headerPre = (String)headers.get( i );
      final String[] header = headerPre.split( ":" );
      headerMap.put( header[0].toLowerCase(), header[1] );
    }
    return headerMap;
  }




  /**
   * Receives the Handshake response and verifies that it is valid.
   * @param Base64 encoded key
   * @throws IOException
   */
  private void receiveHandshakeResponse( final String key ) throws IOException {
    final BufferedReader in = new BufferedReader( new InputStreamReader( input ) );
    final ArrayList responseLines = new ArrayList();
    String line = in.readLine();
    if ( line == null ) {
      throw new IOException( "WebSocket Response header: Invalid response from Server, It may not support WebSockets." );
    }
    while ( !line.equals( EMPTY ) ) {
      responseLines.add( line );
      line = in.readLine();
    }
    final Map headerMap = getHeaders( responseLines );

    final String connectionHeader = (String)headerMap.get( HTTP_HEADER_CONNECTION );
    if ( ( connectionHeader == null ) || connectionHeader.equalsIgnoreCase( HTTP_HEADER_CONNECTION_VALUE ) ) {
      throw new IOException( "WebSocket Response header: Incorrect connection header" );
    }

    final String upgradeHeader = (String)headerMap.get( HTTP_HEADER_UPGRADE );
    if ( !upgradeHeader.toLowerCase().contains( HTTP_HEADER_UPGRADE_WEBSOCKET ) ) {
      throw new IOException( "WebSocket Response header: Incorrect upgrade." );
    }

    final String secWebsocketProtocolHeader = (String)headerMap.get( HTTP_HEADER_SEC_WEBSOCKET_PROTOCOL );
    if ( secWebsocketProtocolHeader == null ) {
      throw new IOException( "WebSocket Response header: empty sec-websocket-protocol" );
    }

    if ( !headerMap.containsKey( HTTP_HEADER_SEC_WEBSOCKET_ACCEPT ) ) {
      throw new IOException( "WebSocket Response header: Missing Sec-WebSocket-Accept" );
    }

    try {
      verifyWebSocketKey( key, (String)headerMap.get( HTTP_HEADER_SEC_WEBSOCKET_ACCEPT ) );
    } catch ( final NoSuchAlgorithmException e ) {
      throw new IOException( e.getMessage() );
    } catch ( final HandshakeFailedException e ) {
      throw new IOException( "WebSocket Response header: Incorrect Sec-WebSocket-Key" );
    }

  }




  /**
   * Builds and sends the HTTP Header GET Request
   * for the socket.
   * @param Base64 encoded key
   * @throws IOException
   */
  private void sendHandshakeRequest( final String key ) throws IOException {
    try {
      String path = "/mqtt";
      final URI srvUri = new URI( uri );
      if ( ( srvUri.getRawPath() != null ) && !srvUri.getRawPath().isEmpty() ) {
        path = srvUri.getRawPath();
        if ( ( srvUri.getRawQuery() != null ) && !srvUri.getRawQuery().isEmpty() ) {
          path += "?" + srvUri.getRawQuery();
        }
      }

      final PrintWriter pw = new PrintWriter( output );
      pw.print( "GET " + path + " HTTP/1.1" + LINE_SEPARATOR );
      pw.print( "Host: " + host + ":" + port + LINE_SEPARATOR );
      pw.print( "Upgrade: websocket" + LINE_SEPARATOR );
      pw.print( "Connection: Upgrade" + LINE_SEPARATOR );
      pw.print( "Sec-WebSocket-Key: " + key + LINE_SEPARATOR );
      pw.print( "Sec-WebSocket-Protocol: mqttv3.1" + LINE_SEPARATOR );
      pw.print( "Sec-WebSocket-Version: 13" + LINE_SEPARATOR );
      pw.print( LINE_SEPARATOR );
      pw.flush();
    } catch ( final URISyntaxException e ) {
      throw new IllegalStateException( e );
    }
  }




  /**
   * Returns the sha1 byte array of the provided string.
   * @param input
   * @return sha1 digest of the given string
   * @throws NoSuchAlgorithmException
   */
  private byte[] sha1( final String input ) throws NoSuchAlgorithmException {
    final MessageDigest mDigest = MessageDigest.getInstance( SHA1_PROTOCOL );
    final byte[] result = mDigest.digest( input.getBytes() );
    return result;
  }




  /**
   * Verifies that the Accept key provided is correctly built from the
   * original key sent.
   * @param key
   * @param accept
   * @throws NoSuchAlgorithmException
   * @throws HandshakeFailedException
   */
  private void verifyWebSocketKey( final String key, final String accept ) throws NoSuchAlgorithmException, HandshakeFailedException {
    // We build up the accept in the same way the server should
    // then we check that the response is the same.
    final byte[] sha1Bytes = sha1( key + ACCEPT_SALT );
    final String encodedSha1Bytes = Base64.encodeBytes( sha1Bytes ).trim();
    if ( !encodedSha1Bytes.equals( encodedSha1Bytes ) ) {
      throw new HandshakeFailedException();
    }
  }

}
