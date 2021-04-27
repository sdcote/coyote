/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public interface ConfigurableComponent extends Component {

  /**
   * Set the configuration for this component
   * 
   * @param cfg the configuration to set
   * 
   * @throws ConfigurationException if the configuration is not valid for this 
   *         component in any way.
   */
  public void setConfiguration(Config cfg) throws ConfigurationException;




  /**
   * @return the configuration object set in this component
   */
  public Config getConfiguration();




  /**
   * @return true if this component is enabled to run, false if not 
   */
  public boolean isEnabled();




  /**
   * @param flag true to enable this component, false to prevent it from being
   *        executed.
   */
  public void setEnabled(boolean flag);

}
