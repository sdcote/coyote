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
package coyote.i13n;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Test;


/**
 * 
 */
public class EventListTest {

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  /**
   * Test method for {@link coyote.i13n.EventList#lastSequence()}.
   */
  //@Test
  public void testLastSequence() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#EventList()}.
   */
  //@Test
  public void testEventList() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#getMaxEvents()}.
   */
  @Test
  public void testGetMaxEvents() {
    EventList list = new EventList();
    list.setMaxEvents( 5 );

    AppEvent alert0 = list.createEvent( "Zero" );
    AppEvent alert1 = list.createEvent( "One" );
    AppEvent alert2 = list.createEvent( "Two" );
    AppEvent alert3 = list.createEvent( "Three" );
    AppEvent alert4 = list.createEvent( "Four" );
    AppEvent alert5 = list.createEvent( "Five" );
    AppEvent alert6 = list.createEvent( "Six" );
    //System.out.println( "Max="+list.getMaxEvents()+" Size=" + list.getSize() );
    assertTrue( list._list.size() == 5 );

    // should result in the list being trimmed immediately
    list.setMaxEvents( 2 );
    assertTrue( list._list.size() == 2 );

    list.add( alert0 );
    list.add( alert1 );
    list.add( alert2 );
    list.add( alert3 );
    list.add( alert4 );
    list.add( alert5 );
    list.add( alert6 );

    // should still only contain 2 events
    assertTrue( list._list.size() == 2 );

    // Check the first and last event in the list
    assertEquals( alert5, list.getFirst() );
    assertEquals( alert6, list.getLast() );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#setMaxEvents(int)}.
   */
  //@Test
  public void testSetMaxEvents() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#add(coyote.i13n.AppEvent)}.
   */
  //@Test
  public void testAdd() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#remove(coyote.i13n.AppEvent)}.
   */
  //@Test
  public void testRemove() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#get(long)}.
   */
  //@Test
  public void testGet() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#getFirst()}.
   */
  //@Test
  public void testGetFirst() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#getLast()}.
   */
  //@Test
  public void testGetLast() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#getSize()}.
   */
  //@Test
  public void testGetSize() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#createEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, int, java.lang.String)}.
   */
  //@Test
  public void testCreateEventStringStringStringStringIntIntIntString() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#createEvent(java.lang.String)}.
   */
  //@Test
  public void testCreateEventString() {
    fail( "Not yet implemented" );
  }




  /**
   * Test method for {@link coyote.i13n.EventList#createEvent(java.lang.String, int, int)}.
   */
  //@Test
  public void testCreateEventStringIntInt() {
    fail( "Not yet implemented" );
  }

}
