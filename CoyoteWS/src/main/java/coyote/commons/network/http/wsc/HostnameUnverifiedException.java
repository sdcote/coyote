package coyote.commons.network.http.wsc;

import javax.net.ssl.SSLSocket;


/**
 * The certificate of the peer does not match the expected hostname.
 *
 * <p>
 * {@link #getError()} of this class returns {@link WebSocketError#HOSTNAME_UNVERIFIED
 * HOSTNAME_UNVERIFIED}.
 */
public class HostnameUnverifiedException extends WebSocketException {
  private static final long serialVersionUID = 6642711380453778218L;

  private final SSLSocket secureSocket;
  private final String hostName;




  private static String stringifyPrincipal(final SSLSocket socket) {
    try {
      return String.format(" (%s)", socket.getSession().getPeerPrincipal().toString());
    } catch (final Exception e) {
      // Principal information is not available.
      return "";
    }
  }




  /**
   * Constructor with the SSL socket and the expected hostname.
   *
   * @param socket
   *         The SSL socket against which the hostname verification failed.
   *
   * @param hostname
   *         The expected hostname.
   */
  public HostnameUnverifiedException(final SSLSocket socket, final String hostname) {
    super(WebSocketError.HOSTNAME_UNVERIFIED, String.format("The certificate of the peer%s does not match the expected hostname (%s)", stringifyPrincipal(socket), hostname));

    secureSocket = socket;
    this.hostName = hostname;
  }




  /**
   * Get the expected hostname.
   *
   * @return
   *         The expected hostname.
   */
  public String getHostname() {
    return hostName;
  }




  /**
   * Get the SSL socket against which the hostname verification failed.
   *
   * @return
   *         The SSL socket.
   */
  public SSLSocket getSSLSocket() {
    return secureSocket;
  }
}
