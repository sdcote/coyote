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
package coyote.dx.validate;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.FrameValidator;
import coyote.dx.TransactionContext;
import coyote.dx.validate.NotNull;
import coyote.dx.validate.ValidationException;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class NotNullTest extends AbstractTest {

  @Test
  public void test() {

    String cfgData = "{ \"field\" : \"model\",  \"desc\" : \"Model cannot be empty\" }";
    DataFrame configuration = parseConfiguration( cfgData );

    // create the component to test
    FrameValidator validator = new NotNull();

    // Configure it
    try {
      validator.setConfiguration( configuration );
    } catch ( ConfigurationException e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

    // open (initialize) the component 
    validator.open( getTransformContext() );

    // Create a transaction context
    TransactionContext context = createTransactionContext();

    // Populate it with test data
    DataFrame sourceFrame = new DataFrame();
    sourceFrame.put( "model", "PT4500" );

    context.setSourceFrame( sourceFrame );

    try {
      boolean result = validator.process( context );
      assertTrue( result );
    } catch ( ValidationException e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put( "model", " " );
    context.setSourceFrame( sourceFrame );

    try {
      boolean result = validator.process( context );
      assertTrue( result );
    } catch ( ValidationException e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

    context = createTransactionContext();
    sourceFrame = new DataFrame();
    sourceFrame.put( "model", null );
    context.setSourceFrame( sourceFrame );

    try {
      boolean result = validator.process( context );
      assertFalse( result );
    } catch ( ValidationException e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }

  }

}
