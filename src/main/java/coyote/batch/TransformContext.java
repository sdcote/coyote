/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;


/**
 * This is an operational context for the transformation job as a whole.
 */
public class TransformContext extends OperationalContext {

  Map<String, Database> databases = new HashMap<String, Database>();




  public TransformContext( List<ContextListener> listeners ) {
    super( listeners );
  }




  public TransformContext() {
    super();
  }




  public void addDataStore( Database store ) {
    if ( store != null && StringUtil.isNotBlank( store.getName() ) ) {
      databases.put( store.getName(), store );
    }
  }




  public Database getDatabase( String name ) {
    return databases.get( name );
  }




  /**
   * Open (initialize) the context
   */
  public void open() {

  }




  /**
   * Close (terminate) the context.
   */
  public void close() {
    for ( Map.Entry<String, Database> entry : databases.entrySet() ) {
      //System.out.printf("Closing : %s %n", entry.getKey());
      try {
        entry.getValue().close();
      } catch ( IOException ignore ) {
        //System.out.printf("Problems closing : %s - %s %n", entry.getKey(), ignore.getMessage());
      }
    }
  }

}
