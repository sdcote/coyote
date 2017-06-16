/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.context;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.DefaultTransformEngine;
import coyote.dx.Symbols;
import coyote.dx.TransformEngine;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class DatabaseContextTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {

  }




  @Test
  public void contextWithLibraryAttribute() {
    String jobName = "ContextTest";
    
    DataFrame config = new DataFrame()
        .set( "class", "DatabaseContext" )
        .set( "target", "jdbc:h2:./test" )
        .set( "autocreate", true )
        .set( "library", "jar:file:src/resources/demojars/h2-1.4.187.jar!/" )
        .set( "driver", "org.h2.Driver" )
        .set( "username", "sa" )
        .set( "password", "" )
        .set( "fields", new DataFrame()
              .set( "SomeKey", "SomeValue" )
              .set( "AnotherKey", "AnotherValue" ) 
            );

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName( jobName);
    TransformContext context = new DatabaseContext();
    context.setConfiguration( new Config( config ) );
    engine.setContext( context );

    turnOver( engine );

    Object obj = context.get( Symbols.RUN_COUNT );
    assertTrue( obj instanceof Long );
    long runcount = (Long)obj;
    assertTrue( runcount > 0 );

    turnOver( engine );

    obj = context.get( Symbols.RUN_COUNT );
    assertTrue( obj instanceof Long );
    long nextRunCount = (Long)obj;
    assertEquals( runcount + 1, nextRunCount );

    // Replace the context with a new one to test reading from database
    context = new DatabaseContext();
    context.setConfiguration( new Config( config ) );
    engine.setContext( context );

    turnOver( engine );
    
    obj = context.get( Symbols.RUN_COUNT );
    assertTrue( obj instanceof Long );
    long lastRunCount = (Long)obj;
    assertEquals( nextRunCount + 1, lastRunCount );
  }


  @Test
  public void msqltests() {
    String jobName = "ContextTest";
    
    DataFrame config = new DataFrame()
        .set( "class", "DatabaseContext" )
        .set( "target", "jdbc:sqlserver://coyote.database.windows.net:1433;database=coyotedx" )
        .set( "autocreate", true )
        .set( "library", "jar:file:src/resources/demojars/sqljdbc42.jar!/" )
        .set( "driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver" )
        .set( "ENC:username", "Z3d0v5lmgvPZRCsUdG/B4FsyrmPUM1WsVrQY8szJIetIJE3TBbjmBQ==" )
        .set( "ENC:password", "k0Vl7ZgH3Fb0xaR3tlZcWkQKlyFNmIGISCRN0wW45gU=" )
        .set( "fields", new DataFrame()
              .set( "SomeKey", "SomeValue" )
              .set( "AnotherKey", "AnotherValue" ) 
            );

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName( jobName);
    TransformContext context = new DatabaseContext();
    context.setConfiguration( new Config( config ) );
    engine.setContext( context );

    turnOver( engine );

    Object obj = context.get( Symbols.RUN_COUNT );
    assertTrue( obj instanceof Long );
    long runcount = (Long)obj;
    assertTrue( runcount > 0 );

  }




  /**
   * Run and close the given engine.
   * @param engine to run and close
   */
  private void turnOver( TransformEngine engine ) {
    try {
      engine.run();
    } catch ( Exception e ) {}
    try {
      engine.close();
    } catch ( Exception e ) {}
  }




}
