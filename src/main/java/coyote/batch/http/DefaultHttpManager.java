/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http;

import java.io.IOException;

import coyote.batch.Service;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class DefaultHttpManager extends HTTPDRouter implements HttpManager {

  private static final int PORT = 55290;

  private final Service service;




  /**
   * Create the server instance with all the defaults.
   * 
   * @param cfg Any configuration data 
   * @param service the service we are to manage
   */
  public DefaultHttpManager( DataFrame cfg, Service service ) throws IOException {
    super( PORT );
    if ( service == null )
      throw new IllegalArgumentException( "Cannot create HttpManager without a service reference" );

    // Our connection to the service instance we are managing
    this.service = service;

    // Add the nuggets handling requests to this service
    addMappings();
  }




  /**
   * Add the routes.
   */
  @Override
  public void addMappings() {
    // Set the default mappings
    super.addMappings();

    // REST interfaces
    addRoute( "/api/cmd/(.)+", CommandHandler.class, service );
    addRoute( "/api/log/(.)+", LogApiHandler.class, service );

  }

}
