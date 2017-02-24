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
package coyote.batch.http;

import java.util.Map;

import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.nugget.UriResource;

/**
 * 
 */
public class LogApiHandler extends AbstractBatchNugget {
  
  
  

  /**
   * Retrieve contents of a log if a log is identified.
   * Retrieve a list of logs if no log is identified.
   * 
   * @see coyote.batch.http.AbstractBatchNugget#get(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return super.get( uriResource, urlParams, session );
  }




  /**
   * Add entries to an identified log.
   * 
   * @see coyote.batch.http.AbstractBatchNugget#put(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response put( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return super.put( uriResource, urlParams, session );
  }


  /**
   * Delete an entire log.
   * 
   * @see coyote.batch.http.AbstractBatchNugget#delete(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response delete( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    // TODO Auto-generated method stub
    return super.delete( uriResource, urlParams, session );
  }







  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getStatus()
   */
  @Override
  public IStatus getStatus() {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getText()
   */
  @Override
  public String getText() {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultStreamHandler#getMimeType()
   */
  @Override
  public String getMimeType() {
    // TODO Auto-generated method stub
    return null;
  }

}
