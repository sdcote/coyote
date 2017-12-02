package coyote.commons.network.http.wsc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import coyote.commons.network.http.wsc.Token;


public class TokenTest {
  private static void isValid(String text) {
    assertTrue(Token.isValid(text));
  }




  private static void isInvalid(String text) {
    assertFalse(Token.isValid(text));
  }




  private static void unescape(String expected, String input) {
    assertEquals(expected, Token.unescape(input));
  }




  private static void unquote(String expected, String input) {
    assertEquals(expected, Token.unquote(input));
  }




  @Test
  public void test001() {
    isInvalid(null);
  }




  @Test
  public void test002() {
    isInvalid("");
  }




  @Test
  public void test003() {
    isInvalid(" ");
  }




  @Test
  public void test004() {
    isValid("abc");
  }




  @Test
  public void test005() {
    unescape(null, null);
  }




  @Test
  public void test006() {
    unescape("", "");
  }




  @Test
  public void test007() {
    unescape("abc", "abc");
  }




  @Test
  public void test008() {
    unescape("abc", "ab\\c");
  }




  @Test
  public void test009() {
    unescape("ab\\", "ab\\\\");
  }




  @Test
  public void test010() {
    unescape("ab\\c", "ab\\\\c");
  }




  @Test
  public void test011() {
    unquote(null, null);
  }




  @Test
  public void test012() {
    unquote("", "");
  }




  @Test
  public void test013() {
    unquote("abc", "abc");
  }




  @Test
  public void test014() {
    unquote("abc", "\"abc\"");
  }




  @Test
  public void test015() {
    unquote("\"abc", "\"abc");
  }




  @Test
  public void test016() {
    unquote("abc\"", "abc\"");
  }




  @Test
  public void test017() {
    unquote("abc", "\"ab\\c\"");
  }




  @Test
  public void test018() {
    unquote("ab\\c", "\"ab\\\\c\"");
  }
}
