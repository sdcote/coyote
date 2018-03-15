/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.util.HashMap;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.loader.log.Log;


/**
 * 
 */
public class Login extends ViewResponder implements Responder {

  @Override
  public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {

    SessionProfile profile = SessionProfileManager.retrieveOrCreateProfile(session);
    Log.info("Profile: " + profile);

    if (StringUtil.isBlank(session.getUserName())) {
      Log.info("No user");
    }
    if (session.getUserGroups().size() == 0) {
      Log.info("No groups");
    }

    AuthProvider auth = resource.getAuthProvider();

    Map<String, String> credentials = new HashMap<String, String>();
    credentials.put(AuthProvider.USERNAME, "sysop");
    credentials.put(AuthProvider.PASSWORD, "secret");

    if (auth.authenticate(session, credentials)) {
      // session should now contain username and groups
      Log.info("User: " + session.getUserName());
      Log.info("Groups: " + session.getUserGroups());

    } else {
      // authentication failed
      Log.notice("Authentication for XXX failed: username or password are not valid");
    }

    // all done, return the response
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }
}
