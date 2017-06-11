package coyote.commons.network.http;


import static org.junit.Assert.*;

import org.junit.Test;


public class HttpParsingTest extends HttpServerTest {

  @Test
  public void testMultibyteCharacterSupport() throws Exception {
    final String expected = "Chinese \u738b Letters";
    final String input = "Chinese+%e7%8e%8b+Letters";
    assertEquals( expected, HTTPD.decodePercent( input ) );
  }




  @Test
  public void testNormalCharacters() throws Exception {
    for ( int i = 0x20; i < 0x80; i++ ) {
      final String hex = Integer.toHexString( i );
      final String input = "%" + hex;
      final char expected = (char)i;
      assertEquals( "" + expected, HTTPD.decodePercent( input ) );
    }
  }




  @Test
  public void testPlusInQueryParams() throws Exception {
    assertEquals( "foo bar", HTTPD.decodePercent( "foo+bar" ) );
  }

}
