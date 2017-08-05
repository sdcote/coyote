/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import coyote.commons.network.MimeType;

/**
 * 
 */
public class ParametersTest {

  @Test
  public void testAcceptHeader() {
    Parameters params = new Parameters();
    
    params.setAcceptType( MimeType.HTML );
    params.addAcceptType( MimeType.TEXT );
    params.addAcceptType( MimeType.ANY );
    
    String header = params.getAcceptHeaderValue();
    assertEquals("text/html; text/plain; */*",header);
    params.clearAcceptTypes();
    assertNull(params.getAcceptHeaderValue());
    
    
    params.setAcceptType( MimeType.HTML ).addAcceptType( MimeType.TEXT ).addAcceptType( MimeType.ANY );
    header = params.getAcceptHeaderValue();
    assertEquals("text/html; text/plain; */*",header);
    params.clearAcceptTypes();
    assertNull(params.getAcceptHeaderValue());
    
    
    params.setAcceptType( MimeType.JSON );
    header = params.getAcceptHeaderValue();
    assertEquals("application/json",header);
    params.clearAcceptTypes();
    assertNull(params.getAcceptHeaderValue());
    
  }

}
