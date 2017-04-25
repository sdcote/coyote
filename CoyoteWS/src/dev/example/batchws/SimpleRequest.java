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
package example.batchws;

import org.jsoup.nodes.Document;

import coyote.batch.web.Resource;
import coyote.batch.web.Response;


/**
 * This simple retrieves HTML from a well known site and returns a DOM. 
 */
public class SimpleRequest {

  /**
   * @param args
   */
  public static void main( String[] args ) {

    // Setup a Location of a service; described by its URL
    try (Resource resource = new Resource( "http://www.google.com/" )) {

      // invoke the operation and receive an object representing our results
      final Response response = resource.request();

      // wait for results (invocation may be asynchronous)
      while ( !response.isComplete() ) {
        if ( response.isTimedOut() ) {
          // nothing happened
          System.err.println( "Operation timed-out" );
          System.exit( 1 );
        } else if ( response.isInError() ) {
          // we received one or more errors
          System.err.println( "Operation failed" );
          System.exit( 2 );
        } else {
          // wait for the results to arrive
          Thread.yield();
        }
      }

      // Get the document object model
      Document dom = response.getDocument();
      
      // print it out
      System.out.println( dom.toString() );

    } catch ( final Exception e ) {
      System.err.println( e.getMessage() );
    }

  }

}
