/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.Service;
import coyote.dx.http.helpers.MainMenu;
import coyote.dx.http.helpers.NavBar;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;

import java.util.List;
import java.util.Map;


/**
 * Displays the 404 Error Page
 */
public class NotFound extends ViewResponder implements Responder {


  @Override
  public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
    loadTemplate(this.getClass().getSimpleName());
    setPreProcessing(true);
    getSymbols().put("NavBar", new NavBar(session).Symbols(getSymbols()).CurrentPage(MainMenu.HOME).build());
    getSymbols().put("MainMenu", new MainMenu(session).Symbols(getSymbols()).CurrentPage(MainMenu.HOME).build());
    getSymbols().put("Footer", loadFragment("fragments/Footer.html"));
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }

}
