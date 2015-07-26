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
package coyote.batch.writer;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.AbstractEngineTest;
import coyote.batch.TransformContext;
import coyote.batch.TransformEngine;
import coyote.commons.template.SymbolTable;


/**
 * This tests the ability to simply run a bunch of tasks in order within a 
 * context to perform some generic function.
 */
public class FlatFileTest extends AbstractEngineTest {
  private static final Logger LOG = LoggerFactory.getLogger( FlatFileTest.class );




  @Test
  public void test() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "ffwritertest" );
    assertNotNull( engine );

    try {
      engine.run();
    } catch ( Exception e ) {
      e.printStackTrace();
      LOG.error( e.getMessage() );
      fail( e.getMessage() );
    }
    try {
      engine.close();
    } catch ( IOException e ) {
      e.printStackTrace();
      LOG.error( e.getMessage() );
    }

  }

}
