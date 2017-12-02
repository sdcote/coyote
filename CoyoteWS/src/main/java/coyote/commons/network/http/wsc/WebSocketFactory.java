package coyote.commons.network.http.wsc;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


/**
 * Factory to create {@link WebSocket} instances.
 */
public class WebSocketFactory {
  private final SocketFactorySettings socketFactorySettings;
  private final ProxySettings proxySettings;
  private int connectionTimeout;
  private boolean verifyHostname = true;




  /**
   * Create a webSocketFactory with the default setting.
   */
  public WebSocketFactory() {
    socketFactorySettings = new SocketFactorySettings();
    proxySettings = new ProxySettings(this);
  }




  private SocketConnector createDirectRawSocket(final String host, final int port, final boolean secure, final int timeout) throws IOException {
    // Select a socket factory.
    final SocketFactory factory = socketFactorySettings.selectSocketFactory(secure);

    // Let the socket factory create a socket.
    final Socket socket = factory.createSocket();

    // The address to connect to.
    final Address address = new Address(host, port);

    // Create an instance that will execute the task to connect to the server later.
    return new SocketConnector(socket, address, timeout).setVerifyHostname(verifyHostname);
  }




  private SocketConnector createProxiedRawSocket(final String host, final int port, final boolean secure, final int timeout) throws IOException {
    // Determine the port number of the proxy server.
    // Especially, if getPort() returns -1, the value
    // is converted to 80 or 443.
    final int proxyPort = determinePort(proxySettings.getPort(), proxySettings.isSecure());

    // Select a socket factory.
    final SocketFactory socketFactory = proxySettings.selectSocketFactory();

    // Let the socket factory create a socket.
    final Socket socket = socketFactory.createSocket();

    // The address to connect to.
    final Address address = new Address(proxySettings.getHost(), proxyPort);

    // The delegatee for the handshake with the proxy.
    final ProxyHandshaker handshaker = new ProxyHandshaker(socket, host, port, proxySettings);

    // SSLSocketFactory for SSL handshake with the WebSocket endpoint.
    final SSLSocketFactory sslSocketFactory = secure ? (SSLSocketFactory)socketFactorySettings.selectSocketFactory(secure) : null;

    // Create an instance that will execute the task to connect to the server later.
    return new SocketConnector(socket, address, timeout, handshaker, sslSocketFactory, host, port).setVerifyHostname(verifyHostname);
  }




  private SocketConnector createRawSocket(final String host, int port, final boolean secure, final int timeout) throws IOException {
    // Determine the port number. Especially, if 'port' is -1,
    // it is converted to 80 or 443.
    port = determinePort(port, secure);

    // True if a proxy server should be used.
    final boolean proxied = (proxySettings.getHost() != null);

    // See "Figure 2 -- Proxy server traversal decision tree" at
    // http://www.infoq.com/articles/Web-Sockets-Proxy-Servers

    if (proxied) {
      // Create a connector to connect to the proxy server.
      return createProxiedRawSocket(host, port, secure, timeout);
    } else {
      // Create a connector to connect to the WebSocket endpoint directly.
      return createDirectRawSocket(host, port, secure, timeout);
    }
  }




  /**
   * Create a WebSocket.
   *
   * <p>This method is an alias of {@link #createSocket(String, int)
   * createSocket}{@code (uri, }{@link #getConnectionTimeout()}{@code )}.
   * </p>
   *
   * @param uri The URI of the WebSocket endpoint on the server side.
   *
   * @return A WebSocket.
   *
   * @throws IllegalArgumentException The given URI is {@code null} or violates
   *         RFC 2396.
   *
   * @throws IOException Failed to create a socket. Or, HTTP proxy handshake 
   *         or SSL handshake failed.
   */
  public WebSocket createSocket(final String uri) throws IOException {
    return createSocket(uri, getConnectionTimeout());
  }




  /**
   * Create a WebSocket.
   *
   * <p>This method is an alias of {@link #createSocket(URI, int) 
   * createSocket}{@code (}{@link URI#create(String) URI.create}{@code (uri), 
   * timeout)}.
   *
   * @param uri The URI of the WebSocket endpoint on the server side.
   *
   * @param timeout The timeout value in milliseconds for socket connection. 
   *        A timeout of zero is interpreted as an infinite timeout.
   *
   * @return A WebSocket.
   *
   * @throws IllegalArgumentException The given URI is {@code null} or 
   *         violates RFC 2396, or the given timeout value is negative.
   * @throws IOException Failed to create a socket. Or, HTTP proxy handshake 
   *         or SSL handshake failed.
   */
  public WebSocket createSocket(final String uri, final int timeout) throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("The given URI is null.");
    }

    if (timeout < 0) {
      throw new IllegalArgumentException("The given timeout value is negative.");
    }

    return createSocket(URI.create(uri), timeout);
  }




  private WebSocket createSocket(final String scheme, final String userInfo, final String host, final int port, String path, final String query, final int timeout) throws IOException {
    // True if 'scheme' is 'wss' or 'https'.
    final boolean secure = isSecureConnectionRequired(scheme);

    // Check if 'host' is specified.
    if (host == null || host.length() == 0) {
      throw new IllegalArgumentException("The host part is empty.");
    }

    // Determine the path.
    path = determinePath(path);

    // Create a Socket instance and a connector to connect to the server.
    final SocketConnector connector = createRawSocket(host, port, secure, timeout);

    // Create a WebSocket instance.
    return createWebSocket(secure, userInfo, host, port, path, query, connector);
  }




  /**
   * Create a WebSocket. 
   * 
   * <p>This method is an alias of {@link #createSocket(URI, int)
   * createSocket}{@code (uri, }{@link #getConnectionTimeout()}{@code )}.
   *
   * <p>A socket factory (= a {@link SocketFactory} instance) to create a raw
   * socket (= a {@link Socket} instance) is determined as described below.
   *
   * <ol>
   * <li>
   *   If the scheme of the URI is either {@code wss} or {@code https},
   *   <ol type="i">
   *     <li>
   *       If an {@link SSLContext} instance has been set by {@link
   *       #setSSLContext(SSLContext)}, the value returned from {@link
   *       SSLContext#getSocketFactory()} method of the instance is used.
   *     <li>
   *       Otherwise, if an {@link SSLSocketFactory} instance has been
   *       set by {@link #setSSLSocketFactory(SSLSocketFactory)}, the
   *       instance is used.
   *     <li>
   *       Otherwise, the value returned from {@link SSLSocketFactory#getDefault()}
   *       is used.
   *   </ol>
   * <li>
   *   Otherwise (= the scheme of the URI is either {@code ws} or {@code http}),
   *   <ol type="i">
   *     <li>
   *       If a {@link SocketFactory} instance has been set by {@link
   *       #setSocketFactory(SocketFactory)}, the instance is used.
   *     <li>
   *       Otherwise, the value returned from {@link SocketFactory#getDefault()}
   *       is used.
   *   </ol>
   * </ol>
   *
   * @param uri The URI of the WebSocket endpoint on the server side. The 
   * scheme part of the URI must be one of {@code ws}, {@code wss}, {@code 
   * http} and {@code https} (case-insensitive).
   *
   * @return A WebSocket.
   *
   * @throws IllegalArgumentException The given URI is {@code null} or violates 
   *         RFC 2396.
   *
   * @throws IOException Failed to create a socket.
   */
  public WebSocket createSocket(final URI uri) throws IOException {
    return createSocket(uri, getConnectionTimeout());
  }




  /**
   * Create a WebSocket.
   *
   * <p>A socket factory (= a {@link SocketFactory} instance) to create a raw
   * socket (= a {@link Socket} instance) is determined as described below.
   *
   * <ol>
   * <li>
   *   If the scheme of the URI is either {@code wss} or {@code https},
   *   <ol type="i">
   *     <li>
   *       If an {@link SSLContext} instance has been set by {@link
   *       #setSSLContext(SSLContext)}, the value returned from {@link
   *       SSLContext#getSocketFactory()} method of the instance is used.
   *     <li>
   *       Otherwise, if an {@link SSLSocketFactory} instance has been
   *       set by {@link #setSSLSocketFactory(SSLSocketFactory)}, the
   *       instance is used.
   *     <li>
   *       Otherwise, the value returned from {@link SSLSocketFactory#getDefault()}
   *       is used.
   *   </ol>
   * <li>
   *   Otherwise (= the scheme of the URI is either {@code ws} or {@code http}),
   *   <ol type="i">
   *     <li>
   *       If a {@link SocketFactory} instance has been set by {@link
   *       #setSocketFactory(SocketFactory)}, the instance is used.
   *     <li>
   *       Otherwise, the value returned from {@link SocketFactory#getDefault()}
   *       is used.
   *   </ol>
   * </ol>
   *
   * @param uri The URI of the WebSocket endpoint on the server side. The 
   * scheme part of the URI must be one of {@code ws}, {@code wss}, {@code 
   * http} and {@code https} (case-insensitive).
   *
   * @param timeout The timeout value in milliseconds for socket connection.
   *
   * @return A WebSocket.
   *
   * @throws IllegalArgumentException The given URI is {@code null} or violates
   *         RFC 2396, or the given timeout value is negative.
   *
   * @throws IOException Failed to create a socket.
   */
  public WebSocket createSocket(final URI uri, final int timeout) throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("The given URI is null.");
    }

    if (timeout < 0) {
      throw new IllegalArgumentException("The given timeout value is negative.");
    }

    // Split the URI.
    final String scheme = uri.getScheme();
    final String userInfo = uri.getUserInfo();
    final String host = WebSocketUtil.extractHost(uri);
    final int port = uri.getPort();
    final String path = uri.getRawPath();
    final String query = uri.getRawQuery();

    return createSocket(scheme, userInfo, host, port, path, query, timeout);
  }




  /**
   * Create a WebSocket.
   *
   * <p>This method is an alias of {@link #createSocket(URL, int) createSocket}
   * {@code (url, }{@link #getConnectionTimeout()}{@code )}.
   *
   * @param url The URL of the WebSocket endpoint on the server side.
   *
   * @return A WebSocket.
   *
   * @throws IllegalArgumentException The given URL is {@code null} or failed 
   *         to be converted into a URI.
   *
   * @throws IOException Failed to create a socket. Or, HTTP proxy handshake 
   *         or SSL handshake failed.
   */
  public WebSocket createSocket(final URL url) throws IOException {
    return createSocket(url, getConnectionTimeout());
  }




  /**
   * Create a WebSocket.
   *
   * <p>This method is an alias of {@link #createSocket(URI, int) 
   * createSocket} {@code (url.}{@link URL#toURI() toURI()}{@code , 
   * timeout)}.
   *
   * @param url The URL of the WebSocket endpoint on the server side.
   *
   * @param timeout The timeout value in milliseconds for socket connection.
   *
   * @return A WebSocket.
   *
   * @throws IllegalArgumentException The given URL is {@code null} or failed 
   *         to be converted into a URI, or the given timeout value is 
   *         negative.
   *
   * @throws IOException Failed to create a socket. Or, HTTP proxy handshake 
   *         or SSL handshake failed.
   */
  public WebSocket createSocket(final URL url, final int timeout) throws IOException {
    if (url == null) {
      throw new IllegalArgumentException("The given URL is null.");
    }

    if (timeout < 0) {
      throw new IllegalArgumentException("The given timeout value is negative.");
    }

    try {
      return createSocket(url.toURI(), timeout);
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException("Failed to convert the given URL into a URI.");
    }
  }




  private WebSocket createWebSocket(final boolean secure, final String userInfo, String host, final int port, String path, final String query, final SocketConnector connector) {
    // The value for "Host" HTTP header.
    if (0 <= port) {
      host = host + ":" + port;
    }

    // The value for Request-URI of Request-Line.
    if (query != null) {
      path = path + "?" + query;
    }

    return new WebSocket(this, secure, userInfo, host, path, connector);
  }




  /**
   * Get the timeout value in milliseconds for socket connection.
   * 
   * <p>The default value is 0 and it means an infinite timeout.
   * <p>When a {@code createSocket} method which does not have {@code timeout} 
   * argument is called, the value returned by this method is used as a 
   * timeout value for socket connection.
   *
   * @return The connection timeout value in milliseconds.
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }




  /**
   * Get the proxy settings.
   *
   * @return The proxy settings.
   *
   * @see ProxySettings
   */
  public ProxySettings getProxySettings() {
    return proxySettings;
  }




  /**
   * Get the socket factory that has been set by {@link
   * #setSocketFactory(SocketFactory)}.
   *
   * @return The socket factory.
   */
  public SocketFactory getSocketFactory() {
    return socketFactorySettings.getSocketFactory();
  }




  /**
   * Get the SSL context that has been set by {@link 
   * #setSSLContext(SSLContext)}.
   *
   * @return The SSL context.
   */
  public SSLContext getSSLContext() {
    return socketFactorySettings.getSSLContext();
  }




  /**
   * Get the SSL socket factory that has been set by {@link
   * #setSSLSocketFactory(SSLSocketFactory)}.
   *
   * @return The SSL socket factory.
   */
  public SSLSocketFactory getSSLSocketFactory() {
    return socketFactorySettings.getSSLSocketFactory();
  }




  /**
   * Get the flag which indicates whether the hostname in the server's 
   * certificate should be verified or not. 
   * 
   * <p>The default value is {@code true}. See the description of {@link 
   * #setVerifyHostname(boolean)} to understand what this boolean flag means.
   *
   * @return {@code true} if hostname verification is enabled.
   */
  public boolean getVerifyHostname() {
    return verifyHostname;
  }




  /**
   * Set the timeout value in milliseconds for socket connection.
   * 
   * <p>A timeout of zero is interpreted as an infinite timeout.
   *
   * @param timeout The connection timeout value in milliseconds.
   *
   * @return {@code this} object.
   *
   * @throws IllegalArgumentException The given timeout value is negative.
   */
  public WebSocketFactory setConnectionTimeout(final int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout value cannot be negative.");
    }
    connectionTimeout = timeout;
    return this;
  }




  /**
   * Set a socket factory.
   * 
   * <p>See {@link #createSocket(URI)} for details.
   *
   * @param factory A socket factory.
   *
   * @return {@code this} instance.
   */
  public WebSocketFactory setSocketFactory(final SocketFactory factory) {
    socketFactorySettings.setSocketFactory(factory);
    return this;
  }




  /**
   * Set an SSL context to get a socket factory.
   * 
   * <p>See {@link #createSocket(URI)} for details.
   *
   * @param context An SSL context.
   *
   * @return {@code this} instance.
   */
  public WebSocketFactory setSSLContext(final SSLContext context) {
    socketFactorySettings.setSSLContext(context);
    return this;
  }




  /**
   * Set an SSL socket factory.
   * 
   * <p>See {@link #createSocket(URI)} for details.
   *
   * @param factory An SSL socket factory.
   *
   * @return {@code this} instance.
   */
  public WebSocketFactory setSSLSocketFactory(final SSLSocketFactory factory) {
    socketFactorySettings.setSSLSocketFactory(factory);
    return this;
  }




  /**
   * Set the flag which indicates whether the hostname in the
   * server's certificate should be verified or not. The default
   * value is {@code true}.
   *
   * <p>Manual hostname verification has been enabled since the version 2.1.
   * Because the verification is executed manually after {@code Socket.}{@link
   * java.net.Socket#connect(java.net.SocketAddress, int)
   * connect(SocketAddress, int)} succeeds, the hostname verification is
   * always executed even if you has passed an {@link SSLContext} which
   * naively accepts any server certificate (e.g. {@code NaiveSSLContext}).
   * However, this behavior is not desirable in some cases and you may want to
   * disable the hostname verification. This setter method exists for the
   * purpose and you can disable hostname verification by passing {@code false}
   * to this method.
   *
   * @param verifyHostname {@code true} to enable hostname verification.
   *        {@code false} to disable hostname verification.
   *
   * @return {@code this} object.
   */
  public WebSocketFactory setVerifyHostname(final boolean verifyHostname) {
    this.verifyHostname = verifyHostname;
    return this;
  }




  private static String determinePath(final String path) {
    if (path == null || path.length() == 0) {
      return "/";
    }

    if (path.startsWith("/")) {
      return path;
    } else {
      return "/" + path;
    }
  }




  private static int determinePort(final int port, final boolean secure) {
    if (0 <= port) {
      return port;
    }

    if (secure) {
      return 443;
    } else {
      return 80;
    }
  }




  private static boolean isSecureConnectionRequired(final String scheme) {
    if (scheme == null || scheme.length() == 0) {
      throw new IllegalArgumentException("The scheme part is empty.");
    }

    if ("wss".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
      return true;
    }

    if ("ws".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
      return false;
    }

    throw new IllegalArgumentException("Bad scheme: " + scheme);
  }

}
