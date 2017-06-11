/*
 * 
 */
package coyote.dataframe;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


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
    assertNotNull( frame );
    assertTrue( frame.getTypeCount() == 18 );
    assertTrue( frame.getFieldCount() == 0 );
  }




  /**
   * Test method for {@link coyote.dataframe.DataFrame#add(java.lang.Object)}.
   */
  @Test
  public void testAddObject() {
    DataFrame frame = new DataFrame();
    assertNotNull( frame );
    assertTrue( frame.getFieldCount() == 0 );

    DataFrame child = new DataFrame();
    frame.add( child );
    assertTrue( frame.getFieldCount() == 1 );
  }




  /**
   * Test method for {@link coyote.dataframe.DataFrame#add(java.lang.String, java.lang.Object)}.
   */
  @Test
  public void testAddStringObject() {
    DataFrame frame = new DataFrame();
    assertNotNull( frame );
    assertTrue( frame.getFieldCount() == 0 );

    DataFrame child = new DataFrame();
    frame.add( "KID", child );
    assertTrue( frame.getFieldCount() == 1 );
  }




  /**
   * Test method for {@link coyote.dataframe.DataFrame#toString()}.
   */
  @Test
  public void testToString() {
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", 1L );
    frame1.add( "beta", 2L );

    DataFrame frame2 = new DataFrame();
    frame2.add( "gamma", 3L );
    frame2.add( "delta", 4L );

    DataFrame frame3 = new DataFrame();
    frame3.add( "epsilon", 5L );
    frame3.add( "zeta", 6L );

    frame2.add( "frame3", frame3 );
    frame1.add( "frame2", frame2 );

    String text = frame1.toString();
    //System.out.println(text);

    assertTrue( text.contains( "alpha" ) );
    assertTrue( text.contains( "beta" ) );
    assertTrue( text.contains( "gamma" ) );
    assertTrue( text.contains( "delta" ) );
    assertTrue( text.contains( "epsilon" ) );
    assertTrue( text.contains( "zeta" ) );
    assertTrue( text.contains( "frame3" ) );
    assertTrue( text.contains( "frame2" ) );
  }




  @Test
  public void testToBoolean() {
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", 1L );
    frame1.add( "beta", 0L );
    frame1.add( "gamma", -1L );

    try {
      assertTrue( frame1.getAsBoolean( "alpha" ) );
      assertFalse( frame1.getAsBoolean( "beta" ) );
      assertFalse( frame1.getAsBoolean( "gamma" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }

    frame1 = new DataFrame();
    frame1.add( "alpha", true );
    frame1.add( "beta", "true" );
    frame1.add( "gamma", "1" );

    try {
      assertTrue( frame1.getAsBoolean( "alpha" ) );
      assertTrue( frame1.getAsBoolean( "beta" ) );
      assertTrue( frame1.getAsBoolean( "gamma" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
    frame1 = new DataFrame();
    frame1.add( "alpha", true );
    frame1.add( "beta", "true" );
    frame1.add( "gamma", "1" );

    try {
      assertTrue( frame1.getAsBoolean( "alpha" ) );
      assertTrue( frame1.getAsBoolean( "beta" ) );
      assertTrue( frame1.getAsBoolean( "gamma" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
    frame1 = new DataFrame();
    frame1.add( "alpha", false );
    frame1.add( "beta", "false" );
    frame1.add( "gamma", "0" );

    try {
      assertFalse( frame1.getAsBoolean( "alpha" ) );
      assertFalse( frame1.getAsBoolean( "beta" ) );
      assertFalse( frame1.getAsBoolean( "gamma" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testToDouble() {
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", 0L );
    frame1.add( "beta", "0" );
    frame1.add( "gamma", "0.0" );

    try {
      assertNotNull( frame1.getAsDouble( "alpha" ) );
      assertNotNull( frame1.getAsDouble( "beta" ) );
      assertNotNull( frame1.getAsDouble( "gamma" ) );

    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testToFloat() {
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", 0L );
    frame1.add( "beta", "0" );
    frame1.add( "gamma", "0.0" );

    try {
      assertNotNull( frame1.getAsFloat( "alpha" ) );
      assertNotNull( frame1.getAsFloat( "beta" ) );
      assertNotNull( frame1.getAsFloat( "gamma" ) );

    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testToInt() {
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", 0L );
    frame1.add( "beta", "0" );
    frame1.add( "gamma", Integer.MAX_VALUE );

    try {
      assertNotNull( frame1.getAsInt( "alpha" ) );
      assertNotNull( frame1.getAsInt( "beta" ) );
      assertNotNull( frame1.getAsInt( "gamma" ) );

    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void testToLong() {
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", Short.MAX_VALUE );
    frame1.add( "beta", "0" );
    frame1.add( "gamma", Long.MAX_VALUE );

    try {
      assertNotNull( frame1.getAsLong( "alpha" ) );
      assertNotNull( frame1.getAsLong( "beta" ) );
      assertNotNull( frame1.getAsLong( "gamma" ) );

    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
  }




  @Test
  public void constructorChain() {
    DataFrame frame = new DataFrame()
        .set( "alpha", Short.MAX_VALUE )
        .set( "beta", "0" )
        .set( "gamma", Long.MAX_VALUE );

    try {
      assertTrue( frame.getFieldCount() == 3 );
      assertNotNull( frame.getAsString( "alpha" ) );
      assertNotNull( frame.getAsString( "beta" ) );
      assertNotNull( frame.getAsString( "gamma" ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }
    
    frame = new DataFrame()
        .set( Short.MAX_VALUE )
        .set( "0" )
        .set( Long.MAX_VALUE );

    try {
      assertTrue( frame.getFieldCount() == 3 );
      assertNotNull( frame.getAsString( 0 ) );
      assertNotNull( frame.getAsString( 1 ) );
      assertNotNull( frame.getAsString( 2 ) );
    } catch ( Exception e ) {
      fail( e.getMessage() );
    }

  }

}
