/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.http.responder;

import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.auth.Auth;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.Service;
import coyote.loader.log.Log;
import coyote.loader.thread.Scheduler;


/**
 * This is the command responder for the management interface.
 */
@Auth(groups = "devop,sysop", requireSSL = true)
public class CommandResponder extends AbstractBatchResponder implements Responder {

  private static final String SHUTDOWN = "shutdown";




  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {

    // The first init parameter should be the service in which everything is running
    Service service = resource.initParameter(0, Service.class);

    // Get the command from the URL parameters specified when we were registered with the router 
    String command = urlParams.get("command");

    // Process the command
    if (StringUtil.isNotBlank(command)) {
      results.put("command", command);
      switch (command) {
        case SHUTDOWN:
          // Create a Scheduled Job which will shutdown the service in a few seconds
          service.getScheduler().schedule(new ShutdownCmd(), System.currentTimeMillis() + 2000);
          results.put("result", "success");
          break;
        default:
          results.put("result", "Unknown command");
          break;
      }
    } else {
      results.put("result", "No command found");
    }

    // Send the result
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }

  // 

  // The classes below are "runnables" which will be placed in the scheduler to 
  // execute the commands supported by the API.

  //

  /**
   * This command simply exits the JVM.
   *  
   * <p>Shutdown hooks in the loader will gracefully shut everything down.
   */
  private class ShutdownCmd implements Runnable {
    @Override
    public void run() {
      Log.append(Scheduler.SCHED, "Running shutdown command");
      System.exit(1);
    }
  }

}
