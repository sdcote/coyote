/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http;

import coyote.dx.Service;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This represents the interface all StaticManagers must implement so commands 
 * can be implemented in the HTTP server.
 */
public interface ManagerFactoryBinder {

  /**
   * Create a manager with the given configuration for the given Service job.
   * 
   * @param cfg Optional configuration for the returned HttpManager
   * @param svc The service the returned manager is to manage.
   * 
   * @return A configured HttpManager connected to the given DX service
   * 
   * @throws ConfigurationException if the passed configuration was invalid
   */
  public HttpManager createManager(Config cfg, Service svc) throws ConfigurationException;
}
