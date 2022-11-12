/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.*;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;

import java.util.Map;


/**
 * 
 */
public class Logout extends ViewResponder implements Responder {


    @Override
    public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
        SessionProfile profile = SessionProfileManager.retrieveOrCreateProfile(session);
        SessionProfileManager.destroyProfile(session);

        // In Loader v0.9.0 this call will be:   SessionProfileManager.destroyProfile(session);

        final Response redirect = Response.createFixedLengthResponse(Status.REDIRECT_TEMPORARY, MimeType.TEXT.getType(), null);
        redirect.addHeader("Location", "/");
        return redirect;
    }


}
