/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web;

import java.util.Map;

import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.DefaultResponder;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;


/**
 * Handle requests by generating a 
 */
public class WebRequestHandler extends DefaultResponder implements Responder {

  /**
   * @see coyote.commons.network.http.responder.DefaultResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.HTTPSession)
   */
  @Override
  public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {

    // The first init parameter should be the queue to place our response future objects
    ResponseFutureQueue queue = resource.initParameter(0, ResponseFutureQueue.class);

    ResponseFuture response = new ResponseFuture(session);

    // put the response object in the queue for the WebServerReader to pickup and pass through the transform
    try {
      queue.put(response);
    } catch (InterruptedException e) {}

    // wait for the response to be completed
    while (!response.isComplete()) {
      try {
        response.wait(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Send the result
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Status getStatus() {
    // TODO Auto-generated method stub
    return null;
  }




  @Override
  public String getText() {
    // TODO Auto-generated method stub
    return null;
  }




  @Override
  public String getMimeType() {
    // TODO Auto-generated method stub
    return null;
  }

}
