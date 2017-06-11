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
package coyote.loader;

import coyote.loader.cfg.Config;


/**
 * An interface to a component which monitors and restarts components.
 */
public interface WatchDog {

  /**
   * An implementation of checking in with the watchdog. 
   * 
   * <p>Some refer to this as "petting the dog" for as long as you are petting 
   * the dog, it will not bite you. In this context, as long as you check in 
   * with the watchdog, it will not assume you are frozen and in need of 
   * restart.</p> 
   *  
   * @param component
   */
  public void checkIn( Object component );




  /**
   * Inform the watchdog how logs to wait before assuming the component is 
   * frozen.
   * 
   * <p>This allows a component to register with the watchdog the time to wait 
   * before attempting a restart and the configuration to be used when creating 
   * a new instance of the component.</p>
   * 
   * @param millis
   * @param component
   * @param cfg The configuration to use when creating a new instance of this component.
   */
  public void setHangTime( long millis, Object component, Config cfg );

}
