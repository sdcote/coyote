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
import coyote.batch.http.nugget.ResourceHandler;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.commons.network.http.nugget.IndexHandler;
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

    // remove default content mappings
    super.removeRoute( "/" ); 
    super.removeRoute( "/index.html" );
    
    // REST interfaces with a default priority of 100
    addRoute( "/api/cmd/(.)+", CommandHandler.class, service );
    addRoute( "/api/log/:logname/:action", LogApiHandler.class, service );
    addRoute( "/api/health",HealthCheckHandler.class,service);

    // Content handler - higher priority allows it to be a catch-all
    addRoute( "/", Integer.MAX_VALUE, ResourceHandler.class, "content" );
    addRoute( "/(.)+", Integer.MAX_VALUE, ResourceHandler.class, "content" );
  }

}
