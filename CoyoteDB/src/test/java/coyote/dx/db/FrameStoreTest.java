/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import coyote.dataframe.DataFrame;


/**
 * 
 */
public class FrameStoreTest {

  @Test
  public void simpleSlots() {
    DataFrame frame = new DataFrame().set("Field1", "Value1").set("Field2", "Value2").set("Field3", "Value3");
    String parent = "123A";
    List<FieldSlot> slots = FrameStore.getSlots(frame, parent);
    assertTrue(slots.size() == 3);
    assertTrue(slots.get(2).getName().equals("Field3"));
    assertTrue(slots.get(1).getName().equals("Field2"));
    assertTrue(slots.get(0).getName().equals("Field1"));
  }




  @Test
  public void emptyFrame() {
    DataFrame frame = new DataFrame();
    String parent = "123A";
    List<FieldSlot> slots = FrameStore.getSlots(frame, parent);
    assertNotNull(slots);
    assertTrue(slots.size() == 0);
  }




  @Test
  public void treeSlots() {
    DataFrame frame = new DataFrame().set("Field1", "Value1").set("Field2", new DataFrame().set("Key1", "Value1")).set("Field3", "Value3");
    String parent = "123A";
    List<FieldSlot> slots = FrameStore.getSlots(frame, parent);
    assertTrue(slots.size() == 4);

    assertTrue(slots.get(3).getName().equals("Field3"));
    assertTrue(slots.get(2).getName().equals("Key1"));
    assertTrue(slots.get(1).getName().equals("Field2"));
    assertTrue(slots.get(0).getName().equals("Field1"));

    // test parentage
    assertEquals(slots.get(2).getParent(), slots.get(1).getSysId());

    // test sequence
    assertTrue(slots.get(3).getSequence() == 2);
    assertTrue(slots.get(2).getSequence() == 0);
    assertTrue(slots.get(1).getSequence() == 1);
    assertTrue(slots.get(0).getSequence() == 0);

    // test types
    assertTrue(slots.get(3).getType() == 3); // type 3 is a string
    assertTrue(slots.get(2).getType() == 3);
    assertTrue(slots.get(1).getType() == 0); // type 0 is a frame
    assertTrue(slots.get(0).getType() == 3);

    frame = new DataFrame().set("Field1", "Value1").set("Field2", new DataFrame().set("Key1", new DataFrame().set("level", "Three"))).set("Field3", "Value3").set("Field2", new DataFrame().set("Key2", "Value2"));
    slots = FrameStore.getSlots(frame, parent);
    assertTrue(slots.size() == 7);
    // for(FieldSlot slot: slots){ System.out.println(slot.toString()); }
  }

}
