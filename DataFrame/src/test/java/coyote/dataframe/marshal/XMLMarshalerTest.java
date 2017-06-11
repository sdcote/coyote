/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class XMLMarshalerTest {

  static final String XML0 = "<date/>";
  static final String XML1 = "<text></text>";
  static final String XML2 = "<text>hello</text>";
  static final String XML3 = "<text>hello</text>\r\n<data>world</data>";
  static final String XML4 = "<text>hello</text>\r\n<data>world</data>\r\n<extra>This is fun!</extra>";
  static final String XML5 = "<parent>\r\n<child>inner</child>\r\n</parent>";
  static final String XML6 = "<peer>text</peer>\r\n<peer>\r\n<inner>Child</inner>\r\n</peer>";
  static final String XML7 = "<?xml version=\"1.0\"?>"; // preamble=true, comment=false, empty=true, open=true, close=false
  static final String XMLA = "<?xml version=\"1.0\"?>\r\n" + "<doc>\r\n   <assembly>\r\n       <name>Linkage</name>\r\n   </assembly>\r\n   <members>\r\n       <member name=\"T:Linkage.Logging.IFormatter\">\r\n           <summary> Class IFormatter</summary>\r\n       </member>\r\n       <member name=\"M:Linkage.Logging.IFormatter.Initialize\">\r\n           <summary></summary>\r\n       </member>\r\n       <!-- Comments can occur anywhere -->\r\n       <member name=\"M:Linkage.Logging.IFormatter.Format(System.Object,System.String)\">\r\n           <summary> Format the given object into a string based upon the given category.</summary>\r\n           <param name=\"obj\">The object to format into a string.</param>\r\n           <param name=\"category\">The category of the event to be used in optional condition\r\n           formatting.</param>\r\n            <returns> String representation of the event as it will be written to the log</returns>\r\n       </member>\r\n   </members>\r\n" + "</doc>\r\n";
  static final String XMLY = "<SAML-Signature><![CDATA[]]></SAML-Signature>";
  static final String XMLZ = "<SAML-Signature><![CDATA[ <![CDATA[]]> ]]></SAML-Signature>"; // pure evil!




  /**
   * Test method for {@link coyote.dataframe.marshal.XMLMarshaler#marshal(java.lang.String)}.
   */
  @Test
  public void testMarshalString() {
    List<DataFrame> frames = null;
    DataFrame frame = null;
    DataField field = null;

    // XMLMarshaler.marshal( "<date?" );  // bad
    // XMLMarshaler.marshal( "</close>" );  // bad

    frames = XMLMarshaler.marshal( XML0 ); // simple empty tag
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 1 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "date", field.getName() );

    frames = XMLMarshaler.marshal( XML1 ); // single empty field
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 1 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "text", field.getName() );

    frames = XMLMarshaler.marshal( XML2 ); // single field
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 1 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "text", field.getName() );

    frames = XMLMarshaler.marshal( XML3 ); // multiple fields
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 2 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "text", field.getName() );

    frames = XMLMarshaler.marshal( XML4 ); // multiple fields
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 3 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "text", field.getName() );

    frames = XMLMarshaler.marshal( XML5 ); // nested fields
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 1 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "parent", field.getName() );

    frames = XMLMarshaler.marshal( XML6 ); // nested fields
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );
    assertTrue( frame.size() == 2 );
    field = frame.getField( 0 );
    assertNotNull( field );
    assertEquals( "peer", field.getName() );
    field = frame.getField( 1 );
    assertNotNull( field );
    assertEquals( "peer", field.getName() );

    frames = XMLMarshaler.marshal( XML7 ); // preamble only
    assertNotNull( frames );
    assertTrue( frames.size() == 0 );

    frames = XMLMarshaler.marshal( XMLA ); // nested fields
    assertNotNull( frames );
    assertTrue( frames.size() == 1 );
    frame = frames.get( 0 );
    assertNotNull( frame );

    // Can't cope with <![CDATA[ ]]> yet
    // frames = XMLMarshaler.marshal( XMLY ); // nested fields
    // assertNotNull( frames );
    // assertTrue( frames.size() == 1 );
    // frame = frames.get( 0 );
    // assertNotNull( frame );

    // System.out.println( JSONMarshaler.toFormattedString( frame ) );
  }




  /**
   * Test method for {@link coyote.dataframe.marshal.XMLMarshaler#marshal(coyote.dataframe.DataFrame)}.
   */
  //@Test
  public void testMarshalDataFrame() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.dataframe.marshal.XMLMarshaler#toFormattedString(coyote.dataframe.DataFrame)}.
   */
  //@Test
  public void testToFormattedString() {
    fail( "Not yet implemented" );
  }

}
