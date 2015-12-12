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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.batch.AbstractTest;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurationException;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.commons.FileUtil;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class CopyTaskTest extends AbstractTest {

  private static File testDir = null;
  private static final TransformContext context = new TransformContext();




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testDir = new File( FileUtil.getCurrentWorkingDirectory(), "testdir" );
    FileUtil.makeDirectory( testDir );
    System.out.println( testDir.getAbsolutePath() );

  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    // clean up the work directory
    //FileUtil.deleteDirectory( workDir );
  }




  @Test
  public void test() {
    DataFrame cfg = new DataFrame();
    cfg.put( ConfigTag.FROMDIR, new File( FileUtil.getCurrentWorkingDirectory(), "src" ).getAbsolutePath() );
    cfg.put( ConfigTag.TODIR, testDir.getAbsolutePath() );
    cfg.put( ConfigTag.PATTERN, "([^\\s]+(\\.(?i)(java))$)" );
    cfg.put( ConfigTag.HALT_ON_ERROR, false );

    try (Copy task = new Copy()) {

      // configure the task
      task.setConfiguration( cfg );

      // initialize it within an operational context
      task.open( context );

      // execute the task within the context it was opened
      task.execute();
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    // now check for the existance of java file in the testdir
  }

}
