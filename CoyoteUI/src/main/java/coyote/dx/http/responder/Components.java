/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.ScheduledBatchJob;
import coyote.dx.Service;
import coyote.dx.http.helpers.ComponentList;
import coyote.dx.http.helpers.MainMenu;
import coyote.dx.http.helpers.NavBar;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;

import java.util.List;
import java.util.Map;


/**
 * 
 */
public class Components extends ViewResponder implements Responder {


  @Override
  public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
    SessionProfile profile = SessionProfileManager.retrieveOrCreateProfile(session);
    Service service = resource.initParameter(0, Service.class);
    setPreProcessing(true);

    loadTemplate(this.getClass().getSimpleName());

    mergeSymbols(service.getContext().getSymbols()); // operational context
    mergeSymbols(service.getSymbols()); // loader

    getSymbols().put("Footer", loadFragment("fragments/Footer.html"));
    getSymbols().put("NavBar", new NavBar(session).Symbols(getSymbols()).CurrentPage(MainMenu.HOME).build());
    getSymbols().put("MainMenu", new MainMenu(session).Symbols(getSymbols()).CurrentPage(MainMenu.HOME).build());

    // Get the component name from the URL parameters, specified when we were registered with the router
    String command = urlParams.get("name");
    if( StringUtil.isNotBlank(command)) {
      Log.info("Received a component name of '" + command + "'");
      // getSymbols().put("Contents", new ComponentDetail(service.getComponents()).Symbols(getSymbols()).build());
    } else {
      getSymbols().put("Contents", new ComponentList(service.getComponents()).Symbols(getSymbols()).build());
    }




    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }

}
