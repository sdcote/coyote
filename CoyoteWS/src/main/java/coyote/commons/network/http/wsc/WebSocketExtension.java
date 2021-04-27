package coyote.commons.network.http.wsc;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A class to hold the name and the parameters of
 * a WebSocket extension.
 */
public class WebSocketExtension {
  /**
   * The name of <code>permessage-deflate</code> extension that is
   * defined in <a href="https://tools.ietf.org/html/rfc7692#section-7"
   * >7&#46; The "permessage-deflate" Extension</a> in <a href=
   * "https://tools.ietf.org/html/rfc7692">RFC 7692</a>.
   */
  public static final String PERMESSAGE_DEFLATE = "permessage-deflate";
  private final String name;
  private final Map<String, String> parameters;




  private static WebSocketExtension createInstance(final String name) {
    if (PERMESSAGE_DEFLATE.equals(name)) {
      return new PerMessageDeflateExtension(name);
    }

    return new WebSocketExtension(name);
  }




  private static String extractValue(final String[] pair) {
    if (pair.length != 2) {
      return null;
    }

    return Token.unquote(pair[1]);
  }




  /**
   * Parse a string as a {@link WebSocketExtension}. 
   * 
   * <p>The input string should comply with the format described in 
   * <a href="https://tools.ietf.org/html/rfc6455#section-9.1">9.1. 
   * Negotiating Extensions</a> in 
   * <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>.
   *
   * @param string A string that represents a WebSocket extension.
   *
   * @return A new {@link WebSocketExtension} instance that represents the 
   *         given string. If the input string does not comply with RFC 6455, 
   *         {@code null} is returned.
   */
  public static WebSocketExtension parse(final String string) {
    if (string == null) {
      return null;
    }

    // Split the string by semi-colons.
    final String[] elements = string.trim().split("\\s*;\\s*");

    if (elements.length == 0) {
      // Even an extension name is not included.
      return null;
    }

    // The first element is the extension name.
    final String name = elements[0];

    if (Token.isValid(name) == false) {
      // The extension name is not a valid token.
      return null;
    }

    // Create an instance for the extension name.
    final WebSocketExtension extension = createInstance(name);

    // For each "{key}[={value}]".
    for (int i = 1; i < elements.length; ++i) {
      // Split by '=' to get the key and the value.
      final String[] pair = elements[i].split("\\s*=\\s*", 2);

      // If {key} is not contained.
      if (pair.length == 0 || pair[0].length() == 0) {
        // Ignore.
        continue;
      }

      // The name of the parameter.
      final String key = pair[0];

      if (Token.isValid(key) == false) {
        // The parameter name is not a valid token.
        // Ignore this parameter.
        continue;
      }

      // The value of the parameter.
      final String value = extractValue(pair);

      if (value != null) {
        if (Token.isValid(value) == false) {
          // The parameter value is not a valid token.
          // Ignore this parameter.
          continue;
        }
      }

      // Add the pair of the key and the value.
      extension.setParameter(key, value);
    }

    return extension;
  }




  /**
   * Constructor with an extension name.
   *
   * @param name The extension name.
   *
   * @throws IllegalArgumentException The given name is not a valid token.
   */
  public WebSocketExtension(final String name) {
    // Check the validity of the name.
    if (Token.isValid(name) == false) {
      // The name is not a valid token.
      throw new IllegalArgumentException("'name' is not a valid token.");
    }

    this.name = name;
    parameters = new LinkedHashMap<String, String>();
  }




  /**
   * Copy constructor.
   *
   * @param source A source extension. Must not be {@code null}.
   *
   * @throws IllegalArgumentException The given argument is {@code null}.
   */
  public WebSocketExtension(final WebSocketExtension source) {
    if (source == null) {
      // If the given instance is null.
      throw new IllegalArgumentException("'source' is null.");
    }

    name = source.getName();
    parameters = new LinkedHashMap<String, String>(source.getParameters());
  }




  /**
   * Check if the parameter identified by the key is contained.
   *
   * @param key The name of the parameter.
   *
   * @return {@code true} if the parameter is contained.
   */
  public boolean containsParameter(final String key) {
    return parameters.containsKey(key);
  }




  /**
   * Get the extension name.
   *
   * @return The extension name.
   */
  public String getName() {
    return name;
  }




  /**
   * Get the value of the specified parameter.
   *
   * @param key The name of the parameter.
   *
   * @return The value of the parameter. {@code null} may be returned.
   */
  public String getParameter(final String key) {
    return parameters.get(key);
  }




  /**
   * Get the parameters.
   *
   * @return The parameters.
   */
  public Map<String, String> getParameters() {
    return parameters;
  }




  /**
   * Set a value to the specified parameter.
   *
   * @param key The name of the parameter.
   *
   * @param value The value of the parameter. If not {@code null}, it must be
   *        a valid token. Note that 
   *        <a href="http://tools.ietf.org/html/rfc6455">RFC 6455</a> says 
   *        "<i>When using the quoted-string syntax variant, the value after 
   *        quoted-string unescaping MUST conform to the 'token' ABNF.</i>"
   *
   * @return {@code this} object.
   *
   * @throws IllegalArgumentException The key is not a valid token.
   *         The value is not {@code null} and it is not a valid token.
   */
  public WebSocketExtension setParameter(final String key, final String value) {
    // Check the validity of the key.
    if (Token.isValid(key) == false) {
      // The key is not a valid token.
      throw new IllegalArgumentException("'key' is not a valid token.");
    }

    // If the value is not null.
    if (value != null) {
      // Check the validity of the value.
      if (Token.isValid(value) == false) {
        // The value is not a valid token.
        throw new IllegalArgumentException("'value' is not a valid token.");
      }
    }

    parameters.put(key, value);

    return this;
  }




  /**
   * Stringify this object into the format "{name}[; {key}[={value}]]*".
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(name);
    for (final Map.Entry<String, String> entry : parameters.entrySet()) {
      builder.append("; ").append(entry.getKey());
      final String value = entry.getValue();
      if (value != null && value.length() != 0) {
        builder.append("=").append(value);
      }
    }
    return builder.toString();
  }




  /**
   * Validate this instance.
   * 
   * <p>This method is expected to be overridden.
   */
  void validate() throws WebSocketException {
    // should be over-ridden
  }

}
