/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.web.worker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTP;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.web.Parameters;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.loader.log.Log;


/**
 * This worker sends data to the server using basic HTTP expecting a JSON 
 * formatted response.
 * 
 */
public class JsonHttpWorker extends AbstractWorker implements ResourceWorker {

  /**
   * @param resource the resource this worker is to manage
   */
  public JsonHttpWorker(Resource resource) {
    super(resource);
  }




  /**
   * This marshals the body into a UrlEncodedFormEntity with each field placed as a URL parameter.
   * 
   * @see coyote.dx.web.worker.AbstractWorker#marshalRequestBody(org.apache.http.client.methods.HttpEntityEnclosingRequestBase, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalRequestBody(final HttpEntityEnclosingRequestBase request, final Parameters params) {
    StringBuffer body = new StringBuffer();
    if (params.getPayload() != null) {
      DataFrame payload = params.getPayload();
      if (payload.getFieldCount() > 0) {
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (DataField field : payload.getFields()) {
          if (field.isFrame()) {
            Log.error("Cannot add object to request body - field name: " + field.getName() + " - " + field.getStringValue());
          } else {
            // TODO: marshal values against context variables and symbol table
            formParams.add(new BasicNameValuePair(field.getName(), field.getStringValue()));
          }
        }
        try {
          request.setEntity(new UrlEncodedFormEntity(formParams));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        request.setHeader(HTTP.HDR_CONTENT_TYPE, MimeType.APPLICATION_FORM.getType());
      }
    }
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#setRequestHeaders(org.apache.http.HttpRequest, coyote.dx.web.Parameters)
   */
  @Override
  public void setRequestHeaders(HttpRequest request, Parameters params) {
    request.setHeader(HTTP.HDR_ACCEPT, "application/json, text/plain, */*");
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#marshalResponseBody(coyote.dx.web.Response, org.apache.http.HttpResponse, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalResponseBody(final Response workerResponse, final HttpResponse httpResponse, final Parameters params) {
    try {
      final org.apache.http.entity.ContentType ctype = org.apache.http.entity.ContentType.getOrDefault(httpResponse.getEntity());
      final String body = EntityUtils.toString(httpResponse.getEntity(), ctype.getCharset());
      log.debug(this.getClass().getSimpleName() + " marshaling response body of '%s%s", body.substring(0, body.length() > 500 ? 500 : body.length()), body.length() <= 500 ? "'" : " ...'");

      workerResponse.setBody(body);
      final List<DataFrame> responseFrames = JSONMarshaler.marshal(body);

      if (responseFrames.size() > 0) {
        workerResponse.setResult(responseFrames.get(0));
      }
      return;
    } catch (final Exception e) {
      Log.error(e.getMessage());
    }
    super.marshalResponseBody(workerResponse, httpResponse, params);
  }

}
