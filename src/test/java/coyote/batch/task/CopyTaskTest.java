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
package coyote.batch.task;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.AbstractTest;
import coyote.batch.TransformContext;
import coyote.batch.TransformEngine;


/**
 * 
 */
public class CopyTaskTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger( CopyTaskTest.class );




  @Test
  public void test() {

    //TODO we should delete the work directory

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "copytest" );
    assertNotNull( engine );

    TransformContext context = engine.getContext();
    assertNotNull( context );

    try {
      engine.run();
    } catch ( Exception e ) {
      e.printStackTrace();
      LOG.error( e.getMessage() );
      fail( e.getMessage() );
    }

    // TODO: we should check for the existence of the files in the work directory

    try {
      engine.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

  }

}
