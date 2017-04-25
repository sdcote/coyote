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
package coyote.dx.reader;

import coyote.dx.web.ResponseFuture;
import coyote.dx.web.ResponseFutureQueue;
import coyote.commons.network.http.responder.HTTPDRouter;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.TransactionContext;


/**
 * This reader stands a web server up at a particular port and sends any 
 * received data as a data frame through the transformation pipeline.
 * 
 * <p>Responses can be a default response code or a WebServerWriter can be 
 * used to post a response in the transaction context for the request 
 * thread to block on and return when ready.
 * 
 * <p>Note that the transform is a single thread and multiple requests result 
 * in reach request thread placing messages in a queue for serial processing. 
 * WebServerWriters then post results in the transaction context using the 
 * future object contained therein.
 * 
 * <p>This reader uses a WebRequestHandler to place ResponseFuture objects in 
 * a queue for processing which them block the request thread until a writer 
 * (or other component like a Listener) generates a response in the future and 
 * allows the request thread to respond. 
 */
public class WebServerReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  private static final int DEFAULT_PORT = 80;

  private static final String PORT = "Port";

  private HTTPDRouter server = null;

  private ResponseFutureQueue futureQueue = new ResponseFutureQueue( 1024 );




  @Override
  public DataFrame read( TransactionContext context ) {
    DataFrame retval = null;

    ResponseFuture future = null;
    while ( future == null ) {
      try {
        future = futureQueue.get( 1000 );
      } catch ( InterruptedException e ) {}

      if ( future == null ) {
        //Check to see if we should shutdown
      } else {
        // retrieve data from it
        // make sure it is valid - if not update the future with the error and go back to blocking
        // 
      }

    } // while - blocking read
    
    return retval;
  }




  @Override
  public boolean eof() {
    // TODO Auto-generated method stub
    return false;
  }

}
