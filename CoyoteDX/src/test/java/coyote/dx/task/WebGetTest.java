/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.FileUtil;
import coyote.commons.template.SymbolTable;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.Symbols;
import coyote.dx.TaskException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class WebGetTest extends AbstractTest {

  private static final TransformContext context = new TransformContext();
  protected static final SymbolTable symbols = new SymbolTable();

  private static File testDir = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    context.setSymbols(symbols);
    testDir = new File(FileUtil.getCurrentWorkingDirectory(), "testdir");
    context.getSymbols().put(Symbols.JOB_DIRECTORY, testDir.getAbsolutePath());
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    deleteWorkDirectory(testDir);
  }




  @Test
  public void primaryUseCase() throws ConfigurationException, IOException, TaskException {
    resetDirectory(testDir);

    Config cfg = new Config();
    cfg.put(ConfigTag.SOURCE, "http://mirrors.ibiblio.org/apache//commons/pool/binaries/commons-pool2-2.4.2-bin.tar.gz");

    try (WebGet task = new WebGet()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());
    }

    cfg.put(ConfigTag.SOURCE, "https://www.apache.org/dist/commons/pool/binaries/commons-pool2-2.4.2-bin.tar.gz.md5");
    try (WebGet task = new WebGet()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());
    }

    String testFile = new File(testDir, "commons-pool2-2.4.2-bin.tar.gz").getAbsolutePath();

    cfg = new Config();
    cfg.put(ConfigTag.FILE, testFile);

    try (CheckMD5 task = new CheckMD5()) {
      task.setConfiguration(cfg);
      task.open(context);
      task.execute();
      assertFalse(context.getErrorMessage(), context.isInError());
    }

  }
}
