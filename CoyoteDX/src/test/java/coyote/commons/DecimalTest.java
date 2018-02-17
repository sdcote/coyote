/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coyote.commons.Decimal;


/**
 * Just a general note when using big decimal...if you use the double 
 * primative, there will be some level of inexactness. Using a string as a 
 * constructor with BigDecimal is a more precise way to populate the value. If 
 * you use the primative, 0.25 might be 0.249999...
 */
public class DecimalTest {

  /**
   * Test method for {@link coyote.commons.Decimal#remainder(coyote.commons.Decimal)}.
   */
  @Test
  public void testRemainder() {

    Decimal subject = Decimal.valueOf(5.5D);
    Decimal result = subject.remainder(Decimal.TWO);
    //System.out.println("Remainder: " + result);
    Decimal expected = Decimal.valueOf(1.5D);
    assertTrue(expected.isEqual(result));
  }




  /**
   * Test method for {@link coyote.commons.Decimal#getWholePart()}.
   */
  @Test
  public void testGetWholePart() {
    Decimal subject = Decimal.valueOf(5.52D);
    Decimal result = subject.getWholePart();
    //System.out.println("WholePart: " + result);
    Decimal expected = Decimal.valueOf(5D);
    assertTrue(expected.isEqual(result));
  }




  /**
   * Test method for {@link coyote.commons.Decimal#getFractionalPart()}.
   */
  @Test
  public void testGetFractionalPart() {
    Decimal subject = Decimal.valueOf("5.52");
    Decimal result = subject.getFractionalPart();
    Decimal expected = Decimal.valueOf("0.52");
    assertTrue(expected.isEqual(result));
  }




  /**
   * Test method for {@link coyote.commons.Decimal#getFractionalValue()}.
   */
  @Test
  public void testGetFractionalValue() {
    Decimal subject = Decimal.valueOf("5.52");
    Decimal result = subject.getFractionalValue();
    Decimal expected = Decimal.valueOf("52");
    assertTrue(expected.isEqual(result));
  }




  @Test
  public void testRoundUpToWhole() {
    Decimal subject = Decimal.valueOf("5.52");
    Decimal result = subject.roundUpToWhole();
    Decimal expected = Decimal.valueOf("6");
    assertTrue(expected.isEqual(result));

    subject = Decimal.valueOf("5.0");
    result = subject.roundUpToWhole();
    expected = Decimal.valueOf("5");
    assertTrue(expected.isEqual(result));

    subject = Decimal.valueOf("-0.5");
    result = subject.roundUpToWhole();
    expected = Decimal.valueOf("0");
    assertTrue(expected.isEqual(result));

    subject = Decimal.valueOf("-1.0");
    result = subject.roundUpToWhole();
    expected = Decimal.valueOf("-1");
    assertTrue(expected.isEqual(result));

    subject = Decimal.valueOf("-1.9");
    result = subject.roundUpToWhole();
    expected = Decimal.valueOf("-1");
    assertTrue(expected.isEqual(result));

  }

}
