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
package example.ws;

import java.io.IOException;

import coyote.commons.network.MimeType;
import coyote.dx.web.ExchangeType;
import coyote.dx.web.Method;
import coyote.dx.web.Parameters;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class ExportRequest {

  /**
   * @param args
   */
  public static void main( String[] args ) {
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.ALL_EVENTS ) );
    //Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( Log.ERROR_EVENTS | Log.FATAL_EVENTS ) ); 

    /**
     * filename Structure_04_26_2017_140410.cxf
     * filetype  cxf
     * json {"nodes":[{"id":"cdj84","sym":"C","pos":{"x":9971,"y":10004.5},"type":"ring_or_chain","nodeNumber":1 }],"bonds":[],"primaryValidationType":1,"secondaryValidationTypes":[],"sendingProgram":{"programName" :"SciFinder-NV","programVersion":"1.0"}}
     * 
     * application/x-www-form-urlencoded
     * filename=Structure_04_26_2017_140410.cxf&filetype=cxf&json=%7B%22nodes%22%3A%5B%7B%22id%22%3A%22cdj84%22%2C%22sym%22%3A%22C%22%2C%22pos%22%3A%7B%22x%22%3A9971%2C%22y%22%3A10004.5%7D%2C%22type%22%3A%22ring_or_chain%22%2C%22nodeNumber%22%3A1%7D%5D%2C%22bonds%22%3A%5B%5D%2C%22primaryValidationType%22%3A1%2C%22secondaryValidationTypes%22%3A%5B%5D%2C%22sendingProgram%22%3A%7B%22programName%22%3A%22SciFinder-NV%22%2C%22programVersion%22%3A%221.0%22%7D%7D
     */

    // This particular service takes a JSON request and returns a PDF file
    String resourceUrl = "http://sdc29-desktop.sf.cloud.cas.org:8080/api/structure-editor/structures/export?uiContext=375&uiSubContext=615&drawingToolId=614";
    String body = "filename=Structure_04_26_2017_140410.cxf&filetype=cxf&json=%7B%22nodes%22%3A%5B%7B%22id%22%3A%22cdj84%22%2C%22sym%22%3A%22C%22%2C%22pos%22%3A%7B%22x%22%3A9971%2C%22y%22%3A10004.5%7D%2C%22type%22%3A%22ring_or_chain%22%2C%22nodeNumber%22%3A1%7D%5D%2C%22bonds%22%3A%5B%5D%2C%22primaryValidationType%22%3A1%2C%22secondaryValidationTypes%22%3A%5B%5D%2C%22sendingProgram%22%3A%7B%22programName%22%3A%22SciFinder-NV%22%2C%22programVersion%22%3A%221.0%22%7D%7D";

    // Describe the parameters of our request
    Parameters requestParameters = new Parameters();
    requestParameters.setExchangeType( ExchangeType.BASIC );
    requestParameters.setMethod( Method.POST );
    requestParameters.setAcceptType( MimeType.ANY );
    requestParameters.setContentType( MimeType.APPLICATION_FORM );
    requestParameters.setBody( body );

    // Create a resource pointing to the given URL and using the request parameters as the default
    try (Resource resource = new Resource( resourceUrl, requestParameters )) {

      // Make a request of the resource using the set default parameters 
      Response response = resource.request();

      // Wait until the response is complete, it's running in a separate thread
      while ( !response.isComplete() ) {
        Thread.yield();
      }

      // Access the retrieved (PDF) data
      byte[] data = response.getData();
      System.out.println( "Received " + data.length + " bytes of data" );

    } catch ( IOException e ) {
      e.printStackTrace();
    }

  }

}
