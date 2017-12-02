package coyote.commons.network.http.wsc;

import static org.junit.Assert.assertEquals;
import java.net.URI;
import org.junit.Test;

import coyote.commons.network.http.wsc.WebSocketUtil;


public class MiscTest {
  private static void extractHostTest(String expected, String input) {
    URI uri = URI.create(input);

    String result = WebSocketUtil.extractHost(uri);

    assertEquals(expected, result);
  }




  private static void extractHostFromAuthorityPartTest(String expected, String input) {
    String result = WebSocketUtil.extractHostFromAuthorityPart(input);

    assertEquals(expected, result);
  }




  private static void extractHostFromEntireUriTest(String expected, String input) {
    String result = WebSocketUtil.extractHostFromEntireUri(input);

    assertEquals(expected, result);
  }




  @Test
  public void test01() {
    extractHostFromAuthorityPartTest("example.com", "example.com");
  }




  @Test
  public void test02() {
    extractHostFromAuthorityPartTest("example.com", "example.com:8080");
  }




  @Test
  public void test03() {
    extractHostFromAuthorityPartTest("example.com", "id:password@example.com");
  }




  @Test
  public void test04() {
    extractHostFromAuthorityPartTest("example.com", "id:password@example.com:8080");
  }




  @Test
  public void test05() {
    extractHostFromAuthorityPartTest("example.com", "id@example.com");
  }




  @Test
  public void test06() {
    extractHostFromAuthorityPartTest("example.com", "id:@example.com");
  }




  @Test
  public void test07() {
    extractHostFromAuthorityPartTest("example.com", ":@example.com");
  }




  @Test
  public void test08() {
    extractHostFromAuthorityPartTest("example.com", ":password@example.com");
  }




  @Test
  public void test09() {
    extractHostFromAuthorityPartTest("example.com", "@example.com");
  }




  @Test
  public void test10() {
    extractHostFromEntireUriTest("example.com", "ws://example.com");
  }




  @Test
  public void test11() {
    extractHostFromEntireUriTest("example.com", "ws://example.com:8080");
  }




  @Test
  public void test12() {
    extractHostFromEntireUriTest("example.com", "ws://id:password@example.com");
  }




  @Test
  public void test13() {
    extractHostFromEntireUriTest("example.com", "ws://id:password@example.com:8080");
  }




  @Test
  public void test14() {
    extractHostFromEntireUriTest("example.com", "ws://example.com/");
  }




  @Test
  public void test15() {
    extractHostFromEntireUriTest("example.com", "ws://example.com:8080/");
  }




  @Test
  public void test16() {
    extractHostFromEntireUriTest("example.com", "ws://id:password@example.com/");
  }




  @Test
  public void test17() {
    extractHostFromEntireUriTest("example.com", "ws://id:password@example.com:8080/");
  }




  @Test
  public void test18() {
    extractHostFromEntireUriTest("example.com", "ws://example.com/path?key=@value");
  }




  @Test
  public void test19() {
    extractHostFromEntireUriTest("example.com", "ws://example.com:8080/path?key=@value");
  }




  @Test
  public void test20() {
    extractHostFromEntireUriTest("example.com", "ws://id:password@example.com/path?key=@value");
  }




  @Test
  public void test21() {
    extractHostFromEntireUriTest("example.com", "ws://id:password@example.com:8080/path?key=@value");
  }




  @Test
  public void test22() {
    extractHostTest("example.com", "ws://example.com");
  }




  @Test
  public void test23() {
    extractHostTest("example.com", "ws://example.com:8080");
  }




  @Test
  public void test24() {
    extractHostTest("example.com", "ws://id:password@example.com");
  }




  @Test
  public void test25() {
    extractHostTest("example.com", "ws://id:password@example.com:8080");
  }




  @Test
  public void test26() {
    extractHostTest("example.com", "ws://id:password@example.com:8080/path?key=@value");
  }
}
