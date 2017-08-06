/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 *
 */
public class EvaluatorTest {

  private static TransactionContext context = null;
  private static Evaluator evaluator = null;
  private static TransformContext transformContext = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    // Create simple transformation context
    transformContext = new TransformContext();
    transformContext.set("string", "Nylon");
    transformContext.setRow(42);

    // create a transaction context within the transformation context
    context = new TransactionContext(transformContext);
    context.setLastFrame(true);
    context.setWorkingFrame(new DataFrame().set("field1", "value1").set("Field2", "Value2").set("NullField", null));

    // Mimic the transform engine and place a reference to the transaction
    // context in the transform context
    transformContext.setTransaction(context);

    // create a new evaluator setting the transform context used to resolve
    // variables
    evaluator = new Evaluator(transformContext);

  }




  /**
   * Test some more complex examples
   */
  @Test
  public void booleanComplex() {

    String expression;
    try {

      expression = "islast || equals(Working.record_type,\"22\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! islast || equals(Working.record_type,\"22\")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "islast || equals(Working.field1,\"value1\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "islast && equals(Working.field1,value1)";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "islast && equals(Working.field1,\"value1\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! islast && equals(Working.field1,\"value1\")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "! islast && ! equals(Working.field1,\"value1\")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "contextError && equals(currentRow,0)";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "! noRowsProcessed";
      assertTrue(evaluator.evaluateBoolean(expression));

      transformContext.setError(true);
      transformContext.setRow(0);

      expression = "contextError && equals(currentRow,0)";
      assertTrue(evaluator.evaluateBoolean(expression));

      // job ran successfully but no rows processed
      transformContext.setError(false);
      expression = "! contextError && equals(currentRow,0)";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "noRowsProcessed";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! islast && match(Working.userName,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );

      expression = "! islast && match(Working.userName,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );

    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    finally {
      transformContext.setError(false);
      transformContext.setRow(42);
    }

  }




  /**
   * contextError is a literal for the current error state of the current transform (parent) context.
   */
  @Test
  public void booleanContextError() {

    String expression;
    try {
      expression = "contextError"; // constant supported by the boolean evaluator
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "! contextError";
      assertTrue(evaluator.evaluateBoolean(expression));

      transformContext.setError(true);

      expression = "contextError";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! contextError";
      assertFalse(evaluator.evaluateBoolean(expression));
    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    finally {
      transformContext.setError(false);
    }
  }




  /**
   * Empty is a method which tests the value of a frame field, transform
   * context key or symbol in the transform context symbol table.
   */
  @Test
  public void booleanEmpty() {
    String expression;
    try {
      expression = "empty(\"Working.NullField\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "empty(Working.Field1)";
      assert (evaluator.evaluateBoolean(expression));

      expression = "empty(\"Working.field1\")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "empty(Working.field1)";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "empty(\"field1\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "empty(field1)";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "empty(\"Working.Field1\")"; // case sensitive
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "empty(Working.Field1)"; // case sensitive
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "empty(string)"; // context
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "empty(\"string\")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "empty(\" string \")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "empty(String)";
      assertTrue(evaluator.evaluateBoolean(expression));

    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }




  @Test
  public void booleanEquals() {
    String expression;
    try {
      expression = "equals(5,5)";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "equals(5,4)";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "equals(currentRow,42)";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "equals(currentRow,43)";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "equals(Working.field1,value1)";
      assertTrue(evaluator.evaluateBoolean(expression));

    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }




  /**
   * Exists is a method to check for the existence of a field in the
   * transaction context of the transform context.
   */
  @Test
  public void booleanExists() {

    String expression;

    // Test the exists method
    try {
      expression = "exists(\"Source.RecordType\")";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "exists(\"Working.field1\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "exists(\"field1\")"; // working assumed
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "exists(\"Working.Field1\")"; // case sensitive
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "exists(\"Working.NullField\")";
      assertTrue(evaluator.evaluateBoolean(expression));

      // test un-quoted values
      expression = "exists(field1)";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "exists(Working.Field1)";
      assertFalse(evaluator.evaluateBoolean(expression));

    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }




  @Test
  public void booleanIsLast() {

    String expression;
    try {
      expression = "islast"; // constant supported by the boolean evaluator
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! islast";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "islast || false";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! islast || false";
      assertFalse(evaluator.evaluateBoolean(expression));

    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }




  /**
   * Test method for {@link coyote.dx.eval.Evaluator#evaluateBoolean(java.lang.String)}.
   */
  @Test
  public void booleanOperators() {

    String expression;
    try {

      expression = "true == true";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "true == false";
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "true && false";
      assertFalse(evaluator.evaluateBoolean(expression));
      //System.out.println( expression + " = " + evaluator.evaluateBoolean( expression ) );

      expression = "true || false";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "!true";
      assertFalse(evaluator.evaluateBoolean(expression));

    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }




  /**
   * TransactionError is a literal for the current error state of the current transaction context.
   */
  @Test
  public void booleanTransactionError() {

    String expression;
    try {
      expression = "transactionError"; // constant supported by the boolean evaluator
      assertFalse(evaluator.evaluateBoolean(expression));

      expression = "! transactionError";
      assertTrue(evaluator.evaluateBoolean(expression));

      context.setError(true);

      expression = "transactionError";
      assertTrue(evaluator.evaluateBoolean(expression));

      expression = "! transactionError";
      assertFalse(evaluator.evaluateBoolean(expression));
    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    finally {
      context.setError(false);
    }
  }




  /**
   * Test method for {@link coyote.dx.eval.Evaluator#evaluateNumeric(java.lang.String)}.
   */
  @Test
  public void testEvaluateNumeric() {

    assertEquals(-2, evaluator.evaluateNumeric("2+-2^2"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("6 / 3"), 0.001);
    assertEquals(Double.POSITIVE_INFINITY, evaluator.evaluateNumeric("2/0"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("7 % 2.5"), 0.001);
    assertEquals(-1., evaluator.evaluateNumeric("-1"), 0.001);
    assertEquals(1., evaluator.evaluateNumeric("1"), 0.001);
    assertEquals(-3, evaluator.evaluateNumeric("1+-4"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("3-1"), 0.001);
    assertEquals(-4, evaluator.evaluateNumeric("-2^2"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("4^0.5"), 0.001);
    assertEquals(1, evaluator.evaluateNumeric("sin ( pi /2)"), 0.001);
    assertEquals(-1, evaluator.evaluateNumeric("cos(pi)"), 0.001);
    assertEquals(1, evaluator.evaluateNumeric("tan(pi/4)"), 0.001);
    assertEquals(Math.PI, evaluator.evaluateNumeric("acos( -1)"), 0.001);
    assertEquals(Math.PI / 2, evaluator.evaluateNumeric("asin(1)"), 0.001);
    assertEquals(Math.PI / 4, evaluator.evaluateNumeric("atan(1)"), 0.001);
    assertEquals(1, evaluator.evaluateNumeric("ln(e)"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("log(100)"), 0.001);
    assertEquals(-1, evaluator.evaluateNumeric("min(1,-1)"), 0.001);
    assertEquals(-1, evaluator.evaluateNumeric("min(8,3,1,-1)"), 0.001);
    assertEquals(11, evaluator.evaluateNumeric("sum(8,3,1,-1)"), 0.001);
    assertEquals(3, evaluator.evaluateNumeric("avg(8,3,1,0)"), 0.001);
    assertEquals(3, evaluator.evaluateNumeric("abs(-3)"), 0.001);
    assertEquals(3, evaluator.evaluateNumeric("ceil(2.45)"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("floor(2.45)"), 0.001);
    assertEquals(2, evaluator.evaluateNumeric("round(2.45)"), 0.001);
    assertEquals(evaluator.evaluateNumeric("tanh(5)"), evaluator.evaluateNumeric("sinh(5)/cosh(5)"), 0.001);
    assertEquals(-1, evaluator.evaluateNumeric("min(1,min(3+2,2))+-round(4.1)*0.5"), 0.001);

    // (2^3-1)*sin(pi/4)/ln(pi^2) = 2.1619718020347976
    //System.out.println(evaluator.evaluateNumeric( "(2^3-1)*sin(pi/4)/ln(pi^2)" ));

    final double rnd = evaluator.evaluateNumeric("random()");
    assertTrue((rnd >= 0) && (rnd <= 1.0));

  }




  /**
   * Test method for {@link coyote.dx.eval.Evaluator#Evaluator()}.
   */
  @Test
  public void testEvaluator() {
    final Evaluator subject = new Evaluator();
    assertNotNull(subject);
  }




  /**
   * Test method for {@link coyote.dx.eval.Evaluator#Evaluator(coyote.dx.context.TransformContext)}.
   */
  @Test
  public void testEvaluatorOperationalContext() {
    final Evaluator subject = new Evaluator(transformContext);
    assertNotNull(subject);
  }




  /**
   * Test method for {@link coyote.dx.eval.Evaluator#setContext(TransformContext)}.
   */
  @Test
  public void testSetContext() {
    evaluator.setContext(transformContext);
  }

}
