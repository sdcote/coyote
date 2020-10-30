/*
 * Copyright (c) 2020 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.Service;

import java.util.Map;


/**
 * This responder reports the health of the service via OpenMetrics.
 * 
 * <p>This is the endpoint Prometheus systems can scrape for data about this service.
 */
public class OpenMetricsResponder extends AbstractCoyoteResponder implements Responder {

  private String generateMetrics(Service service){
    return "# Future home of OpenMetrics for this node.";
  }


  @Override
  public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
    Service service = resource.initParameter(0, Service.class);
    return Response.createFixedLengthResponse(getStatus(), MimeType.TEXT.getType(), generateMetrics(service));
  }

}
