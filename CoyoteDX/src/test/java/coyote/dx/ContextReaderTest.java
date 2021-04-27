/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataFrame;
import coyote.dx.context.TransformContext;
import coyote.dx.reader.ContextReader;
import coyote.dx.writer.ContextWriter;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class ContextReaderTest extends AbstractTest {
  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );
  }




  @Test
  public void singleFrame() {

    // load the configuration from the class path
    TransformEngine engine = loadEngine("ContextReadWrite");
    assertNotNull(engine);

    TransformContext transformContext = new TransformContext();

    engine.setContext(transformContext);
    TransformContext context = engine.getContext();
    assertNotNull(context);

    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull(symbols);

    DataFrame frame = new DataFrame().set("field1", "value1").set("Field2", "Value2").set("NullField", null);

    context.set(ContextReader.DEFAULT_CONTEXT_FIELD, frame);

    turnOver(engine);
    System.out.println(engine.getContext().getRow());
    assertTrue(1 == context.getRow());
  }




  @Test
  public void arrayTest() {
    TransformEngine engine = loadEngine("ContextReadWrite");
    assertNotNull(engine);

    TransformContext transformContext = new TransformContext();

    engine.setContext(transformContext);
    TransformContext context = engine.getContext();
    assertNotNull(context);

    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull(symbols);

    DataFrame[] frames = new DataFrame[2];
    frames[0] = new DataFrame().set("field1", "value1").set("Field2", "Value2").set("NullField", null);
    frames[1] = new DataFrame().set("field3", "value3").set("Field4", "Value4").set("NullField", null);

    context.set(ContextReader.DEFAULT_CONTEXT_FIELD, frames);

    turnOver(engine);
    System.out.println(engine.getContext().getRow());
    assertTrue(2 == context.getRow());
  }




  @Test
  public void listTest() {
    TransformEngine engine = loadEngine("ContextReadWrite");
    assertNotNull(engine);

    TransformContext transformContext = new TransformContext();

    engine.setContext(transformContext);
    TransformContext context = engine.getContext();
    assertNotNull(context);

    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull(symbols);

    List<DataFrame> frames = new ArrayList<DataFrame>();
    frames.add(new DataFrame().set("field1", "value1").set("Field2", "Value2").set("NullField", null));
    frames.add(new DataFrame().set("field3", "value3").set("Field4", "Value4").set("NullField", null));

    context.set(ContextReader.DEFAULT_CONTEXT_FIELD, frames);

    turnOver(engine);
    System.out.println(engine.getContext().getRow());
    assertTrue(2 == context.getRow());
  }




  @Test
  public void readWrite() {
    TransformEngine engine = loadEngine("ContextReadWrite");
    assertNotNull(engine);
    TransformContext transformContext = new TransformContext();
    engine.setContext(transformContext);
    TransformContext context = engine.getContext();
    assertNotNull(context);
    SymbolTable symbols = engine.getSymbolTable();
    assertNotNull(symbols);
    DataFrame[] frames = new DataFrame[3];
    frames[0] = new DataFrame().set("field1", "value1").set("Field2", "Value2").set("NullField", null);
    frames[1] = new DataFrame().set("field3", "value3").set("Field4", "Value4").set("NullField", null);
    frames[2] = new DataFrame().set("field5", "value5").set("Field6", "Value6").set("NullField", null);
    context.set(ContextReader.DEFAULT_CONTEXT_FIELD, frames);
    turnOver(engine);
    assertTrue(3 == context.getRow());

    Object dataobj = context.get(ContextWriter.DEFAULT_CONTEXT_FIELD);
    assertNotNull(dataobj);

    assertTrue(dataobj instanceof DataFrame[]);
    DataFrame[] outframes = (DataFrame[])dataobj;
    assertTrue(outframes.length == 3);
  }
}
