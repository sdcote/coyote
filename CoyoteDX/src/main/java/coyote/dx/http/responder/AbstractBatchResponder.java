/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.util.Map;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.DefaultResponder;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.XMLMarshaler;


/**
 * Base class for all DX responders.
 */
public abstract class AbstractBatchResponder extends DefaultResponder implements Responder {

  protected Status status = Status.OK;
  protected DataFrame results = new DataFrame();
  protected MimeType mimetype = MimeType.JSON;




  @Override
  public Response delete(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response other(String method, Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response post(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response put(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public IStatus getStatus() {
    return status;
  }




  @Override
  public String getText() {
    if (mimetype.equals(MimeType.XML)) {
      return XMLMarshaler.marshal(results);
    } else {
      return JSONMarshaler.marshal(results);
    }
  }




  @Override
  public String getMimeType() {
    return mimetype.getType();
  }




  /**
   * @return the results
   */
  protected DataFrame getResults() {
    return results;
  }




  /**
   * @param results the results to set
   */
  protected void setResults(DataFrame results) {
    this.results = results;
  }




  /**
   * @param status the status to set
   */
  protected void setStatus(Status status) {
    this.status = status;
  }




  /**
   * @param type the mimetype to set
   */
  protected void setMimetype(MimeType type) {
    mimetype = type;
  }

}
