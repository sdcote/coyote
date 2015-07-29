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

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.commons.template.SymbolTable;


/**
 * 
 */
public class ContextTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger( ContextTest.class );




  @Test
  public void test() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "contexttest" );
    assertNotNull( engine );

    TransformContext context = engine.getContext();
    assertNotNull( context );

    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull( symbols );

    String filename = context.getAsString( "filename" );
    assertNotNull( filename );
    LOG.debug( filename );

    LOG.debug( context.dump() );

    try {
      engine.close();
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
