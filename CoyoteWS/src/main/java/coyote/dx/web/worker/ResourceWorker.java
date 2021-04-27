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
package coyote.dx.web.worker;

import java.io.Closeable;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import coyote.dx.web.InvocationException;
import coyote.dx.web.Parameters;
import coyote.dx.web.Response;


public interface ResourceWorker extends Closeable {

  /**
   * Marshal the data frame in the parameters into the appropriate body format.
   *
   * <p>The implementation is expected to set the body and possibly the
   * Content-Type headers.
   *
   * @param request the HTTP Request which is to be sent.
   * @param params the request parameters containing the payload data frame.
   */
  public void marshalRequestBody( HttpEntityEnclosingRequestBase request, Parameters params );




  /**
   * Marshal the HTTP response into the appropriate structure of the worker response.
   *
   * <p>This will allow each worker to parse the response into a usable object.
   * Normally this will be a data frame, but often it will be an HTML document.
   *
   * @param workerResponse the response from the worker
   * @param httpResponse the HTTP response from the remote resource
   * @param params the request parameters containing details of the request
   *        set by the caller.
   */
  void marshalResponseBody( Response workerResponse, HttpResponse httpResponse, Parameters params );




  /**
   * Make a request of the resource using the given parameters.
   *
   * @param params the parameters of the request.
   *
   * @return the response to the request
   *
   * @throws InvocationException if problems were encountered during the invocation
   */
  Response request( Parameters params ) throws InvocationException;




  /**
   * Send a message (i.e. event) to the resource using the given parameters.
   *
   * <p>No response is expected as this is an asynchronous message.</p>
   *
   * @param params the parameters of the event to send
   */
  void send( Parameters params );




  /**
   * Set the request headers.
   *
   * <p>This is the last step before the request is sent and the worker is
   * given the chance to alter the request headers as necessary to ensure the
   * server can properly process the body. This normally involves adding
   * Content-Type and Content-Encoding headers.
   *
   * @param request the HTTP Request which is to be sent and to which headers
   *        are to be added.
   * @param params the request parameters containing details of the request
   *        set by the caller.
   */
  void setRequestHeaders( HttpRequest request, Parameters params );

}
