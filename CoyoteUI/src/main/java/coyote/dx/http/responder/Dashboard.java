/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.util.Map;

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.Service;


/**
 * 
 */
public class Dashboard extends ViewResponder implements Responder {

  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {

    // The first init parameter should be the service in which everything is running
    Service service = resource.initParameter(0, Service.class);

    // populate our symbols with those of the service
    mergeSymbols(service.getContext().getSymbols());
    

    // load the template
    loadTemplate(this.getClass().getSimpleName());

    // if there is no session cookie, then send the login page
    // if(session.getCookies().getCookie("session")){ loadTemplate("login"); }

    //

    // modify the template as we see fit

    //

    // all done, return the response
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }

}
