/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * 
 */
public class StringUtilTest {

  private static final String BAR = "bar";
  private static final String FOO = "foo";
  private static final String FOOBAR = "foobar";




  @Test
  public void fixedLengthTest() {
    String text = "Coyote";
    String field = null;
    int LEFT = 0;
    int CENTER = 1;
    int RIGHT = 2;

    // Alignment Tests = = = = = =
    field = StringUtil.fixedLength(text, 10, LEFT, '*');
    //System.out.println( field );
    assertTrue(field.length() == 10);
    assertEquals(field, "Coyote****");

    field = StringUtil.fixedLength(text, 10, CENTER, '*');
    assertTrue(field.length() == 10);
    assertEquals(field, "**Coyote**");

    field = StringUtil.fixedLength(text, 10, RIGHT, '*');
    assertTrue(field.length() == 10);
    assertEquals(field, "****Coyote");

    // Size Match Tests = = = = = 
    field = StringUtil.fixedLength(text, 6, LEFT, '*');
    assertTrue(field.length() == 6);
    assertEquals(field, "Coyote");

    field = StringUtil.fixedLength(text, 6, CENTER, '*');
    assertTrue(field.length() == 6);
    assertEquals(field, "Coyote");

    field = StringUtil.fixedLength(text, 6, RIGHT, '*');
    assertTrue(field.length() == 6);
    assertEquals(field, "Coyote");

    // Truncation Tests = = = = =
    field = StringUtil.fixedLength(text, 5, LEFT, '*');
    assertTrue(field.length() == 5);
    assertEquals(field, "Coyot");

    field = StringUtil.fixedLength(text, 5, CENTER, '*');
    assertTrue(field.length() == 5);
    assertEquals(field, "Coyot");

    field = StringUtil.fixedLength(text, 5, RIGHT, '*');
    assertTrue(field.length() == 5);
    assertEquals(field, "oyote");

    field = StringUtil.fixedLength(text, 4, LEFT, '*');
    assertTrue(field.length() == 4);
    assertEquals(field, "Coyo");

    field = StringUtil.fixedLength(text, 4, CENTER, '*');
    assertTrue(field.length() == 4);
    assertEquals(field, "oyot");

    field = StringUtil.fixedLength(text, 4, RIGHT, '*');
    assertTrue(field.length() == 4);
    assertEquals(field, "yote");
  }




  @Test
  public void empty() {
    assertTrue(StringUtil.isEmpty(""));
    assertTrue(StringUtil.isEmpty(null));
    assertFalse(StringUtil.isEmpty(" "));
  }




  @Test
  public void notEmpty() {
    assertFalse(StringUtil.isNotEmpty(""));
    assertFalse(StringUtil.isNotEmpty(null));
    assertTrue(StringUtil.isNotEmpty(" "));
  }




  @Test
  public void quotedValue() {

    String text = "";
    String expected = null;
    String result = StringUtil.getQuotedValue(text);
    //System.out.println( "EMPTY>" + result + "<" );
    assertNull(result);

    text = "\"";
    result = StringUtil.getQuotedValue(text);
    //System.out.println( "ONE>" + result + "<" );
    assertNull(result);

    text = "\"\"";
    expected = "";
    result = StringUtil.getQuotedValue(text);
    //System.out.println( "TWO>" + result + "<" );
    assertNotNull(result);
    assertEquals(result, expected);

    text = "\"123\"";
    expected = "123";
    result = StringUtil.getQuotedValue(text);
    //System.out.println( "123>" + result + "<" );
    assertNotNull(result);
    assertEquals(result, expected);

    text = "  \"ABC\"   ";
    expected = "ABC";
    result = StringUtil.getQuotedValue(text);
    //System.out.println( "SPACED>" + result+ "<" );
    assertNotNull(result);
    assertEquals(result, expected);

    text = "  \"\"A\"\"B\"\"C\"\"   ";
    expected = "\"A\"\"B\"\"C\"";
    result = StringUtil.getQuotedValue(text);
    //System.out.println( "INCLOSED>" + result + "<" );
    assertNotNull(result);
    assertEquals(result, expected);
  }




  @Test
  public void isDigits() {
    assertFalse(StringUtil.isDigits(null));
    assertFalse(StringUtil.isDigits(""));
    assertTrue(StringUtil.isDigits("12345"));
    assertFalse(StringUtil.isDigits("1234.5"));
    assertFalse(StringUtil.isDigits("1ab"));
    assertFalse(StringUtil.isDigits("abc"));
  }




  @Test
  public void testEqualsIgnoreCase() {
    assertTrue(StringUtil.equalsIgnoreCase(null, null));
    assertTrue(StringUtil.equalsIgnoreCase(FOO, FOO));
    assertTrue(StringUtil.equalsIgnoreCase(FOO, new String(new char[]{'f', 'o', 'o'})));
    assertTrue(StringUtil.equalsIgnoreCase(FOO, new String(new char[]{'f', 'O', 'O'})));
    assertFalse(StringUtil.equalsIgnoreCase(FOO, BAR));
    assertFalse(StringUtil.equalsIgnoreCase(FOO, null));
    assertFalse(StringUtil.equalsIgnoreCase(null, FOO));
    assertTrue(StringUtil.equalsIgnoreCase("", ""));
    assertFalse(StringUtil.equalsIgnoreCase("abcd", "abcd "));
    assertTrue(StringUtil.equalsIgnoreCase("abcd", "abcD"));
  }




  @Test
  public void testIsEmpty() {
    assertTrue(StringUtil.isEmpty(null));
    assertTrue(StringUtil.isEmpty(""));
    assertFalse(StringUtil.isEmpty(" "));
    assertFalse(StringUtil.isEmpty("foo"));
    assertFalse(StringUtil.isEmpty("  foo  "));
  }




  @Test
  public void testIsNotEmpty() {
    assertFalse(StringUtil.isNotEmpty(null));
    assertFalse(StringUtil.isNotEmpty(""));
    assertTrue(StringUtil.isNotEmpty(" "));
    assertTrue(StringUtil.isNotEmpty("foo"));
    assertTrue(StringUtil.isNotEmpty("  foo  "));
  }




  @Test
  public void testIsBlank() {
    assertTrue(StringUtil.isBlank(null));
    assertTrue(StringUtil.isBlank(""));
    assertTrue(StringUtil.isBlank(" "));
    assertFalse(StringUtil.isBlank("foo"));
    assertFalse(StringUtil.isBlank("  foo  "));
  }




  @Test
  public void testIsNotBlank() {
    assertFalse(StringUtil.isNotBlank(null));
    assertFalse(StringUtil.isNotBlank(""));
    assertFalse(StringUtil.isNotBlank(" "));
    assertTrue(StringUtil.isNotBlank("foo"));
    assertTrue(StringUtil.isNotBlank("  foo  "));
  }




  @Test
  public void testEquals() {
    final CharSequence fooCs = new StringBuilder(FOO), barCs = new StringBuilder(BAR), foobarCs = new StringBuilder(FOOBAR);
    assertTrue(StringUtil.equals(null, null));
    assertTrue(StringUtil.equals(fooCs, fooCs));
    // assertTrue(StringUtil.equals(fooCs, new StringBuilder(FOO))); // -- ???
    // assertTrue(StringUtil.equals(fooCs, new String(new char[]{'f', 'o', 'o'}))); // -- ???
    assertFalse(StringUtil.equals(fooCs, new String(new char[]{'f', 'O', 'O'})));
    assertFalse(StringUtil.equals(fooCs, barCs));
    assertFalse(StringUtil.equals(fooCs, null));
    assertFalse(StringUtil.equals(null, fooCs));
    assertFalse(StringUtil.equals(fooCs, foobarCs));
    assertFalse(StringUtil.equals(foobarCs, fooCs));
  }




  @Test
  public void testEqualsOnStrings() {
    assertTrue(StringUtil.equals(null, null));
    assertTrue(StringUtil.equals(FOO, FOO));
    assertTrue(StringUtil.equals(FOO, new String(new char[]{'f', 'o', 'o'})));
    assertFalse(StringUtil.equals(FOO, new String(new char[]{'f', 'O', 'O'})));
    assertFalse(StringUtil.equals(FOO, BAR));
    assertFalse(StringUtil.equals(FOO, null));
    assertFalse(StringUtil.equals(null, FOO));
    assertFalse(StringUtil.equals(FOO, FOOBAR));
    assertFalse(StringUtil.equals(FOOBAR, FOO));
  }
  
}
