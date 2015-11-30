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
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
  private static Evaluator evaluator = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    // Create simple transformation context
    transformContext = new TransformContext();
    transformContext.set( "string", "Nylon" );

    // create a transaction context within the transformation context  
    context = new TransactionContext( transformContext );
    context.setLastFrame( true );

    // Mimic the transform engine and place a reference to the transaction 
    // context in the transform context
    transformContext.setTransaction( context );

    // create a new evaluator setting the transform context used to resolve 
    // variables
    evaluator = new Evaluator(transformContext);
    
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#Evaluator()}.
   */
  @Test
  public void testEvaluator() {
    Evaluator subject = new Evaluator();
    assertNotNull( subject );
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#Evaluator(coyote.batch.OperationalContext)}.
   */
  @Test
  public void testEvaluatorOperationalContext() {
    Evaluator subject = new Evaluator( context );
    assertNotNull( subject );
  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#evaluateBoolean(java.lang.String)}.
   */
  @Test
  public void testEvaluateBoolean() {

    String expression;
    try {
      expression = "true && false";
      assertFalse( evaluator.evaluateBoolean( expression ) );
      //System.out.println( expression + " = " + evaluator.evaluateBoolean( expression ) );
      
      expression = "true || false";
      assertTrue( evaluator.evaluateBoolean( expression ) );

      expression = "!true";
      assertFalse( evaluator.evaluateBoolean( expression ) );

      expression = "isLastFrame";
      //assertTrue( evaluator.evaluateBoolean( expression ) );

      expression = "not isLastFrame";
      // assertFalse( evaluator.evaluateBoolean( expression ) );

      expression = "!isLastFrame";
      // assertFalse( evaluator.evaluateBoolean( expression ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    try {
      expression = "isLastFrame and equals(WorkingFrame.record_type,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );

      expression = "not isLastFrame and matches(WorkingFrame.userName,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#evaluateDouble(java.lang.String)}.
   */
  @Test
  public void testEvaluateDouble() {

    assertEquals( -2, evaluator.evaluateDouble( "2+-2^2" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "6 / 3" ), 0.001 );
    assertEquals( Double.POSITIVE_INFINITY, evaluator.evaluateDouble( "2/0" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "7 % 2.5" ), 0.001 );
    assertEquals( -1., evaluator.evaluateDouble( "-1" ), 0.001 );
    assertEquals( 1., evaluator.evaluateDouble( "1" ), 0.001 );
    assertEquals( -3, evaluator.evaluateDouble( "1+-4" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "3-1" ), 0.001 );
    assertEquals( -4, evaluator.evaluateDouble( "-2^2" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "4^0.5" ), 0.001 );
    assertEquals( 1, evaluator.evaluateDouble( "sin ( pi /2)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateDouble( "cos(pi)" ), 0.001 );
    assertEquals( 1, evaluator.evaluateDouble( "tan(pi/4)" ), 0.001 );
    assertEquals( Math.PI, evaluator.evaluateDouble( "acos( -1)" ), 0.001 );
    assertEquals( Math.PI / 2, evaluator.evaluateDouble( "asin(1)" ), 0.001 );
    assertEquals( Math.PI / 4, evaluator.evaluateDouble( "atan(1)" ), 0.001 );
    assertEquals( 1, evaluator.evaluateDouble( "ln(e)" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "log(100)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateDouble( "min(1,-1)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateDouble( "min(8,3,1,-1)" ), 0.001 );
    assertEquals( 11, evaluator.evaluateDouble( "sum(8,3,1,-1)" ), 0.001 );
    assertEquals( 3, evaluator.evaluateDouble( "avg(8,3,1,0)" ), 0.001 );
    assertEquals( 3, evaluator.evaluateDouble( "abs(-3)" ), 0.001 );
    assertEquals( 3, evaluator.evaluateDouble( "ceil(2.45)" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "floor(2.45)" ), 0.001 );
    assertEquals( 2, evaluator.evaluateDouble( "round(2.45)" ), 0.001 );
    assertEquals( evaluator.evaluateDouble( "tanh(5)" ), evaluator.evaluateDouble( "sinh(5)/cosh(5)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateDouble( "min(1,min(3+2,2))+-round(4.1)*0.5" ), 0.001 );

    double rnd = evaluator.evaluateDouble( "random()" );
    assertTrue( rnd >= 0 && rnd <= 1.0 );

  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#setContext(coyote.batch.OperationalContext)}.
   */
  @Test
  public void testSetContext() {
    evaluator.setContext( transformContext );
  }

}
