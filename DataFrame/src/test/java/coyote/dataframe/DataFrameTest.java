/*
 * 
 */
package coyote.dataframe;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import coyote.commons.ByteUtil;


/**
 * 
 */
public class DataFrameTest {

  /**
   * Test method for {@link coyote.dataframe.DataFrame#DataFrame()}.
   */
  @Test
  public void testDataFrame() {
    DataFrame frame = new DataFrame();
    assertNotNull(frame);
    assertTrue(frame.getTypeCount() == 18);
    assertTrue(frame.getFieldCount() == 0);
  }




  /**
   * Test method for {@link coyote.dataframe.DataFrame#add(java.lang.Object)}.
   */
  @Test
  public void testAddObject() {
    DataFrame frame = new DataFrame();
    assertNotNull(frame);
    assertTrue(frame.getFieldCount() == 0);

    DataFrame child = new DataFrame();
    frame.add(child);
    assertTrue(frame.getFieldCount() == 1);
  }




  /**
   * Test method for {@link coyote.dataframe.DataFrame#add(java.lang.String, java.lang.Object)}.
   */
  @Test
  public void testAddStringObject() {
    DataFrame frame = new DataFrame();
    assertNotNull(frame);
    assertTrue(frame.getFieldCount() == 0);

    DataFrame child = new DataFrame();
    frame.add("KID", child);
    assertTrue(frame.getFieldCount() == 1);
  }




  /**
   * Test method for {@link coyote.dataframe.DataFrame#toString()}.
   */
  @Test
  public void testToString() {
    DataFrame frame1 = new DataFrame();
    frame1.add("alpha", 1L);
    frame1.add("beta", 2L);

    DataFrame frame2 = new DataFrame();
    frame2.add("gamma", 3L);
    frame2.add("delta", 4L);

    DataFrame frame3 = new DataFrame();
    frame3.add("epsilon", 5L);
    frame3.add("zeta", 6L);

    frame2.add("frame3", frame3);
    frame1.add("frame2", frame2);

    String text = frame1.toString();
    //System.out.println(text);

    assertTrue(text.contains("alpha"));
    assertTrue(text.contains("beta"));
    assertTrue(text.contains("gamma"));
    assertTrue(text.contains("delta"));
    assertTrue(text.contains("epsilon"));
    assertTrue(text.contains("zeta"));
    assertTrue(text.contains("frame3"));
    assertTrue(text.contains("frame2"));
    assertFalse(text.contains("FooBar"));

  }




  @Test
  public void testToBoolean() {
    DataFrame frame1 = new DataFrame();
    frame1.add("alpha", 1L);
    frame1.add("beta", 0L);
    frame1.add("gamma", -1L);

    try {
      assertTrue(frame1.getAsBoolean("alpha"));
      assertFalse(frame1.getAsBoolean("beta"));
      assertFalse(frame1.getAsBoolean("gamma"));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    frame1 = new DataFrame();
    frame1.add("alpha", true);
    frame1.add("beta", "true");
    frame1.add("gamma", "1");

    try {
      assertTrue(frame1.getAsBoolean("alpha"));
      assertTrue(frame1.getAsBoolean("beta"));
      assertTrue(frame1.getAsBoolean("gamma"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    frame1 = new DataFrame();
    frame1.add("alpha", true);
    frame1.add("beta", "true");
    frame1.add("gamma", "1");

    try {
      assertTrue(frame1.getAsBoolean("alpha"));
      assertTrue(frame1.getAsBoolean("beta"));
      assertTrue(frame1.getAsBoolean("gamma"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    frame1 = new DataFrame();
    frame1.add("alpha", false);
    frame1.add("beta", "false");
    frame1.add("gamma", "0");

    try {
      assertFalse(frame1.getAsBoolean("alpha"));
      assertFalse(frame1.getAsBoolean("beta"));
      assertFalse(frame1.getAsBoolean("gamma"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }




  @Test
  public void testToDouble() throws DataFrameException {
    DataFrame frame1 = new DataFrame();
    frame1.add("alpha", 0L);
    frame1.add("beta", "0");
    frame1.add("gamma", "0.0");
    frame1.add("delta", "123");
    frame1.add("epsilon", System.currentTimeMillis());

    assertNotNull(frame1.getAsDouble("alpha"));
    assertNotNull(frame1.getAsDouble("beta"));
    assertNotNull(frame1.getAsDouble("gamma"));
    assertNotNull(frame1.getAsDouble("delta"));
    assertNotNull(frame1.getAsDouble("epsilon"));
    assertNotNull(frame1.getAsDouble(0));

    try {
      frame1.getAsInt("foo");
      fail("getAsDouble should throw exception fon not found");
    } catch (DataFrameException e) {
      // expected
    }

  }




  @Test
  public void testToFloat() throws DataFrameException {
    DataFrame frame1 = new DataFrame();
    frame1.add("alpha", 0L);
    frame1.add("beta", "0");
    frame1.add("gamma", "0.0");
    frame1.add("delta", "123");
    frame1.add("epsilon", System.currentTimeMillis());

    assertNotNull(frame1.getAsFloat("alpha"));
    assertNotNull(frame1.getAsFloat("beta"));
    assertNotNull(frame1.getAsFloat("gamma"));
    assertNotNull(frame1.getAsFloat("delta"));
    assertNotNull(frame1.getAsFloat("epsilon"));
    assertNotNull(frame1.getAsFloat(0));
    try {
      frame1.getAsInt("foo");
      fail("getAsDouble should throw exception fon not found");
    } catch (DataFrameException e) {
      // expected
    }
  }




  @Test
  public void testToInt() throws DataFrameException {
    DataFrame frame1 = new DataFrame();
    frame1.add("alpha", 0L);
    frame1.add("beta", "0");
    frame1.add("gamma", Integer.MAX_VALUE);
    frame1.add("delta", "123");
    frame1.add("epsilon", System.currentTimeMillis());

    assertNotNull(frame1.getAsInt("alpha"));
    assertNotNull(frame1.getAsInt("beta"));
    assertNotNull(frame1.getAsInt("gamma"));
    assertNotNull(frame1.getAsInt("delta"));
    assertNotNull(frame1.getAsInt(0));

    try {
      frame1.getAsInt("foo");
      fail("getInt should throw exception fon not found");
    } catch (DataFrameException e) {
      // expected
    }

    try {
      frame1.getAsInt("epsilon");
      fail("getInt should throw exception for long");
    } catch (DataFrameException e) {
      // expected
    }

  }




  @Test
  public void testToLong() throws DataFrameException {
    DataFrame frame1 = new DataFrame();
    frame1.add("alpha", Short.MAX_VALUE);
    frame1.add("beta", "0");
    frame1.add("gamma", Long.MAX_VALUE);
    frame1.add("delta", "123");
    frame1.add("epsilon", System.currentTimeMillis());

    assertNotNull(frame1.getAsLong("alpha"));
    assertNotNull(frame1.getAsLong("beta"));
    assertNotNull(frame1.getAsLong("gamma"));
    assertNotNull(frame1.getAsLong("delta"));
    assertNotNull(frame1.getAsLong(0));
    try {
      frame1.getAsInt("foo");
      fail("getAsLong should throw exception fon not found");
    } catch (DataFrameException e) {
      // expected
    }
  }




  @Test
  public void constructorChain() {
    DataFrame frame = new DataFrame().set("alpha", Short.MAX_VALUE).set("beta", "0").set("gamma", Long.MAX_VALUE);

    try {
      assertTrue(frame.getFieldCount() == 3);
      assertNotNull(frame.getAsString("alpha"));
      assertNotNull(frame.getAsString("beta"));
      assertNotNull(frame.getAsString("gamma"));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    frame = new DataFrame().set(Short.MAX_VALUE).set("0").set(Long.MAX_VALUE);

    try {
      assertTrue(frame.getFieldCount() == 3);
      assertNotNull(frame.getAsString(0));
      assertNotNull(frame.getAsString(1));
      assertNotNull(frame.getAsString(2));
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }




  @Test
  public void fieldContstructor() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
  }




  @Test
  public void byteError() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    byte[] data = frame.getBytes();
    //System.out.println(ByteUtil.dump(data));

    DataFrame newframe = new DataFrame(data);
    assertTrue(newframe.getFieldCount() == 1);
    assertFalse(newframe.isModified());

    byte[] badata = Arrays.copyOf(data, 5);

    try {
      new DataFrame(badata);
      fail("Should generate a decode exception");
    } catch (Exception e) {
      // catch decode exception
    }

    badata = Arrays.copyOf(data, 10);
    badata[5] = 120;
    //System.out.println(ByteUtil.dump(badata));

    try {
      new DataFrame(badata);
      fail("Should generate a decode exception");
    } catch (Exception e) {
      // catch decode exception
    }
  }




  @Test
  public void get() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    assertNotNull(frame.get("Test"));
    assertNull(frame.get("TeSt"));
    assertNull(frame.get(0)); // key based, not index
  }




  @Test
  public void getField() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    assertNotNull(frame.getField("Test"));
    assertNull(frame.getField("TeSt"));
    assertNotNull(frame.getField(0));
    assertNull(frame.getField(1));
    assertNull(frame.getField(-1));
  }




  @Test
  public void getFieldIgnoreCase() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    assertNotNull(frame.getFieldIgnoreCase("Test"));
    assertNotNull(frame.getFieldIgnoreCase("TeSt"));
    assertNull(frame.getFieldIgnoreCase("Foo"));
  }




  @Test
  public void containsIgnoreCase() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    assertTrue(frame.containsIgnoreCase("Test"));
    assertTrue(frame.containsIgnoreCase("TeSt"));
    assertFalse(frame.containsIgnoreCase("Foo"));
  }




  @Test
  public void getAsBoolean() throws DataFrameException {
    DataFrame frame = new DataFrame();
    frame.add("true1", true);
    frame.add("true2", "true");
    frame.add("true3", "yes");
    frame.add("true4", 1);
    frame.add("true5", 1L);
    frame.add("true6", 1D);
    frame.add("true7", 1F);
    frame.add("true8", (short)1);
    frame.add("false1", false);
    frame.add("false2", "false");
    frame.add("false3", "no");
    frame.add("false4", 0);
    assertTrue(frame.getFieldCount() == 12);
    assertTrue(frame.getAsBoolean(0));
    assertTrue(frame.getAsBoolean("true1"));
    assertTrue(frame.getAsBoolean("true2"));
    assertTrue(frame.getAsBoolean("true3"));
    assertTrue(frame.getAsBoolean("true4"));
    assertTrue(frame.getAsBoolean("true5"));
    assertTrue(frame.getAsBoolean("true6"));
    assertTrue(frame.getAsBoolean("true7"));
    assertTrue(frame.getAsBoolean("true8"));
    assertFalse(frame.getAsBoolean(8));
    assertFalse(frame.getAsBoolean("false1"));
    assertFalse(frame.getAsBoolean("false2"));
    assertFalse(frame.getAsBoolean("false3"));
    assertFalse(frame.getAsBoolean("false4"));

    try {
      frame.getAsBoolean("foo");
      fail("Should generate a DataFrame exception");
    } catch (DataFrameException e) {
      // expected
    }
  }




  @Test
  public void getAsDate() throws DataFrameException {
    Date date = new Date();
    long time = date.getTime();

    DataFrame frame = new DataFrame();
    frame.add("date1", date);
    frame.add("date2", time);
    Date newdate = frame.getAsDate(0);
    assertNotNull(newdate);
    newdate = frame.getAsDate("date1");
    assertNotNull(newdate);
    newdate = frame.getAsDate("date2");
    assertNotNull(newdate);

    try {
      frame.getAsDate("foo");
      fail("Should generate a DataFrame exception");
    } catch (DataFrameException e) {
      // expected
    }
  }




  @Test
  public void getAsString() throws DataFrameException {
    Date date = new Date();

    DataFrame frame = new DataFrame();
    frame.add("1", true);
    frame.add("2", "true");
    frame.add("3", "yes");
    frame.add("4", 1);
    frame.add("5", 1L);
    frame.add("6", 1D);
    frame.add("7", 1F);
    frame.add("8", (short)1);
    frame.add("9", date);

    assertTrue(frame.getFieldCount() == 9);
    assertNotNull(frame.getAsString(0));
    assertNotNull(frame.getAsString("1"));
    assertNotNull(frame.getAsString("2"));
    assertNotNull(frame.getAsString("3"));
    assertNotNull(frame.getAsString("4"));
    assertNotNull(frame.getAsString("5"));
    assertNotNull(frame.getAsString("6"));
    assertNotNull(frame.getAsString("7"));
    assertNotNull(frame.getAsString("8"));
    assertNotNull(frame.getAsString("9"));
    assertNull(frame.getAsString("foo"));

    try {
      frame.getAsString(10);
      fail("Should generate a DataFrame exception");
    } catch (DataFrameException e) {
      // expected
    }
  }




  @Test
  public void getAsFrame() throws DataFrameException {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", new DataFrame().set("gamma", 3L).set("delta", 4L));
    assertTrue(frame.getFieldCount() == 2);

    DataFrame newFrame = frame.getAsFrame("beta");
    assertNotNull(newFrame);
    newFrame = frame.getAsFrame(1);
    assertNotNull(newFrame);

    assertNull(frame.getAsFrame("foo"));
    assertNull(frame.getAsFrame(2));

    try {
      frame.getAsFrame("alpha"); // not a frame
      fail("Should generate a DataFrame exception");
    } catch (DataFrameException e) {
      // expected
    }
    try {
      frame.getAsFrame(0); // not a frame
      fail("Should generate a DataFrame exception");
    } catch (DataFrameException e) {
      // expected
    }
  }




  @Test
  public void getAsObject() throws DataFrameException {
    Date date = new Date();

    DataFrame frame = new DataFrame();
    frame.add("1", true);
    frame.add("2", "true");
    frame.add("3", "yes");
    frame.add("4", 1);
    frame.add("5", 1L);
    frame.add("6", 1D);
    frame.add("7", 1F);
    frame.add("8", (short)1);
    frame.add("9", date);

    assertTrue(frame.getFieldCount() == 9);
    assertNotNull(frame.getObject(0));
    assertNotNull(frame.getObject("1"));
    assertNotNull(frame.getObject("2"));
    assertNotNull(frame.getObject("3"));
    assertNotNull(frame.getObject("4"));
    assertNotNull(frame.getObject("5"));
    assertNotNull(frame.getObject("6"));
    assertNotNull(frame.getObject("7"));
    assertNotNull(frame.getObject("8"));
    assertNotNull(frame.getObject("9"));
    assertNull(frame.getObject("foo"));
    assertNull(frame.getObject(10));
  }




  @Test
  public void valueConstructor() {
    DataFrame frame = new DataFrame("1", true);
    assertTrue(frame.getFieldCount() == 1);
    assertNotNull(frame.getObject(0));
    assertNotNull(frame.getObject("1"));
  }




  @Test
  public void add() {
    DataFrame frame = new DataFrame();
    assertFalse(frame.isModified());
    frame.add(new DataField("one", 123L));
    assertTrue(frame.isModified());
    assertTrue(frame.getFieldCount() == 1);
    assertNotNull(frame.getObject(0));
    assertNotNull(frame.getObject("one"));
  }




  @Test
  public void put() {
    DataFrame frame = new DataFrame();
    assertFalse(frame.isModified());

    int position = frame.put("one", 1);
    assertTrue(frame.isModified());
    frame.add("two", "second");

    assertTrue(position == 0);
    assertTrue(frame.getFieldCount() == 2);
    assertNotNull(frame.getObject(0));
    Object obj = frame.getObject("one");
    assertNotNull(obj);
    assertTrue(obj instanceof Integer);

    // replace it
    position = frame.put("one", 123L);
    assertTrue(position == 0); // replaced first position
    assertTrue(frame.getFieldCount() == 2);
    assertNotNull(frame.getObject(0));
    obj = frame.getObject("one");
    assertNotNull(obj);
    assertTrue(obj instanceof Long);

    // remove it
    position = frame.put("one", null);
    assertTrue(position == 0); // removed first position 
    assertTrue(frame.getFieldCount() == 1);
    assertNotNull(frame.getObject(0)); // now contains "two"
    assertNull(frame.getObject("one"));
  }




  @Test
  public void remove() {
    Date date = new Date();

    DataFrame frame = new DataFrame();
    assertFalse(frame.isModified());
    frame.add("1", true);
    frame.add("2", "true");
    frame.add("3", "yes");
    frame.add("4", 1);
    frame.add("5", 1L);
    frame.add("6", 1D);
    frame.add("7", 1F);
    frame.add("8", (short)1);
    frame.add("9", date);
    frame.add("9", date.getTime()); // duplicate "9"
    assertTrue(frame.isModified());

    assertTrue(frame.getFieldCount() == 10);
    assertNotNull(frame.remove("1"));
    assertTrue(frame.getFieldCount() == 9);
    assertFalse(frame.contains("1"));
    assertNull(frame.remove("1")); // cannot remove it again
    assertTrue(frame.getFieldCount() == 9);

    assertNotNull(frame.remove("9"));
    assertTrue(frame.getFieldCount() == 8);
    assertTrue(frame.contains("9")); // duplicate is still there
    Object obj = frame.get("9");
    assertTrue(obj instanceof Long); // should have removed the Date object 

  }




  @Test
  public void replace() {
    Date date = new Date();

    DataFrame frame = new DataFrame();
    assertFalse(frame.isModified());
    frame.add("1", 1);
    frame.add("2", date);
    assertTrue(frame.getFieldCount() == 2);

    frame.replace("2", date.getTime()); // replace "2"
    assertTrue(frame.getFieldCount() == 2);
    Object obj = frame.get("2");
    assertTrue(obj instanceof Long); // should have replaced the Date object 
  }




  @Test
  public void replaceAll() {
    DataFrame frame = new DataFrame();
    assertFalse(frame.isModified());
    frame.add("Boolean", true);
    frame.add("Boolean", "true");
    frame.add("Boolean", "yes");
    frame.add("Number", 1);
    frame.add("Number", 1L);
    frame.add("Number", 1D);
    frame.add("Number", 1F);
    frame.add("Number", (short)1);
    assertTrue(frame.getFieldCount() == 8);
    frame.replaceAll("Boolean", false);
    assertTrue(frame.getFieldCount() == 6);
    frame.replaceAll("Number", 123);
    assertTrue(frame.getFieldCount() == 2);
  }




  @Test
  public void removeAll() {
    DataFrame frame = new DataFrame();
    assertFalse(frame.isModified());
    frame.add("Boolean", true);
    frame.add("Boolean", "true");
    frame.add("Boolean", "yes");
    frame.add("Number", 1);
    frame.add("Number", 1L);
    frame.add("Number", 1D);
    frame.add("Number", 1F);
    frame.add("Number", (short)1);
    assertTrue(frame.getFieldCount() == 8);
    frame.removeAll("Boolean");
    assertTrue(frame.getFieldCount() == 5);
    frame.removeAll("Number");
    assertTrue(frame.getFieldCount() == 0);
  }




  @Test
  public void digest() {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", 0L);
    frame.add("gamma", -1L);
    assertTrue(frame.getFieldCount() == 3);

    byte[] digest = frame.getDigest();
    String frameDigest = ByteUtil.bytesToHex(digest);
    //System.out.println(frameDigest);
    //assertEquals("F462712CA62F76218E4C57DA44C8C3A4D4CBC6CD",frameDigest);
    String digestString = frame.getDigestString();
    assertEquals(frameDigest, digestString);
    //System.out.println(digestString);
    frame.put("alpha", 2L);
    assertTrue(frame.getFieldCount() == 3);

    digest = frame.getDigest();
    String frameDigest2 = ByteUtil.bytesToHex(digest, "");
    //System.out.println(frameDigest2);
    assertNotEquals(frameDigest, frameDigest2);
    String digestString2 = frame.getDigestString();
    assertNotEquals(digestString, digestString2);
  }




  @Test
  public void getBytes() {
    DataField field = new DataField("Test", 123L);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    byte[] bytes1 = frame.getBytesOrig();

    DataFrame.setCheckFlag(true);
    byte[] bytes2 = frame.getBytes();

    DataFrame.setCheckFlag(false);
    byte[] bytes3 = frame.getBytes();

    assertEquals(bytes1.length, bytes2.length);
    assertEquals(bytes1.length, bytes3.length);
    assertTrue(Arrays.equals(bytes1, bytes2));
    assertTrue(Arrays.equals(bytes1, bytes3));
  }




  @Test
  public void getBytesForField() {
    byte[] raw = {'1', '2', '3'};
    DataField field = new DataField("Test", raw);
    DataFrame frame = new DataFrame(field);
    assertTrue(frame.getFieldCount() == 1);
    byte[] data = frame.getBytes("Test");
    //System.out.println(ByteUtil.dump(data));
    assertTrue(data[0] == '1');
    assertTrue(data[1] == '2');
    assertTrue(data[2] == '3');
  }




  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void listFields() {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", 0L);
    frame.add("gamma", -1L);
    assertTrue(frame.getFieldCount() == 3);

    List<DataField> fields = frame.getFields();
    assertTrue(fields.size() == 3);

    DataFrame frame2 = new DataFrame();
    assertTrue(frame2.getFieldCount() == 0);
    ArrayList<DataField> newlist = new ArrayList();
    for (DataField field : fields) {
      newlist.add(field);
    }
    frame2.setFields(newlist);
    assertTrue(frame2.getFieldCount() == 3);
    assertNotNull(frame2.get("alpha"));
  }




  @Test
  public void mapMethods() {
    DataFrame frame = new DataFrame();
    frame.put((Object)"alpha", new Long("123"));
    frame.put((Object)"beta", new Integer("123"));
    assertTrue(frame.getFieldCount() == 2);
    try {
      frame.put((Object)new Boolean(true), new Integer("123"));
      fail("Should generate a DataFrame exception");
    } catch (Exception e) {
      // expected
    }

    for (Object key : frame.keySet()) {
      assertNotNull(key);
      assertTrue(key instanceof String);
    }

    for (Object value : frame.values()) {
      assertNotNull(value);
    }

    frame.put((Object)"beta", new Double("123")); //replace
    assertTrue(frame.getFieldCount() == 2);

    frame.put((Object)"alpha", null); // remove by assigning null to key
    assertTrue(frame.getFieldCount() == 1);

    frame.remove((Object)"beta");
    assertTrue(frame.getFieldCount() == 0);

  }




  @Test
  public void populate() {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", 0L);
    DataFrame frame2 = new DataFrame();
    frame2.populate(frame);
    assertTrue(frame2.getFieldCount() == 2);

  }




  @Test
  public void merge() {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", 0L);
    frame.add("gamma", -1L);

    DataFrame frame2 = new DataFrame();
    frame2.add("alpha", 1L);
    frame2.add("beta", 0L);

    frame2.merge(frame);
    assertTrue(frame2.getFieldCount() == 3);
  }




  @Test
  public void cloneTest() {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", 0L);

    DataFrame frame2 = (DataFrame)frame.clone();
    assertTrue(frame2.getFieldCount() == 2);
  }




  @Test
  public void clear() {
    DataFrame frame = new DataFrame();
    frame.add("alpha", 1L);
    frame.add("beta", 0L);
    assertFalse(frame.isEmpty());
    assertTrue(frame.getFieldCount() == 2);
    frame.clear();
    assertTrue(frame.getFieldCount() == 0);
    assertTrue(frame.isEmpty());
  }
}
