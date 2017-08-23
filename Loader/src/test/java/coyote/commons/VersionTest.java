/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class VersionTest {

  @Test
  public void testToString() {
    System.out.println();
    Version version = new Version(1, 2, 3, Version.EXPERIMENTAL);
    assertEquals("1.2.3-exp", version.toString());

    version = new Version(1, 2, 3, Version.DEVELOPMENT);
    assertEquals("1.2.3-dev", version.toString());

    version = new Version(1, 2, 3, Version.ALPHA);
    assertEquals("1.2.3-alpha", version.toString());

    version = new Version(1, 2, 3, Version.BETA);
    assertEquals("1.2.3-beta", version.toString());

    version = new Version(1, 2, 3, Version.GENERAL);
    assertEquals("1.2.3", version.toString());
  }




  @Test
  public void testEquals() {
    Version version = new Version(0, 0, 0);
    Version version1 = new Version(0, 0, 0);

    assertTrue("Empty attributes forward", version.equals(version1));
    assertTrue("Empty attributes reverse", version1.equals(version));

    version = new Version(1, 2, 3, Version.EXPERIMENTAL);
    version1 = new Version(1, 2, 3, Version.EXPERIMENTAL);

    assertTrue("Completed attributes forward", version.equals(version1));
    assertTrue("Completed attributes reverse", version1.equals(version));

  }

}
