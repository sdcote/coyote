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
package coyote.commons;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * 
 */
public class ExceptionUtilTest {

  /**
   * Test method for {@link coyote.commons.ExceptionUtil#getLocalJavaName(java.lang.String)}.
   */
  @Test
  public void testGetLocalJavaName() {
    assertNotNull( ExceptionUtil.getLocalJavaName( this.getClass().getName() ) );
    assertTrue( "ExceptionUtilTest".equals( ExceptionUtil.getLocalJavaName( this.getClass().getName() ) ) );
  }




  @Test
  public void testGetAbbreviatedClassname() {
    assertTrue( "c.c.ExceptionUtilTest".equals( ExceptionUtil.getAbbreviatedClassname( this.getClass().getName() ) ) );
    assertTrue( "ThisClass".equals( ExceptionUtil.getAbbreviatedClassname( "ThisClass" ) ) );
    assertNotNull( ExceptionUtil.getAbbreviatedClassname( "" ) );
    assertTrue( "".equals( ExceptionUtil.getAbbreviatedClassname( "" ) ) );
    assertNotNull( ExceptionUtil.getAbbreviatedClassname( null ) );
    assertTrue( "".equals( ExceptionUtil.getAbbreviatedClassname( null ) ) );
  }

}
