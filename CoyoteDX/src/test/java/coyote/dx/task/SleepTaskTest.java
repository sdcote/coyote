/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

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
 * Any time based test is flakey...expecting elapsed times to be exact will 
 * lead to false failures in builds.
 * 
 * Close enough is be good enough for unit testing. Let PA/QA testing verify
 * timings using actual deployment configurations. CI build environments are 
 * not designed to be production-quality and there may be many other processes
 * running at the same time causing variations in timings.
 */
public class SleepTaskTest {
  private static final TransformContext context = new TransformContext();
  private static final long TIMEOUT = 2000;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void milliseconds() {
    Config cfg = new Config();
    cfg.put(ConfigTag.MILLIS, TIMEOUT);
    System.out.println(cfg);

    try (Sleep task = new Sleep()) {
      task.setConfiguration(cfg);
      task.open(context);
      long start = System.currentTimeMillis();
      task.execute();
      long elapsed = System.currentTimeMillis() - start;
      assertTrue("Milliseconds elapsed was" + elapsed + " expected >= " + TIMEOUT, elapsed >= TIMEOUT * 0.66); // close enough
    } catch (ConfigurationException | TaskException | IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }




  @Test
  public void seconds() {
    Config cfg = new Config();
    cfg.put(ConfigTag.SECONDS, TIMEOUT / 1000);
    System.out.println(cfg);

    try (Sleep task = new Sleep()) {
      task.setConfiguration(cfg);
      task.open(context);
      long start = System.currentTimeMillis();
      task.execute();
      long elapsed = System.currentTimeMillis() - start;
      assertTrue("Milliseconds elapsed was" + elapsed + " expected >= " + TIMEOUT, elapsed >= TIMEOUT * 0.66); // close enough
    } catch (ConfigurationException | TaskException | IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
