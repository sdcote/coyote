/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

import java.io.OutputStream;

/**
 * A temporary cache file.
 *
 * <p>Cache files are responsible for managing the temporary storage  of large 
 * amounts of data received by the server.
 */
public interface CacheFile {

  public void delete() throws Exception;




  public String getName();




  public OutputStream open() throws Exception;
}