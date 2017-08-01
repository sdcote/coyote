/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

/**
 * Run client handlers asynchronously with any object implementing this 
 * interface.
 */
public interface Executor {

  /**
   * Close all the currently active client handlers (i.e. requests) currently 
   * in the server.
   */
  void closeAll();




  /**
   * Remove the given chandler from the collection of currently active requests 
   * as it has been closed.
   * 
   * @param handler the client handler to close
   */
  void closed( ClientHandler handler );




  /**
   * Start the given handler running in its own thread.
   * 
   * @param handler the client handler to run.
   */
  void exec( ClientHandler handler );

}