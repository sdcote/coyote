/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.CommandLineProcess.Result;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class CommandLineProcessTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
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




  /**
   * Find a (positive) integer in the given string between two tags.
   *
   * @param string the string to search
   * @param startTag the start tag
   * @param endTag the end tag
   *
   * @return the integer value found in the string or -1 if not found
   */
  public static int findInteger(String string, String startTag, String endTag) {
    long l = findLong(string, startTag, endTag);

    if (l <= Integer.MAX_VALUE) {
      return (int)l;
    } else {
      return -1;
    }
  }




  /**
   * Find a (positive) long number in the given string between two tags.
   *
   * @param string the string to search
   * @param startTag the start tag
   * @param endTag the end tag
   *
   * @return the long value found in the string or -1 if not found
   */
  public static long findLong(String string, String startTag, String endTag) {
    int indx = string.indexOf(startTag);
    long retval = -1L;

    if (indx < 0) {
      return retval;
    }

    for (indx += startTag.length(); indx < string.length(); indx++) {
      if (Character.isDigit(string.charAt(indx))) {
        break;
      }
    }

    if (indx == string.length()) {
      return -1L;
    }

    for (; indx < string.length(); indx++) {
      char ch = string.charAt(indx);

      if (!Character.isDigit(ch)) {
        break;
      }

      if (retval > 0L) {
        retval *= 10L;
      } else {
        retval = 0L;
      }

      retval += Character.digit(ch, 10);
    }

    if ((endTag != null) && (indx < string.length())) {
      int j = string.indexOf(endTag, indx);

      if (j < 0) {
        retval = -1L;
      }
    }

    return retval;
  }
}
