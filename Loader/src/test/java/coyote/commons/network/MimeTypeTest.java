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
package coyote.commons.network;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;


/**
 * 
 */
public class MimeTypeTest {

  @Test
  public void test() {
    MimeType type = null;
    List<MimeType> types = MimeType.get( "Test.java" );
    assertNotNull( types );
    //    System.out.println( types.size() );
    //    for ( MimeType mt : types ) {
    //      System.out.println( mt );
    //    }

  }




  @Test
  public void testEquals() {
    MimeType html = MimeType.HTML;
    MimeType htmlx = MimeType.get( "index.htm" ).get( 0 );
    assertTrue( htmlx.equals( html ) );
    assertFalse( MimeType.JSON.equals( MimeType.SOAP ) );
  }

}
