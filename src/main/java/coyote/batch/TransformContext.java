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

  protected Map<String, Database> databases = new HashMap<String, Database>();

  protected TransformEngine engine = null;

  private volatile TransactionContext transactionContext = null;




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




  /**
   * @param engine the engine to which this context is associated.
   */
  public void setEngine( TransformEngine engine ) {
    this.engine = engine;
  }




  /**
   * @return the engine to which this context is associated.
   */
  protected TransformEngine getEngine() {
    return engine;
  }




  /**
   * Sets the current transaction in the transformation context.
   * 
   * @param context the current transaction context being processed
   */
  public void setTransaction( TransactionContext context ) {
    transactionContext = context;
  }




  /**
  * @return the current transaction context
  */
  public TransactionContext getTransaction() {
    return transactionContext;
  }

}
