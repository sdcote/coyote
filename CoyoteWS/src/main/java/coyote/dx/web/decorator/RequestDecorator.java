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

import org.apache.http.HttpMessage;

import coyote.dataframe.DataFrame;


/**
 * Simple interface for modifying a request message before it is sent.
 */
public interface RequestDecorator {

  /**
   * Modify the given request message.
   * 
   * <p>This method is called on a HTTP request message just before it is sent 
   * to the service instance. It is the final change for the message to be 
   * customized before being sent through the web service infrastructure.
   * 
   * <p>Another common use case is to generate a message hash or some other 
   * signature to verify the integrity of the request message exchange.
   * 
   * @param request the message to be queried and modified by this decorator.
   */
  void process( HttpMessage request );




  /**
   * Configures the Decorator.
   * 
   * <p>May have the side-effect of initializing the decorator.</p>
   * 
   * @param frame the data frame holding configuration data.
   * 
   * @throws IllegalArgumentException if there is a problem with the configuration.
   */
  public void setConfiguration( DataFrame frame );

}
