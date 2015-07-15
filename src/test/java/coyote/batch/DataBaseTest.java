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
package coyote.batch;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.DataBase;
import coyote.batch.TransformContext;
import coyote.batch.TransformEngine;


/**
 * This tests the ability to simply run a bunch of tasks in order within a 
 * context to perform some generic function.
 */
public class DataBaseTest extends AbstractEngineTest {
  private static final Logger LOG = LoggerFactory.getLogger( DataBaseTest.class );




  @Test
  public void test() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "databasetest" );
    assertNotNull( engine );

    TransformContext context = engine.getContext();
    assertNotNull( context );

    // Get a connection to DevDB
    DataBase devstore = context.getDataStore( "DevDB" );
    assertNotNull( devstore );

    // Get a connection to MyDB
    DataBase mystore = context.getDataStore( "MyDB" );
    assertNotNull( mystore );

    assertTrue( devstore != mystore );
    
    Connection devconn = devstore.getConnection();
    assertNotNull( devconn );
    
    Connection myconn = mystore.getConnection();
    assertNotNull( myconn );
    
    assertTrue( devconn != myconn );

    

    // Run the engine to make sure it is valid even if nothing is defined
    try {

      engine.run();

    } catch ( Exception e ) {

      e.printStackTrace();
      LOG.error( e.getMessage() );
      fail( e.getMessage() );

    }
    finally {

      try {
        engine.close();
      } catch ( IOException e ) {
        e.printStackTrace();
        LOG.error( e.getMessage() );
      }

    } //try-catch-finally

  }

}
