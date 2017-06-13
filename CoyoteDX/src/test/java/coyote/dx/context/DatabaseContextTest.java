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
    
    // TODO: Insert test data
    // INSERT INTO PUBLIC.CONTEXT (SYSID, NAME, "KEY", VALUE, "TYPE", CREATEDBY, CREATEDON, MODIFIEDBY, MODIFIEDON) VALUES('856c63f9-3d17-42d0-9de8-a74e2e4e9671', '', '', '', 0, '', '', '', '');
    // INSERT INTO PUBLIC.CONTEXT (SYSID, NAME, "KEY", VALUE, "TYPE", CREATEDBY, CREATEDON, MODIFIEDBY, MODIFIEDON) VALUES('02178772-5166-4e78-91ca-d0c8a7733a4f', '', '', '', 0, '', '', '', '');
    // INSERT INTO PUBLIC.CONTEXT (SYSID, NAME, "KEY", VALUE, "TYPE", CREATEDBY, CREATEDON, MODIFIEDBY, MODIFIEDON) VALUES('daf248e5-70d1-481c-836f-e5160e06964a', '', '', '', 0, '', '', '', '');
    // INSERT INTO PUBLIC.CONTEXT (SYSID, NAME, "KEY", VALUE, "TYPE", CREATEDBY, CREATEDON, MODIFIEDBY, MODIFIEDON) VALUES('91d7cca4-9216-4b47-8fbe-92032f1b5064', '', '', '', 0, '', '', '', '');

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

    // Replace the context with a new one
    context = new DatabaseContext();
    context.setConfiguration( new Config( config ) );
    engine.setContext( context );

    turnOver( engine );
    
    obj = context.get( Symbols.RUN_COUNT );
    assertTrue( obj instanceof Long );
    long lastRunCount = (Long)obj;
    //assertEquals( nextRunCount + 1, lastRunCount );
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




  @Ignore
  public void contextWithoutLibraryAttribute() {
    String jobName = "ContextTest";

    DataFrame config = new DataFrame().set( "class", "DatabaseContext" ).set( "Target", "jdbc:h2:[#$jobdir#]/test;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" ).set( "autocreate", true ).set( "driver", "org.h2.Driver" ).set( "username", "sa" ).set( "password", "" ).set( "fields", new DataFrame().set( "SomeKey", "SomeValue" ).set( "AnotherKey", "AnotherValue" ) );

    System.out.println( JSONMarshaler.toFormattedString( config ) );

    TransformEngine engine = new DefaultTransformEngine();
    engine.setName( jobName );
    TransformContext context = new DatabaseContext();
    context.setConfiguration( new Config( config ) );
    engine.setContext( context );

    turnOver( engine );

    Object obj = context.get( Symbols.RUN_COUNT );
    assertTrue( obj instanceof Long );
    long runcount = (Long)obj;
    assertTrue( runcount > 0 );
  }




  @Ignore
  public void emptyContext() {

  }




  @Ignore
  public void existingContext() {

  }




  @Ignore
  public void differentTypes() {

  }




  @Ignore
  public void runCount() {

  }




  @Ignore
  public void lastRun() {

  }

}
