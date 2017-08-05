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
package coyote.commons;

import java.util.Map;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.DefaultResponder;
import coyote.commons.network.http.responder.Resource;
import coyote.loader.cfg.Config;


/**
 * A simple handler for testing
 */
public class TestHandler extends DefaultResponder {
  private String testData = "Hello";




  @Override
  public Response get( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {

    // These initialization parameters always exist
    WebServer loader = resource.initParameter( 0, WebServer.class );
    Config config = resource.initParameter( 1, Config.class );

    // if there are more, then assume the next one is our test data
    if ( resource.getInitParameterLength() > 2 ) {
      if ( resource.initParameter( 2, Object.class ) instanceof String ) {
        testData = resource.initParameter( 2, String.class );
      }
    }

    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  @Override
  public String getMimeType() {
    return MimeType.TEXT.getType();
  }




  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  @Override
  public String getText() {
    return testData;
  }

}