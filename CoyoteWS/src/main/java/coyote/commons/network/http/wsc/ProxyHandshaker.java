package coyote.commons.network.http.wsc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;


class ProxyHandshaker {
  private static final String RN = "\r\n";
  private final Socket socket;
  private final String host;
  private final int port;
  private final ProxySettings settings;




  public ProxyHandshaker(final Socket socket, final String host, final int port, final ProxySettings settings) {
    this.socket = socket;
    this.host = host;
    this.port = port;
    this.settings = settings;
  }




  private void addHeaders(final StringBuilder builder) {
    // For each additional header.
    for (final Map.Entry<String, List<String>> header : settings.getHeaders().entrySet()) {
      // Header name.
      final String name = header.getKey();

      // For each header value.
      for (String value : header.getValue()) {
        if (value == null) {
          value = "";
        }

        builder.append(name).append(": ").append(value).append(RN);
      }
    }
  }




  private void addProxyAuthorization(final StringBuilder builder) {
    final String id = settings.getId();

    if (id == null || id.length() == 0) {
      return;
    }

    String password = settings.getPassword();

    if (password == null) {
      password = "";
    }

    // {id}:{password}
    final String credentials = String.format("%s:%s", id, password);

    // The current implementation always uses Basic Authentication.
    builder.append("Proxy-Authorization: Basic ").append(Base64.encode(credentials)).append(RN);
  }




  private String buildRequest() {
    final String text = String.format("%s:%d", host, port);

    // CONNECT
    final StringBuilder builder = new StringBuilder().append("CONNECT ").append(text).append(" HTTP/1.1").append(RN).append("Host: ").append(text).append(RN);

    // Additional headers
    addHeaders(builder);

    // Proxy-Authorization
    addProxyAuthorization(builder);

    // The entire request.
    return builder.append(RN).toString();
  }




  /**
   * To be able to verify the hostname of the certificate received
   * if a connection is made to an https/wss endpoint, access to this
   * hostname is required.
   *
   * @return the hostname of the server the proxy is asked to connect to.
   */
  String getProxiedHostname() {
    return host;
  }




  public void perform() throws IOException {
    // Send a CONNECT request to the proxy server.
    sendRequest();

    // Receive a response.
    receiveResponse();
  }




  private void readStatusLine(final InputStream input) throws IOException {
    // Read the status line.
    final String statusLine = WebSocketUtil.readLine(input, "UTF-8");

    // If the response from the proxy server does not contain a status line.
    if (statusLine == null || statusLine.length() == 0) {
      throw new IOException("The response from the proxy server does not contain a status line.");
    }

    // Expect "HTTP/1.1 200 Connection established"
    final String[] elements = statusLine.split(" +", 3);

    if (elements.length < 2) {
      throw new IOException("The status line in the response from the proxy server is badly formatted. " + "The status line is: " + statusLine);
    }

    // If the status code is not "200".
    if ("200".equals(elements[1]) == false) {
      throw new IOException("The status code in the response from the proxy server is not '200 Connection established'. " + "The status line is: " + statusLine);
    }

    // OK. A connection was established.
  }




  private void receiveResponse() throws IOException {
    // Get the stream to read data from the proxy server.
    final InputStream input = socket.getInputStream();

    // Read the status line.
    readStatusLine(input);

    // Skip HTTP headers, including an empty line (= the separator
    // between the header part and the body part).
    skipHeaders(input);
  }




  private void sendRequest() throws IOException {
    // Build a CONNECT request.
    final String request = buildRequest();

    // Convert the request to a byte array.
    final byte[] requestBytes = WebSocketUtil.getBytesUTF8(request);

    // Get the stream to send data to the proxy server.
    final OutputStream output = socket.getOutputStream();

    // Send the request to the proxy server.
    output.write(requestBytes);
    output.flush();
  }




  private void skipHeaders(final InputStream input) throws IOException {
    // The number of normal letters in a line.
    int count = 0;

    while (true) {
      // Read a byte from the stream.
      int ch = input.read();

      // If the end of the stream was reached.
      if (ch == -1) {
        // Unexpected EOF.
        throw new EOFException("The end of the stream from the proxy server was reached unexpectedly.");
      }

      // If the end of the line was reached.
      if (ch == '\n') {
        // If there is no normal byte in the line.
        if (count == 0) {
          // An empty line (the separator) was found.
          return;
        }

        // Reset the counter and go to the next line.
        count = 0;
        continue;
      }

      // If the read byte is not a carriage return.
      if (ch != '\r') {
        // Increment the number of normal bytes on the line.
        ++count;
        continue;
      }

      // Read the next byte.
      ch = input.read();

      // If the end of the stream was reached.
      if (ch == -1) {
        // Unexpected EOF.
        throw new EOFException("The end of the stream from the proxy server was reached unexpectedly after a carriage return.");
      }

      if (ch != '\n') {
        // Regard the last '\r' as a normal byte as well as the current 'ch'.
        count += 2;
        continue;
      }

      // '\r\n' was detected.

      // If there is no normal byte in the line.
      if (count == 0) {
        // An empty line (the separator) was found.
        return;
      }

      // Reset the counter and go to the next line.
      count = 0;
    }
  }
}
