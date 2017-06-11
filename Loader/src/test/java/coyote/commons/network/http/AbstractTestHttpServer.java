package coyote.commons.network.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;


public class AbstractTestHttpServer {

  protected byte[] readContents( final HttpEntity entity ) throws IOException {
    final InputStream instream = entity.getContent();
    return readContents( instream );
  }




  protected byte[] readContents( final InputStream instream ) throws IOException {
    byte[] bytes;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      final byte[] buffer = new byte[1024];
      int count;
      while ( ( count = instream.read( buffer ) ) >= 0 ) {
        out.write( buffer, 0, count );
      }
      bytes = out.toByteArray();
    }
    finally {
      instream.close();
    }
    return bytes;
  }

}
