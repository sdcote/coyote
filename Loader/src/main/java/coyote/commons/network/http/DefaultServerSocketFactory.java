/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Creates a normal ServerSocket for TCP connections
 */
public class DefaultServerSocketFactory implements ServerSocketFactory {

  @Override
  public ServerSocket create() throws IOException {
    return new ServerSocket();
  }

}