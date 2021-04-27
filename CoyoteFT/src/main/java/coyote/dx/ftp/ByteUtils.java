package coyote.dx.ftp;

import java.net.Socket;


public class ByteUtils {

  private static final byte[] b64 = ByteUtils.str2byte("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=");
  private static String[] chars = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};




  private static byte val(final byte foo) {
    if (foo == '=')
      return 0;
    for (int j = 0; j < b64.length; j++) {
      if (foo == b64[j])
        return (byte)j;
    }
    return 0;
  }




  protected static byte[] fromBase64(final byte[] buf, final int start, final int length) {
    final byte[] foo = new byte[length];
    int j = 0;
    for (int i = start; i < (start + length); i += 4) {
      foo[j] = (byte)((val(buf[i]) << 2) | ((val(buf[i + 1]) & 0x30) >>> 4));
      if (buf[i + 2] == (byte)'=') {
        j++;
        break;
      }
      foo[j + 1] = (byte)(((val(buf[i + 1]) & 0x0f) << 4) | ((val(buf[i + 2]) & 0x3c) >>> 2));
      if (buf[i + 3] == (byte)'=') {
        j += 2;
        break;
      }
      foo[j + 2] = (byte)(((val(buf[i + 2]) & 0x03) << 6) | (val(buf[i + 3]) & 0x3f));
      j += 3;
    }
    final byte[] bar = new byte[j];
    System.arraycopy(foo, 0, bar, 0, j);
    return bar;
  }




  protected static byte[] toBase64(final byte[] buf, final int start, final int length) {

    final byte[] tmp = new byte[length * 2];
    int i;
    int j;
    int k;

    int foo = ((length / 3) * 3) + start;
    i = 0;
    for (j = start; j < foo; j += 3) {
      k = (buf[j] >>> 2) & 0x3f;
      tmp[i++] = b64[k];
      k = ((buf[j] & 0x03) << 4) | ((buf[j + 1] >>> 4) & 0x0f);
      tmp[i++] = b64[k];
      k = ((buf[j + 1] & 0x0f) << 2) | ((buf[j + 2] >>> 6) & 0x03);
      tmp[i++] = b64[k];
      k = buf[j + 2] & 0x3f;
      tmp[i++] = b64[k];
    }

    foo = (start + length) - foo;
    if (foo == 1) {
      k = (buf[j] >>> 2) & 0x3f;
      tmp[i++] = b64[k];
      k = ((buf[j] & 0x03) << 4) & 0x3f;
      tmp[i++] = b64[k];
      tmp[i++] = (byte)'=';
      tmp[i++] = (byte)'=';
    } else if (foo == 2) {
      k = (buf[j] >>> 2) & 0x3f;
      tmp[i++] = b64[k];
      k = ((buf[j] & 0x03) << 4) | ((buf[j + 1] >>> 4) & 0x0f);
      tmp[i++] = b64[k];
      k = ((buf[j + 1] & 0x0f) << 2) & 0x3f;
      tmp[i++] = b64[k];
      tmp[i++] = (byte)'=';
    }
    final byte[] bar = new byte[i];
    System.arraycopy(tmp, 0, bar, 0, i);
    return bar;
  }




  @SuppressWarnings({"rawtypes", "unchecked"})
  protected static String[] split(final String foo, final String split) {
    if (foo == null)
      return null;
    final byte[] buf = ByteUtils.str2byte(foo);
    final java.util.Vector bar = new java.util.Vector();
    int start = 0;
    int index;
    while (true) {
      index = foo.indexOf(split, start);
      if (index >= 0) {
        bar.addElement(ByteUtils.byte2str(buf, start, index - start));
        start = index + 1;
        continue;
      }
      bar.addElement(ByteUtils.byte2str(buf, start, buf.length - start));
      break;
    }
    final String[] result = new String[bar.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = (String)(bar.elementAt(i));
    }
    return result;
  }




  protected static boolean glob(final byte[] pattern, final byte[] name) {
    return glob0(pattern, 0, name, 0);
  }




  private static boolean glob0(final byte[] pattern, final int pattern_index, final byte[] name, final int name_index) {
    if ((name.length > 0) && (name[0] == '.')) {
      if ((pattern.length > 0) && (pattern[0] == '.')) {
        if ((pattern.length == 2) && (pattern[1] == '*'))
          return true;
        return glob(pattern, pattern_index + 1, name, name_index + 1);
      }
      return false;
    }
    return glob(pattern, pattern_index, name, name_index);
  }




  private static boolean glob(final byte[] pattern, final int pattern_index, final byte[] name, final int name_index) {

    final int patternlen = pattern.length;
    if (patternlen == 0)
      return false;

    final int namelen = name.length;
    int i = pattern_index;
    int j = name_index;

    while ((i < patternlen) && (j < namelen)) {
      if (pattern[i] == '\\') {
        if ((i + 1) == patternlen)
          return false;
        i++;
        if (pattern[i] != name[j])
          return false;
        i += skipUTF8Char(pattern[i]);
        j += skipUTF8Char(name[j]);
        continue;
      }

      if (pattern[i] == '*') {
        while (i < patternlen) {
          if (pattern[i] == '*') {
            i++;
            continue;
          }
          break;
        }
        if (patternlen == i)
          return true;

        byte foo = pattern[i];
        if (foo == '?') {
          while (j < namelen) {
            if (glob(pattern, i, name, j)) {
              return true;
            }
            j += skipUTF8Char(name[j]);
          }
          return false;
        } else if (foo == '\\') {
          if ((i + 1) == patternlen)
            return false;
          i++;
          foo = pattern[i];
          while (j < namelen) {
            if (foo == name[j]) {
              if (glob(pattern, i + skipUTF8Char(foo), name, j + skipUTF8Char(name[j]))) {
                return true;
              }
            }
            j += skipUTF8Char(name[j]);
          }
          return false;
        }

        while (j < namelen) {
          if (foo == name[j]) {
            if (glob(pattern, i, name, j)) {
              return true;
            }
          }
          j += skipUTF8Char(name[j]);
        }
        return false;
      }

      if (pattern[i] == '?') {
        i++;
        j += skipUTF8Char(name[j]);
        continue;
      }

      if (pattern[i] != name[j])
        return false;

      i += skipUTF8Char(pattern[i]);
      j += skipUTF8Char(name[j]);

      if (!(j < namelen)) { // name is end
        if (!(i < patternlen)) { // pattern is end
          return true;
        }
        if (pattern[i] == '*') {
          break;
        }
      }
      continue;
    }

    if ((i == patternlen) && (j == namelen))
      return true;

    if (!(j < namelen) && // name is end
        (pattern[i] == '*')) {
      boolean ok = true;
      while (i < patternlen) {
        if (pattern[i++] != '*') {
          ok = false;
          break;
        }
      }
      return ok;
    }

    return false;
  }




  protected static String quote(final String path) {
    final byte[] _path = str2byte(path);
    int count = 0;
    for (int i = 0; i < _path.length; i++) {
      final byte b = _path[i];
      if ((b == '\\') || (b == '?') || (b == '*'))
        count++;
    }
    if (count == 0)
      return path;
    final byte[] _path2 = new byte[_path.length + count];
    for (int i = 0, j = 0; i < _path.length; i++) {
      final byte b = _path[i];
      if ((b == '\\') || (b == '?') || (b == '*')) {
        _path2[j++] = '\\';
      }
      _path2[j++] = b;
    }
    return byte2str(_path2);
  }




  protected static String unquote(final String path) {
    final byte[] foo = str2byte(path);
    final byte[] bar = unquote(foo);
    if (foo.length == bar.length)
      return path;
    return byte2str(bar);
  }




  protected static byte[] unquote(final byte[] path) {
    int pathlen = path.length;
    int i = 0;
    while (i < pathlen) {
      if (path[i] == '\\') {
        if ((i + 1) == pathlen)
          break;
        System.arraycopy(path, i + 1, path, i, path.length - (i + 1));
        pathlen--;
        i++;
        continue;
      }
      i++;
    }
    if (pathlen == path.length)
      return path;
    final byte[] foo = new byte[pathlen];
    System.arraycopy(path, 0, foo, 0, pathlen);
    return foo;
  }





  protected static String getFingerPrint(final HASH hash, final byte[] data) {
    try {
      hash.init();
      hash.update(data, 0, data.length);
      final byte[] foo = hash.digest();
      final StringBuffer sb = new StringBuffer();
      int bar;
      for (int i = 0; i < foo.length; i++) {
        bar = foo[i] & 0xff;
        sb.append(chars[(bar >>> 4) & 0xf]);
        sb.append(chars[(bar) & 0xf]);
        if ((i + 1) < foo.length)
          sb.append(":");
      }
      return sb.toString();
    } catch (final Exception e) {
      return "???";
    }
  }




  protected static boolean arrayEquals(final byte[] foo, final byte bar[]) {
    final int i = foo.length;
    if (i != bar.length)
      return false;
    for (int j = 0; j < i; j++) {
      if (foo[j] != bar[j])
        return false;
    }
    return true;
  }




  protected static Socket createSocket(final String host, final int port, final int timeout) throws FileTransferException {
    Socket socket = null;
    if (timeout == 0) {
      try {
        socket = new Socket(host, port);
        return socket;
      } catch (final Exception e) {
        final String message = e.toString();
        if (e instanceof Throwable)
          throw new FileTransferException(message, e);
        throw new FileTransferException(message);
      }
    }
    final String _host = host;
    final int _port = port;
    final Socket[] sockp = new Socket[1];
    final Exception[] ee = new Exception[1];
    String message = "";
    Thread tmp = new Thread(new Runnable() {
      @Override
      public void run() {
        sockp[0] = null;
        try {
          sockp[0] = new Socket(_host, _port);
        } catch (final Exception e) {
          ee[0] = e;
          if ((sockp[0] != null) && sockp[0].isConnected()) {
            try {
              sockp[0].close();
            } catch (final Exception eee) {}
          }
          sockp[0] = null;
        }
      }
    });
    tmp.setName("Opening Socket " + host);
    tmp.start();
    try {
      tmp.join(timeout);
      message = "timeout: ";
    } catch (final java.lang.InterruptedException eee) {}
    if ((sockp[0] != null) && sockp[0].isConnected()) {
      socket = sockp[0];
    } else {
      message += "socket is not established";
      if (ee[0] != null) {
        message = ee[0].toString();
      }
      tmp.interrupt();
      tmp = null;
      throw new FileTransferException(message);
    }
    return socket;
  }




  protected static byte[] str2byte(final String str, final String encoding) {
    if (str == null)
      return null;
    try {
      return str.getBytes(encoding);
    } catch (final java.io.UnsupportedEncodingException e) {
      return str.getBytes();
    }
  }




  protected static byte[] str2byte(final String str) {
    return str2byte(str, "UTF-8");
  }




  protected static String byte2str(final byte[] str, final String encoding) {
    return byte2str(str, 0, str.length, encoding);
  }




  protected static String byte2str(final byte[] str, final int s, final int l, final String encoding) {
    try {
      return new String(str, s, l, encoding);
    } catch (final java.io.UnsupportedEncodingException e) {
      return new String(str, s, l);
    }
  }




  protected static String byte2str(final byte[] str) {
    return byte2str(str, 0, str.length, "UTF-8");
  }




  protected static String byte2str(final byte[] str, final int s, final int l) {
    return byte2str(str, s, l, "UTF-8");
  }

  static final byte[] empty = str2byte("");




  protected static void bzero(final byte[] foo) {
    if (foo == null)
      return;
    for (int i = 0; i < foo.length; i++)
      foo[i] = 0;
  }




  protected static String diffString(final String str, final String[] not_available) {
    final String[] stra = ByteUtils.split(str, ",");
    String result = null;
    loop: for (int i = 0; i < stra.length; i++) {
      for (int j = 0; j < not_available.length; j++) {
        if (stra[i].equals(not_available[j])) {
          continue loop;
        }
      }
      if (result == null) {
        result = stra[i];
      } else {
        result = result + "," + stra[i];
      }
    }
    return result;
  }




  private static int skipUTF8Char(final byte b) {
    if ((byte)(b & 0x80) == 0)
      return 1;
    if ((byte)(b & 0xe0) == (byte)0xc0)
      return 2;
    if ((byte)(b & 0xf0) == (byte)0xe0)
      return 3;
    return 1;
  }

  /**
   * 
   */
  public interface HASH {
    void init() throws Exception;




    int getBlockSize();




    void update(byte[] foo, int start, int len) throws Exception;




    byte[] digest() throws Exception;
  }
}
