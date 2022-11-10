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
import coyote.commons.network.http.responder.Error404Responder;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.commons.network.http.responder.ResourceResponder;
import coyote.dx.ConfigTag;
import coyote.dx.Service;
import coyote.dx.http.responder.*;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static coyote.commons.network.http.responder.ResourceResponder.REDIRECT_TAG;


/**
 *
 */
public class CoyoteHttpManager extends HTTPDRouter implements HttpManager {

    /** set of symbols we should not expose through the UI **/
    private static final Set<String> protectedSymbols = new HashSet<>();

    static {
        protectedSymbols.add("vault.secret");
    }

    private final Service service;

    /**
     * Create the server instance with all the defaults
     *
     * @param port    the port on which this server should listen
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




    public static boolean symbolIsProtected(String symbol) {
        boolean retval = Pattern.compile(Pattern.quote("password"), Pattern.CASE_INSENSITIVE).matcher(symbol).find();
        if (!retval) retval = protectedSymbols.contains(symbol);
        return retval;
    }




    /**
     * Set the configuration data in this manager
     *
     * @param cfg Config instance containing our configuration (may be null)
     */
    public void setConfiguration(Config cfg) {
        if (cfg != null) {
            Config authConfig = cfg.getSection(GenericAuthProvider.AUTH_SECTION);

            // Setup auth provider from configuration - No configuration results in deny-all operation
            if( authConfig!= null) {
                setAuthProvider(new GenericAuthProvider(authConfig));
            }

            // force the components not to redirect to an index file for safety's sake
            cfg.put(REDIRECT_TAG, false);

            // Configure the IP Access Control List
            configIpACL(cfg.getSection(ConfigTag.IPACL));

            // Configure Denial of Service frequency tables
            configDosTables(cfg.getSection(ConfigTag.FREQUENCY));
        }

        // It is suggested that responders from the Coyote package be used to
        // handle standard, expected functions for consistency across managers.
        // REST interfaces with a default priority of 100
        addRoute("/api/cmd/:command", CommandResponder.class, service, cfg);
        addRoute("/api/ping/:id", PingResponder.class, service, cfg);
        addRoute("/api/health", HealthCheckResponder.class, service, cfg);

        addRoute("/", Dashboard.class, service, cfg);
        addRoute("/components", Components.class, service, cfg);
        addRoute("/components/:name", Components.class, service, cfg);
        addRoute("/events", Events.class, service, cfg);
        addRoute("/login", Login.class, service, cfg);
        addRoute("/logout", Logout.class, service, cfg);
        addRoute("/logging", Logging.class, service, cfg);
        addRoute("/logging/:logger", Logging.class, service, cfg);
        addRoute("/commands", Commands.class, service, cfg);
        addRoute("/commands/:command", Commands.class, service, cfg);

        // Content handler - higher priority value (evaluated later) allows it to be a catch-all
        // Note: internal responders expect initParam 0 to be the web server and initParam 1 to be the web server configuration
        addRoute("/(.)+", Integer.MAX_VALUE, ResourceResponder.class, this, cfg);

        // Set the responder for 404 errors
        setNotFoundResponder(NotFound.class, service, cfg);

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
