/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.http;

import coyote.dataframe.DataField;
import coyote.dx.ConfigTag;
import coyote.dx.Service;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * This is the static fixture used to bind the UI to the service.
 * 
 * <p>Different UI bindings can be used and this class enables a new UI to be 
 * discovered and bound to the service. The core use case is to install a new 
 * version of the UI. This new version may utilize a different look and feel, 
 * or even completely different functionality.</p>
 */
public class StaticManagerBinder implements ManagerFactoryBinder {

  private static final int DEFAULT_PORT = 55290;
  private static int port = DEFAULT_PORT;




  /**
   * @see coyote.dx.http.ManagerFactoryBinder#createManager(coyote.loader.cfg.Config, coyote.dx.Service)
   */
  @Override
  public HttpManager createManager(Config cfg, Service svc) throws ConfigurationException {

    // we need to get the port first as part of the constructor
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        if (ConfigTag.PORT.equalsIgnoreCase(field.getName())) {
          try {
            port = Integer.parseInt(field.getStringValue());
          } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
            Log.error("Port configuration option was not a valid integer");
          }
        }
      }
    }

    // create the manager
    CoyoteHttpManager retval = new CoyoteHttpManager(port, svc);

    // configure the instance based on the config frame
    retval.setConfiguration(cfg);

    // return the manager
    return retval;
  }

}
