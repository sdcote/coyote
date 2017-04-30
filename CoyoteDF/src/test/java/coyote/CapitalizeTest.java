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
package coyote;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coyote.testdata.transform.Capitalize;
import coyote.testdata.transform.DataTransform;


/**
 * 
 */
public class CapitalizeTest {

  /**
   * Test method for {@link coyote.testdata.transform.Capitalize#transform(java.lang.Object)}.
   */
  @Test
  public void testTransform() {
    final DataTransform subject = new Capitalize();

    Object result = subject.transform( " a" );
    assertTrue( result instanceof String );
    String token = (String)result;
    assertEquals( " A", token );

    result = subject.transform( "  " );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "  ", token );

    result = subject.transform( "" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "", token );

    result = subject.transform( "\t" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "\t", token );

    result = subject.transform( "\r" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "\r", token );

    result = subject.transform( "\n" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "\n", token );

    result = subject.transform( "\r\n" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "\r\n", token );

    result = subject.transform( "A" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "A", token );

    result = subject.transform( "123" );
    assertTrue( result instanceof String );
    token = (String)result;
    assertEquals( "123", token );

    result = subject.transform( "now is the time" );
    assertTrue( result instanceof String );
    token = (String)result;
    System.out.println( ">" + token + "<" );
    assertEquals( "Now is the time", token );

  }

}
