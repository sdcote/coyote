/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.dx.context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class TransformContextTest {

  private static TransformContext transformContext = null;
  private static TransactionContext context = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.DEBUG_EVENTS));
    // Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.NOTICE_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );

    transformContext = new TransformContext();
    transformContext.set("one", 1);
    transformContext.set("two", 2);
    transformContext.set("name", "Bob");
    transformContext.set("Nested", new DataFrame().set("bird", "robin").set("egg.baby", "chick"));
    transformContext.set("double.nested", new DataFrame().set("deep.down", "result1").set("deep.below", "result2"));
    Map<String, String> map = new Hashtable<String, String>();
    map.put("legend", "1inch to 1mile");
    map.put("path.secret", "x marks the spot");
    transformContext.set("Map", map);

    context = new TransactionContext(transformContext);
    context.setLastFrame(true);
    context.setSourceFrame(new DataFrame().set("field1", "value1").set("Field2", "Value2").set("BooleanField", true).set("IntegerField", 123));
    context.setWorkingFrame(new DataFrame().set("field3", "value3").set("Field4", "Value4").set("DateField", new Date()).set("DoubleValue", 123.0D));
    context.setTargetFrame(new DataFrame().set("field5", "value5").set("Field6", "Value6").set("LongField", 123L).set("FloatField", 123.0F));
    transformContext.setTransaction(context);
  }




  @Test
  public void get() {
    assertNotNull(transformContext.get("one"));
    assertNotNull(transformContext.get("two"));
    assertNotNull(transformContext.get("name"));
    assertNull(transformContext.get("nested"));
    assertNotNull(transformContext.get("Nested"));
    assertNull(transformContext.get("Target.Field6"));
  }




  @Test
  public void getUseCase() {
    assertNotNull(transformContext.get("one", true));
    assertNotNull(transformContext.get("two", true));
    assertNotNull(transformContext.get("name", true));
    assertNull(transformContext.get("nested", true));
    assertNotNull(transformContext.get("Nested", true));
    assertNull(transformContext.get("Target.Field6", true));
    assertNotNull(transformContext.get("one", false));
    assertNotNull(transformContext.get("two", false));
    assertNotNull(transformContext.get("name", false));
    assertNotNull(transformContext.get("nested", false));
    assertNotNull(transformContext.get("Nested", false));
    assertNull(transformContext.get("Target.Field6", false));
  }




  @Test
  public void resolveFieldValue() {
    assertNull(transformContext.resolveFieldValue("one"));
    assertNotNull(transformContext.resolveFieldValue("Source.Field2"));
    assertNotNull(transformContext.resolveFieldValue("Working.Field4"));
    assertNotNull(transformContext.resolveFieldValue("Target.Field6"));
    assertNull(transformContext.resolveFieldValue("field3")); // don't assume working frame
  }




  @Test
  public void resolveToValue() {
    assertNull(transformContext.resolveFieldValue("one"));
    assertNotNull(transformContext.resolveFieldValue("Source.Field2"));
    assertNotNull(transformContext.resolveFieldValue("Working.Field4"));
    assertNotNull(transformContext.resolveFieldValue("Target.Field6"));
    assertNull(transformContext.resolveToValue("nested")); // case
    assertNotNull(transformContext.resolveToValue("Nested"));
    assertNotNull(transformContext.resolveToValue("Nested.bird"));
    assertNotNull(transformContext.resolveToValue("Map.legend"));
    assertNotNull(transformContext.resolveToValue("Map.path.secret"));

  }

}
