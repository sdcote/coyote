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
package coyote.dx;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import coyote.commons.template.SymbolTable;
import coyote.dx.TransformContext;
import coyote.dx.TransformEngine;
import coyote.loader.log.Log;


/**
 * 
 */
public class ContextTest extends AbstractTest {




  //@Test
  public void test() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "contexttest" );
    assertNotNull( engine );

    TransformContext context = engine.getContext();
    assertNotNull( context );

    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull( symbols );

    // run the engine so the context get opened and initialized
    try {
      engine.run();
    } catch ( Exception ignore ) {
    }
    
    
    String filename = context.getAsString( "filename" );
    assertNotNull( filename );
    Log.debug( filename );

    Log.debug( context.dump() );

    try {
      engine.close();
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
