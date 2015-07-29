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


/**
 * This tests the ability to simply run a bunch of tasks in order within a 
 * context to perform some generic function.
 */
public class DatabaseTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger( DatabaseTest.class );




  @Test
  public void test() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "databasetest" );
    assertNotNull( engine );

    TransformContext context = engine.getContext();
    assertNotNull( context );

    // Get a connection to DevDB
    Database devdb = context.getDatabase( "DevDB" );
    assertNotNull( devdb );

    // Get a connection to MyDB
    Database mydb = context.getDatabase( "MyDB" );
    assertNotNull( mydb );

    assertTrue( devdb != mydb );

    Connection devconn = devdb.getConnection();
    assertNotNull( devconn );

    Connection myconn = mydb.getConnection();
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

      // Close the engine which should close the context and the database 
      // connections it contains
      try {
        engine.close();
      } catch ( IOException e ) {
        e.printStackTrace();
        LOG.error( e.getMessage() );
      }

    } //try-catch-finally

  }

}
