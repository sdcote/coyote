/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

/**
 * Some HTTP response status codes
 */
public enum Status implements IStatus {
  SWITCH_PROTOCOL( 101, "Switching Protocols"), 
  OK( 200, "OK"), 
  CREATED( 201, "Created"), 
  ACCEPTED( 202, "Accepted"), 
  NO_CONTENT( 204, "No Content"), 
  PARTIAL_CONTENT( 206, "Partial Content"), 
  MULTI_STATUS( 207, "Multi-Status"), 
  REDIRECT( 301, "Moved Permanently"), 
  REDIRECT_TEMPORARY( 302, "Found"),
  REDIRECT_SEE_OTHER( 303, "See Other"), 
  NOT_MODIFIED( 304, "Not Modified"), 
  BAD_REQUEST( 400, "Bad Request"), 
  UNAUTHORIZED( 401, "Unauthorized"), 
  FORBIDDEN( 403, "Forbidden"), 
  NOT_FOUND( 404, "Not Found"), 
  METHOD_NOT_ALLOWED( 405, "Method Not Allowed"), 
  NOT_ACCEPTABLE( 406, "Not Acceptable"), 
  REQUEST_TIMEOUT( 408, "Request Timeout"), 
  CONFLICT( 409, "Conflict"), 
  RANGE_NOT_SATISFIABLE( 416, "Requested Range Not Satisfiable"), 
  INTERNAL_ERROR( 500, "Internal Server Error"), 
  NOT_IMPLEMENTED( 501, "Not Implemented"), 
  UNSUPPORTED_HTTP_VERSION( 505, "HTTP Version Not Supported");

  private final int requestStatus;

  private final String description;




  Status( final int requestStatus, final String description ) {
    this.requestStatus = requestStatus;
    this.description = description;
  }




  @Override
  public String getDescription() {
    return "" + requestStatus + " " + description;
  }




  @Override
  public int getRequestStatus() {
    return requestStatus;
  }

}