/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;


/**
 * 
 */
public class CategoryLoggerFactory implements ILoggerFactory {

  private ConcurrentMap<String, Logger> loggerMap;




  public CategoryLoggerFactory() {
    loggerMap = new ConcurrentHashMap<String, Logger>();
  }




  /**
   * Return an appropriate {@link CategoryLogger} instance by name.
   */
  @Override
  public Logger getLogger( String name ) {
    Logger simpleLogger = loggerMap.get( name );
    if ( simpleLogger != null ) {
      return simpleLogger;
    } else {
      Logger newInstance = new CategoryLogger( name );
      Logger oldInstance = loggerMap.putIfAbsent( name, newInstance );
      return oldInstance == null ? newInstance : oldInstance;
    }
  }

}