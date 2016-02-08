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

import java.io.File;
import java.io.IOException;

import coyote.commons.network.http.nugget.GeneralHandler;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.commons.network.http.nugget.StaticPageHandler;
import coyote.commons.network.http.nugget.UriResponder;


/**
 * 
 */
public class HttpManager extends HTTPDRouter {

  private static final int PORT = 55290;




  /**
   * Create the server instance with all the defaults
   */
  public HttpManager() throws IOException {
    super( PORT );
    addMappings();
  }




  /**
   * Add the routes.
   * 
   * <p>Every route is an absolute path. Parameters start with ":". Handler 
   * class should implement {@link UriResponder} interface. If the handler does 
   * not implement {@link UriResponder} interface - toString() is called.</p>
   */
  @Override
  public void addMappings() {
    // Set the default mappings
    super.addMappings();
    
    // Add our routes:handlers
    addRoute( "/user", DebugHandler.class );
    addRoute( "/user/:id", DebugHandler.class );
    addRoute( "/user/help", GeneralHandler.class );
    addRoute( "/general/:param1/:param2", GeneralHandler.class );
    addRoute( "/test", String.class );
    addRoute( "/stream", StreamUrl.class );
    addRoute( "/browse/(.)+", StaticPageHandler.class, new File( "src/test/resources" ).getAbsoluteFile() );
  }

}
