/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe.marshal;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.junit.Test;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;


/**
 * 
 */
public class JSONMarshalTest {

  @Test
  public void testObject() throws Exception {
    String json = "{}";
    System.out.println( json );
    List<DataFrame> results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    DataFrame frame = results.get( 0 );
    System.out.println( frame );
    assertTrue( frame.size() == 0 );
    DataField result = frame.getField( 0 ); // get the JSON data object
    System.out.println( "----------------------------\r\n" );

    json = "{\"one\" : 1}";
    System.out.println( json );
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    assertTrue( frame.size() == 1 );
    result = frame.getField( 0 );
    System.out.println( "----------------------------" );

    json = "{\"one\" : 1,\"two\" : 2,\"three\" : 3}";
    System.out.println( json );
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    assertTrue( frame.size() == 3 );
    result = frame.getField( 0 );
    System.out.println( "----------------------------\r\n" );

    json = "{}{}{}";
    System.out.println( json );
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 3 );
    frame = results.get( 0 );
    System.out.println( frame );
    System.out.println( "----------------------------\r\n" );
  }




  @Test
  public void testArray() throws Exception {
    String json = "[]";
    List<DataFrame> results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    DataFrame frame = results.get( 0 );
    System.out.println( frame );

    json = "[5,3]";
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    //assertEquals( "[5]", obj.toString() );

    json = "[5,10,2]";
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    //assertEquals( "[5,10,2]", obj.toString() );

    json = "[\"hello\\bworld\\\"abc\\tdef\\\\ghi\\rjkl\\n123\\u4e2d\"]";
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    //   assertEquals( "hello\bworld\"abc\tdef\\ghi\rjkl\n123中", ( (List)obj ).get( 0 ).toString() );

    json = "[5,]"; // non-standard
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    //assertEquals( "[5,null]", obj.toString() );

    json = "[5,,2]"; // non-standard
    results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    frame = results.get( 0 );
    System.out.println( frame );
    //assertEquals( "[5,null,2]", obj.toString() );

  }




  @Test
  public void arrayOfFrames() {
    String expected = "{\"Team\":[{\"First\":\"Alice\",\"Last\":\"Smith\"},{\"First\":\"Bob\",\"Last\":\"Wilson\"},{\"First\":\"Carol\",\"Last\":\"Jones\"}]}";
    DataFrame frame1 = new DataFrame().set( "First", "Alice" ).set( "Last", "Smith" );
    DataFrame frame2 = new DataFrame().set( "First", "Bob" ).set( "Last", "Wilson" );
    DataFrame frame3 = new DataFrame().set( "First", "Carol" ).set( "Last", "Jones" );
    DataFrame[] frames = { frame1, frame2, frame3 };
    DataFrame root = new DataFrame().set( "Team", frames );

    String text = JSONMarshaler.marshal( root );
    //System.out.println( text );
    assertEquals( expected, root.toString() );
  }




  @Test
  public void testDataFrameArray() {
    DataFrame frame = new DataFrame().set( "uriList", new String[0] );
    String json = JSONMarshaler.marshal( frame );
    System.out.println( json );
    assertTrue( json.indexOf( ":[]" ) > 0 );
  }




  @Test
  public void testRealObject() throws Exception {
    //String json = "[{\"message_stats\":{\"deliver_get\":2,\"deliver_get_details\":{\"rate\":0.0},\"get_no_ack\":2,\"get_no_ack_details\":{\"rate\":0.0},\"publish\":2,\"publish_details\":{\"rate\":0.0}},\"messages\":0,\"messages_details\":{\"rate\":0.0},\"messages_ready\":0,\"messages_ready_details\":{\"rate\":0.0},\"messages_unacknowledged\":0,\"messages_unacknowledged_details\":{\"rate\":0.0},\"name\":\"/\",\"tracing\":false}]";
    String json = "{ \"skills\" : \"\", \"upon_approval\" : \"proceed\", \"location\" : \"\", \"expected_start\" : \"\", \"reopen_count\" : \"0\", \"close_notes\" : \"\", \"impact\" : \"3\", \"urgency\" : \"3\", \"correlation_id\" : \"\", \"sys_tags\" : \"\", \"sys_domain\" : { \"link\" : \"https://nwdevelopment.service-now.com:443/api/now/table/sys_user_group/global\", \"value\" : \"global\" }, \"description\" : \"\", \"group_list\" : \"\", \"priority\" : \"5\", \"sys_mod_count\" : \"0\", \"work_notes_list\" : \"\", \"follow_up\" : \"\", \"closed_at\" : \"\", \"sla_due\" : \"\", \"sys_updated_on\" : \"2015-04-08 21:12:53\", \"parent\" : \"\", \"work_end\" : \"\", \"number\" : \"INC0010032\", \"closed_by\" : \"\", \"work_start\" : \"\", \"calendar_stc\" : \"\", \"business_duration\" : \"\", \"category\" : \"inquiry\", \"incident_state\" : \"1\", \"activity_due\" : \"\", \"correlation_display\" : \"\", \"company\" : \"\", \"active\" : \"true\", \"due_date\" : \"\", \"assignment_group\" : \"\", \"caller_id\" : \"\", \"knowledge\" : \"false\", \"made_sla\" : \"true\", \"comments_and_work_notes\" : \"\", \"parent_incident\" : \"\", \"state\" : \"1\", \"user_input\" : \"\", \"sys_created_on\" : \"2015-04-08 21:12:53\", \"approval_set\" : \"\", \"reassignment_count\" : \"0\", \"rfc\" : \"\", \"child_incidents\" : \"0\", \"opened_at\" : \"2015-04-08 21:12:53\", \"short_description\" : \"Test with java post\", \"order\" : \"\", \"sys_updated_by\" : \"cotes7\", \"resolved_by\" : \"\", \"notify\" : \"1\", \"upon_reject\" : \"cancel\", \"approval_history\" : \"\", \"problem_id\" : \"\", \"work_notes\" : \"\", \"calendar_duration\" : \"\", \"close_code\" : \"\", \"sys_id\" : \"905040750f9f7100085d6509b1050e7d\", \"approval\" : \"not requested\", \"caused_by\" : \"\", \"severity\" : \"3\", \"sys_created_by\" : \"cotes7\", \"assigned_to\" : \"\", \"resolved_at\" : \"\", \"business_stc\" : \"\", \"cmdb_ci\" : \"\", \"opened_by\" : { \"link\" : \"https://nwdevelopment.service-now.com:443/api/now/table/sys_user/a0b8c4490f093500a5eee478b1050ebe\", \"value\" : \"a0b8c4490f093500a5eee478b1050ebe\" }, \"subcategory\" : \"\", \"sys_class_name\" : \"incident\", \"watch_list\" : \"\", \"time_worked\" : \"\", \"contact_type\" : \"phone\", \"escalation\" : \"0\", \"comments\" : \"\" }";

    System.out.println( json );
    List<DataFrame> results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    DataFrame frame = results.get( 0 );
    System.out.println( frame );

    String formatted = JSONMarshaler.toFormattedString( frame );
    System.out.println( formatted );
    System.out.println( "----------------------------\r\n" );

    //assertTrue(frame.size()==1);
    DataField result = frame.getField( 0 ); // get the JSON data object
  }

  //  public void testx() throws Exception {
  //    String s = "[0,{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}]";
  //    Object obj = JSONValue.parse( s );
  //    JSONArray array = (JSONArray)obj;
  //    System.out.println( "======the 2nd element of array======" );
  //    System.out.println( array.get( 1 ) );
  //    System.out.println();
  //    assertEquals( "{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}", array.get( 1 ).toString() );
  //
  //    DataFrame obj2 = (DataFrame)array.get( 1 );
  //    System.out.println( "======field \"1\"==========" );
  //    System.out.println( obj2.getObject( "1" ) );
  //    assertEquals( "{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}", obj2.getObject( "1" ).toString() );
  //  }
  //  

  /*
   * This 
   * { "emptyString": "" }
   * should not result in this
   * {"emptyString":null }   
   */




  @Test
  public void readEmptyArray() throws Exception {
    String json = "{ \"emptyArray\": [] }";
    String expected = "{\"emptyArray\":[]}";

    List<DataFrame> results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    DataFrame frame = results.get( 0 );

    DataField field = frame.getField( "emptyArray" );
    assertNotNull( field );
    assertTrue( field.isFrame() ); // arrays are stored as a frame of unnamed fields
    assertNull( field.getObjectValue() ); // DataFrame should be null, nothing in it

    //System.out.println( frame );
    assertEquals( expected, frame.toString() );

    String newjson = JSONMarshaler.marshal( frame );
    //System.out.println( newjson );
    assertEquals( expected, newjson );

    String formatted = JSONMarshaler.toFormattedString( frame );
    //System.out.println( formatted );
    assertTrue( formatted.contains( "\"emptyArray\" : []" ) );
    //System.out.println( "----------------------------\r\n" );
  }




  @Test
  public void readArray() throws DataFrameException {
    String json = "{ \"Users\" : [ { \"Name\" : \"admin\", \"Password\" : \"secret\", \"Groups\" : \"sysop,devop\" },{ \"Name\" : \"sysop\", \"Password\" : \"secret\", \"Groups\" : \"sysop\" }, { \"Name\" : \"devop\", \"Password\" : \"secret\", \"Groups\" : \"devop\" }, { \"Name\" : \"user\", \"Password\" : \"secret\" } ] }";
    List<DataFrame> results = JSONMarshaler.marshal( json );
    assertTrue( results.size() == 1 );
    DataFrame frame = results.get( 0 );

    //for(DataField field: frame.getFields()){ System.out.println( field.toString() ); }
    DataField users = frame.getField( "Users" );
    //    assertTrue(users.isArray()); // list of unnamed fields
    //    assertTrue( user);
    //    DataField[] fields = (DataField[])users.getObjectValue();
    //    System.out.println( fields.length );

  }




  @Test
  public void readArrayData() {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File( classLoader.getResource( "nvdcve.json" ).getFile() );
    byte[] bytes = new byte[new Long( file.length() ).intValue()];
    try (DataInputStream dis = new DataInputStream( new FileInputStream( file ) )) {
      dis.readFully( bytes );
    } catch ( final Exception ignore ) {}
    String json = new String( bytes );
    //System.out.println( json );

    List<DataFrame> frames = JSONMarshaler.marshal( json );
    assertTrue( frames.size() == 1 );
    DataFrame frame = frames.get( 0 );
    //System.out.println( frame.toString() );
    System.out.println( JSONMarshaler.toFormattedString( frame ) );
  }

}
