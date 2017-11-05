/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.Service;
import coyote.loader.log.Log;


/**
 * 
 */
public class Dashboard extends ViewResponder implements Responder {

  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {

    SessionProfile profile = SessionProfileManager.retrieveOrCreateProfile(session);
    Log.debug("Profile: " + profile.getIdentifier());

    if (StringUtil.isBlank(session.getUserName())) {
      Log.info("No user");
    }
    if (session.getUserGroups().size() == 0) {
      Log.info("No groups");
    }

    // The first init parameter should be the service in which everything is running
    Service service = resource.initParameter(0, Service.class);

    // leave unresolved tokens in templates. 
    setPreProcessing(true);

    // populate our symbols with those of the service
    mergeSymbols(service.getContext().getSymbols());

    // load the template matching this class
    loadTemplate(this.getClass().getSimpleName());

    //

    // Add more symbols to our table, like other page fragments:
    getSymbols().put("Menu", loadFragment("fragments/menu2.html"));

    // modify the template as we see fit

    //

    // all done, return the response
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }

}
