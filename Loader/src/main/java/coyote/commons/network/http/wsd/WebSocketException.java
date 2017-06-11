/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http.wsd;

import java.io.IOException;

import coyote.commons.network.http.wsd.WebSocketFrame.CloseCode;


public class WebSocketException extends IOException {

  private static final long serialVersionUID = 777960923156306743L;
  private final WebSocketFrame.CloseCode code;
  private final String reason;




  public WebSocketException( WebSocketFrame.CloseCode code, String reason ) {
    this( code, reason, null );
  }




  public WebSocketException( WebSocketFrame.CloseCode code, String reason, Exception cause ) {
    super( code + ": " + reason, cause );
    this.code = code;
    this.reason = reason;
  }




  public WebSocketException( Exception cause ) {
    this( CloseCode.InternalServerError, cause.toString(), cause );
  }




  public WebSocketFrame.CloseCode getCode() {
    return this.code;
  }




  public String getReason() {
    return this.reason;
  }

}