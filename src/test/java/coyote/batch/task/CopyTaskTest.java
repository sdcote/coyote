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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.batch.AbstractTest;
import coyote.batch.ConfigTag;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.commons.FileUtil;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class CopyTaskTest extends AbstractTest {

  private static final TransformContext context = new TransformContext();

  private static File testDir = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = new File( FileUtil.getCurrentWorkingDirectory(), "testdir" );
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    // clean up the work directory
    deleteWorkDirectory( testDir );
  }




  private static void deleteWorkDirectory( File dir ) {
    FileUtil.deleteDirectory( dir );
  }




  private static void makeWorkDirectory( File dir ) throws Exception {
    if ( dir != null && dir.isDirectory() && !dir.exists() ) {
      FileUtil.makeDirectory( dir );
    }
  }




  private static void resetTestDirectory() {
    try {
      deleteWorkDirectory( testDir );
      if ( !testDir.exists() ) {
        makeWorkDirectory( testDir );
      }
    } catch ( Exception ignore ) {}
  }




  /**
   * Test collecting all the files of a type and make sure they are in their 
   * respective directories. 
   */
  @Test
  public void testRecursePreserve() {
    resetTestDirectory();

    DataFrame cfg = new DataFrame();
    cfg.put( ConfigTag.FROMDIR, new File( FileUtil.getCurrentWorkingDirectory(), "src" ).getAbsolutePath() );
    cfg.put( ConfigTag.TODIR, testDir.getAbsolutePath() );
    cfg.put( ConfigTag.PATTERN, ".*\\.(java)$" );
    cfg.put( ConfigTag.HALT_ON_ERROR, false );
    cfg.put( ConfigTag.RECURSE, true );
    cfg.put( ConfigTag.PRESERVE, true );
    System.out.println( cfg );

    try (Copy task = new Copy()) {

      // configure the task
      task.setConfiguration( cfg );

      // initialize it within an operational context
      task.open( context );

      // execute the task within the context it was opened
      task.execute();

      assertTrue( testDir.exists() );
      assertTrue( testDir.isDirectory() );
      File main = new File( testDir, "main" );
      assertTrue( main.exists() );
      assertTrue( main.isDirectory() );
      File java = new File( main, "java" );
      assertTrue( java.exists() );
      assertTrue( java.isDirectory() );
      File coyote = new File( java, "coyote" );
      assertTrue( coyote.exists() );
      assertTrue( coyote.isDirectory() );
      File batch = new File( coyote, "batch" );
      assertTrue( batch.exists() );
      assertTrue( batch.isDirectory() );

      File job = new File( batch, "Job.java" );
      assertTrue( job.exists() );
      assertTrue( job.isFile() );

    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

  }




  /**
   * Test collecting all the files of a type into one directory and make sure 
   * files with the same name are not over-written, but renamed as the default. 
   */
  @Test
  public void testDirectoryOverwrite() {
    resetTestDirectory();

    DataFrame cfg = new DataFrame();
    cfg.put( ConfigTag.FROMDIR, new File( FileUtil.getCurrentWorkingDirectory(), "src" ).getAbsolutePath() );
    cfg.put( ConfigTag.TODIR, testDir.getAbsolutePath() );
    cfg.put( ConfigTag.PATTERN, ".*\\.(java)$" );
    cfg.put( ConfigTag.HALT_ON_ERROR, false );
    cfg.put( ConfigTag.RECURSE, true );
    System.out.println( cfg );

    try (Copy task = new Copy()) {

      // configure the task
      task.setConfiguration( cfg );

      // initialize it within an operational context
      task.open( context );

      // execute the task within the context it was opened
      task.execute();

      assertTrue( testDir.exists() );
      assertTrue( testDir.isDirectory() );

      File info = new File( testDir, "package-info.java" );
      assertTrue( info.exists() );
      assertTrue( info.isFile() );

      File info1 = new File( testDir, "package-info.1.java" );
      assertTrue( info1.exists() );

    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

  }

}
