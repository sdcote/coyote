package coyote.commons.network.http.wsc;

class Token {
  public static boolean isSeparator(final char ch) {
    switch (ch) {
      case '(':
      case ')':
      case '<':
      case '>':
      case '@':
      case ',':
      case ';':
      case ':':
      case '\\':
      case '"':
      case '/':
      case '[':
      case ']':
      case '?':
      case '=':
      case '{':
      case '}':
      case ' ':
      case '\t':
        return true;

      default:
        return false;
    }
  }




  /**
   * Check if the given string conforms to the rules described
   * in "<a href="http://tools.ietf.org/html/rfc2616#section-2.2"
   * >2.2 Basic Rules</a>" of <a href="http://tools.ietf.org/html/rfc2616"
   * >RFC 2616</a>.
   */
  public static boolean isValid(final String token) {
    if (token == null || token.length() == 0) {
      return false;
    }

    final int len = token.length();

    for (int i = 0; i < len; ++i) {
      if (isSeparator(token.charAt(i))) {
        return false;
      }
    }

    return true;
  }




  public static String unescape(final String text) {
    if (text == null) {
      return null;
    }

    if (text.indexOf('\\') < 0) {
      return text;
    }

    final int len = text.length();
    boolean escaped = false;
    final StringBuilder builder = new StringBuilder();

    for (int i = 0; i < len; ++i) {
      final char ch = text.charAt(i);

      if (ch == '\\' && escaped == false) {
        escaped = true;
        continue;
      }

      escaped = false;
      builder.append(ch);
    }

    return builder.toString();
  }




  public static String unquote(String text) {
    if (text == null) {
      return null;
    }

    final int len = text.length();

    if (len < 2 || text.charAt(0) != '"' || text.charAt(len - 1) != '"') {
      return text;
    }

    text = text.substring(1, len - 1);

    return unescape(text);
  }
}
