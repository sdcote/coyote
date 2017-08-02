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

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dx.Symbols;
import coyote.dx.context.TransformContext;


public class JobStatusMethodTest {

  private static Evaluator evaluator = null;
  private static TransformContext transformContext = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    transformContext = new TransformContext();

    evaluator = new Evaluator(transformContext);

    final TransformContext goodContext = new TransformContext();
    Map<String, Object> disposition = new HashMap<String, Object>();
    disposition.put(Symbols.RUN_COUNT, 1);
    disposition.put(TransformContext.STARTTIME, System.currentTimeMillis() - 10000);
    disposition.put(TransformContext.ENDTIME, System.currentTimeMillis());
    disposition.put(TransformContext.ERROR_STATE, false);
    disposition.put(TransformContext.ERROR_MSG, null);
    disposition.put(TransformContext.FRAME_COUNT, 50000);
    goodContext.set(TransformContext.DISPOSITION, disposition);
    transformContext.set("GoodJob", goodContext.toMap());

    final TransformContext badContext = new TransformContext();
    disposition = new HashMap<String, Object>();
    disposition.put(Symbols.RUN_COUNT, 1);
    disposition.put(TransformContext.STARTTIME, System.currentTimeMillis());
    disposition.put(TransformContext.ENDTIME, System.currentTimeMillis());
    disposition.put(TransformContext.ERROR_STATE, true);
    disposition.put(TransformContext.ERROR_MSG, "Something went awry");
    disposition.put(TransformContext.FRAME_COUNT, 0);
    badContext.set(TransformContext.DISPOSITION, disposition);
    transformContext.set("BadJob", badContext.toMap());

  }




  @Test
  public void jobFailure() {

    String expression;

    expression = "jobFailure(BadJob)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "jobFailure(GoodJob)";
    assertFalse(evaluator.evaluateBoolean(expression));
  }




  @Test
  public void jobSuccess() {

    String expression;

    expression = "jobSuccess(GoodJob)";
    assertTrue(evaluator.evaluateBoolean(expression));

    expression = "jobSuccess(BadJob)";
    assertFalse(evaluator.evaluateBoolean(expression));
  }

}
