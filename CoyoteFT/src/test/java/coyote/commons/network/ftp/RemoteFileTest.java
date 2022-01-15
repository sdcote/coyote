/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.network.ftp;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import coyote.commons.network.RemoteFile;
import org.junit.Test;


/**
 * 
 */
public class RemoteFileTest {

  /**
   * Test method for {@link RemoteFile#getName()}.
   */
  @Test
  public void testGetName() {
    String test = "data.txt";
    RemoteFile subject = new RemoteFile("/home/sdcote/data.txt", null);
    assertEquals(test, subject.getName());

    subject = new RemoteFile("/data.txt", null);
    assertEquals(test, subject.getName());

    subject = new RemoteFile("data.txt", null);
    assertEquals(test, subject.getName());
  }




  /**
   * Test method for {@link RemoteFile#getParent()}.
   */
  @Test
  public void testGetParent() {
    RemoteFile subject = new RemoteFile("/home/sdcote/data.txt", null);
    assertNotNull(subject.getParent());
    assertEquals("/home/sdcote", subject.getParent());

    subject = new RemoteFile("/data.txt", null);
    assertNull(subject.getParent());

    subject = new RemoteFile("data.txt", null);
    assertNull(subject.getParent());
  }




  @Test
  public void testIsRelative() {
    RemoteFile subject = new RemoteFile("/home/sdcote/data.txt", null);
    assertFalse(subject.isRelative());

    subject = new RemoteFile("/data.txt", null);
    assertFalse(subject.isRelative());

    subject = new RemoteFile("data.txt", null);
    assertTrue(subject.isRelative());

    subject = new RemoteFile("tmp/data.txt", null);
    assertTrue(subject.isRelative());
  }




  /**
   * Test method for {@link RemoteFile#getAbsolutePath()}.
   */
  @Test
  public void testGetAbsolutePath() {
    RemoteFile subject = new RemoteFile("/home/sdcote/data.txt", null);
    assertEquals("/home/sdcote/data.txt", subject.getAbsolutePath());

    subject = new RemoteFile("/data.txt", null);
    assertEquals("/data.txt", subject.getAbsolutePath());

    subject = new RemoteFile("data.txt", null);
    assertEquals("data.txt", subject.getAbsolutePath());

    subject = new RemoteFile("tmp/data.txt", null);
    assertEquals("tmp/data.txt", subject.getAbsolutePath());
  }

}
