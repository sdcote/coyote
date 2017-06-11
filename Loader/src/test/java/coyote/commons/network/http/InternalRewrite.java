package coyote.commons.network.http;

import java.io.ByteArrayInputStream;
import java.util.Map;

import coyote.commons.network.MimeType;


public class InternalRewrite extends Response {

  private final String uri;

  private final Map<String, String> headers;




  public InternalRewrite( final Map<String, String> headers, final String uri ) {
    super( Status.OK, MimeType.HTML.toString(), new ByteArrayInputStream( new byte[0] ), 0 );
    this.headers = headers;
    this.uri = uri;
  }




  public Map<String, String> getHeaders() {
    return headers;
  }




  public String getUri() {
    return uri;
  }
}
