/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;


/**
 * Default strategy for creating and cleaning up temporary files required by 
 * requests.
 */
class DefaultCacheManagerFactory implements CacheManagerFactory {

  @Override
  public CacheManager create() {
    return new DefaultCacheManager();
  }
}