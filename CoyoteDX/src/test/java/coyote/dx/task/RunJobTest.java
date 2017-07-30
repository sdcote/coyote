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
package coyote.dx.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.context.TransformContext;
import coyote.dx.writer.ContextWriter;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class RunJobTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
    // Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  @Ignore
  public void execute() throws ConfigurationException, TaskException, IOException {
    final TransformContext context = new TransformContext();

    Config cfg = new Config();
    cfg.put( ConfigTag.FILE, "src/resources/demo/BitcoinPrice" ); // Won't work in CI builds!
    cfg.put( ConfigTag.NAME, "RunJobTest" ); // override the name of the job
    Log.info( "\"RunJob\":" + cfg );

    try (RunJob task = new RunJob()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertFalse( context.getErrorMessage(), context.isInError() );

      // The context of jobs are stored under the name of the job
      Object results = context.get( "RunJobTest" );
      assertNotNull( results );
      assertTrue( results instanceof Map ); // contexts are maps

      // The context writer writes all its frames to an array stored in its 
      // context with a default key of ContextOutput 
      Object object = ( (Map)results ).get( ContextWriter.DEFAULT_CONTEXT_FIELD );
      assertNotNull( object );
      DataFrame[] frames = (DataFrame[])object; // should be an array of frames
      assertTrue( frames.length > 0 );

      DataFrame frame = frames[0]; // get the first one
      assertNotNull( frame );
      System.out.println( frame );

      Map jobContext = context.getAsMap( "RunJobTest" );
      assertNotNull( jobContext );
      System.out.println("CONTEXT: "+ jobContext );
      Map jobDisposition = (Map)jobContext.get( "TransformDisposition" );
      assertNotNull( jobDisposition );
      System.out.println("DISPOSITION: "+  jobDisposition );
    }

  }

}
