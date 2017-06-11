package coyote.dataframe.marshal;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


public class PropertyFrameTest {

  @Test
  public void testMarshalProperties()
  {
    PropertyFrame marshaler = new PropertyFrame();
    DataFrame frame = marshaler.marshal( System.getProperties(), true );
    assertNotNull( frame );

    Properties props = new Properties();
    props.setProperty( "java.specification.version", "1.7" );
    props.setProperty( "user.name", "alice" );
    props.setProperty( "dir", "/tmp" );

    frame = marshaler.marshal( props, true );
    assertNotNull( frame );
    assertTrue( 3 == frame.getFieldCount() );
    Object value = frame.getField( "user" );
    assertNotNull( value );
    assertTrue( value instanceof DataField );
    DataField field = (DataField)value;
    assertTrue( field.isFrame() );
    DataFrame uframe = (DataFrame)field.getObjectValue();
    assertTrue( 1 == uframe.getFieldCount() );
    value = uframe.getField( "name" );
    assertNotNull( value );
    field = (DataField)value;
    String val = (String)field.getObjectValue();
    assertTrue( "alice".equals( val ) );
  }




  @Test
  public void testMarshalFrame()
  {
    DataFrame frame = new DataFrame();

    DataFrame vframe = new DataFrame();
    vframe.add( "version", "1.7" );
    DataFrame sframe = new DataFrame();
    sframe.add( "specification", vframe );
    frame.add( "java", sframe );

    DataFrame nframe = new DataFrame();
    nframe.add( "name", "alice" );
    frame.add( "user", nframe );

    PropertyFrame marshaler = new PropertyFrame();
    Properties props = marshaler.marshal( frame );
    assertNotNull( props );
    assertTrue( props.size() == 2 );
    assertNotNull( props.getProperty( "java.specification.version" ) );
    assertTrue( "1.7".equals( props.getProperty( "java.specification.version" ) ) );
    assertNotNull( props.getProperty( "user.name" ) );
    assertTrue( "alice".equals( props.getProperty( "user.name" ) ) );
  }
}
