/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and initial implementation
 */
package coyote.batch.web.worker;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import coyote.batch.web.Parameters;
import coyote.batch.web.Resource;
import coyote.batch.web.Response;
import coyote.loader.log.Log;


/**
 * This performs basic HTTP exchange, but marshals the data into a Document
 */
public class HtmlWorker extends BasicWorker implements ResourceWorker {

  /**
   * @param resource
   */
  public HtmlWorker( final Resource resource ) {
    super( resource );
  }




  /**
   * @see coyote.batch.web.worker.AbstractWorker#marshalRequestBody(org.apache.http.client.methods.HttpEntityEnclosingRequestBase, coyote.batch.web.Parameters)
   */
  @Override
  public void marshalRequestBody( final HttpEntityEnclosingRequestBase request, final Parameters params ) {
    // TODO: If there is data, make sure the Content-Type and Content-Encoding are set properly
    // request.setHeader( coyote.commons.network.http.HTTP.HDR_CONTENT_TYPE, "application/x-www-form-urlencoded" );

  }




  /**
   * @see coyote.batch.web.worker.AbstractWorker#marshalResponseBody(coyote.batch.web.Response, org.apache.http.HttpResponse, coyote.batch.web.Parameters)
   */
  @Override
  public void marshalResponseBody( final Response workerResponse, final HttpResponse httpResponse, final Parameters params ) {
    try {
      // Use Content-Encoding to properly parse the body
      final org.apache.http.entity.ContentType ctype = org.apache.http.entity.ContentType.getOrDefault( httpResponse.getEntity() );
      final String body = EntityUtils.toString( httpResponse.getEntity(), ctype.getCharset() );

      final Document document = Jsoup.parse( body );
      workerResponse.setDocument( document );
      return;
    } catch ( final Exception e ) {
      Log.error( "Problems parsing HTML: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
    }
    super.marshalResponseBody( workerResponse, httpResponse, params );
  }




  /**
   * @see coyote.batch.web.worker.ResourceWorker#setRequestHeaders(org.apache.http.HttpRequest, coyote.batch.web.Parameters)
   */
  @Override
  public void setRequestHeaders( final HttpRequest request, final Parameters params ) {
    request.setHeader( coyote.commons.network.http.HTTP.HDR_ACCEPT, "text/html, text/plain, */*" );
  }

}
