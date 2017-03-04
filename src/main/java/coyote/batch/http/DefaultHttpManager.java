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
import coyote.batch.http.nugget.CommandHandler;
import coyote.batch.http.nugget.HealthCheckHandler;
import coyote.batch.http.nugget.LogApiHandler;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.dataframe.DataFrame;


/**
 * This defines the API for a Batch Service.
 */
public class DefaultHttpManager extends HTTPDRouter implements HttpManager {

  private final Service service;




  /**
   * Create the server instance with all the defaults.
   * 
   * @param service the service we are to manage
   */
  public DefaultHttpManager( int port, DataFrame cfg, Service service ) throws IOException {
    super( port );

    if ( service == null )
      throw new IllegalArgumentException( "Cannot create HttpManager without a service reference" );

    // Our connection to the service instance we are managing
    this.service = service;

    // TODO: Setup Auth provider from configuration
    setAuthProvider( new DefaultAuthProvider() );

    // Set the default mappings
    addMappings();

    // REST interfaces with a default priority of 100
    addRoute( "/api/cmd/(.)+", CommandHandler.class, service );
    addRoute( "/api/log/:logname/:action", LogApiHandler.class, service );
    addRoute( "/api/health", HealthCheckHandler.class, service );
  }

}
