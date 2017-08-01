/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default threading strategy for HTTPD.
 * 
 * <p>By default, the server spawns a new Thread for every incoming request.
 * These are set to <i>daemon</i> status, and named according to the request
 * number. The name is useful when profiling the application.</p>
 */
public class DefaultExecutor implements Executor {

  /**The current number of requests received so far.*/
  private volatile long requestCount;

  /** The list of all the currently active requests for this server */
  private final List<ClientHandler> running = Collections.synchronizedList( new ArrayList<ClientHandler>() );




  /**
   * @see coyote.commons.network.http.Executor#closeAll()
   */
  @Override
  public void closeAll() {
    // copy of the list for concurrency
    for ( final ClientHandler clientHandler : new ArrayList<ClientHandler>( running ) ) {
      clientHandler.close();
    }
  }




  /**
   * @see coyote.commons.network.http.Executor#closed(coyote.commons.network.http.ClientHandler)
   */
  @Override
  public void closed( final ClientHandler clientHandler ) {
    running.remove( clientHandler );
  }




  @Override
  public void exec( final ClientHandler clientHandler ) {
    ++requestCount;
    final Thread t = new Thread( clientHandler );
    t.setDaemon( true );
    t.setName( "HTTPD Request(" + requestCount + ")" );
    running.add( clientHandler );
    t.start();
  }




  /**
   * @return a list with currently running clients.
   */
  public List<ClientHandler> getRunning() {
    return running;
  }
  
}