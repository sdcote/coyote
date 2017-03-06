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

import coyote.batch.Service;
import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.Auth;
import coyote.commons.network.http.nugget.UriResource;
import coyote.commons.network.http.nugget.UriResponder;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.loader.log.Log;
import coyote.loader.thread.Scheduler;


/**
 * This is the command handler for the management interface.
 */
public class CommandHandler extends AbstractBatchNugget implements UriResponder {

  private static final String SHUTDOWN = "shutdown";

  Status status = Status.OK;

  private final DataFrame results = new DataFrame();




  /**
   * @see coyote.commons.network.http.nugget.UriResponder#get(coyote.commons.network.http.nugget.UriResource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  @Auth(groups = "devop,sysop", requireSSL = true)
  public Response get( UriResource uriResource, Map<String, String> urlParams, IHTTPSession session ) {

    // The first init parameter should be the service in which everything is running
    Service service = uriResource.initParameter( 0, Service.class );

    // Get the command from the URL parameters specified when we were registered with the router 
    String command = urlParams.get( "command" );

    // Process the command
    if ( StringUtil.isNotBlank( command ) ) {
      results.put( "command", command );
      switch ( command ) {
        case SHUTDOWN:
          // Create a Scheduled Job which will shutdown the service in a few seconds
          service.getScheduler().schedule( new ShutdownCmd(), System.currentTimeMillis() + 2000 );
          results.put( "result", "success" );
          break;
        default:
          results.put( "result", "Unknown command" );
      }
    } else {
      results.put( "result", "No command found" );
    }

    // Send the result
    return Response.createFixedLengthResponse( getStatus(), getMimeType(), getText() );
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getStatus()
   */
  @Override
  public IStatus getStatus() {
    return status;
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultHandler#getText()
   */
  @Override
  public String getText() {
    return JSONMarshaler.marshal( results );
  }




  /**
   * @see coyote.commons.network.http.nugget.DefaultStreamHandler#getMimeType()
   */
  @Override
  public String getMimeType() {
    return MimeType.JSON.getType();
  }

  //
  
  //
  
  //
  
  //
  
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
