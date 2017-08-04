/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


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
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.NOTICE_EVENTS));
    // Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.NOTICE_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );

    transformContext = new TransformContext();
    transformContext.set("one", 1);
    transformContext.set("two", 2);
    transformContext.set("name", "Bob");

    context = new TransactionContext(transformContext);
    context.setLastFrame(true);
    context.setSourceFrame(new DataFrame().set("field1", "value1").set("Field2", "Value2").set("BooleanField", true).set("IntegerField", 123));
    context.setWorkingFrame(new DataFrame().set("field3", "value3").set("Field4", "Value4").set("DateField", new Date()).set("DoubleValue", 123.0D));
    context.setTargetFrame(new DataFrame().set("field5", "value5").set("Field6", "Value6").set("LongField", 123L).set("FloatField", 123.0F));
    transformContext.setTransaction(context);

    evaluator = new Evaluator(transformContext);
  }




  @Test
  public void evaluatorCheck() {
    String expression;

    expression = "checkField(one, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(one, <, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Working.field3, LT, 123)"; // can't compare
    assertFalse(evaluator.evaluateBoolean(expression));

    expression = "checkField(Working.fieldX, LT, 123)"; // null is < not null
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Source.field1, LT, 123)"; // can't compare
    assertFalse(evaluator.evaluateBoolean(expression));

    expression = "checkField(Source.fieldX, LT, 123)"; // null is < not null
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Target.field5, LT, 123)"; // can't compare
    assertFalse(evaluator.evaluateBoolean(expression));

    expression = "checkField(Target.fieldX, LT, 123)"; // null is < not null
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "checkField(Target.LongField, LT, Working.DateField)";
    assertTrue(evaluator.evaluateBoolean(expression));

  }




  @Test
  public void equalTo() {
    assertTrue(CheckFieldMethod.execute(transformContext, "name", "EQ", "Bob"));
    assertTrue(CheckFieldMethod.execute(transformContext, "name", "==", "Bob"));
    assertTrue(CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EQ.toString(), "Bob"));
    assertFalse(CheckFieldMethod.execute(transformContext, "name", "EQ", "BoB"));
    assertFalse(CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EQ.toString(), "BOB"));
    assertFalse(CheckFieldMethod.execute(transformContext, "name", "EQ", "Robert"));
    assertFalse(CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EQ.toString(), "Robert"));
    
    assertTrue(CheckFieldMethod.execute(transformContext, "Source.IntegerField", "EQ", "123"));
    assertTrue(CheckFieldMethod.execute(transformContext, "Source.IntegerField", "EQ", "Working.DoubleValue"));

  }




  @Ignore
  public void equalToIgnoreCase() {
    assertTrue(CheckFieldMethod.execute(transformContext, "name", "EI", "BOB"));
    assertTrue(CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EI.toString(), "BOB"));
    assertFalse(CheckFieldMethod.execute(transformContext, "name", "EI", "Robert"));
    assertFalse(CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.EI.toString(), "Robert"));
  }




  @Test
  public void lessThan() {
    assertTrue(CheckFieldMethod.execute(transformContext, "one", "LT", "2"));
    assertTrue(CheckFieldMethod.execute(transformContext, "one", "<", "2"));
    assertTrue(CheckFieldMethod.execute(transformContext, "one", CheckFieldMethod.Operator.LT.toString(), "2"));
  }




  @Ignore
  public void lessThanEqualTo() {
    CheckFieldMethod.execute(transformContext, "one", "LE", "2");
    CheckFieldMethod.execute(transformContext, "one", CheckFieldMethod.Operator.LE.toString(), "2");
    CheckFieldMethod.execute(transformContext, "two", "LE", "2");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.LE.toString(), "2");
  }




  @Ignore
  public void greaterThan() {
    CheckFieldMethod.execute(transformContext, "two", "GT", "1");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GT.toString(), "1");
    CheckFieldMethod.execute(transformContext, "one", "GT", "1");
    CheckFieldMethod.execute(transformContext, "one", CheckFieldMethod.Operator.GT.toString(), "1");
  }




  @Ignore
  public void greaterThanEqualTo() {
    CheckFieldMethod.execute(transformContext, "two", "GE", "1");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GE.toString(), "1");
    CheckFieldMethod.execute(transformContext, "two", "GE", "2");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GE.toString(), "2");
    CheckFieldMethod.execute(transformContext, "two", "GE", "3");
    CheckFieldMethod.execute(transformContext, "two", CheckFieldMethod.Operator.GE.toString(), "3");
  }




  @Ignore
  public void notEqual() {
    CheckFieldMethod.execute(transformContext, "name", "NE", "BOB");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.NE.toString(), "BOB");
    CheckFieldMethod.execute(transformContext, "name", "NE", "Robert");
    CheckFieldMethod.execute(transformContext, "name", CheckFieldMethod.Operator.NE.toString(), "Robert");
  }

}
