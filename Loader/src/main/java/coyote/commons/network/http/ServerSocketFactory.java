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
 * Factory to create ServerSocketFactories.
 */
public interface ServerSocketFactory {

  public ServerSocket create() throws IOException;

}