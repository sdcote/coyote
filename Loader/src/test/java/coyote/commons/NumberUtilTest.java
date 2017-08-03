/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;


/**
 *
 */
public class NumberUtilTest {

  private boolean checkParse(final String val) {
    try {
      final Object obj = NumberUtil.parse(val);
      return obj != null;
    } catch (final NumberFormatException e) {
      return false;
    }
  }




  private void compareIsNumericWithParse(final String val, final boolean expected) {
    final boolean isValid = NumberUtil.isNumeric(val);
    final boolean canCreate = checkParse(val);
    if ((isValid == expected) && (canCreate == expected)) {
      return;
    }
    fail("Expecting " + expected + " for isNumeric using \"" + val + "\" but got " + isValid + " and " + canCreate);
  }




  @Test
  public void parse() {
    NumberUtil.parse("-1l");
    NumberUtil.parse("01l");
    NumberUtil.parse("1l");

    assertEquals(Float.valueOf("1234.5"), NumberUtil.parse("1234.5"));
    assertEquals(Integer.valueOf("12345"), NumberUtil.parse("12345"));
    assertEquals(Double.valueOf("1234.5"), NumberUtil.parse("1234.5D"));
    assertEquals(Double.valueOf("1234.5"), NumberUtil.parse("1234.5d"));
    assertEquals(Float.valueOf("1234.5"), NumberUtil.parse("1234.5F"));
    assertEquals(Float.valueOf("1234.5"), NumberUtil.parse("1234.5f"));
    assertEquals(Long.valueOf(Integer.MAX_VALUE + 1L), NumberUtil.parse("" + (Integer.MAX_VALUE + 1L)));
    assertEquals(Long.valueOf(12345), NumberUtil.parse("12345L"));
    assertEquals(Long.valueOf(12345), NumberUtil.parse("12345l"));
    assertEquals(Float.valueOf("-1234.5"), NumberUtil.parse("-1234.5"));
    assertEquals(Integer.valueOf("-12345"), NumberUtil.parse("-12345"));
    assertTrue(0xFADE == NumberUtil.parse("0xFADE").intValue());
    assertTrue(0xFADE == NumberUtil.parse("0Xfade").intValue());
    assertTrue(-0xFADE == NumberUtil.parse("-0xFADE").intValue());
    assertTrue(-0xFADE == NumberUtil.parse("-0Xfade").intValue());
    assertEquals(Double.valueOf("1.1E200"), NumberUtil.parse("1.1E200"));
    assertEquals(Float.valueOf("1.1E20"), NumberUtil.parse("1.1E20"));
    assertEquals(Double.valueOf("-1.1E200"), NumberUtil.parse("-1.1E200"));
    assertEquals(Double.valueOf("1.1E-200"), NumberUtil.parse("1.1E-200"));
    assertEquals(null, NumberUtil.parse(null));
    assertEquals(new BigInteger("12345678901234567890"), NumberUtil.parse("12345678901234567890L"));
    assertEquals( new BigDecimal("1.1E-700"), NumberUtil.parse("1.1E-700F"));
    assertEquals( Long.valueOf("10" + Integer.MAX_VALUE), NumberUtil.parse("10" + Integer.MAX_VALUE + "L"));
    assertEquals(Long.valueOf("10" + Integer.MAX_VALUE), NumberUtil.parse("10" + Integer.MAX_VALUE));
    assertEquals( new BigInteger("10" + Long.MAX_VALUE), NumberUtil.parse("10" + Long.MAX_VALUE));
    assertEquals( Float.valueOf("2."), NumberUtil.parse("2."));
    assertFalse( checkParse("1eE"));
    assertEquals( Double.valueOf(Double.MAX_VALUE), NumberUtil.parse("" + Double.MAX_VALUE));
    assertEquals( Double.valueOf("-160952.54"), NumberUtil.parse("-160952.54"));
    assertEquals( Double.valueOf("6264583.33"), NumberUtil.parse("6264583.33"));
    assertEquals( Double.valueOf("193343.82"), NumberUtil.parse("193343.82"));
    assertEquals(Float.class, NumberUtil.parse("0.0").getClass());
    assertEquals(Float.valueOf("0.0"), NumberUtil.parse("0.0"));
    assertEquals(Float.class, NumberUtil.parse("+0.0").getClass());
    assertEquals(Float.valueOf("+0.0"), NumberUtil.parse("+0.0"));
    assertEquals(Float.class, NumberUtil.parse("-0.0").getClass());
    assertEquals(Float.valueOf("-0.0"), NumberUtil.parse("-0.0"));
    assertEquals(Integer.valueOf(0x8000), NumberUtil.parse("0x8000"));
    assertEquals(Integer.valueOf(0x80000), NumberUtil.parse("0x80000"));
    assertEquals(Integer.valueOf(0x800000), NumberUtil.parse("0x800000"));
    assertEquals(Integer.valueOf(0x8000000), NumberUtil.parse("0x8000000"));
    assertEquals(Integer.valueOf(0x7FFFFFFF), NumberUtil.parse("0x7FFFFFFF"));
    assertEquals(Long.valueOf(0x80000000L), NumberUtil.parse("0x80000000"));
    assertEquals(Long.valueOf(0xFFFFFFFFL), NumberUtil.parse("0xFFFFFFFF"));
    assertEquals(Integer.valueOf(0x8000000), NumberUtil.parse("0x08000000"));
    assertEquals(Integer.valueOf(0x7FFFFFFF), NumberUtil.parse("0x007FFFFFFF"));
    assertEquals(Long.valueOf(0x80000000L), NumberUtil.parse("0x080000000"));
    assertEquals(Long.valueOf(0xFFFFFFFFL), NumberUtil.parse("0x00FFFFFFFF"));
    assertEquals(Long.valueOf(0x800000000L), NumberUtil.parse("0x800000000"));
    assertEquals(Long.valueOf(0x8000000000L), NumberUtil.parse("0x8000000000"));
    assertEquals(Long.valueOf(0x80000000000L), NumberUtil.parse("0x80000000000"));
    assertEquals(Long.valueOf(0x800000000000L), NumberUtil.parse("0x800000000000"));
    assertEquals(Long.valueOf(0x8000000000000L), NumberUtil.parse("0x8000000000000"));
    assertEquals(Long.valueOf(0x80000000000000L), NumberUtil.parse("0x80000000000000"));
    assertEquals(Long.valueOf(0x800000000000000L), NumberUtil.parse("0x800000000000000"));
    assertEquals(Long.valueOf(0x7FFFFFFFFFFFFFFFL), NumberUtil.parse("0x7FFFFFFFFFFFFFFF"));
    assertEquals(new BigInteger("8000000000000000", 16), NumberUtil.parse("0x8000000000000000"));
    assertEquals(new BigInteger("FFFFFFFFFFFFFFFF", 16), NumberUtil.parse("0xFFFFFFFFFFFFFFFF"));
    assertEquals(Long.valueOf(0x80000000000000L), NumberUtil.parse("0x00080000000000000"));
    assertEquals(Long.valueOf(0x800000000000000L), NumberUtil.parse("0x0800000000000000"));
    assertEquals(Long.valueOf(0x7FFFFFFFFFFFFFFFL), NumberUtil.parse("0x07FFFFFFFFFFFFFFF"));
    assertEquals(new BigInteger("8000000000000000", 16), NumberUtil.parse("0x00008000000000000000"));
    assertEquals(new BigInteger("FFFFFFFFFFFFFFFF", 16), NumberUtil.parse("0x0FFFFFFFFFFFFFFFF"));

    final Number bigNum = NumberUtil.parse("-1.1E-700F");
    assertNotNull(bigNum);
    assertEquals(BigDecimal.class, bigNum.getClass());
  }




  @Test
  public void parseBigDecimal() {
    assertEquals(new BigDecimal("1234.5"), NumberUtil.parseBigDecimal("1234.5"));
    assertEquals(null, NumberUtil.parseBigDecimal(null));
    parseBigDecimalFailure("");
    parseBigDecimalFailure(" ");
    parseBigDecimalFailure("\b\t\n\f\r");
    parseBigDecimalFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    parseBigDecimalFailure("-");
    parseBigDecimalFailure("--");
    parseBigDecimalFailure("--0");
    parseBigDecimalFailure("+");
    parseBigDecimalFailure("++");
    parseBigDecimalFailure("++0");
  }




  protected void parseBigDecimalFailure(final String str) {
    try {
      final BigDecimal value = NumberUtil.parseBigDecimal(str);
      fail("parseBigDecimal(\"" + str + "\") should have failed: " + value);
    } catch (final NumberFormatException ex) {
      // empty
    }
  }




  @Test
  public void parseBigInteger() {
    assertEquals(new BigInteger("12345"), NumberUtil.parseBigInteger("12345"));
    assertEquals(null, NumberUtil.parseBigInteger(null));
    parseBigIntegerFailure("");
    parseBigIntegerFailure(" ");
    parseBigIntegerFailure("\b\t\n\f\r");
    parseBigIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
    assertEquals(new BigInteger("255"), NumberUtil.parseBigInteger("0xff"));
    assertEquals(new BigInteger("255"), NumberUtil.parseBigInteger("#ff"));
    assertEquals(new BigInteger("-255"), NumberUtil.parseBigInteger("-0xff"));
    assertEquals(new BigInteger("255"), NumberUtil.parseBigInteger("0377"));
    assertEquals(new BigInteger("-255"), NumberUtil.parseBigInteger("-0377"));
    assertEquals(new BigInteger("-0"), NumberUtil.parseBigInteger("-0"));
    assertEquals(new BigInteger("0"), NumberUtil.parseBigInteger("0"));
    parseBigIntegerFailure("#");
    parseBigIntegerFailure("-#");
    parseBigIntegerFailure("0x");
    parseBigIntegerFailure("-0x");
  }




  protected void parseBigIntegerFailure(final String str) {
    try {
      final BigInteger value = NumberUtil.parseBigInteger(str);
      fail("parseBigInteger(\"" + str + "\") should have failed: " + value);
    } catch (final NumberFormatException ex) {
      // empty
    }
  }




  @Test
  public void parseDouble() {
    assertEquals(Double.valueOf("1234.5"), NumberUtil.parseDouble("1234.5"));
    assertEquals(null, NumberUtil.parseDouble(null));
    parseDoubleFailure("");
    parseDoubleFailure(" ");
    parseDoubleFailure("\b\t\n\f\r");
    parseDoubleFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
  }




  protected void parseDoubleFailure(final String str) {
    try {
      final Double value = NumberUtil.parseDouble(str);
      fail("parseDouble(\"" + str + "\") should have failed: " + value);
    } catch (final NumberFormatException ex) {
      // empty
    }
  }




  @Test
  public void parseFloat() {
    assertEquals(Float.valueOf("1234.5"), NumberUtil.parseFloat("1234.5"));
    assertEquals(null, NumberUtil.parseFloat(null));
    parseFloatFailure("");
    parseFloatFailure(" ");
    parseFloatFailure("\b\t\n\f\r");
    parseFloatFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
  }




  protected void parseFloatFailure(final String str) {
    try {
      final Float value = NumberUtil.parseFloat(str);
      fail("parseFloat(\"" + str + "\") should have failed: " + value);
    } catch (final NumberFormatException ex) {
      // empty
    }
  }




  @Test
  public void parseInteger() {
    assertEquals(Integer.valueOf("12345"), NumberUtil.parseInteger("12345"));
    assertEquals(null, NumberUtil.parseInteger(null));
    parseIntegerFailure("");
    parseIntegerFailure(" ");
    parseIntegerFailure("\b\t\n\f\r");
    parseIntegerFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
  }




  protected void parseIntegerFailure(final String str) {
    try {
      final Integer value = NumberUtil.parseInteger(str);
      fail("parseInteger(\"" + str + "\") should have failed: " + value);
    } catch (final NumberFormatException ex) {
      // empty
    }
  }




  @Test
  public void parseLong() {
    assertEquals(Long.valueOf("12345"), NumberUtil.parseLong("12345"));
    assertEquals(null, NumberUtil.parseLong(null));
    parseLongFailure("");
    parseLongFailure(" ");
    parseLongFailure("\b\t\n\f\r");
    parseLongFailure("\u00A0\uFEFF\u000B\u000C\u001C\u001D\u001E\u001F");
  }




  protected void parseLongFailure(final String str) {
    try {
      final Long value = NumberUtil.parseLong(str);
      fail("parseLong(\"" + str + "\") should have failed: " + value);
    } catch (final NumberFormatException ex) {
      // empty
    }
  }




  @Test
  public void testIsNumeric() {
    compareIsNumericWithParse(" ", false);
    compareIsNumericWithParse(" 1111", false);
    compareIsNumericWithParse("", false);
    compareIsNumericWithParse("--2.3", false);
    compareIsNumericWithParse("-.12345", true);
    compareIsNumericWithParse("-0", true);
    compareIsNumericWithParse("-01234", true);
    compareIsNumericWithParse("-0ABC123", false);
    compareIsNumericWithParse("-0x0", true);
    compareIsNumericWithParse("-0xABC123", true);
    compareIsNumericWithParse("-1234", true);
    compareIsNumericWithParse("-1234.5", true);
    compareIsNumericWithParse("-1234E5", true);
    compareIsNumericWithParse("-123E", false);
    compareIsNumericWithParse("-123E+-212", false);
    compareIsNumericWithParse("-123E2.12", false);
    compareIsNumericWithParse("-221.23F", true);
    compareIsNumericWithParse(".", false);
    compareIsNumericWithParse(".12.3", false);
    compareIsNumericWithParse(".12345", true);
    compareIsNumericWithParse("0", true);
    compareIsNumericWithParse("0.0", true);
    compareIsNumericWithParse("0.1", true);
    compareIsNumericWithParse("0.4790", true);
    compareIsNumericWithParse("00", true);
    compareIsNumericWithParse("0085", false);
    compareIsNumericWithParse("01234", true);
    compareIsNumericWithParse("07", true);
    compareIsNumericWithParse("08", false);
    compareIsNumericWithParse("085", false);
    compareIsNumericWithParse("0xABCD", true);
    compareIsNumericWithParse("0xFAE-1", false);
    compareIsNumericWithParse("0xGF", false);
    compareIsNumericWithParse("1.1L", false);
    compareIsNumericWithParse("11 11", false);
    compareIsNumericWithParse("1111 ", false);
    compareIsNumericWithParse("11a", false);
    compareIsNumericWithParse("11d11", false);
    compareIsNumericWithParse("11def", false);
    compareIsNumericWithParse("11g", false);
    compareIsNumericWithParse("11z", false);
    compareIsNumericWithParse("123.4E-D", false);
    compareIsNumericWithParse("123.4E21D", true);
    compareIsNumericWithParse("123.4E5", true);
    compareIsNumericWithParse("123.4ED", false);
    compareIsNumericWithParse("1234.5", true);
    compareIsNumericWithParse("12345", true);
    compareIsNumericWithParse("1234E+5", true);
    compareIsNumericWithParse("1234E-5", true);
    compareIsNumericWithParse("1234E5", true);
    compareIsNumericWithParse("1234E5l", false);
    compareIsNumericWithParse("1a", false);
    compareIsNumericWithParse("2.", true);
    compareIsNumericWithParse("22338L", true);
    compareIsNumericWithParse("\r\n\t", false);
    compareIsNumericWithParse("a", false);
    compareIsNumericWithParse(null, false);
  }




  @Test
  public void testJdkAssumptions() {
    assertTrue(Double.compare(Double.NaN, Double.NaN) == 0);
    assertTrue(Double.compare(Double.NaN, Double.POSITIVE_INFINITY) == +1);
    assertTrue(Double.compare(Double.NaN, Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(Double.NaN, 1.2d) == +1);
    assertTrue(Double.compare(Double.NaN, 0.0d) == +1);
    assertTrue(Double.compare(Double.NaN, -0.0d) == +1);
    assertTrue(Double.compare(Double.NaN, -1.2d) == +1);
    assertTrue(Double.compare(Double.NaN, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(Double.NaN, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.NaN) == -1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY) == 0);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, 1.2d) == +1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, 0.0d) == +1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, -0.0d) == +1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, -1.2d) == +1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(Double.MAX_VALUE, Double.NaN) == -1);
    assertTrue(Double.compare(Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(Double.MAX_VALUE, Double.MAX_VALUE) == 0);
    assertTrue(Double.compare(Double.MAX_VALUE, 1.2d) == +1);
    assertTrue(Double.compare(Double.MAX_VALUE, 0.0d) == +1);
    assertTrue(Double.compare(Double.MAX_VALUE, -0.0d) == +1);
    assertTrue(Double.compare(Double.MAX_VALUE, -1.2d) == +1);
    assertTrue(Double.compare(Double.MAX_VALUE, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(1.2d, Double.NaN) == -1);
    assertTrue(Double.compare(1.2d, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(1.2d, Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(1.2d, 1.2d) == 0);
    assertTrue(Double.compare(1.2d, 0.0d) == +1);
    assertTrue(Double.compare(1.2d, -0.0d) == +1);
    assertTrue(Double.compare(1.2d, -1.2d) == +1);
    assertTrue(Double.compare(1.2d, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(1.2d, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(0.0d, Double.NaN) == -1);
    assertTrue(Double.compare(0.0d, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(0.0d, Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(0.0d, 1.2d) == -1);
    assertTrue(Double.compare(0.0d, 0.0d) == 0);
    assertTrue(Double.compare(0.0d, -0.0d) == +1);
    assertTrue(Double.compare(0.0d, -1.2d) == +1);
    assertTrue(Double.compare(0.0d, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(0.0d, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(-0.0d, Double.NaN) == -1);
    assertTrue(Double.compare(-0.0d, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(-0.0d, Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(-0.0d, 1.2d) == -1);
    assertTrue(Double.compare(-0.0d, 0.0d) == -1);
    assertTrue(Double.compare(-0.0d, -0.0d) == 0);
    assertTrue(Double.compare(-0.0d, -1.2d) == +1);
    assertTrue(Double.compare(-0.0d, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(-0.0d, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(-1.2d, Double.NaN) == -1);
    assertTrue(Double.compare(-1.2d, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(-1.2d, Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(-1.2d, 1.2d) == -1);
    assertTrue(Double.compare(-1.2d, 0.0d) == -1);
    assertTrue(Double.compare(-1.2d, -0.0d) == -1);
    assertTrue(Double.compare(-1.2d, -1.2d) == 0);
    assertTrue(Double.compare(-1.2d, -Double.MAX_VALUE) == +1);
    assertTrue(Double.compare(-1.2d, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(-Double.MAX_VALUE, Double.NaN) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, 1.2d) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, 0.0d) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, -0.0d) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, -1.2d) == -1);
    assertTrue(Double.compare(-Double.MAX_VALUE, -Double.MAX_VALUE) == 0);
    assertTrue(Double.compare(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY) == +1);

    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.NaN) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, 1.2d) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, 0.0d) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -0.0d) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -1.2d) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE) == -1);
    assertTrue(Double.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY) == 0);

    assertTrue(Float.compare(Float.NaN, Float.NaN) == 0);
    assertTrue(Float.compare(Float.NaN, Float.POSITIVE_INFINITY) == +1);
    assertTrue(Float.compare(Float.NaN, Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(Float.NaN, 1.2f) == +1);
    assertTrue(Float.compare(Float.NaN, 0.0f) == +1);
    assertTrue(Float.compare(Float.NaN, -0.0f) == +1);
    assertTrue(Float.compare(Float.NaN, -1.2f) == +1);
    assertTrue(Float.compare(Float.NaN, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(Float.NaN, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.NaN) == -1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) == 0);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, 1.2f) == +1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, 0.0f) == +1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, -0.0f) == +1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, -1.2f) == +1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(Float.MAX_VALUE, Float.NaN) == -1);
    assertTrue(Float.compare(Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(Float.MAX_VALUE, Float.MAX_VALUE) == 0);
    assertTrue(Float.compare(Float.MAX_VALUE, 1.2f) == +1);
    assertTrue(Float.compare(Float.MAX_VALUE, 0.0f) == +1);
    assertTrue(Float.compare(Float.MAX_VALUE, -0.0f) == +1);
    assertTrue(Float.compare(Float.MAX_VALUE, -1.2f) == +1);
    assertTrue(Float.compare(Float.MAX_VALUE, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(1.2f, Float.NaN) == -1);
    assertTrue(Float.compare(1.2f, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(1.2f, Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(1.2f, 1.2f) == 0);
    assertTrue(Float.compare(1.2f, 0.0f) == +1);
    assertTrue(Float.compare(1.2f, -0.0f) == +1);
    assertTrue(Float.compare(1.2f, -1.2f) == +1);
    assertTrue(Float.compare(1.2f, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(1.2f, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(0.0f, Float.NaN) == -1);
    assertTrue(Float.compare(0.0f, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(0.0f, Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(0.0f, 1.2f) == -1);
    assertTrue(Float.compare(0.0f, 0.0f) == 0);
    assertTrue(Float.compare(0.0f, -0.0f) == +1);
    assertTrue(Float.compare(0.0f, -1.2f) == +1);
    assertTrue(Float.compare(0.0f, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(0.0f, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(-0.0f, Float.NaN) == -1);
    assertTrue(Float.compare(-0.0f, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(-0.0f, Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(-0.0f, 1.2f) == -1);
    assertTrue(Float.compare(-0.0f, 0.0f) == -1);
    assertTrue(Float.compare(-0.0f, -0.0f) == 0);
    assertTrue(Float.compare(-0.0f, -1.2f) == +1);
    assertTrue(Float.compare(-0.0f, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(-0.0f, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(-1.2f, Float.NaN) == -1);
    assertTrue(Float.compare(-1.2f, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(-1.2f, Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(-1.2f, 1.2f) == -1);
    assertTrue(Float.compare(-1.2f, 0.0f) == -1);
    assertTrue(Float.compare(-1.2f, -0.0f) == -1);
    assertTrue(Float.compare(-1.2f, -1.2f) == 0);
    assertTrue(Float.compare(-1.2f, -Float.MAX_VALUE) == +1);
    assertTrue(Float.compare(-1.2f, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(-Float.MAX_VALUE, Float.NaN) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, 1.2f) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, 0.0f) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, -0.0f) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, -1.2f) == -1);
    assertTrue(Float.compare(-Float.MAX_VALUE, -Float.MAX_VALUE) == 0);
    assertTrue(Float.compare(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY) == +1);

    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.NaN) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, 1.2f) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, 0.0f) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -0.0f) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -1.2f) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE) == -1);
    assertTrue(Float.compare(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY) == 0);
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when preceded by -- rather than -
  public void testParseFailure1() {
    NumberUtil.parse("--1.1E-700F");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when both e and E are present (with decimal)
  public void testParseFailure2() {
    NumberUtil.parse("-1.1E+0-7e00");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when both e and E are present (no decimal)
  public void testParseFailure3() {
    NumberUtil.parse("-11E+0-7e00");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when both e and E are present (no decimal)
  public void testParseFailure4() {
    NumberUtil.parse("1eE+00001");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when there are multiple trailing 'f' characters
  public void testParseFailure5() {
    NumberUtil.parse("1234.5ff");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when there are multiple trailing 'F' characters
  public void testParseFailure6() {
    NumberUtil.parse("1234.5FF");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when there are multiple trailing 'd' characters
  public void testParseFailure7() {
    NumberUtil.parse("1234.5dd");
  }




  @Test(expected = NumberFormatException.class)
  // Check that the code fails to parse a valid number when there are multiple trailing 'D' characters
  public void testParseFailure_8() {
    NumberUtil.parse("1234.5DD");
  }




  @Test
  public void parseMagnitude() {
    // Test Float.MAX_VALUE, and same with +1 in final digit to check conversion changes to next Number type
    assertEquals(Float.valueOf(Float.MAX_VALUE), NumberUtil.parse("3.4028235e+38"));
    assertEquals(Double.valueOf(3.4028236e+38), NumberUtil.parse("3.4028236e+38"));

    // Test Double.MAX_VALUE
    assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtil.parse("1.7976931348623157e+308"));
    // Test with +2 in final digit (+1 does not cause roll-over to BigDecimal)
    assertEquals(new BigDecimal("1.7976931348623159e+308"), NumberUtil.parse("1.7976931348623159e+308"));

    assertEquals(Integer.valueOf(0x12345678), NumberUtil.parse("0x12345678"));
    assertEquals(Long.valueOf(0x123456789L), NumberUtil.parse("0x123456789"));

    assertEquals(Long.valueOf(0x7fffffffffffffffL), NumberUtil.parse("0x7fffffffffffffff"));
    // Does not appear to be a way to parse a literal BigInteger of this magnitude
    assertEquals(new BigInteger("7fffffffffffffff0", 16), NumberUtil.parse("0x7fffffffffffffff0"));

    assertEquals(Long.valueOf(0x7fffffffffffffffL), NumberUtil.parse("#7fffffffffffffff"));
    assertEquals(new BigInteger("7fffffffffffffff0", 16), NumberUtil.parse("#7fffffffffffffff0"));

    assertEquals(Integer.valueOf(017777777777), NumberUtil.parse("017777777777")); // 31 bits
    assertEquals(Long.valueOf(037777777777L), NumberUtil.parse("037777777777")); // 32 bits

    assertEquals(Long.valueOf(0777777777777777777777L), NumberUtil.parse("0777777777777777777777")); // 63 bits
    assertEquals(new BigInteger("1777777777777777777777", 8), NumberUtil.parse("01777777777777777777777"));// 64 bits
  }

}
