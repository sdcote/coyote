/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.eval;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.eval.BooleanSetEvaluator.BitSetEvaluationContext;


/**
 * 
 */
public class EvalTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {}




  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {}




  @Test
  public void test() {
    // Create a new evaluator
    DoubleEvaluator evaluator = new DoubleEvaluator();
    String expression = "(2^3-1)*sin(pi/4)/ln(pi^2)";
    // Evaluate an expression
    Double result = evaluator.evaluate(expression);

    assertEquals(result, new Double(2.1619718020347976));
    // System.out.println( expression + " = " + result );
  }




  /**
   * This show how to create an evaluator with a restricted set (subset) of
   * operators.
   */
  @Test
  public void restricting() {
    // Create a double evaluator that only support +,-,*,and / operators, with 
    // no constants, and no functions. The default parenthesis will be allowed

    // First create empty evaluator parameters
    Parameters params = new Parameters();

    // Add the supported operators to these parameters
    params.add(DoubleEvaluator.PLUS);
    params.add(DoubleEvaluator.MINUS);
    params.add(DoubleEvaluator.MULTIPLY);
    params.add(DoubleEvaluator.DIVIDE);
    params.add(DoubleEvaluator.NEGATE);

    // Add the default expression brackets
    params.addExpressionBracket(BracketPair.PARENTHESES);

    // Create the restricted evaluator
    DoubleEvaluator evaluator = new DoubleEvaluator(params);

    // Let's try some expressions
    doIt(evaluator, "(3*-4)+2");
    doIt(evaluator, "3^2");
    doIt(evaluator, "ln(5)");
  }




  private static void doIt(DoubleEvaluator evaluator, String expression) {
    try {
      System.out.println(expression + " = " + evaluator.evaluate(expression));
    } catch (IllegalArgumentException e) {
      System.out.println(expression + " is an invalid expression");
    }
  }




  /**
   * 
   */
  @Test
  public void testVariables() {
    final String expression = "sin(x)"; // Here is the expression to evaluate
    // Create the evaluator
    final DoubleEvaluator eval = new DoubleEvaluator();
    // Create a new empty variable set
    final StaticVariableSet<Double> variables = new StaticVariableSet<Double>();
    double x = 0;
    final double step = Math.PI / 8;
    while (x <= Math.PI / 2) {
      // Set the value of x
      variables.set("x", x);
      // Evaluate the expression
      eval.evaluate(expression, variables);
      x += step;
    }
  }




  @Test
  public void testExtendedEvaluator() {
    String expression = "sqrt(abs(-2))^2";
    System.out.println(expression + " = " + new ExtendedDoubleEvaluator().evaluate(expression));
  }




  @Test
  public void testFrenchEvaluator() {
    LocalizedEvaluator evaluator = new LocalizedEvaluator();
    String expression = "3 000 +moyenne(3 ; somme(1,5 ; 7 ; -3,5))";
    System.out.println(expression + " = " + evaluator.formatter.format(evaluator.evaluate(expression)));
  }




  @Test
  public void testBooleanEvaluator() {
    SimpleBooleanEvaluator evaluator = new SimpleBooleanEvaluator();
    String expression = "true && false";
    System.out.println(expression + " = " + evaluator.evaluate(expression));
    expression = "true || false";
    System.out.println(expression + " = " + evaluator.evaluate(expression));
    expression = "!true";
    System.out.println(expression + " = " + evaluator.evaluate(expression));
  }




  @Test
  public void testBooleanSetEvaluator() {
    BooleanSetEvaluator evaluator = new BooleanSetEvaluator();
    BitSetEvaluationContext context = new BitSetEvaluationContext(4);

    String expression = "0011 * 1010";
    BitSet result = evaluator.evaluate(expression, context);
    System.out.println(expression + " = " + BooleanSetEvaluator.toBinaryString(result));

    expression = "true * 1100";
    result = evaluator.evaluate(expression, context);
    System.out.println(expression + " = " + BooleanSetEvaluator.toBinaryString(result));

    expression = "-false";
    result = evaluator.evaluate(expression, context);
    System.out.println(expression + " = " + BooleanSetEvaluator.toBinaryString(result));

  }




  @Test
  public void testTextualEvaluator() {
    Map<String, String> variableToValue = new HashMap<String, String>();
    variableToValue.put("type", "PORT");
    AbstractEvaluator<Boolean> evaluator = new TextualOperatorsEvaluator();
    System.out.println("type=PORT -> " + evaluator.evaluate("type=PORT", variableToValue));
    System.out.println("type=NORTH -> " + evaluator.evaluate("type=NORTH", variableToValue));
    System.out.println("type=PORT AND true -> " + evaluator.evaluate("type=PORT AND true", variableToValue));
  }
}
