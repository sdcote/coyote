/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.filter;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;

import coyote.dataframe.DataFrame;
import coyote.dx.FrameFilter;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * 
 */
public class RejectTest {
  private static TransformContext transformContext = null;
  private static TransactionContext context = null;




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

    DataFrame workingFrame = new DataFrame();
    workingFrame.add( "Record Type", "PO" );
    workingFrame.add( "PO ID", "12345" );
    workingFrame.add( "Description", "Test purchase order." );
    context.setWorkingFrame( workingFrame );
    // Mimic the transform engine and place a reference to the transaction 
    // context in the transform context
    transformContext.setTransaction( context );

  }




  // @Test
  public void testSetContext() {

    Reject filter = new Reject();
    filter.setContext( transformContext );

    filter.process( context );
  }




  // @Test
  public void testRejectContext() {

    FrameFilter filter = new Reject();
    filter.open( transformContext );

    assertTrue( filter.process( context ) );

  }




  public void testProcessContext() {

    FrameFilter filter = new Reject();
    filter.open( transformContext );
    assertTrue( filter.process( context ) );
    assertFalse( filter.process( context ) );

  }

}
