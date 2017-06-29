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
package coyote.dx.context;

import coyote.dx.TransformEngine;


/**
 * 
 */
public abstract class AbstractContextTest {

  /**
   * Run and close the given engine.
   * @param engine to run and close
   */
  protected void turnOver( TransformEngine engine ) {
    try {
      engine.run();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    try {
      engine.close();
    } catch ( Exception e ) {}
  }

}
