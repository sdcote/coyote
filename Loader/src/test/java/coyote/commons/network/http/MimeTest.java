package coyote.commons.network.http;

import org.junit.Assert;
import org.junit.Test;

import coyote.commons.network.MimeType;


public class MimeTest {

  @Test
  public void testExistingMimeType() throws Exception {
    Assert.assertEquals( "text/html", MimeType.get( "xxxx.html" ).get( 0 ).getType() );
  }




  @Test
  public void testManualMimeType() throws Exception {
    MimeType.add( "flv", "video/manualOverwrite", true );
    // all mimetypes are normalized to lowercase as per RFC 2045 pg.13
    Assert.assertEquals( "video/manualoverwrite", MimeType.get( "xxxx.flv" ).get( 0 ).getType() );

  }




  @Test
  public void testNotExistingMimeType() throws Exception {
    Assert.assertNotNull( MimeType.get( "notExistent" ) ); // at least "unknown"
    Assert.assertEquals( "application/octet-stream", MimeType.get( "xxxx.notExistent" ).get( 0 ).getType() );
  }




  @Test
  public void testOverwritenMimeType() throws Exception {
    Assert.assertEquals( "application/octet-stream", MimeType.get( "xxxx.ts" ).get( 0 ).getType() );
  }
}
