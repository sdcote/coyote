/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http;

/**
 * Factory to create cache managers.
 */
public interface CacheManagerFactory {

  /**
   * @return a new cache manager.
   */
  public CacheManager create();
  
}