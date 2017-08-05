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
package coyote.dx.task;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.FileUtil;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
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









  /**
   * Test collecting all the files of a type and make sure they are in their 
   * respective directories. 
   */
  @Test
  public void testRecursePreserve() {
    resetDirectory(testDir);

    Config cfg = new Config();
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
      File dx = new File( coyote, "dx" );
      assertTrue( dx.exists() );
      assertTrue( dx.isDirectory() );

      File job = new File( dx, "Job.java" );
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
    resetDirectory(testDir);

    Config cfg = new Config();
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
