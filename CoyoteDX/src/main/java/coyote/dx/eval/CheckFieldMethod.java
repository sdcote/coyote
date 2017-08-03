/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import coyote.commons.NumberUtil;
import coyote.dx.context.TransformContext;


/**
 * Checks if the field contains expected values.
 *
 * <p>There are 6 supported operators:<ul>
 * <li>EQ (==) - equal (equivalent) to
 * <li>EI - equal (equivalent) to, ignores case
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
   * <p>The expected value will be resolved against the context just like the
   * field value. This means fields can be checked against each other.
   *
   * <p>True is indicative but false return values are not as false is
   * returned even when conversions are not possible. If the field is missing,
   * the check may return false unless a check for null is being performed.
   *
   * @param context The transform context in which to look for the job status
   * @param field name of the value to resolve in the context.
   * @param operator token representing the comparative operation to perform
   * @param expected the value against which the field is evaluated. This may
   *        be a field name
   *
   * @return true if the named field matches expected comparison, false
   *         otherwise.
   *
   * @throws IllegalArgumentException if the operator is not supported.
   */
  public static Boolean execute(final TransformContext context, final String field, final String operator, final String expected) throws IllegalArgumentException {
    if (field == null) {
      throw new IllegalArgumentException("Null field parameter");
    }
    if (operator == null) {
      throw new IllegalArgumentException("Null operator parameter");
    }
    if (expected == null) {
      throw new IllegalArgumentException("Null expected value parameter");
    }

    String key = sanitize(field);
    final Object fieldObject = context.resolveToValue(key);

    key = sanitize(expected);
    Object expectedObject = context.resolveToValue(key);
    if (expectedObject == null) {
      if (NumberUtil.isNumeric(expected)) {
        expectedObject = NumberUtil.parse(expected);
      } else {
        expectedObject = expected;
      }
    }

    final Operator op = Operator.getOperator(operator);
    switch (op) {
      case EQ:
        return equals(fieldObject, expectedObject);
      case EI:
        return equalsIgnoreCase(fieldObject, expectedObject);
      case LT:
        return lessThan(fieldObject, expectedObject);
      case LE:
        return lessThanEquals(fieldObject, expectedObject);
      case GT:
        return greaterThan(fieldObject, expectedObject);
      case GE:
        return greaterThanEquals(fieldObject, expectedObject);
      case NE:
        return notEqual(fieldObject, expectedObject);
      default:
        return false;
     }

  }




  private static Boolean equals(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("equals");
    return retval;
  }




  private static Boolean equalsIgnoreCase(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("equals, ignore case");
    return retval;
  }




  private static Boolean greaterThan(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("greater than");
    return retval;
  }




  private static Boolean greaterThanEquals(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("greater than or equal");
    return retval;
  }




  private static Boolean lessThan(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("less than");
    return retval;
  }




  private static Boolean lessThanEquals(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("less than or equal");
    return retval;
  }




  private static Boolean notEqual(final Object fieldObject, final Object expectedObject) {
    final boolean retval = true;
    System.out.println("not equal");
    return retval;
  }

  /**
   * Supported comparison operators
   */
  public enum Operator {
    EI, EQ, GE, GT, LE, LT, NE;

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
    public static Operator getOperator(final String token) throws IllegalArgumentException {
      if (token != null) {
        final String value = token.trim().toUpperCase();
        switch (value) {
          case "EQ":
          case "==":
            return EQ;
          case "EI":
            return EI;
          case "LT":
          case "<":
            return LT;
          case "LE":
          case "<=":
            return LE;
          case "GT":
          case ">":
            return GT;
          case "GE":
          case ">=":
            return GE;
          case "NE":
          case "!=":
            return NE;
          default:
            throw new IllegalArgumentException("Unsupported operator '" + token + "'");
        }
      } else {
        throw new IllegalArgumentException("Null operator");
      }
    }
  }

}
