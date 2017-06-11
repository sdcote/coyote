/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * 
 */
public class StringUtilTest {

  @Test
  public void fixedLengthTest() {
    String text = "Coyote";
    String field = null;
    int LEFT = 0;
    int CENTER = 1;
    int RIGHT = 2;

    // Alignment Tests = = = = = =
    field = StringUtil.fixedLength( text, 10, LEFT, '*' );
    //System.out.println( field );
    assertTrue( field.length() == 10 );
    assertEquals( field, "Coyote****" );

    field = StringUtil.fixedLength( text, 10, CENTER, '*' );
    assertTrue( field.length() == 10 );
    assertEquals( field, "**Coyote**" );

    field = StringUtil.fixedLength( text, 10, RIGHT, '*' );
    assertTrue( field.length() == 10 );
    assertEquals( field, "****Coyote" );

    // Size Match Tests = = = = = 
    field = StringUtil.fixedLength( text, 6, LEFT, '*' );
    assertTrue( field.length() == 6 );
    assertEquals( field, "Coyote" );

    field = StringUtil.fixedLength( text, 6, CENTER, '*' );
    assertTrue( field.length() == 6 );
    assertEquals( field, "Coyote" );

    field = StringUtil.fixedLength( text, 6, RIGHT, '*' );
    assertTrue( field.length() == 6 );
    assertEquals( field, "Coyote" );

    // Truncation Tests = = = = =
    field = StringUtil.fixedLength( text, 5, LEFT, '*' );
    assertTrue( field.length() == 5 );
    assertEquals( field, "Coyot" );

    field = StringUtil.fixedLength( text, 5, CENTER, '*' );
    assertTrue( field.length() == 5 );
    assertEquals( field, "Coyot" );

    field = StringUtil.fixedLength( text, 5, RIGHT, '*' );
    assertTrue( field.length() == 5 );
    assertEquals( field, "oyote" );

    field = StringUtil.fixedLength( text, 4, LEFT, '*' );
    assertTrue( field.length() == 4 );
    assertEquals( field, "Coyo" );

    field = StringUtil.fixedLength( text, 4, CENTER, '*' );
    assertTrue( field.length() == 4 );
    assertEquals( field, "oyot" );

    field = StringUtil.fixedLength( text, 4, RIGHT, '*' );
    assertTrue( field.length() == 4 );
    assertEquals( field, "yote" );
  }




  @Test
  public void empty() {
    assertTrue( StringUtil.isEmpty( "" ) );
    assertTrue( StringUtil.isEmpty( null ) );
    assertFalse( StringUtil.isEmpty( " " ) );
  }




  @Test
  public void notEmpty() {
    assertFalse( StringUtil.isNotEmpty( "" ) );
    assertFalse( StringUtil.isNotEmpty( null ) );
    assertTrue( StringUtil.isNotEmpty( " " ) );
  }




  @Test
  public void quotedValue() {

    String text = "";
    String expected = null;
    String result = StringUtil.getQuotedValue( text );
    //System.out.println( "EMPTY>" + result + "<" );
    assertNull( result );

    text = "\"";
    result = StringUtil.getQuotedValue( text );
    //System.out.println( "ONE>" + result + "<" );
    assertNull( result );

    text = "\"\"";
    expected = "";
    result = StringUtil.getQuotedValue( text );
    //System.out.println( "TWO>" + result + "<" );
    assertNotNull( result );
    assertEquals( result, expected );

    text = "\"123\"";
    expected = "123";
    result = StringUtil.getQuotedValue( text );
    //System.out.println( "123>" + result + "<" );
    assertNotNull( result );
    assertEquals( result, expected );

    text =  "  \"ABC\"   ";
    expected = "ABC";
    result = StringUtil.getQuotedValue( text );
    //System.out.println( "SPACED>" + result+ "<" );
    assertNotNull( result );
    assertEquals( result, expected );
    
    text =  "  \"\"A\"\"B\"\"C\"\"   ";
    expected = "\"A\"\"B\"\"C\"";
    result = StringUtil.getQuotedValue( text );
    //System.out.println( "INCLOSED>" + result + "<" );
    assertNotNull( result );
    assertEquals( result, expected );
  }
  
}
