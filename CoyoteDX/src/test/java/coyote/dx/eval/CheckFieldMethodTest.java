/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * Unit tests for the checkField method.
 */
public class CheckFieldMethodTest {

  private static Evaluator evaluator = null;
  private static TransformContext transformContext = null;
  private static TransactionContext context = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    transformContext = new TransformContext();
    transformContext.set("one", 1);
    transformContext.set("two", 2);
    transformContext.set("name", "Bob");

    context = new TransactionContext(transformContext);
    context.setLastFrame(true);
    context.setSourceFrame(new DataFrame().set("field1", "value1").set("Field2", "Value2").set("BooleanField", true));
    context.setWorkingFrame(new DataFrame().set("field3", "value3").set("Field4", "Value4").set("DateField", new Date()));
    context.setTargetFrame(new DataFrame().set("field5", "value5").set("Field6", "Value6").set("LongField", 123L));
    transformContext.setTransaction(context);

    evaluator = new Evaluator(transformContext);
  }




  @Test
  public void evaluatorCheck() {
    String expression;

    expression = "checkField(Test, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Working.field3, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Working.fieldX, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Source.field1, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Source.fieldX, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Target.field5, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Target.fieldX, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Target.LongField, LT, Working.DateField)";
    assertTrue(evaluator.evaluateBoolean(expression));

  }




  @Test
  public void equalTo() {
    CheckFieldMethod.execute(transformContext, "name", "EQ", "Bob");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EQ.toString(), "Bob");
    CheckFieldMethod.execute(transformContext, "name", "EQ", "Robert");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EQ.toString(), "Robert");
  }




  @Test
  public void equalToIgnoreCase() {
    CheckFieldMethod.execute(transformContext, "name", "EI", "BOB");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EI.toString(), "BOB");
    CheckFieldMethod.execute(transformContext, "name", "EQ", "Robert");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EQ.toString(), "Robert");
  }




  @Test
  public void lessThan() {
    CheckFieldMethod.execute(transformContext, "one", "LT", "2");
    CheckFieldMethod.execute(transformContext, "one", CheckFieldMethod.Operator.LT.toString(), "2");
  }




  @Test
  public void lessThanEqualTo() {
    CheckFieldMethod.execute(transformContext, "one", "LE", "2");
    CheckFieldMethod.execute(transformContext, "one", CheckFieldMethod.Operator.LE.toString(), "2");
    CheckFieldMethod.execute(transformContext, "two", "LE", "2");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.LE.toString(), "2");
  }




  @Test
  public void greaterThan() {
    CheckFieldMethod.execute(transformContext, "two", "GT", "1");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GT.toString(), "1");
    CheckFieldMethod.execute(transformContext, "one", "GT", "1");
    CheckFieldMethod.execute(transformContext, "one", CheckFieldMethod.Operator.GT.toString(), "1");
  }




  @Test
  public void greaterThanEqualTo() {
    CheckFieldMethod.execute(transformContext, "two", "GE", "1");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GE.toString(), "1");
    CheckFieldMethod.execute(transformContext, "two", "GE", "2");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GE.toString(), "2");
    CheckFieldMethod.execute(transformContext, "two", "GE", "3");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GE.toString(), "3");
  }




  @Test
  public void notEqual() {
    CheckFieldMethod.execute(transformContext, "name", "NE", "BOB");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.NE.toString(), "BOB");
    CheckFieldMethod.execute(transformContext, "name", "NE", "Robert");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.NE.toString(), "Robert");
  }

}
