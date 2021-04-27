package coyote.commons.network.http.wsc;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


class HandshakeBuilder {
  private static final String[] CONNECTION_HEADER = {"Connection", "Upgrade"};
  private static final String[] UPGRADE_HEADER = {"Upgrade", "websocket"};
  private static final String[] VERSION_HEADER = {"Sec-WebSocket-Version", "13"};
  private static final String RN = "\r\n";

  private final boolean mSecure;
  private String mUserInfo;
  private final String mHost;
  private final String mPath;
  private final URI mUri;
  private String mKey;
  private Set<String> mProtocols;
  private List<WebSocketExtension> mExtensions;
  private List<String[]> mHeaders;




  public HandshakeBuilder(final boolean secure, final String userInfo, final String host, final String path) {
    mSecure = secure;
    mUserInfo = userInfo;
    mHost = host;
    mPath = path;

    // 'host' may contain ':{port}' at its end.
    // 'path' may contain '?{query}' at its end.
    mUri = URI.create(String.format("%s://%s%s", (secure ? "wss" : "ws"), host, path));
  }




  public HandshakeBuilder(final HandshakeBuilder source) {
    mSecure = source.mSecure;
    mUserInfo = source.mUserInfo;
    mHost = source.mHost;
    mPath = source.mPath;
    mUri = source.mUri;
    mKey = source.mKey;
    mProtocols = copyProtocols(source.mProtocols);
    mExtensions = copyExtensions(source.mExtensions);
    mHeaders = copyHeaders(source.mHeaders);
  }




  public void addExtension(final String extension) {
    addExtension(WebSocketExtension.parse(extension));
  }




  public void addExtension(final WebSocketExtension extension) {
    if (extension == null) {
      return;
    }

    synchronized (this) {
      if (mExtensions == null) {
        mExtensions = new ArrayList<WebSocketExtension>();
      }

      mExtensions.add(extension);
    }
  }




  public void addHeader(final String name, String value) {
    if (name == null || name.length() == 0) {
      return;
    }

    if (value == null) {
      value = "";
    }

    synchronized (this) {
      if (mHeaders == null) {
        mHeaders = new ArrayList<String[]>();
      }

      mHeaders.add(new String[]{name, value});
    }
  }




  public void addProtocol(final String protocol) {
    if (isValidProtocol(protocol) == false) {
      throw new IllegalArgumentException("'protocol' must be a non-empty string with characters in the range " + "U+0021 to U+007E not including separator characters.");
    }

    synchronized (this) {
      if (mProtocols == null) {
        // 'LinkedHashSet' is used because the elements
        // "MUST all be unique strings" and must be
        // "ordered by preference. See RFC 6455, p18, 10.
        mProtocols = new LinkedHashSet<String>();
      }

      mProtocols.add(protocol);
    }
  }




  public List<String[]> buildHeaders() {
    final List<String[]> headers = new ArrayList<String[]>();

    // Host
    headers.add(new String[]{"Host", mHost});

    // Connection
    headers.add(CONNECTION_HEADER);

    // Upgrade
    headers.add(UPGRADE_HEADER);

    // Sec-WebSocket-Version
    headers.add(VERSION_HEADER);

    // Sec-WebSocket-Key
    headers.add(new String[]{"Sec-WebSocket-Key", mKey});

    // Sec-WebSocket-Protocol
    if (mProtocols != null && mProtocols.size() != 0) {
      headers.add(new String[]{"Sec-WebSocket-Protocol", WebSocketUtil.join(mProtocols, ", ")});
    }

    // Sec-WebSocket-Extensions
    if (mExtensions != null && mExtensions.size() != 0) {
      headers.add(new String[]{"Sec-WebSocket-Extensions", WebSocketUtil.join(mExtensions, ", ")});
    }

    // Authorization: Basic
    if (mUserInfo != null && mUserInfo.length() != 0) {
      headers.add(new String[]{"Authorization", "Basic " + Base64.encode(mUserInfo)});
    }

    // Custom headers
    if (mHeaders != null && mHeaders.size() != 0) {
      headers.addAll(mHeaders);
    }

    return headers;
  }




  public String buildRequestLine() {
    return String.format("GET %s HTTP/1.1", mPath);
  }




  public void clearExtensions() {
    synchronized (this) {
      mExtensions = null;
    }
  }




  public void clearHeaders() {
    synchronized (this) {
      mHeaders = null;
    }
  }




  public void clearProtocols() {
    synchronized (this) {
      mProtocols = null;
    }
  }




  public void clearUserInfo() {
    synchronized (this) {
      mUserInfo = null;
    }
  }




  public boolean containsExtension(final String name) {
    if (name == null) {
      return false;
    }

    synchronized (this) {
      if (mExtensions == null) {
        return false;
      }

      for (final WebSocketExtension extension : mExtensions) {
        if (extension.getName().equals(name)) {
          return true;
        }
      }

      return false;
    }
  }




  public boolean containsExtension(final WebSocketExtension extension) {
    if (extension == null) {
      return false;
    }

    synchronized (this) {
      if (mExtensions == null) {
        return false;
      }

      return mExtensions.contains(extension);
    }
  }




  public boolean containsProtocol(final String protocol) {
    synchronized (this) {
      if (mProtocols == null) {
        return false;
      }

      return mProtocols.contains(protocol);
    }
  }




  public URI getURI() {
    return mUri;
  }




  public void removeExtension(final WebSocketExtension extension) {
    if (extension == null) {
      return;
    }

    synchronized (this) {
      if (mExtensions == null) {
        return;
      }

      mExtensions.remove(extension);

      if (mExtensions.size() == 0) {
        mExtensions = null;
      }
    }
  }




  public void removeExtensions(final String name) {
    if (name == null) {
      return;
    }

    synchronized (this) {
      if (mExtensions == null) {
        return;
      }

      final List<WebSocketExtension> extensionsToRemove = new ArrayList<WebSocketExtension>();

      for (final WebSocketExtension extension : mExtensions) {
        if (extension.getName().equals(name)) {
          extensionsToRemove.add(extension);
        }
      }

      for (final WebSocketExtension extension : extensionsToRemove) {
        mExtensions.remove(extension);
      }

      if (mExtensions.size() == 0) {
        mExtensions = null;
      }
    }
  }




  public void removeHeaders(final String name) {
    if (name == null || name.length() == 0) {
      return;
    }

    synchronized (this) {
      if (mHeaders == null) {
        return;
      }

      final List<String[]> headersToRemove = new ArrayList<String[]>();

      for (final String[] header : mHeaders) {
        if (header[0].equals(name)) {
          headersToRemove.add(header);
        }
      }

      for (final String[] header : headersToRemove) {
        mHeaders.remove(header);
      }

      if (mHeaders.size() == 0) {
        mHeaders = null;
      }
    }
  }




  public void removeProtocol(final String protocol) {
    if (protocol == null) {
      return;
    }

    synchronized (this) {
      if (mProtocols == null) {
        return;
      }

      mProtocols.remove(protocol);

      if (mProtocols.size() == 0) {
        mProtocols = null;
      }
    }
  }




  public void setKey(final String key) {
    mKey = key;
  }




  public void setUserInfo(final String userInfo) {
    synchronized (this) {
      mUserInfo = userInfo;
    }
  }




  public void setUserInfo(String id, String password) {
    if (id == null) {
      id = "";
    }

    if (password == null) {
      password = "";
    }

    final String userInfo = String.format("%s:%s", id, password);

    setUserInfo(userInfo);
  }




  public static String build(final String requestLine, final List<String[]> headers) {
    final StringBuilder builder = new StringBuilder();

    // Append the request line, "GET {path} HTTP/1.1".
    builder.append(requestLine).append(RN);

    // For each header.
    for (final String[] header : headers) {
      // Append the header, "{name}: {value}".
      builder.append(header[0]).append(": ").append(header[1]).append(RN);
    }

    // Append an empty line.
    builder.append(RN);

    return builder.toString();
  }




  private static List<WebSocketExtension> copyExtensions(final List<WebSocketExtension> extensions) {
    if (extensions == null) {
      return null;
    }

    final List<WebSocketExtension> newExtensions = new ArrayList<WebSocketExtension>(extensions.size());

    for (final WebSocketExtension extension : extensions) {
      newExtensions.add(new WebSocketExtension(extension));
    }

    return newExtensions;
  }




  private static String[] copyHeader(final String[] header) {
    final String[] newHeader = new String[2];

    newHeader[0] = header[0];
    newHeader[1] = header[1];

    return newHeader;
  }




  private static List<String[]> copyHeaders(final List<String[]> headers) {
    if (headers == null) {
      return null;
    }

    final List<String[]> newHeaders = new ArrayList<String[]>(headers.size());

    for (final String[] header : headers) {
      newHeaders.add(copyHeader(header));
    }

    return newHeaders;
  }




  private static Set<String> copyProtocols(final Set<String> protocols) {
    if (protocols == null) {
      return null;
    }

    final Set<String> newProtocols = new LinkedHashSet<String>(protocols.size());

    newProtocols.addAll(protocols);

    return newProtocols;
  }




  private static boolean isValidProtocol(final String protocol) {
    if (protocol == null || protocol.length() == 0) {
      return false;
    }

    final int len = protocol.length();

    for (int i = 0; i < len; ++i) {
      final char ch = protocol.charAt(i);

      if (ch < 0x21 || 0x7E < ch || Token.isSeparator(ch)) {
        return false;
      }
    }

    return true;
  }

}
