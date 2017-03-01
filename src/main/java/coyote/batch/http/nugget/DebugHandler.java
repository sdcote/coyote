/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.util.Map;

import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.nugget.DefaultHandler;
import coyote.commons.network.http.nugget.UriResource;


public class DebugHandler extends DefaultHandler {

  @Override
  public String getText() {
    return "not implemented";
  }




  public String getText( Map<String, String> urlParams, IHTTPSession session ) {
    String text = "<html><body>Debug handler. Method: " + session.getMethod().toString() + "<br>";
    text += "<h1>Uri parameters:</h1>";
    for ( Map.Entry<String, String> entry : urlParams.entrySet() ) {
      String key = entry.getKey();
      String value = entry.getValue();
      text += "<div> Param: " + key + "&nbsp;Value: " + value + "</div>";
    }
    text += "<h1>Query parameters:</h1>";
    for ( Map.Entry<String, String> entry : session.getParms().entrySet() ) {
      String key = entry.getKey();
      String value = entry.getValue();
      text += "<div> Query Param: " + key + "&nbsp;Value: " + value + "</div>";
    }
    text += "</body></html>";

    return text;
  }




  @Override
  public String getMimeType() {
    return "text/html";
  }




  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {
    String text = getText( urlParams, session );
    ByteArrayInputStream inp = new ByteArrayInputStream( text.getBytes() );
    int size = text.getBytes().length;
    return HTTPD.newFixedLengthResponse( getStatus(), getMimeType(), inp, size );
  }

}