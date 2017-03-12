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

import coyote.batch.ConfigTag;
import coyote.batch.Service;
import coyote.batch.http.nugget.CommandHandler;
import coyote.batch.http.nugget.HealthCheckHandler;
import coyote.batch.http.nugget.LogApiHandler;
import coyote.batch.http.nugget.PingHandler;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.Config;


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
  public DefaultHttpManager( int port, Config cfg, Service service ) throws IOException {
    super( port );

    if ( service == null )
      throw new IllegalArgumentException( "Cannot create HttpManager without a service reference" );

    // Our connection to the service instance we are managing
    this.service = service;

    if ( cfg != null ) {
      // Setup auth provider from configuration - No configuration results in deny-all operation
      DataFrame authConfig = null;
      for ( DataField field : cfg.getFields() ) {
        if ( GenericAuthProvider.AUTH_SECTION.equalsIgnoreCase( field.getName() ) && field.isFrame() ) {
          setAuthProvider( new GenericAuthProvider( new Config( (DataFrame)field.getObjectValue() ) ) );
        }
      }

      // Configure the IP Access Control List
      super.configIpACL( cfg.getSection( ConfigTag.IPACL ) );

      // Configure Denial of Service frequency tables
      super.configDosTables( cfg.getSection( ConfigTag.FREQUENCY ) );
    }

    // Set the default mappings
    addMappings();

    // REST interfaces with a default priority of 100
    addRoute( "/api/cmd/:command", CommandHandler.class, service );
    addRoute( "/api/ping/:id", PingHandler.class, service );
    addRoute( "/api/log/:logname/:action", LogApiHandler.class, service );
    addRoute( "/api/health", HealthCheckHandler.class, service );
  }




}
