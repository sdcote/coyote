/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

/**
 * Cache file manager.
 * 
 * <p>Cache file managers are created 1-to-1 with incoming requests, to create
 * and cleanup cache files created as a result of handling the request. For 
 * example, when files are uploaded to the server and are determined to be too 
 * large to fit safely into memory, the Cache Manager is used to create and 
 * later delete the files when the connection closes.
 */
public interface CacheManager {

  /**
   * Delete all the cache files created.
   * 
   * <p>This can be called at any time, but usually during connection close.
   */
  void clear();




  /**
   * Create a cache file using the given tag in its naming.
   * 
   * @param tag a name to use in the creation of the cache file
   * 
   * @return instance of the cache file created.
   * 
   * @throws Exception if there were problems creating the file.
   */
  public CacheFile createCacheFile( String tag ) throws Exception;

}