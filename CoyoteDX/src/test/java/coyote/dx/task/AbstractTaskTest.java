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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class AbstractTaskTest extends AbstractTest {

  private static final TransformContext context = new TransformContext();




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * Test collecting all the files of a type and make sure they are in their 
   * respective directories. 
   */
  @Test
  public void contitionalTask() {

    Config cfg = new Config();
    cfg.put( ConfigTag.HALT_ON_ERROR, false );
    cfg.put( ConfigTag.ENABLED, false );
    cfg.put( ConfigTag.CONDITION, "" ); // should generate an error log event
    //System.out.println( cfg );

    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();

      assertFalse( task.executed() );
      assertFalse( task.isEnabled() );
      assertFalse( task.haltOnError() );
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    cfg.put( ConfigTag.CONDITION, "contextError" );
    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertFalse( task.executed() );
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    cfg.put( ConfigTag.CONDITION, "! contextError" );
    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertFalse( task.executed() );// still not enabled
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    cfg.put( ConfigTag.ENABLED, true );
    cfg.put( ConfigTag.CONDITION, "! contextError" );
    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertTrue( task.executed() );
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    cfg.put( ConfigTag.CONDITION, "contextError" );
    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertFalse( task.executed() ); // enabled, but no error
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    context.setError( true );
    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertTrue( task.executed() ); // enabled, and context is in error
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

    cfg.put( ConfigTag.CONDITION, "! contextError" );
    try (MockTask task = new MockTask()) {
      task.setConfiguration( cfg );
      task.open( context );
      task.execute();
      assertFalse( task.executed() ); // enabled, but context is in error
    } catch ( ConfigurationException | TaskException | IOException e ) {
      fail( e.getMessage() );
    }

  }

}
