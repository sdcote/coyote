/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.eval;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dx.Symbols;
import coyote.dx.context.TransformContext;

/**
 * 
 */
public class CheckFieldMethodTest {

  private static Evaluator evaluator = null;
   private static TransformContext transformContext = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    transformContext = new TransformContext();

    evaluator = new Evaluator(transformContext);

  }



  @Test
  public void test() {
    String expression;

    expression = "checkField(Test, LT, 123)";
    assertTrue(evaluator.evaluateBoolean(expression));    
  }

}
