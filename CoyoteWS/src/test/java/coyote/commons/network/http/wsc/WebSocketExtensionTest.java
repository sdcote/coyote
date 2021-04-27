package coyote.commons.network.http.wsc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import coyote.commons.network.http.wsc.WebSocketExtension;


public class WebSocketExtensionTest {
  private static WebSocketExtension parse(String text) {
    return WebSocketExtension.parse(text);
  }




  @Test
  public void test001() {
    WebSocketExtension extension = parse("abc");

    assertNotNull(extension);
    assertEquals("abc", extension.getName());
  }




  @Test
  public void test002() {
    WebSocketExtension extension = parse("abc; x=1; y=2");

    assertNotNull(extension);
    assertEquals("abc", extension.getName());
    assertEquals("1", extension.getParameter("x"));
    assertEquals("2", extension.getParameter("y"));
  }




  @Test
  public void test003() {
    WebSocketExtension extension = parse("abc; x");

    assertNotNull(extension);
    assertEquals("abc", extension.getName());
    assertNull(extension.getParameter("x"));
    assertTrue(extension.containsParameter("x"));
  }




  @Test
  public void test004() {
    WebSocketExtension extension = parse("abc; x=");

    assertNotNull(extension);
    assertEquals("abc", extension.getName());
    assertFalse(extension.containsParameter("x"));
  }




  @Test
  public void test005() {
    WebSocketExtension extension = parse("abc; x=\"1\"; y=\"2\"");

    assertNotNull(extension);
    assertEquals("abc", extension.getName());
    assertEquals("1", extension.getParameter("x"));
    assertEquals("2", extension.getParameter("y"));
  }
}
