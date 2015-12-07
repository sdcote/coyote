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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    evaluator = new Evaluator( transformContext );

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
   * Test method for {@link coyote.batch.eval.Evaluator#Evaluator(coyote.batch.TransformContext)}.
   */
  @Test
  public void testEvaluatorOperationalContext() {
    Evaluator subject = new Evaluator( transformContext );
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

      expression = "islast"; // constant supported by the boolean evaluator
      assertTrue( evaluator.evaluateBoolean( expression ) );

      expression = "! islast";
      assertFalse( evaluator.evaluateBoolean( expression ) );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

    // we need to create methods...functions which take strings as aguments...
    // this will allow us to do more logical things like looking up values
    try {
      expression = "exists(\"Source.RecordType\")";

      evaluator.evaluateBoolean( expression );
      // assertFalse( evaluator.evaluateBoolean( expression ) );

      expression = "! islast and matches(WorkingFrame.userName,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

    try {
      expression = "islast and equals(WorkingFrame.record_type,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );

      expression = "! islast and matches(WorkingFrame.userName,\"22\")";
      // assertFalse( evaluator.evaluateBoolean( expression ) );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

  }




  /**
   * Test method for {@link coyote.batch.eval.Evaluator#evaluateNumeric(java.lang.String)}.
   */
  @Test
  public void testEvaluateNumeric() {

    assertEquals( -2, evaluator.evaluateNumeric( "2+-2^2" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "6 / 3" ), 0.001 );
    assertEquals( Double.POSITIVE_INFINITY, evaluator.evaluateNumeric( "2/0" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "7 % 2.5" ), 0.001 );
    assertEquals( -1., evaluator.evaluateNumeric( "-1" ), 0.001 );
    assertEquals( 1., evaluator.evaluateNumeric( "1" ), 0.001 );
    assertEquals( -3, evaluator.evaluateNumeric( "1+-4" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "3-1" ), 0.001 );
    assertEquals( -4, evaluator.evaluateNumeric( "-2^2" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "4^0.5" ), 0.001 );
    assertEquals( 1, evaluator.evaluateNumeric( "sin ( pi /2)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateNumeric( "cos(pi)" ), 0.001 );
    assertEquals( 1, evaluator.evaluateNumeric( "tan(pi/4)" ), 0.001 );
    assertEquals( Math.PI, evaluator.evaluateNumeric( "acos( -1)" ), 0.001 );
    assertEquals( Math.PI / 2, evaluator.evaluateNumeric( "asin(1)" ), 0.001 );
    assertEquals( Math.PI / 4, evaluator.evaluateNumeric( "atan(1)" ), 0.001 );
    assertEquals( 1, evaluator.evaluateNumeric( "ln(e)" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "log(100)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateNumeric( "min(1,-1)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateNumeric( "min(8,3,1,-1)" ), 0.001 );
    assertEquals( 11, evaluator.evaluateNumeric( "sum(8,3,1,-1)" ), 0.001 );
    assertEquals( 3, evaluator.evaluateNumeric( "avg(8,3,1,0)" ), 0.001 );
    assertEquals( 3, evaluator.evaluateNumeric( "abs(-3)" ), 0.001 );
    assertEquals( 3, evaluator.evaluateNumeric( "ceil(2.45)" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "floor(2.45)" ), 0.001 );
    assertEquals( 2, evaluator.evaluateNumeric( "round(2.45)" ), 0.001 );
    assertEquals( evaluator.evaluateNumeric( "tanh(5)" ), evaluator.evaluateNumeric( "sinh(5)/cosh(5)" ), 0.001 );
    assertEquals( -1, evaluator.evaluateNumeric( "min(1,min(3+2,2))+-round(4.1)*0.5" ), 0.001 );

    // (2^3-1)*sin(pi/4)/ln(pi^2) = 2.1619718020347976
    //System.out.println(evaluator.evaluateNumeric( "(2^3-1)*sin(pi/4)/ln(pi^2)" ));
    
    double rnd = evaluator.evaluateNumeric( "random()" );
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
