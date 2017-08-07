/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;


/**
 * 
 */
public class OperationalContextTest {

  @Test
  public void contextMerge() {
    String key = "Now";
    OperationalContext source = new TransformContext();
    source.set(key, new Date());
    OperationalContext target = new TransformContext();
    assertNull(target.get(key));
    target.merge(source);
    assertNotNull(target.get(key));
  }

}
