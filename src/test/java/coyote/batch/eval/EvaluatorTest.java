/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.eval;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;


/**
 * 
 */
public class EvaluatorTest {

  private static TransformContext transformContext = null;

  private static TransactionContext context = null;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    transformContext = new TransformContext();

    context = new TransactionContext(transformContext);
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#Evaluator()}.
   */
  @Test
  public void testEvaluator() {
    Evaluator subject = new Evaluator();
    assertNotNull(subject);
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#Evaluator(coyote.batch.OperationalContext)}.
   */
  @Test
  public void testEvaluatorOperationalContext() {
    Evaluator subject = new Evaluator( context );
    assertNotNull(subject);
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#evaluateBoolean(java.lang.String)}.
   */
  @Test
  public void testEvaluateBoolean() {
    //fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#evaluateDouble(java.lang.String)}.
   */
  @Test
  public void testEvaluateDouble() {
    //fail( "Not yet implemented" ); // TODO
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#setContext(coyote.batch.OperationalContext)}.
   */
  @Test
  public void testSetContext() {
    //fail( "Not yet implemented" ); // TODO
  }

}
