/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http;

/**
 * HTTP Request methods, with the ability to decode a <code>String</code>
 * back to its enum value.
 */
public enum Method {
  GET, PUT, POST, DELETE, HEAD, OPTIONS, TRACE, CONNECT, PATCH, PROPFIND, PROPPATCH, MKCOL, MOVE, COPY, LOCK, UNLOCK;

  static Method lookup( final String method ) {
    if ( method == null ) {
      return null;
    }

    try {
      return valueOf( method );
    } catch ( final IllegalArgumentException e ) {
      // TODO: Log it?
      return null;
    }
  }
}