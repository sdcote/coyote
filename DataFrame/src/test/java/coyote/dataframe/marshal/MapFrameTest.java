/*
 *
 */
package coyote.dataframe.marshal;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import coyote.dataframe.DataFrame;


/**
 * 
 * @author Steve Cote
 */
public class MapFrameTest {

  /**
   * Test method for {@link coyote.dataframe.marshal.MapFrame#marshal(java.util.Map)}.
   */
  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testMarshalMap() {
    HashMap data = new HashMap();
    data.put( "One", "One" );
    data.put( "Two", 2 );

    MapFrame marshaler = new MapFrame();
    DataFrame frame = marshaler.marshal( data );
    assertNotNull( frame );
    assertTrue( frame.getFieldCount() == 2 );
    assertTrue( frame.contains( "One" ) );
  }




  /**
   * Test method for {@link coyote.dataframe.marshal.MapFrame#marshal(coyote.dataframe.DataFrame)}.
   */
  @SuppressWarnings("rawtypes")
  @Test
  public void testMarshalDataFrame() {
    DataFrame frame = new DataFrame();

    DataFrame vframe = new DataFrame();
    vframe.add( "version", "1.7" );
    DataFrame sframe = new DataFrame();
    sframe.add( "specification", vframe );
    frame.add( "java", sframe );

    DataFrame nframe = new DataFrame();
    nframe.add( "name", "alice" );
    frame.add( "user", nframe );

    MapFrame marshaler = new MapFrame();
    Map map = marshaler.marshal( frame );
    assertNotNull( map );
    assertTrue( map.size() == 2 );
    Object obj = map.get( "java" );
    assertNotNull( obj );
    assertTrue( obj instanceof Map );
    Map jmap = (Map)obj;
    assertTrue( jmap.size() == 1 );
    obj = jmap.get( "specification" );
    assertNotNull( obj );
    assertTrue( obj instanceof Map );
    Map smap = (Map)obj;
    assertTrue( smap.size() == 1 );
    obj = smap.get( "version" );
    assertNotNull( obj );
    assertTrue( obj instanceof String );
    String ver = (String)obj;
    assertEquals( "1.7", ver );
  }
}
