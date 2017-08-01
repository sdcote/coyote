/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http.responder;

import java.util.Map;

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;


/**
 * This represents a class which responds to a URI requested from the HTTP
 * server.
 *
 * <p>Responders are classes which are instantiated for each request. They do
 * not have any state between requests and are therefore state-less in nature.
 * Many instances of a responder can be created which will require garbage
 * collecting so design your responder accordingly.
 *
 * <p>All responders should implement this interface to support requests.</p>
 *
 * <p>The Resource can contain important data for the operation of the
 * responder. This data is set when the routing was added to the server and
 * can be retrieved through the {@code initParameter} attribute. The
 * UriResponder must know beforehand the type of data placed in the
 * initialization attribute:<pre>
 * File baseDirectory = resource.initParameter( File.class );</pre>
 *
 * <p>The {@code initParameter} attribute is actually an array of objects which
 * the UriResponder can retrieve via index:<pre>
 * File baseDirectory = resource.initParameter( 0, File.class );</pre>
 */
public interface Responder {

  /**
   * Respond to the HTTP "delete" method requests.
   *
   * @param resource the instance of the Resource which contains our initialization parameters
   * @param urlParams parameters to process
   * @param session the session established with the HTTP server
   *
   * @return The response based on this method's processing
   */
  public Response delete(Resource resource, Map<String, String> urlParams, IHTTPSession session);




  /**
   * Respond to the HTTP "get" method requests.
   *
   * @param resource the instance of the Resource which contains our initialization parameters
   * @param urlParams parameters to process
   * @param session the session established with the HTTP server
   *
   * @return The response based on this method's processing
   */
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session);




  /**
   * Respond to the HTTP method requests which do not map to get, put, post or delete.
   *
   * @param resource the instance of the Resource which contains our initialization parameters
   * @param urlParams parameters to process
   * @param session the session established with the HTTP server
   *
   * @return The response based on this method's processing
   */
  public Response other(String method, Resource resource, Map<String, String> urlParams, IHTTPSession session);




  /**
   * Respond to the HTTP "post" method requests.
   *
   * @param resource the instance of the Resource which contains our initialization parameters
   * @param urlParams parameters to process
   * @param session the session established with the HTTP server
   *
   * @return The response based on this method's processing
   */
  public Response post(Resource resource, Map<String, String> urlParams, IHTTPSession session);




  /**
   * Respond to the HTTP "put" method requests.
   *
   * @param resource the instance of the Resource which contains our initialization parameters
   * @param urlParams parameters to process
   * @param session the session established with the HTTP server
   *
   * @return The response based on this method's processing
   */
  public Response put(Resource resource, Map<String, String> urlParams, IHTTPSession session);

}