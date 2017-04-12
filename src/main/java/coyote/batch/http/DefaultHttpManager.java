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
import coyote.batch.http.responder.CommandResponder;
import coyote.batch.http.responder.HealthCheckResponder;
import coyote.batch.http.responder.PingResponder;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


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

    boolean secureServer;
    try {
      secureServer = cfg.getAsBoolean( "SecureServer" );
    } catch ( DataFrameException e1 ) {
      secureServer = false;
    }
    
    if ( port == 443 || secureServer ) {
      try {
        makeSecure( HTTPD.makeSSLSocketFactory( "/keystore.jks", "password".toCharArray() ), null );
      } catch ( IOException e ) {
        Log.error( "Could not make the server secure: " + e.getMessage() );
      }
    }

    
    
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
      configIpACL( cfg.getSection( ConfigTag.IPACL ) );

      // Configure Denial of Service frequency tables
      configDosTables( cfg.getSection( ConfigTag.FREQUENCY ) );
    }

    // Set the default routes
    addDefaultRoutes();

    // REST interfaces with a default priority of 100
    addRoute( "/api/cmd/:command", CommandResponder.class, service );
    addRoute( "/api/ping/:id", PingResponder.class, service );
    addRoute( "/api/health", HealthCheckResponder.class, service );
  }




}
