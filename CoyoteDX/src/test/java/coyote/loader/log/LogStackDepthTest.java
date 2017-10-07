/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * 
 */
public class LogStackDepthTest {

  @Test
  public void test() {
    int depth = Log.getStackDepth();
    StringAppender logger = new StringAppender();
    Log.addLogger("StringAppender", logger);
    Log.startLogging(Log.DEBUG);
    Log.debug("This is a test: " + depth);
    String entry = logger.toString();
    logger.clear();
    assertNotNull(entry);
    // System.out.println(entry);
    assertTrue(entry.contains("LogStackDepthTest.test():27"));
    Log.removeLogger("StringAppender");
  }

}
