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

import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.network.http.auth.GenericAuthProvider;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.commons.network.http.responder.ResourceResponder;
import coyote.dx.Service;
import coyote.dx.http.responder.CommandResponder;
import coyote.dx.http.responder.Commands;
import coyote.dx.http.responder.Dashboard;
import coyote.dx.http.responder.HealthCheckResponder;
import coyote.dx.http.responder.Logging;
import coyote.dx.http.responder.Login;
import coyote.dx.http.responder.Logout;
import coyote.dx.http.responder.PingResponder;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * 
 */
public class CoyoteHttpManager extends HTTPDRouter implements HttpManager {

  private final Service service;




  /**
   * Create the server instance with all the defaults
   * @param port the port on which this server should listen
   * @param service the Batch Service this component manages.
   */
  public CoyoteHttpManager(int port, Service service) {
    super(port);
    this.service = service;

    if (service == null)
      throw new IllegalArgumentException("Cannot create HttpManager without a service reference");

    // Try to load any session data from the file system
    try {
      SessionProfileManager.load();
    } catch (Exception e) {
      Log.notice("Could not load sessions from file system", e);
    }

    // Set the default routes
    addDefaultRoutes();

    // remove the root and index routes as we will add our own
    removeRoute("/");
    removeRoute("/index.html");
  }




  /**
   * Set the configuration data in this manager
   * 
   * @param cfg Config instance containing our configuration (may be null) 
   */
  public void setConfiguration(Config cfg) {

    Config authConfig = null;
    if (cfg != null) {
      authConfig = cfg.getSection(GenericAuthProvider.AUTH_SECTION);
    }

    // Setup auth provider from configuration - No configuration results in deny-all operation
    setAuthProvider(new GenericAuthProvider(authConfig));

    // It is suggested that responders from the Coyote package be used to
    // handle standard, expected functions for consistency across managers.
    // REST interfaces with a default priority of 100
    addRoute("/api/cmd/:command", CommandResponder.class, service);
    addRoute("/api/ping/:id", PingResponder.class, service);
    addRoute("/api/health", HealthCheckResponder.class, service);

    addRoute("/", Dashboard.class, service);
    addRoute("/login", Login.class, service);
    addRoute("/logout", Logout.class, service);
    addRoute("/logging", Logging.class, service);
    addRoute("/logging/:logger", Logging.class, service);
    addRoute("/commands", Commands.class, service);
    addRoute("/commands/:command", Commands.class, service);

    // Content handler - higher priority value (evaluated later) allows it to 
    // be a catch-all
    addRoute("/(.)+", Integer.MAX_VALUE, ResourceResponder.class, "content");

  }




  /**
   * @see coyote.commons.network.http.HTTPD#stop()
   */
  @Override
  public void stop() {
    try {
      SessionProfileManager.save();
    } catch (Exception e) {
      Log.notice("Could not save sessions to file system", e);
    }
    super.stop();
  }

}
