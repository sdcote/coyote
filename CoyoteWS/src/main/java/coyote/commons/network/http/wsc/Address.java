package coyote.commons.network.http.wsc;

import java.net.InetSocketAddress;


class Address {
  private final String host;
  private final int port;
  private transient String desc;




  Address(final String host, final int port) {
    this.host = host;
    this.port = port;
  }




  String getHostname() {
    return host;
  }




  InetSocketAddress toInetSocketAddress() {
    return new InetSocketAddress(host, port);
  }




  @Override
  public String toString() {
    if (desc == null) {
      desc = String.format("%s:%d", host, port);
    }
    return desc;
  }
  
}
