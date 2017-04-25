/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import coyote.dx.TransformContext;


/**
 * 
 */
public class TransformContextTest {

  /**
   * Test method for {@link coyote.dx.TransformContext#get(java.lang.String, boolean)}.
   */
  @Test
  public void testGetStringBoolean() {
    TransformContext context = new TransformContext();
    context.set( "MyString", "w00t!" );

    assertNotNull( context.get( "mystring", false ) );
    assertNull( context.get( "mystring", true ) );
    assertNotNull( context.get( "MyString", true ) );
    assertNull( context.get( "yourstring", false ) );
    assertNull( context.get( "yourstring", true ) );

  }

}
