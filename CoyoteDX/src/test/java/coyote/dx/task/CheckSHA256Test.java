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

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.FileUtil;
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
public class CheckSHA256Test {
  private static final TransformContext context = new TransformContext();




  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void simpleCheck() throws ConfigurationException, TaskException, IOException {

    String testFile = new File(FileUtil.getCurrentWorkingDirectory(), "src/test/resources/coyote.jpg").getAbsolutePath();
    String expected = "744ceb14ce533326ec88eb28554f36f9d21cd04e362c7658b4ec5fae56f7fce1";
    context.set("Checksum", expected);

    Config cfg = new Config();
    cfg.put(ConfigTag.FILE, testFile);
    cfg.put(ConfigTag.CONTEXT, "Checksum");
    System.out.println(cfg);

    String checksumFile = null;
    try (CheckSHA256 task = new CheckSHA256()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());
      checksumFile = testFile + task.getFileExtension();
    }

    File file = new File(checksumFile);
    try {
      assertNotNull(context.get(checksumFile));
      String retrievedChecksum = context.get(checksumFile).toString();
      assertEquals(expected, retrievedChecksum);
    }
    finally {
      file.delete();
    }

  }

}
