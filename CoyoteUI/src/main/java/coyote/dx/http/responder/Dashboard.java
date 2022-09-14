/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.awt.*;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
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
public class Dashboard extends ViewResponder implements Responder {

  private static final String COMPONENT_COUNT = "ComponentCount";
  private static final String JOB_COUNT = "JobCount";

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

    // The first init parameter should be the service in which everything is running
    Service service = resource.initParameter(0, Service.class);

    // Leave unresolved tokens in templates, so we can debug typos in the templates.
    setPreProcessing(true);

    // populate our symbols
    mergeSymbols(service.getContext().getSymbols()); // operational context shared between components
    mergeSymbols(service.getSymbols()); // loader symbols
    List<ManagedComponent> components = service.getComponents();
    getSymbols().put(COMPONENT_COUNT, components.size());

    int jobCount = 0;
    for(ManagedComponent component:components){
      if( component instanceof ScheduledBatchJob ) jobCount++;
    }
    getSymbols().put(JOB_COUNT,jobCount);

    // load the template matching this class
    loadTemplate(this.getClass().getSimpleName());


    // Add more symbols to our table, like other page fragments:
    getSymbols().put("Footer", loadFragment("fragments/Footer.html"));


    // Call helper classes that all pages can share. These create dynamic content that can be placed in the symbol table
    getSymbols().put("NavBar", new NavBar(session).Service(service).Symbols(getSymbols()).CurrentPage(MainMenu.HOME).build());
    getSymbols().put("MainMenu", new MainMenu(session).Symbols(getSymbols()).CurrentPage(MainMenu.HOME).build());

    // modify the template as we see fit

    //

    // all done, return the response. The superclass getText method does all the heavy lifting
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }

}
