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
package demo.proxy;

import coyote.dx.web.Parameters;
import coyote.dx.web.Proxy;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.BasicAuthentication;
import coyote.dataframe.DataFrame;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * This is one of the more complex examples. 
 * 
 * <p>We are calling SOAP web services (complex in itself) through a NTLM proxy 
 * which requires authentication.</p> 
 * 
 * <p>We are also calling an operation multiple request payloads, basically 
 * calling the operation multiple times with one request.</p>
 * 
 * <p>The SOAP service requires Basic Authentication which we provide 
 * preemptively.</p>
 */
public class InsertMultipleTest {

  /**
   * @param args
   */
  public static void main( String[] args ) {
    // Add a logger that will send log messages to the console 
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS ) );

    // This is some basic information about the web service we are going to use
    String url = "https://nwdevelopment.service-now.com/u_incident_import.do?SOAP";
    String user = "sgSNtest";
    String credential = "Password1";

    // we define some proxy settings
    Proxy proxy = new Proxy();
    proxy.setHost( "http-proxy.somecompany.net" );
    proxy.setPort( 8080 );
    proxy.setUsername( "proxyuser" );
    proxy.setPassword( "5eCr3t^" );
    proxy.setDomain( "CORP" );

    // Create a set of default protocol settings
    Parameters protocol = new Parameters();
    protocol.setSoapOperation( "insertMultiple" );
    //protocol.setSoapNamespace( "u", "http://www.service-now.com/u_incident_import" );

    // Setup a resource to call; described by its URL, using SOAP and accessible through a proxy
    try (Resource resource = new Resource( url, protocol, proxy )) {

      // This resource uses RFC 2617 basic authentication
      Authenticator auth = new BasicAuthentication( user, credential );
      auth.setPreemptiveAuthentication( true );
      resource.setAuthenticator( auth );

      // initialize the resource performing any required authorization processing 
      resource.open();
      
      // Set up an object to hold our request parameters these will over-ride 
      // the default protocol parameters
      Parameters params = new Parameters();

      // Create our payload - multiple calls to the operation
      DataFrame payload = new DataFrame();

      // the names of the fields may need the namespace defined above, we don't
      DataFrame frame = new DataFrame();
      frame.add( "number", "INC0012578" );
      frame.add( "u_external_system", "SARCOM" );
      frame.add( "u_extrenal_system_id", "6af7f19f" );
      payload.add( "record", frame );

      frame = new DataFrame();
      frame.add( "number", "INC0016578" );
      frame.add( "u_external_system", "SARCOM" );
      frame.add( "u_extrenal_system_id", "6af7f1tf" );
      payload.add( "record", frame );

      // Place the request payload in the request parameters
      params.setPayload( payload );

      // invoke the operation and receive an object representing our results
      final Response response = resource.request( params );

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
          response.wait( 100 );
        }
      }

      // get the body of the response
      DataFrame result = response.getResult();

      // print it out
      System.out.println( result.toString() );
    } catch ( final Exception e ) {
      System.err.println( e.getMessage() );
    }

  }
}
