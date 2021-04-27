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
package coyote.dx.web.decorator;

import coyote.dataframe.DataFrame;
import coyote.loader.cfg.Config;


/**
 * 
 */
public abstract class AbstractDecorator implements RequestDecorator {

  protected Config configuration = new Config();




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration(DataFrame frame) {
    if (frame != null) {
      configuration = new Config(frame);
    }
  }

}
