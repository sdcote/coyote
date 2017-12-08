/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.csv.CSVReader;
import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.ConfigTag;
import coyote.dx.CsvTestData;
import coyote.dx.TransformEngine;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class CsvReaderTest extends AbstractTest {

  private static final char SEPARATOR = ',';




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    //Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }




  @Test
  public void test() throws IOException, ParseException {
    DataFrame config = new DataFrame().set(ConfigTag.READER,
        new DataFrame() //
            .set(ConfigTag.CLASS, "CsvReader") //
            .set(ConfigTag.TARGET, "dummy") // will set target & reader later
            .set(ConfigTag.HEADER, true) //
    );

    TransformEngine engine = createEngine(config);
    TransformContext engineContext = engine.contextInit();
    assertNotNull(engineContext);
    CsvReader reader = (CsvReader)engine.getReader();
    reader.open(engineContext);

    // Set a special reader with our test data
    CSVReader csvReader = new CSVReader(new StringReader(CsvTestData.simpleNumericData()), SEPARATOR);
    reader.setReader(csvReader);
    assertFalse(reader.eof());
    TransactionContext context = new TransactionContext(engineContext);
    DataFrame frame = reader.read(context);
    assertNotNull(frame);
    assertFalse(reader.eof());
    assertFalse(context.isLastFrame());

    frame = reader.read(context);
    assertNotNull(frame);
    assertTrue(reader.eof());
    assertTrue(context.isLastFrame());

  }

}
