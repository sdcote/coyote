package coyote.commons.network.http.wsc;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


class SocketFactorySettings {
  private SocketFactory socketFactory;
  private SSLSocketFactory sslSocketFactory;
  private SSLContext sslContext;




  public SocketFactory getSocketFactory() {
    return socketFactory;
  }




  public SSLContext getSSLContext() {
    return sslContext;
  }




  public SSLSocketFactory getSSLSocketFactory() {
    return sslSocketFactory;
  }




  public SocketFactory selectSocketFactory(final boolean secure) {
    if (secure) {
      if (sslContext != null) {
        return sslContext.getSocketFactory();
      }

      if (sslSocketFactory != null) {
        return sslSocketFactory;
      }

      return SSLSocketFactory.getDefault();
    }

    if (socketFactory != null) {
      return socketFactory;
    }

    return SocketFactory.getDefault();
  }




  public void setSocketFactory(final SocketFactory factory) {
    socketFactory = factory;
  }




  public void setSSLContext(final SSLContext context) {
    sslContext = context;
  }




  public void setSSLSocketFactory(final SSLSocketFactory factory) {
    sslSocketFactory = factory;
  }
}
