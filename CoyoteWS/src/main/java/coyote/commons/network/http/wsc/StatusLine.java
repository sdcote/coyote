package coyote.commons.network.http.wsc;

/**
 * HTTP status line returned from an HTTP server.
 */
public class StatusLine {
  /**
   * HTTP version.
   */
  private final String httpVersion;

  /**
   * Status code.
   */
  private final int statusCode;

  /**
   * Reason phrase.
   */
  private final String reasonPhrase;

  /**
   * String representation of this instance (= the raw status line).
   */
  private final String desc;




  /**
   * Constructor with a raw status line.
   *
   * @param line A status line.
   *
   * @throws NullPointerException {@code line} is {@code null}
   *
   * @throws IllegalArgumentException The number of elements in {@code line} 
   *         is less than 2.
   *
   * @throws NumberFormatException Failed to parse the second element in 
   *         {@code line} as an integer.
   */
  StatusLine(final String line) {
    // HTTP-Version Status-Code Reason-Phrase
    final String[] elements = line.split(" +", 3);

    if (elements.length < 2) {
      throw new IllegalArgumentException();
    }

    httpVersion = elements[0];
    statusCode = Integer.parseInt(elements[1]);
    reasonPhrase = (elements.length == 3) ? elements[2] : null;
    desc = line;
  }




  /**
   * Get the HTTP version.
   *
   * @return The HTTP version. For example, {@code "HTTP/1.1"}.
   */
  public String getHttpVersion() {
    return httpVersion;
  }




  /**
   * Get the reason phrase.
   *
   * @return The reason phrase. For example, {@code "Not Found"}.
   */
  public String getReasonPhrase() {
    return reasonPhrase;
  }




  /**
   * Get the status code.
   *
   * @return The status code. For example, {@code 404}.
   */
  public int getStatusCode() {
    return statusCode;
  }




  /**
   * Get the string representation of this instance, which is equal to the raw 
   * status line.
   *
   * @return The raw status line. E.g., {@code "HTTP/1.1 404 Not Found"}.
   */
  @Override
  public String toString() {
    return desc;
  }

}
