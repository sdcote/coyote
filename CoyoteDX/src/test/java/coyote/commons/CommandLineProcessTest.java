/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import coyote.commons.CommandLineProcess.Result;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;


/**
 *
 */
public class CommandLineProcessTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }


  //@Test
  @Ignore("This has been known to block indefinitely on some Windows configurations")
  public void test() {
    Result result = CommandLineProcess.exec("arp -a ");
    String[] array = result.getOutput();
    for (int i = 0; i < array.length; i++) {
      String line = array[i];
      System.out.println(line);
    }
    if (array.length == 0) {
      fail("No output");
    }
  }

}
