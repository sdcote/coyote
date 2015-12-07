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
package coyote.batch.filter;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.batch.FrameFilter;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class AcceptTest {
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




  //@Test
  public void testSetContext() {

    Accept filter = new Accept();
    filter.open( transformContext );

    filter.process( context );
  }




  //@Test
  public void testAcceptConstructor() {
    Accept filter = new Accept("match( Working.Record Type , PO )");
    filter.open( transformContext );

    // this should fire and return false: do not process other filters
    assertFalse( filter.process( context ) );

    // The 
    assertNotNull( context.getWorkingFrame() );

  }

  @Test
  public void testAcceptContext() {
    Accept filter = new Accept();
    String expression = "match( Working.Record Type , PO )";
    filter.setCondition( expression );
    filter.open( transformContext );

    // this should fire and return false: do not process other filters
    assertFalse( filter.process( context ) );

    // The 
    assertNotNull( context.getWorkingFrame() );

  }




  public void testProcessContext() {

    FrameFilter filter = new Accept();
    assertTrue( filter.process( context ) );
    assertFalse( filter.process( context ) );

  }

}
