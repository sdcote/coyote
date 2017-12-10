package coyote.commons.network.http.wsc;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


/**
 * A class to connect to the server.
 */
class SocketConnector {
  private Socket socket;
  private final Address address;
  private final int connectionTimeout;
  private final ProxyHandshaker proxyHandshaker;
  private final SSLSocketFactory sslSocketFactory;
  private final String host;
  private final int port;
  private boolean verifyHostname;




  SocketConnector(final Socket socket, final Address address, final int timeout) {
    this(socket, address, timeout, null, null, null, 0);
  }




  SocketConnector(final Socket socket, final Address address, final int timeout, final ProxyHandshaker handshaker, final SSLSocketFactory sslSocketFactory, final String host, final int port) {
    this.socket = socket;
    this.address = address;
    this.connectionTimeout = timeout;
    this.proxyHandshaker = handshaker;
    this.sslSocketFactory = sslSocketFactory;
    this.host = host;
    this.port = port;
  }




  void closeSilently() {
    try {
      socket.close();
    } catch (final Throwable t) {
      // Ignored.
    }
  }




  public void connect() throws WebSocketException {
    try {
      // Connect to the server (either a proxy or a WebSocket endpoint).
      doConnect();
    } catch (final WebSocketException e) {
      // Failed to connect the server.

      try {
        // Close the socket.
        socket.close();
      } catch (final IOException ioe) {
        // Ignore any error raised by close().
      }

      throw e;
    }
  }




  private void doConnect() throws WebSocketException {
    // True if a proxy server is set.
    final boolean proxied = proxyHandshaker != null;

    try {
      // Connect to the server (either a proxy or a WebSocket endpoint).
      socket.connect(address.toInetSocketAddress(), connectionTimeout);

      if (socket instanceof SSLSocket) {
        // Verify that the hostname matches the certificate here since
        // this is not automatically done by the SSLSocket.
        verifyHostname((SSLSocket)socket, address.getHostname());
      }
    } catch (final IOException e) {
      // Failed to connect the server.
      final String message = String.format("Failed to connect to %s'%s': %s", (proxied ? "the proxy " : ""), address, e.getMessage());

      // Raise an exception with SOCKET_CONNECT_ERROR.
      throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR, message, e);
    }

    // If a proxy server is set.
    if (proxied) {
      // Perform handshake with the proxy server.
      // SSL handshake is performed as necessary, too.
      handshake();
    }
  }




  public int getConnectionTimeout() {
    return connectionTimeout;
  }




  public Socket getSocket() {
    return socket;
  }




  /**
   * Perform proxy handshake and optionally SSL handshake.
   */
  private void handshake() throws WebSocketException {
    try {
      // Perform handshake with the proxy server.
      proxyHandshaker.perform();
    } catch (final IOException e) {
      // Handshake with the proxy server failed.
      final String message = String.format("Handshake with the proxy server (%s) failed: %s", address, e.getMessage());

      // Raise an exception with PROXY_HANDSHAKE_ERROR.
      throw new WebSocketException(WebSocketError.PROXY_HANDSHAKE_ERROR, message, e);
    }

    if (sslSocketFactory == null) {
      // SSL handshake with the WebSocket endpoint is not needed.
      return;
    }

    try {
      // Overlay the existing socket.
      socket = sslSocketFactory.createSocket(socket, host, port, true);
    } catch (final IOException e) {
      // Failed to overlay an existing socket.
      final String message = "Failed to overlay an existing socket: " + e.getMessage();

      // Raise an exception with SOCKET_OVERLAY_ERROR.
      throw new WebSocketException(WebSocketError.SOCKET_OVERLAY_ERROR, message, e);
    }

    try {
      // Start the SSL handshake manually. As for the reason, see
      // http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/samples/sockets/client/SSLSocketClient.java
      ((SSLSocket)socket).startHandshake();

      if (socket instanceof SSLSocket) {
        // Verify that the proxied hostname matches the certificate here since
        // this is not automatically done by the SSLSocket.
        verifyHostname((SSLSocket)socket, proxyHandshaker.getProxiedHostname());
      }
    } catch (final IOException e) {
      // SSL handshake with the WebSocket endpoint failed.
      final String message = String.format("SSL handshake with the WebSocket endpoint (%s) failed: %s", address, e.getMessage());

      // Raise an exception with SSL_HANDSHAKE_ERROR.
      throw new WebSocketException(WebSocketError.SSL_HANDSHAKE_ERROR, message, e);
    }
  }




  SocketConnector setVerifyHostname(final boolean verifyHostname) {
    this.verifyHostname = verifyHostname;

    return this;
  }




  private void verifyHostname(final SSLSocket socket, final String hostname) throws HostnameUnverifiedException {
    if (verifyHostname == false) {
      // Skip hostname verification.
      return;
    }

    // Hostname verifier.
    final DefaultHostnameVerifier verifier = DefaultHostnameVerifier.INSTANCE;

    // The SSL session.
    final SSLSession session = socket.getSession();

    // Verify the hostname.
    if (verifier.verify(hostname, session)) {
      // Verified. No problem.
      return;
    }

    // The certificate of the peer does not match the expected hostname.
    throw new HostnameUnverifiedException(socket, hostname);
  }

}
