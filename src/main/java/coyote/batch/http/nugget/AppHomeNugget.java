/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http.nugget;

import java.util.Map;

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.nugget.UriResource;
import coyote.commons.network.http.nugget.UriResponder;


/**
 * This nugget give access to resources in app.home
 */
public class AppHomeNugget implements UriResponder {

  /**
   * @see coyote.commons.network.http.nugget.UriResponder#delete(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.commons.network.http.nugget.UriResponder#get(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.commons.network.http.nugget.UriResponder#other(java.lang.String, coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response other( String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.commons.network.http.nugget.UriResponder#post(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response post( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.commons.network.http.nugget.UriResponder#put(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return null;
  }

}
