package coyote.commons.network.http.wsc;

import static coyote.commons.network.http.wsc.WebSocketOpcode.BINARY;
import static coyote.commons.network.http.wsc.WebSocketOpcode.CLOSE;
import static coyote.commons.network.http.wsc.WebSocketOpcode.CONTINUATION;
import static coyote.commons.network.http.wsc.WebSocketOpcode.PING;
import static coyote.commons.network.http.wsc.WebSocketOpcode.PONG;
import static coyote.commons.network.http.wsc.WebSocketOpcode.TEXT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Many different utilities for the web socket client library.
 * 
 * <p>This is a duplication of may of the methods in coyote.commons, but one
 * goal of this code is to reduce dependencies and provide a web socket client 
 * which may be distributed on its own. This may change in the future.
 */
class WebSocketUtil {
  private static final SecureRandom sRandom = new SecureRandom();




  public static String extractHost(final URI uri) {
    // Extract the host part from the URI.
    String host = uri.getHost();

    if (host != null) {
      return host;
    }

    // Extract the host part from the authority part of the URI.
    host = extractHostFromAuthorityPart(uri.getRawAuthority());

    if (host != null) {
      return host;
    }

    // Extract the host part from the entire URI.
    return extractHostFromEntireUri(uri.toString());
  }




  static String extractHostFromAuthorityPart(final String authority) {
    // If the authority part is not available.
    if (authority == null) {
      // Hmm... This should not happen.
      return null;
    }

    // Parse the authority part. The expected format is "[id:password@]host[:port]".
    final Matcher matcher = Pattern.compile("^(.*@)?([^:]+)(:\\d+)?$").matcher(authority);

    // If the authority part does not match the expected format.
    if (matcher == null || matcher.matches() == false) {
      // Hmm... This should not happen.
      return null;
    }

    // Return the host part.
    return matcher.group(2);
  }




  static String extractHostFromEntireUri(final String uri) {
    String retval = null;
    if (uri != null) {
      // Parse the URI. The expected format is "scheme://[id:password@]host[:port][...]".
      final Matcher matcher = Pattern.compile("^\\w+://([^@/]*@)?([^:/]+)(:\\d+)?(/.*)?$").matcher(uri);

      // If the URI matches the expected format.
      if (matcher != null && matcher.matches()) {
        // Return the host part.
        retval = matcher.group(2);
      }
    }

    return retval;
  }




  /**
   * Get a UTF-8 byte array representation of the given string.
   */
  public static byte[] getBytesUTF8(final String string) {
    byte[] retval = null;
    if (string != null) {
      try {
        retval = string.getBytes("UTF-8");
      } catch (final UnsupportedEncodingException ignore) {
        // ignore, should not happen
      }
    }
    return retval;
  }




  public static String join(final Collection<?> values, final String delimiter) {
    final StringBuilder builder = new StringBuilder();
    join(builder, values, delimiter);
    return builder.toString();
  }




  private static void join(final StringBuilder builder, final Collection<?> values, final String delimiter) {
    boolean first = true;

    for (final Object value : values) {
      if (first) {
        first = false;
      } else {
        builder.append(delimiter);
      }

      builder.append(value.toString());
    }
  }




  /**
   * Find the maximum value from the given array.
   */
  public static int max(final int[] values) {
    int retval = Integer.MIN_VALUE;

    for (final int value : values) {
      if (retval < value) {
        retval = value;
      }
    }

    return retval;
  }




  /**
   * Find the minimum value from the given array.
   */
  public static int min(final int[] values) {
    int retval = Integer.MAX_VALUE;

    for (final int value : values) {
      if (value < retval) {
        retval = value;
      }
    }

    return retval;
  }




  /**
   * Fill the given buffer with random bytes.
   */
  public static byte[] getRandomBytes(final byte[] buffer) {
    sRandom.nextBytes(buffer);
    return buffer;
  }




  /**
   * Create a buffer of the given size filled with random bytes.
   */
  public static byte[] getRandomBytes(final int nBytes) {
    final byte[] buffer = new byte[nBytes];

    return getRandomBytes(buffer);
  }




  /**
   * Read a line from the given stream.
   */
  public static String readLine(final InputStream in, final String charset) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    while (true) {
      // Read one byte from the stream.
      final int b = in.read();

      // If the end of the stream was reached.
      if (b == -1) {
        if (baos.size() == 0) {
          // No more line.
          return null;
        } else {
          // The end of the line was reached.
          break;
        }
      }

      if (b == '\n') {
        // The end of the line was reached.
        break;
      }

      if (b != '\r') {
        // Normal character.
        baos.write(b);
        continue;
      }

      // Read one more byte.
      final int b2 = in.read();

      // If the end of the stream was reached.
      if (b2 == -1) {
        // Treat the '\r' as a normal character.
        baos.write(b);

        // The end of the line was reached.
        break;
      }

      // If '\n' follows the '\r'.
      if (b2 == '\n') {
        // The end of the line was reached.
        break;
      }

      // Treat the '\r' as a normal character.
      baos.write(b);

      // Append the byte which follows the '\r'.
      baos.write(b2);
    }

    // Convert the byte array to a string.
    return baos.toString(charset);
  }




  /**
   * Convert a WebSocket opcode into a string representation.
   */
  public static String toOpcodeName(final int opcode) {
    switch (opcode) {
      case CONTINUATION:
        return "CONTINUATION";

      case TEXT:
        return "TEXT";

      case BINARY:
        return "BINARY";

      case CLOSE:
        return "CLOSE";

      case PING:
        return "PING";

      case PONG:
        return "PONG";

      default:
        break;
    }

    if (0x1 <= opcode && opcode <= 0x7) {
      return String.format("DATA(0x%X)", opcode);
    }

    if (0x8 <= opcode && opcode <= 0xF) {
      return String.format("CONTROL(0x%X)", opcode);
    }

    return String.format("0x%X", opcode);
  }




  /**
   * Convert a UTF-8 byte array into a string.
   */
  public static String toStringUTF8(final byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    return toStringUTF8(bytes, 0, bytes.length);
  }




  /**
   * Convert a UTF-8 byte array into a string.
   */
  public static String toStringUTF8(final byte[] bytes, final int offset, final int length) {
    if (bytes == null) {
      return null;
    }

    try {
      return new String(bytes, offset, length, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      // This never happens.
      return null;
    } catch (final IndexOutOfBoundsException e) {
      return null;
    }
  }

}
