/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * This class contains unit test for the ArrayType
 * 
 * @author Stephan D. Cote'
 */
public class ArrayTypeTest {

  /**
   * Test method for {@link coyote.dataframe.ArrayType#checkType(java.lang.Object)}.
   */
  @Test
  public void testCheckType() {
    String[] array = new String[3];
    ArrayType subject = new ArrayType();
    assertTrue( subject.checkType( array ) );
  }




  /**
   * Test method for {@link coyote.dataframe.ArrayType#decode(byte[])}.
   */
  @Test
  public void testDecode() {
    byte[] data = new byte[12];
    data[0] = 3; // type of string
    data[1] = 0; // first byte of length
    data[2] = 1; // second byte of length
    data[3] = 65; // Latin-1 Capital 'A'
    data[4] = 3;
    data[5] = 0;
    data[6] = 1;
    data[7] = 66;
    data[8] = 3;
    data[9] = 0;
    data[10] = 1;
    data[11] = 67;

    ArrayType subject = new ArrayType();
    Object obj = subject.decode( data );
    assertTrue( obj instanceof DataFrame );
    DataFrame array = (DataFrame)obj;
    assertTrue( array.size() == 3 );
    DataField value1 = array.getField( 0 );
    assertTrue( value1.getType() == DataField.STRING );
  }




  /**
   * Test method for {@link coyote.dataframe.ArrayType#encode(java.lang.Object)}.
   */
  @Test
  public void testEncode() {
    String[] array = new String[3];
    array[0] = "A";
    array[1] = "B";
    array[2] = "C";
    ArrayType subject = new ArrayType();
    byte[] payload = subject.encode( array );
    assertTrue( payload.length == 12 );
  }




  /**
   * Test method for {@link coyote.dataframe.ArrayType#getTypeName()}.
   */
  @Test
  public void testGetTypeName() {
    ArrayType subject = new ArrayType();
    assertTrue( subject.getTypeName().equals( "ARY" ) );
  }




  /**
   * Test method for {@link coyote.dataframe.ArrayType#isNumeric()}.
   */
  @Test
  public void testIsNumeric() {
    ArrayType subject = new ArrayType();
    org.junit.Assert.assertFalse( subject.isNumeric() );
  }




  /**
   * Test method for {@link coyote.dataframe.ArrayType#getSize()}.
   */
  @Test
  public void testGetSize() {
    ArrayType subject = new ArrayType();
    assertTrue( subject.getSize() == -1 );
  }




  @Test
  public void roundTrip() {
    byte[] bytes = new byte[1];
    bytes[0] = (byte)255;
    //System.out.println( ByteUtil.dump( bytes ) );

    Object[] values = new Object[10];
    values[0] = "test";
    values[1] = (short)255; //U8 type5
    values[2] = (short)-32768; //S16 type6
    values[3] = 65535; //U16 type7
    values[4] = -2147483648; //S32 type8
    values[5] = 4294967296L; //U32 type9
    values[6] = -9223372036854775808L; //S64 type10
    values[7] = 9223372036854775807L; //U64 type11
    values[8] = 123456.5F; //type12
    values[9] = 123456.5D; //type13

    ArrayType subject = new ArrayType();
    byte[] payload = subject.encode( values );
    assertTrue( payload.length == 61 );
    //System.out.println( coyote.util.ByteUtil.dump( payload ) );

    Object obj = subject.decode( payload );
    assertTrue( obj instanceof DataFrame );
    DataFrame array = (DataFrame)obj;
    assertTrue( array.size() == 10 );
    DataField element = array.getField( 0 );
    //System.out.println( "Element 0 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[0] );
    assertTrue( element.getType() == DataField.STRING );
    assertTrue( "test".equals( element.getStringValue() ) );
    element = array.getField( 1 );
    //System.out.println( "Element 1 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[1] );
    assertTrue( element.getType() == DataField.U8 );
    assertTrue( 255 == ( (Short)element.getObjectValue() ).shortValue() );
    element = array.getField( 2 );
    //System.out.println( "Element 2 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[2] );
    assertTrue( element.getType() == DataField.S16 );
    assertTrue( -32768 == ( (Short)element.getObjectValue() ).shortValue() );
    element = array.getField( 3 );
    //System.out.println( "Element 3 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[3] );
    assertTrue( element.getType() == DataField.U16 );
    assertTrue( 65535 == ( (Integer)element.getObjectValue() ).intValue() );
    element = array.getField( 4 );
    //System.out.println( "Element 4 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[4] );
    assertTrue( -2147483648 == ( (Integer)element.getObjectValue() ).intValue() );
    element = array.getField( 5 );
    //System.out.println( "Element 5 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[5] );
    assertTrue( element.getType() == DataField.S64 );
    assertTrue( 4294967296L == ( (Long)element.getObjectValue() ).longValue() );
    element = array.getField( 6 );
    //System.out.println( "Element 6 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[6] );
    assertTrue( element.getType() == DataField.S64 );
    assertTrue( -9223372036854775808L == ( (Long)element.getObjectValue() ).longValue() );
    element = array.getField( 7 );
    //System.out.println( "Element 7 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[7] );
    assertTrue( element.getType() == DataField.S64 );
    assertTrue( 9223372036854775807L == ( (Long)element.getObjectValue() ).longValue() );
    element = array.getField( 8 );
    //System.out.println( "Element 8 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[8] );
    assertTrue( element.getType() == DataField.FLOAT );
    assertTrue( 123456.5F == ( (Float)element.getObjectValue() ).floatValue() );
    element = array.getField( 9 );
    //System.out.println( "Element 9 is " + element.getClass() + " value=>" + element.toString() + " Original=>" + values[9] );
    assertTrue( element.getType() == DataField.DOUBLE );
    assertTrue( 123456.5D == ( (Double)element.getObjectValue() ).doubleValue() );
  }




  @Test
  public void testToString() {
    ArrayType subject = new ArrayType();
    Number[] numbers = new Number[] { 0, 1, 2, 3.4 };

    byte[] bytes = subject.encode( numbers );
    String text = subject.stringValue( bytes );
    assertEquals( "[0,1,2,3.4]", text );

    Boolean[] bools = new Boolean[] { true, false, true, true, false };
    bytes = subject.encode( bools );
    text = subject.stringValue( bytes );
    //System.out.println( text );
    assertEquals( "[true,false,true,true,false]", text );

    String[] strings = new String[] { "a", "b", "c", "6" };
    bytes = subject.encode( strings );
    text = subject.stringValue( bytes );
    //System.out.println( text );
    assertEquals( "[\"a\",\"b\",\"c\",\"6\"]", text );
  }




  @Test
  public void arrayOfFrames() {
    String expected = "{\"Team\":[{\"First\":\"Alice\",\"Last\":\"Smith\"},{\"First\":\"Bob\",\"Last\":\"Wilson\"},{\"First\":\"Carol\",\"Last\":\"Jones\"}]}";
    DataFrame frame1 = new DataFrame().set( "First", "Alice" ).set( "Last", "Smith" );
    DataFrame frame2 = new DataFrame().set( "First", "Bob" ).set( "Last", "Wilson" );
    DataFrame frame3 = new DataFrame().set( "First", "Carol" ).set( "Last", "Jones" );
    DataFrame[] frames = { frame1, frame2, frame3 };
    DataFrame root = new DataFrame().set( "Team", frames );
    String text = root.toString();
    System.out.println( text );
    assertEquals(expected,root.toString());
  }

}
