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
public class ArchiveTaskTest extends AbstractTest {
  private static final TransformContext context = new TransformContext();
  private static File testDir = null;
  private static final String DIRECTORY_NAME = "testworkdir";




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = new File( FileUtil.getCurrentWorkingDirectory(), DIRECTORY_NAME );
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    deleteWorkDirectory( testDir );
  }




  @Test
  public void archiveDirectory() {
    resetDirectory( testDir );

    // TODO: Setup directory

    Config cfg = new Config();
    cfg.put( ConfigTag.DIRECTORY, testDir.getAbsolutePath() );
    cfg.put( ConfigTag.TARGET, new File( FileUtil.getCurrentWorkingDirectory(), "jobwrkdir.zip" ).getAbsolutePath() );
    System.out.println( cfg );

    try (Archive task = new Archive()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    // TODO: Test for endstate
    // TODO: delete archive
  }




  @Test
  public void archiveFile() {

    Config cfg = new Config();
    cfg.put( ConfigTag.FILE, new File( FileUtil.getCurrentWorkingDirectory(), "README.md" ).getAbsolutePath() );
    cfg.put( ConfigTag.TARGET, new File( FileUtil.getCurrentWorkingDirectory(), "jobwrkdir.zip" ).getAbsolutePath() );
    System.out.println( cfg );

    try (Archive task = new Archive()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    // TODO: Test for endstate
    // TODO: delete archive
  }

}
