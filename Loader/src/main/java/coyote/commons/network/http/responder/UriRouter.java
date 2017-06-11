/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http.responder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SecurityResponseException;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.loader.log.Log;


/**
 * This is the heart of the URI routing mechanism in a routing HTTP server.
 */
public class UriRouter {

  private final List<Resource> mappings;

  private Resource error404Url;

  private Class<?> notImplemented;




  /**
   * Default constructor
   */
  public UriRouter() {
    mappings = new ArrayList<Resource>();
  }




  /**
   * Add a route to this router with the responder class for that route.
   * 
   * @param url the regex to match against the request URL
   * @param priority the priority in which the router will check the route, 
   *        lower values return before larger priorities.
   * @param responder the responder class for this mapping. If null, the 
   *        NotImplemented responder will be used.
   * @param authProvider the auth provider the URI resource should use for 
   *        this route
   * @param initParameter the initialization parameters for the responder when 
   *        it receives a request.
   */
  void addRoute( final String url, final int priority, final Class<?> responder, final AuthProvider authProvider, final Object... initParameter ) {
    if ( url != null ) {
      if ( responder != null ) {
        mappings.add( new Resource( url, priority + mappings.size(), responder, authProvider, initParameter ) );
      } else {
        mappings.add( new Resource( url, priority + mappings.size(), notImplemented, authProvider ) );
      }
      sortMappings();
    }
  }




  /**
   * Search in the mappings if the given request URI matches some of the rules.
   * 
   * <p>If there are more than one match, this returns the rule with least 
   * parameters. For example: mapping 1 = /user/:id  - mapping 2 = /user/help. 
   * If the incoming URI is www.example.com/user/help - mapping 2 is returned. 
   * If the incoming URI is www.example.com/user/3232 - mapping 1 is 
   * returned.</p>
   * 
   * @param session the HTTP session encapsulating the request
   * 
   * @return the Response from the URI resource processing
   * 
   * @throws SecurityResponseException if processing request generated a security exception
   */
  public Response process( final IHTTPSession session ) throws SecurityResponseException {

    final String request = HTTPDRouter.normalizeUri( session.getUri() );

    Map<String, String> params = null;
    Resource retval = error404Url;

    // For all the resources, see which one matches first
    for ( final Resource resource : mappings ) {
      params = resource.match( request );
      if ( params != null ) {
        retval = resource;
        break;
      }
    }

    if ( Log.isLogging( HTTPD.EVENT ) ) {
      if ( error404Url == retval ) {
        Log.append( HTTPD.EVENT, "No responder defined for '" + request + "' from " + session.getRemoteIpAddress() + ":" + session.getRemoteIpPort() );
      } else {
        Log.append( HTTPD.EVENT, "Resource '" + retval + "' servicing '" + session.getMethod() + "' request for '" + request + "' from " + session.getRemoteIpAddress() + ":" + session.getRemoteIpPort() );
      }
    }
    // Have the found (or default 404) URI resource process the session
    return retval.process( params, session );
  }




  void removeRoute( final String url ) {
    final String uriToDelete = HTTPDRouter.normalizeUri( url );
    final Iterator<Resource> iter = mappings.iterator();
    while ( iter.hasNext() ) {
      final Resource resource = iter.next();
      if ( uriToDelete.equals( resource.getUri() ) ) {
        iter.remove();
        break;
      }
    }
  }




  public void setNotFoundResponder( final Class<?> responder ) {
    error404Url = new Resource( null, 100, responder, null );
  }




  public void setNotImplementedResponder( final Class<?> responder ) {
    notImplemented = responder;
  }




  /**
   * @return the list of URI resource objects responsible for handling 
   *         requests of the server.
   */
  public List<Resource> getMappings() {
    return mappings;
  }




  private void sortMappings() {
    Collections.sort( mappings, new Comparator<Resource>() {

      @Override
      public int compare( final Resource o1, final Resource o2 ) {
        return o1.priority - o2.priority;
      }
    } );
  }

}