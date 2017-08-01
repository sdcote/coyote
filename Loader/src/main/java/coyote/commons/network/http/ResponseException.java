/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

public final class ResponseException extends Exception {

  private static final long serialVersionUID = 3423102004759131858L;

  private final Status status;




  public ResponseException( final Status status, final String message ) {
    super( message );
    this.status = status;
  }




  public ResponseException( final Status status, final String message, final Exception e ) {
    super( message, e );
    this.status = status;
  }




  public Status getStatus() {
    return status;
  }
}