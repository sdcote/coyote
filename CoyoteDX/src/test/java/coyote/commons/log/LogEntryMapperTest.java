/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.log;

import coyote.commons.Assert;
import coyote.commons.eval.DoubleEvaluator;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 */
public class LogEntryMapperTest {


  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    //Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }


  @Test
  public void parseFormat() {
    String logFormat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{UNIQUE_ID}e\"";
    LogEntryMapper mapper = new LogEntryMapper("apache", logFormat);
    LogFormat format = mapper.getLogFormat();
    Assert.notNull(format);
    Assert.isTrue(format.getMode().getName().equalsIgnoreCase("apache"));
    Assert.isTrue(format.size() == 10);

    LogFieldFormat fieldFormat = format.get(0);
    Assert.notNull(fieldFormat);
    Assert.isTrue("RemoteHostname".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(1);
    Assert.notNull(fieldFormat);
    Assert.isTrue("RemoteLogName".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(2);
    Assert.notNull(fieldFormat);
    Assert.isTrue("RemoteUser".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(3);
    Assert.notNull(fieldFormat);
    Assert.isTrue("Time".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(4);
    Assert.notNull(fieldFormat);
    Assert.isTrue("RequestLine".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(5);
    Assert.notNull(fieldFormat);
    Assert.isTrue("Status".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(6);
    Assert.notNull(fieldFormat);
    Assert.isTrue("ResponseSizeCLF".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(7);
    Assert.notNull(fieldFormat);
    Assert.isTrue("VAR-Referer".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(8);
    Assert.notNull(fieldFormat);
    Assert.isTrue("VAR-User-Agent".equalsIgnoreCase(fieldFormat.getName()));

    fieldFormat = format.get(9);
    Assert.notNull(fieldFormat);
    Assert.isTrue("ENV-UNIQUE_ID".equalsIgnoreCase(fieldFormat.getName()));

  }


  @Test
  public void mapToFrame() {
    // Create a Log Entry Mapper - The format string controls how log entries are named and formatted
    String logFormat = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{UNIQUE_ID}e\"";
    LogEntryMapper mapper = new LogEntryMapper("apache", logFormat);

    // A sample log entry
    String logLine = "10.96.95.245 - - [21/Jun/2017:13:17:33 -0400] \"GET /post/simple-branching-strategy/featured_hu5cda23b3f3a1dbf428630ed056832eac_3976455_550x0_resize_q90_lanczos_2.png HTTP/1.1\" 200 459970 \"https://coyote.example.com/\" \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36\" \"YNDJrXDe3HntbrxrDHrTZQAAAAI\"";

    // Map the log entry line to a Data Frame
    DataFrame frame = mapper.mapToFrame(logLine);

    DataField field = frame.getField("Status");
    Assert.notNull(field);
    Assert.isTrue(field.isNumeric());
    field = frame.getField("ResponseSizeCLF");
    Assert.notNull(field);
    Assert.isTrue(field.isNumeric());
    field = frame.getField("Time");
    Assert.notNull(field);
    Assert.isTrue(field.getType() == DataField.DATE);

  }

}
