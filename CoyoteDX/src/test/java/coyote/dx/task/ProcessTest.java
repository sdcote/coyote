/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

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
public class ProcessTest {

  /**
   * @see coyote.dx.task.AbstractTaskTest#setUpBeforeClass()
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender());
  }




  @Test
  public void execute() throws IOException, ConfigurationException, TaskException {
    final TransformContext context = new TransformContext();
    final String cmd = "arp -a";
    final Config cfg = new Config();
    cfg.put(Process.COMMAND, cmd);
    Log.info("\"Process\":" + cfg);

    try (Process task = new Process()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());

      // The context of jobs are stored under the name of the job or the name 
      // of the command being called if name is not specified
      final Object exitCode = context.get("arp." + Process.EXIT_CODE);
      assertNotNull(exitCode);
      assertTrue(exitCode instanceof Integer);
      assertTrue(0 == ((Integer)exitCode).intValue());

      final Object duration = context.get("arp." + Process.DURATION);
      assertNotNull(duration);
      assertTrue(duration instanceof Long);

      final Object start = context.get("arp." + Process.START);
      assertNotNull(start);
      assertTrue(start instanceof Long);
      assertTrue((Long)start > 0);

      final Object command = context.get("arp." + Process.COMMAND);
      assertNotNull(command);
      assertTrue(cmd instanceof String);
      assertEquals(cmd, (String)command);

      final Object output = context.get("arp." + Process.OUTPUT);
      assertNotNull(output);
      assertTrue(output instanceof String[]);
      //      String[] array = (String[])output;
      //      for (int x = 0; x < array.length; x++) {
      //        System.out.println(array[x]);
      //      }

      final Object error = context.get("arp." + Process.ERROR);
      assertNotNull(error);
      assertTrue(error instanceof String[]);
      //      array = (String[])error;
      //      for (int x = 0; x < array.length; x++) {
      //        System.out.println(array[x]);
      //      }
    }
  }




  @Test
  public void executeNamed() throws IOException, ConfigurationException, TaskException {
    final TransformContext context = new TransformContext();

    final Config cfg = new Config();
    cfg.put(Process.COMMAND, "arp -a");
    cfg.put(ConfigTag.NAME, "ProcessTest"); // override the name of the process
    Log.info("\"Process\":" + cfg);

    try (Process task = new Process()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());

      final Object exitCode = context.get("ProcessTest." + Process.EXIT_CODE);
      assertNotNull(exitCode);

      final Object duration = context.get("ProcessTest." + Process.DURATION);
      assertNotNull(duration);

      final Object output = context.get("ProcessTest." + Process.OUTPUT);
      assertNotNull(output);

      final Object error = context.get("ProcessTest." + Process.ERROR);
      assertNotNull(error);
    }
  }




  @Test
  public void invalidCommand() throws IOException, ConfigurationException, TaskException {
    final TransformContext context = new TransformContext();
    Log.startLogging(Log.DEBUG);

    final Config cfg = new Config();
    cfg.put(Process.COMMAND, "foo -bar -baz");
    cfg.put(ConfigTag.HALT_ON_ERROR, false);
    Log.info("\"Process\":" + cfg);

    try (Process task = new Process()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());

      final Object exitCode = context.get("foo." + Process.EXIT_CODE);
      assertNotNull(exitCode);
      assertTrue(exitCode instanceof Integer);
      assertTrue(-1 == ((Integer)exitCode).intValue());

      final Object duration = context.get("foo." + Process.DURATION);
      assertNotNull(duration);

      final Object output = context.get("foo." + Process.OUTPUT);
      assertNotNull(output);

      final Object error = context.get("foo." + Process.ERROR);
      assertNotNull(error);
    }
    Log.stopLogging(Log.DEBUG);
  }




  @Test
  public void badParameter() throws IOException, ConfigurationException, TaskException {
    final TransformContext context = new TransformContext();

    final Config cfg = new Config();
    cfg.put(Process.COMMAND, "arp -bar -baz");
    cfg.put(ConfigTag.HALT_ON_ERROR, false);
    Log.info("\"Process\":" + cfg);

    try (Process task = new Process()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());

      final Object exitCode = context.get("arp." + Process.EXIT_CODE);
      assertNotNull(exitCode);
      assertTrue(exitCode instanceof Integer);

      final Object duration = context.get("arp." + Process.DURATION);
      assertNotNull(duration);

      final Object output = context.get("arp." + Process.OUTPUT);
      assertNotNull(output);

      final Object error = context.get("arp." + Process.ERROR);
      assertNotNull(error);
    }
  }




  @Test(expected = TaskException.class)
  public void haltOnError() throws IOException, ConfigurationException, TaskException {
    final TransformContext context = new TransformContext();

    final Config cfg = new Config();
    cfg.put(Process.COMMAND, "foo -baz -bar");
    cfg.put(ConfigTag.HALT_ON_ERROR, true);
    Log.info("\"Process\":" + cfg);

    try (Process task = new Process()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
    }
    fail("Should have thrown an exception");
  }




  @Test
  public void noCommand() {
    final TransformContext context = new TransformContext();
    final Config cfg = new Config();
    cfg.put(ConfigTag.HALT_ON_ERROR, true);
    try (Process task = new Process()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
    } catch (Exception e) {
      fail("Should not have thrown an exception for no command");
    }
  }

}
