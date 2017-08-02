/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import coyote.dx.context.TransformContext;


/**
 * Checks if the field contains expected values.
 * 
 * <p>There are 6 supported operators:<ul>
 * <li>EQ (==) - equal (equivalent) to
 * <li>EI - equal (equivalent) to ignores case
 * <li>LT (&lt;) - Less Than
 * <li>LE (&lt;=) - Less than or Equal to
 * <li>GT (&gt;) - Greater Than
 * <li>GE (&gt;=) - Greater Than or Equal to
 * <li>NE (!=) - Not equal to
 * </ul>
 * 
 * <p>String values in the context will be parsed into numeric values with any 
 * parsing errors leaving the value as a string and the test probably failing.
 * 
 * <p>Dates are converted to longs representing epoch times.
 * 
 * <p>Booleans are converted to 0 (false) and 1 (true) for comparison.
 * 
 * <p>EI (equal to ignoring case) causes values to be converted to strings for 
 * comparison. Numerics are not formatted in any way and will not contain 
 * group separators.
 */
public class CheckFieldMethod extends AbstractBooleanMethod {

  /**
   * Resolve the named field in the context and determine if it satisfies the 
   * operator and comparison value.
   * 
   * <p>The expected value will determine what type conversions are performed. 
   * If an expected value is a numeric, and the field is a date, then the date 
   * will be converted to a numeric(long) for comparison. If the expected 
   * value is a date and the field value is a numeric, the field value will be 
   * converted to a date (if possible) for comparison.
   * 
   * <p>True is indicative but false return values are not as false is 
   * returned even when conversions are not possible
   *
   * @param context The transform context in which to look for the job status
   * @param field name of the value to resolve in the context.
   * @param operator token representing the comparative operation to perform
   * @param expected the value against which the field is evaluated.
   *
   * @return true if the named field matches expected comparison, false 
   *         otherwise.
   *
   * @throws IllegalArgumentException if the operator is not supported.
   */
  public static Boolean execute(TransformContext context, String field, String operator, String expected) throws IllegalArgumentException {
    String key = sanitize(field);
    Object fieldObject = context.resolveToValue(field);
    Object expectedObject = context.resolveToValue(expected);
    

    Operator op = Operator.getOperator(operator);
    if (Operator.EQ.equals(op)) {
      System.out.println("equals");
    } else if (Operator.EI.equals(op)) {
      System.out.println("equals, ignore case");
    } else if (Operator.LT.equals(op)) {
      System.out.println("equals");
    } else if (Operator.LE.equals(op)) {
      System.out.println("equals");
    } else if (Operator.GT.equals(op)) {
      System.out.println("equals");
    } else if (Operator.GE.equals(op)) {
      System.out.println("equal");
    } else if (Operator.NE.equals(op)) {
      System.out.println("not equals");
    }

    return true;
  }

  /**
   * Supported comparison operators
   */
  public enum Operator {
    EQ, LT, LE, GT, GE, NE, EI;

    /**
     * Convert text into an operator.
     * 
     * @param token the text to parse
     * 
     * @return the operator represented by the text
     * 
     * @throws IllegalArgumentException if the text could not be parsed into 
     *         an operator.
     */
    public static Operator getOperator(String token) throws IllegalArgumentException {
      if (token != null) {
        String value = token.trim().toUpperCase();
        switch (value) {
          case "EQ":
          case "==":
            return EQ;
          case "EI":
            return EI;
          case "LT":
          case "<":
            return EQ;
          case "LE":
          case "<=":
            return EQ;
          case "GT":
          case ">":
            return EQ;
          case "GE":
          case ">=":
            return EQ;
          case "NE":
          case "!=":
            return EQ;
          default:
            throw new IllegalArgumentException("Unsupported operator '" + token + "'");
        }
      } else {
        throw new IllegalArgumentException("No operator");
      }
    }
  }

}
