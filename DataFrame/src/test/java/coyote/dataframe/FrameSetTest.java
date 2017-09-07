/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


/**
 * 
 */
public class FrameSetTest {

  /**
   * Test method for {@link coyote.dataframe.FrameSet#add(coyote.dataframe.DataFrame)}.
   */
  @Test
  public void testAdd() {

    FrameSet set = new FrameSet();

    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", "first" );
    frame1.add( "beta", "second" );
    set.add( frame1 );
    DataFrame frame2 = new DataFrame();
    frame2.add( "alpha", "third" );
    frame2.add( "beta", "fourth" );
    set.add( frame2 );

    assertTrue( set.size() == 2 );
  }




  /**
   * Test method for {@link coyote.dataframe.FrameSet#getColumns()}.
   */
  @Test
  public void testGetColumns() {
    FrameSet set = new FrameSet();

    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", "first" );
    frame1.add( "beta", "second" );
    set.add( frame1 );
    DataFrame frame2 = new DataFrame();
    frame2.add( "gamma", "third" );
    frame2.add( "delta", "fourth" );
    set.add( frame2 );
    assertTrue( set.size() == 2 );
    List<String> columnNames = set.getColumns();
    assertNotNull( columnNames );
    assertTrue( columnNames.size() == 4 );
  }




  /**
   * Test method for {@link coyote.dataframe.FrameSet#get(int)}.
   */
  @Test
  public void testGet() {
    FrameSet set = new FrameSet();
    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", "first" );
    set.add( frame1 );
    DataFrame frame2 = new DataFrame();
    frame2.add( "beta", "second" );
    set.add( frame2 );
    assertTrue( set.size() == 2 );

    assertTrue( set.get( 0 ) == frame1 );
    assertTrue( set.get( 1 ) == frame2 );

  }




  /**
   * Test the addAll method
   */
  @Test
  public void testAddAll() {
    List<DataFrame> list = new ArrayList<DataFrame>();

    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", "first" );
    list.add( frame1 );
    DataFrame frame2 = new DataFrame();
    frame2.add( "beta", "second" );
    list.add( frame2 );

    // addAll(Collection<DataFrame>) is called by the constructor
    FrameSet set = new FrameSet( list );
    assertTrue( set.size() == 2 );
    assertTrue( set.get( 0 ) == frame1 );
    assertTrue( set.get( 1 ) == frame2 );

    // try addAll on its own
    set = new FrameSet();
    set.addAll( list );
    assertTrue( set.size() == 2 );
    assertTrue( set.get( 0 ) == frame1 );
    assertTrue( set.get( 1 ) == frame2 );
  }




  @Test
  public void testGetColumn() {
    FrameSet set = new FrameSet();

    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", "first" );
    frame1.add( "beta", "second" );
    set.add( frame1 );
    DataFrame frame2 = new DataFrame();
    frame2.add( "alpha", "third" );
    frame2.add( "beta", "fourth" );
    set.add( frame2 );
    DataFrame frame3 = new DataFrame();
    frame3.add( "alpha", "fifth" );
    frame3.add( "beta", "sixth" );
    set.add( frame3 );
    assertTrue( set.size() == 3 );
    List<String> columnStrings = set.getColumn( "alpha" );
    assertNotNull( columnStrings );
    assertTrue( columnStrings.size() == 3 );
    assertNotNull( columnStrings.get( 0 ) );
    assertTrue( "first".equals( columnStrings.get( 0 ) ) );
    assertNotNull( columnStrings.get( 1 ) );
    assertTrue( "third".equals( columnStrings.get( 1 ) ) );
    assertNotNull( columnStrings.get( 2 ) );
    assertTrue( "fifth".equals( columnStrings.get( 2 ) ) );
  }




  @Test
  public void testGetColumnValues() {
    FrameSet set = new FrameSet();

    DataFrame frame1 = new DataFrame();
    frame1.add( "alpha", 1L );
    frame1.add( "beta", 2L );
    set.add( frame1 );
    DataFrame frame2 = new DataFrame();
    frame2.add( "alpha", 3L );
    frame2.add( "beta", 4L );
    set.add( frame2 );
    DataFrame frame3 = new DataFrame();
    frame3.add( "alpha", 5L );
    frame3.add( "beta", 6L );
    set.add( frame3 );
    assertTrue( set.size() == 3 );
    List<Object> columnObjects = set.getColumnValue( "alpha" );
    assertNotNull( columnObjects );
    assertTrue( columnObjects.size() == 3 );

    Object obj = columnObjects.get( 0 );
    assertNotNull( obj );
    assertTrue( obj instanceof Long );
    assertTrue( 1 == ( (Long)obj ).longValue() );

    obj = columnObjects.get( 1 );
    assertNotNull( obj );
    assertTrue( obj instanceof Long );
    assertTrue( 3 == ( (Long)obj ).longValue() );

    obj = columnObjects.get( 2 );
    assertNotNull( obj );
    assertTrue( obj instanceof Long );
    assertTrue( 5 == ( (Long)obj ).longValue() );

  }




  /**
   * Test the use of generics in the API to allow anything which subclasses 
   * DataFrame to be used.
   */
  @Test
  public void testAddAllThing() {
    List<MyThing> list = new ArrayList<MyThing>();

    MyThing thing1 = new MyThing();
    thing1.add( "alpha", "first" );
    list.add( thing1 );
    MyThing thing2 = new MyThing();
    thing2.add( "beta", "second" );
    list.add( thing2 );

    // addAll(Collection<DataFrame>) is called by the constructor
    FrameSet set = new FrameSet( list );
    assertTrue( set.size() == 2 );
    assertTrue( set.get( 0 ) == thing1 );
    assertTrue( set.get( 1 ) == thing2 );

    // try addAll on its own
    set = new FrameSet();
    set.addAll( list );
    assertTrue( set.size() == 2 );
    assertTrue( set.get( 0 ) == thing1 );
    assertTrue( set.get( 1 ) == thing2 );
  }

  public class MyThing extends DataFrame {

  }
}
