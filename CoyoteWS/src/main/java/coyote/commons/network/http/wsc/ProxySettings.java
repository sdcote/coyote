package coyote.commons.network.http.wsc;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


/**
 * Proxy settings.
 *
 * <p>If a proxy server's host name is set (= if {@link #getHost()} returns a
 * non-null value), a socket factory that creates a socket to communicate with
 * the proxy server is selected based on the settings of this
 * {@code ProxySettings} instance. The following is the concrete flow to
 * select a socket factory.
 *
 * <blockquote>
 * <ol>
 * <li>
 *   If {@link #isSecure()} returns {@code true},
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
 *   Otherwise (= {@link #isSecure()} returns {@code false}),
 *   <ol type="i">
 *     <li>
 *       If a {@link SocketFactory} instance has been set by {@link
 *       #setSocketFactory(SocketFactory)}, the instance is used.
 *     <li>
 *       Otherwise, the value returned from {@link SocketFactory#getDefault()}
 *       is used.
 *   </ol>
 * </ol>
 * </blockquote>
 *
 * <p>
 * Note that the current implementation supports only Basic Authentication
 * for authentication at the proxy server.
 *
 * @see WebSocketFactory#getProxySettings()
 */
public class ProxySettings {
  private final WebSocketFactory webSocketFactory;
  private final Map<String, List<String>> headers;
  private final SocketFactorySettings socketFactorySettings;
  private boolean secure;
  private String host;
  private int port;
  private String identifier;
  private String password;




  ProxySettings(final WebSocketFactory factory) {
    webSocketFactory = factory;
    headers = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
    socketFactorySettings = new SocketFactorySettings();
    reset();
  }




  /**
   * Add an additional HTTP header passed to the proxy server.
   *
   * @param name
   *         The name of an HTTP header (case-insensitive).
   *         If {@code null} or an empty string is given,
   *         nothing is added.
   *
   * @param value
   *         The value of the HTTP header.
   *
   * @return
   *         {@code this} object.
   */
  public ProxySettings addHeader(final String name, final String value) {
    if (name == null || name.length() == 0) {
      return this;
    }

    List<String> list = headers.get(name);

    if (list == null) {
      list = new ArrayList<String>();
      headers.put(name, list);
    }

    list.add(value);
    return this;
  }




  /**
   * Get additional HTTP headers passed to the proxy server.
   *
   * @return
   *         Additional HTTP headers passed to the proxy server.
   *         The comparator of the returned map is {@link
   *         String#CASE_INSENSITIVE_ORDER}.
   */
  public Map<String, List<String>> getHeaders() {
    return headers;
  }




  /**
   * Get the host name of the proxy server.
   *
   * <p>
   * The default value is {@code null}. If this method returns
   * a non-null value, it is used as the proxy server.
   * </p>
   *
   * @return
   *         The host name of the proxy server.
   */
  public String getHost() {
    return host;
  }




  /**
   * Get the ID for authentication at the proxy server.
   *
   * <p>
   * The default value is {@code null}. If this method returns
   * a non-null value, it is used as the ID for authentication
   * at the proxy server. To be concrete, the value is used to
   * generate the value of {@code <a href=
   * "http://tools.ietf.org/html/rfc2616#section-14.34"
   * >Proxy-Authorization</a>} header.
   * </p>
   *
   * @return
   *         The ID for authentication at the proxy server.
   */
  public String getId() {
    return identifier;
  }




  /**
   * Get the password for authentication at the proxy server.
   *
   * @return
   *         The password for authentication at the proxy server.
   */
  public String getPassword() {
    return password;
  }




  /**
   * Get the port number of the proxy server.
   *
   * <p>
   * The default value is {@code -1}. {@code -1} means that
   * the default port number ({@code 80} for non-secure
   * connections and {@code 443} for secure connections)
   * should be used.
   * </p>
   *
   * @return
   *         The port number of the proxy server.
   */
  public int getPort() {
    return port;
  }




  /**
   * Get the socket factory that has been set by {@link
   * #setSocketFactory(SocketFactory)}.
   *
   * @return
   *         The socket factory.
   */
  public SocketFactory getSocketFactory() {
    return socketFactorySettings.getSocketFactory();
  }




  /**
   * Get the SSL context that has been set by {@link #setSSLContext(SSLContext)}.
   *
   * @return
   *         The SSL context.
   */
  public SSLContext getSSLContext() {
    return socketFactorySettings.getSSLContext();
  }




  /**
   * Get the SSL socket factory that has been set by {@link
   * #setSSLSocketFactory(SSLSocketFactory)}.
   *
   * @return
   *         The SSL socket factory.
   */
  public SSLSocketFactory getSSLSocketFactory() {
    return socketFactorySettings.getSSLSocketFactory();
  }




  /**
   * Get the associated {@link WebSocketFactory} instance.
   * @return the websocket factory instance for this proxy
   */
  public WebSocketFactory getWebSocketFactory() {
    return webSocketFactory;
  }




  /**
   * Check whether use of TLS is enabled or disabled.
   *
   * @return
   *         {@code true} if TLS is used in the communication with
   *         the proxy server.
   */
  public boolean isSecure() {
    return secure;
  }




  /**
   * Reset the proxy settings. To be concrete, parameter values are
   * set as shown below.
   *
   * <blockquote>
   * <table summary="" border="1" cellpadding="5" style="border-collapse: collapse;">
   *   <thead>
   *     <tr>
   *       <th>Name</th>
   *       <th>Value</th>
   *       <th>Description</th>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td>Secure</td>
   *       <td>{@code false}</td>
   *       <td>Use TLS to connect to the proxy server or not.</td>
   *     </tr>
   *     <tr>
   *       <td>Host</td>
   *       <td>{@code null}</td>
   *       <td>The host name of the proxy server.</td>
   *     </tr>
   *     <tr>
   *       <td>Port</td>
   *       <td>{@code -1}</td>
   *       <td>The port number of the proxy server.</td>
   *     </tr>
   *     <tr>
   *       <td>ID</td>
   *       <td>{@code null}</td>
   *       <td>The ID for authentication at the proxy server.</td>
   *     </tr>
   *     <tr>
   *       <td>Password</td>
   *       <td>{@code null}</td>
   *       <td>The password for authentication at the proxy server.</td>
   *     </tr>
   *     <tr>
   *       <td>Headers</td>
   *       <td>Cleared</td>
   *       <td>Additional HTTP headers passed to the proxy server.</td>
   *     </tr>
   *   </tbody>
   * </table>
   * </blockquote>
   *
   * @return
   *         {@code this} object.
   */
  public ProxySettings reset() {
    secure = false;
    host = null;
    port = -1;
    identifier = null;
    password = null;
    headers.clear();
    return this;
  }




  SocketFactory selectSocketFactory() {
    return socketFactorySettings.selectSocketFactory(secure);
  }




  private void setByScheme(final String scheme) {
    if ("http".equalsIgnoreCase(scheme)) {
      secure = false;
    } else if ("https".equalsIgnoreCase(scheme)) {
      secure = true;
    }
  }




  private void setByUserInfo(final String userInfo) {
    if (userInfo == null) {
      return;
    }

    final String[] pair = userInfo.split(":", 2);
    String id;
    String pw;

    switch (pair.length) {
      case 2:
        id = pair[0];
        pw = pair[1];
        break;

      case 1:
        id = pair[0];
        pw = null;
        break;

      default:
        return;
    }

    if (id.length() == 0) {
      return;
    }

    identifier = id;
    password = pw;
  }




  /**
   * Set credentials for authentication at the proxy server.
   * 
   * <p>This method is an alias of {@link #setId(String) setId}{@code (id).}
   * {@link #setPassword(String) setPassword}{@code (password)}.
   *
   * @param id The ID.
   *
   * @param password The password.
   *
   * @return {@code this} object.
   */
  public ProxySettings setCredentials(final String id, final String password) {
    return setId(id).setPassword(password);
  }




  /**
   * Set the host name of the proxy server.
   *
   * <p>If a non-null value is set, it is used as the proxy server.
   *
   * @param host The host name of the proxy server.
   *
   * @return {@code this} object.
   */
  public ProxySettings setHost(final String host) {
    this.host = host;
    return this;
  }




  /**
   * Set the ID for authentication at the proxy server.
   *
   * <p>If a non-null value is set, it is used as the ID for authentication at 
   * the proxy server. To be concrete, the value is used to generate the value 
   * of {@code <a href="http://tools.ietf.org/html/rfc2616#section-14.34">
   * Proxy-Authorization</a>} header.
   *
   * @param id The ID for authentication at the proxy server.
   *
   * @return {@code this} object.
   */
  public ProxySettings setId(final String id) {
    this.identifier = id;
    return this;
  }




  /**
   * Set the password for authentication at the proxy server.
   *
   * @param password The password for authentication at the proxy server.
   *
   * @return {@code this} object.
   */
  public ProxySettings setPassword(final String password) {
    this.password = password;
    return this;
  }




  /**
   * Set the port number of the proxy server.
   *
   * <p>If {@code -1} is set, the default port number ({@code 80} for non-
   * secure connections and {@code 443} for secure connections) is used.
   *
   * @param port The port number of the proxy server.
   *
   * @return {@code this} object.
   */
  public ProxySettings setPort(final int port) {
    this.port = port;
    return this;
  }




  /**
   * Enable or disable use of TLS.
   *
   * @param secure {@code true} to use TLS in the communication with the proxy 
   * server.
   *
   * @return {@code this} object.
   */
  public ProxySettings setSecure(final boolean secure) {
    this.secure = secure;
    return this;
  }




  /**
   * Set the proxy server by a URI. See the description of {@link 
   * #setServer(URI)} about how the parameters are updated.
   *
   * @param uri The URI of the proxy server. If {@code null} is given, none of 
   *        the parameters are updated.
   *
   * @return {@code this} object.
   *
   * @throws IllegalArgumentException Failed to convert the given string to a 
   *         {@link URI} instance.
   */
  public ProxySettings setServer(final String uri) {
    if (uri == null) {
      return this;
    }

    return setServer(URI.create(uri));
  }




  private ProxySettings setServer(final String scheme, final String userInfo, final String host, final int port) {
    setByScheme(scheme);
    setByUserInfo(userInfo);
    this.host = host;
    this.port = port;
    return this;
  }




  /**
   * Set the proxy server by a URI. The parameters are updated as described 
   * below.
   *
   * <blockquote>
   * <dl>
   *   <dt>Secure</dt>
   *   <dd><p>
   *     If the URI contains the scheme part and its value is
   *     either {@code "http"} or {@code "https"} (case-insensitive),
   *     the {@code secure} parameter is updated to {@code false}
   *     or to {@code true} accordingly. In other cases, the parameter
   *     is not updated.
   *   </p></dd>
   *   <dt>ID &amp; Password</dt>
   *   <dd><p>
   *     If the URI contains the userinfo part and the ID embedded
   *     in the userinfo part is not an empty string, the {@code
   *     id} parameter and the {@code password} parameter are updated
   *     accordingly. In other cases, the parameters are not updated.
   *   </p></dd>
   *   <dt>Host</dt>
   *   <dd><p>
   *     The {@code host} parameter is always updated by the given URI.
   *   </p></dd>
   *   <dt>Port</dt>
   *   <dd><p>
   *     The {@code port} parameter is always updated by the given URI.
   *   </p></dd>
   * </dl>
   * </blockquote>
   *
   * @param uri The URI of the proxy server. If {@code null} is given, none of 
   *        the parameters is updated.
   *
   * @return {@code this} object.
   */
  public ProxySettings setServer(final URI uri) {
    if (uri == null) {
      return this;
    }

    final String scheme = uri.getScheme();
    final String userInfo = uri.getUserInfo();
    final String host = uri.getHost();
    final int port = uri.getPort();

    return setServer(scheme, userInfo, host, port);
  }




  /**
   * Set the proxy server by a URL. See the description of {@link 
   * #setServer(URI)} about how the parameters are updated.
   *
   * @param url The URL of the proxy server. If {@code null} is given, none of 
   *        the parameters are updated.
   *
   * @return {@code this} object.
   *
   * @throws IllegalArgumentException Failed to convert the given URL to a {@link URI} instance.
   */
  public ProxySettings setServer(final URL url) {
    if (url == null) {
      return this;
    }

    try {
      return setServer(url.toURI());
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }




  /**
   * Set a socket factory.
   *
   * @param factory A socket factory.
   *
   * @return {@code this} instance.
   */
  public ProxySettings setSocketFactory(final SocketFactory factory) {
    socketFactorySettings.setSocketFactory(factory);
    return this;
  }




  /**
   * Set an SSL context to get a socket factory.
   *
   * @param context An SSL context.
   *
   * @return {@code this} instance.
   */
  public ProxySettings setSSLContext(final SSLContext context) {
    socketFactorySettings.setSSLContext(context);
    return this;
  }




  /**
   * Set an SSL socket factory.
   *
   * @param factory An SSL socket factory.
   *
   * @return {@code this} instance.
   */
  public ProxySettings setSSLSocketFactory(final SSLSocketFactory factory) {
    socketFactorySettings.setSSLSocketFactory(factory);
    return this;
  }

}
