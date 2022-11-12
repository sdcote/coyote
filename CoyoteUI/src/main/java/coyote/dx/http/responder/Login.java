/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.*;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.ScheduledBatchJob;
import coyote.dx.Service;
import coyote.dx.http.helpers.MainMenu;
import coyote.dx.http.helpers.NavBar;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;


/**
 *
 */
public class Login extends ViewResponder implements Responder {

    private static final String LOGIN_URL = "LoginURL";
    private static final String LOGIN_MESSAGE = "LoginMessage";


    @Override
    public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
        return loginPage("", resource, session);
    }


    @Override
    public Response post(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
        // First parse the body to get the form elements
        try {
            session.parseBody();
        } catch (Exception e) {
            Log.error(e);
        }

        AuthProvider auth = resource.getAuthProvider();

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(AuthProvider.USERNAME, session.getParms().get("account"));
        credentials.put(AuthProvider.PASSWORD, session.getParms().get("password"));

        if (auth.authenticate(session, credentials)) {
            final Response redirect = Response.createFixedLengthResponse(Status.REDIRECT_TEMPORARY, MimeType.TEXT.getType(), "Authentication successful");
            redirect.addHeader("Location", "/");
            return redirect;
        } else {
            return loginPage("Authentication failed", resource, session);
        }

    }


    private Response loginPage(String message, Resource resource, HTTPSession session) {
        SessionProfileManager.retrieveOrCreateProfile(session);
        Service service = resource.initParameter(0, Service.class);
        setPreProcessing(true);
        loadTemplate(this.getClass().getSimpleName());

        // populate our symbols
        mergeSymbols(service.getContext().getSymbols()); // operational context shared between components
        mergeSymbols(service.getSymbols()); // loader symbols
        getSymbols().put(LOGIN_URL, resource.getUri());
        getSymbols().put(LOGIN_MESSAGE, message);
        return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
    }

}
