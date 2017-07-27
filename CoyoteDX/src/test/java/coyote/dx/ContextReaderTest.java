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
package coyote.dx;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrame;
import coyote.dx.context.TransformContext;
import coyote.dx.reader.ContextReader;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class ContextReaderTest extends AbstractTest {
  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  @Test
  public void singleFrame() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine( "ContextReadWrite" );
    assertNotNull( engine );

    TransformContext transformContext = new TransformContext();

    engine.setContext( transformContext );
    TransformContext context = engine.getContext();
    assertNotNull( context );

    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull( symbols );

    DataFrame frame = new DataFrame().set( "field1", "value1" ).set( "Field2", "Value2" ).set( "NullField", null );

    context.set( ContextReader.DEFAULT_CONTEXT_FIELD, frame );

    turnOver( engine );
    System.out.println( engine.getContext().getRow() );
    assertTrue( 1 == context.getRow() );

    Object obj = context.get( "ContextOutput" );

    //assertNotNull(obj);
  }

}
