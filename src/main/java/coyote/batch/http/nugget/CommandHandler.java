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

import java.io.ByteArrayInputStream;
import java.util.Map;

import coyote.batch.Service;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.Auth;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.commons.network.http.nugget.UriResource;
import coyote.commons.network.http.nugget.UriResponder;
import coyote.loader.log.Log;
import coyote.loader.thread.Scheduler;


/**
 * This is the command handler for the management interface.
 */
public class CommandHandler extends AbstractBatchNugget implements UriResponder {

  private static final String SHUTDOWN = "shutdown";




  /**
   * @see coyote.commons.network.http.nugget.UriResponder#get(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  @Auth(groups = "devop,sysop", requireSSL = true)
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {

    // The first init parameter should be the service in which everything is running
    Service service = uriResource.initParameter( 0, Service.class );

    final String baseUri = uriResource.getUri();
    Log.append( HTTPD.EVENT, "BASE URI: '" + baseUri + "'" );

    String realUri = HTTPDRouter.normalizeUri( session.getUri() );
    Log.append( HTTPD.EVENT, "REAL URI: '" + realUri + "'" );

    for ( int index = 0; index < Math.min( baseUri.length(), realUri.length() ); index++ ) {
      if ( baseUri.charAt( index ) != realUri.charAt( index ) ) {
        realUri = HTTPDRouter.normalizeUri( realUri.substring( index ) );
        break;
      }
    }
    Log.append( HTTPD.EVENT, "NEXT URI: '" + realUri + "'" );

    String[] pathTokens = NuggetUtil.getPathArray( realUri );
    if ( SHUTDOWN.equalsIgnoreCase( pathTokens[0] ) ) {
      Log.append( HTTPD.EVENT, "Received a shutdown command" );
      // Create a Scheduled Job which will shutdown the service in a few seconds
      service.getScheduler().schedule( new ShutdownCmd(), System.currentTimeMillis() + 2000 );
    }

    String text = getText( urlParams, session );
    ByteArrayInputStream inp = new ByteArrayInputStream( text.getBytes() );
    int size = text.getBytes().length;

    Log.append( HTTPD.EVENT, "Sending a text response of " + size + " bytes" );

    return Response.createFixedLengthResponse( getStatus(), getMimeType(), inp, size );

  }




  public String getText( Map<String, String> urlParams, IHTTPSession session ) {
    String text = "<html><body>Command handler. Method: " + session.getMethod().toString() + "<br>";
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




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getStatus()
   */
  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getText()
   */
  @Override
  public String getText() {
    return "not implemented";
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultStreamHandler#getMimeType()
   */
  @Override
  public String getMimeType() {
    return MimeType.HTML.getType();
  }

  
  
  
  /**
   * 
   */
  private class ShutdownCmd implements Runnable {
    @Override
    public void run() {
      Log.append( Scheduler.SCHED, "Running shutdown command" );
      System.exit( 1 );
    }
  }

}
