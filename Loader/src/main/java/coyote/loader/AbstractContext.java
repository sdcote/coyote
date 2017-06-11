/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.Assert;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;


/**
 * This is a context base class providing basic contextual functions.
 * 
 * <p>This context allows components to share data amongst each other while 
 * remaining functionally separate and decoupled.</p>
 */
public abstract class AbstractContext implements Context {
  public static final String ERROR_STATUS = "Error";
  protected String status = null;
  protected String message = null;
  protected volatile long startTime = 0;
  protected volatile long endTime = 0;
  protected final Map<String, Object> properties = new HashMap<String, Object>();
  protected AbstractContext parent = null;

  /** Flag indicating the context is in errorFlag */
  protected volatile boolean errorFlag = false;

  /** List of listeners which will do something when different events happen. */
  List<ContextListener> listeners = new ArrayList<ContextListener>();

  SymbolTable symbols = null;




  public AbstractContext() {}




  public AbstractContext( List<ContextListener> listeners ) {
    setListeners( listeners );
  }




  /**
   * @return the symbolTable for this context
   */
  public synchronized SymbolTable getSymbols() {
    if ( symbols == null ) {
      symbols = new SymbolTable();
    }
    return symbols;
  }




  /**
   * @param symbols the symbols to set in this context
   */
  public synchronized void setSymbols( SymbolTable symbols ) {
    this.symbols = symbols;
  }




  /**
   * Return the object from this context with the given (case sensitive) key.
   * 
   * @param key the name of the object to return
   * 
   * @return the object with that name or null if the named object is not found
   */
  public Object get( String key ) {
    return get( key, true );
  }




  /**
   * Performs a search for a property with the given name taking case into 
   * account.
   * 
   * @param key the key for which to search
   * @param usecase true to indicate case sensitive, false to perform a case 
   *        insensitive search.
   * 
   * @return the value of the property, or null if the property was not found 
   *         with the given key of if the key was null or blank.
   */
  public synchronized Object get( String key, boolean usecase ) {
    if ( StringUtil.isNotBlank( key ) ) {
      if ( usecase ) {
        return properties.get( key );
      } else {
        for ( Map.Entry<String, Object> entry : properties.entrySet() ) {
          if ( key.equalsIgnoreCase( entry.getKey() ) ) {
            if ( entry.getValue() != null ) {
              return entry.getValue().toString();
            }
            // don't break; keep looking in case there is another non-null match
          } // if match
        } // for
      } // else
    } // key is not blank
    return null;
  }




  /**
   * Return the string representation of the object from this context with the 
   * given key.
   * 
   * <p>This is a convenience method for calling {@code toString()} on the 
   * returned value after having checked for null.</p>
   * 
   * @param key the name of the object to return
   * 
   * @return the string representation of the object with that name or null if 
   *         the named object is not found
   */
  public String getAsString( String key ) {
    Object retval = get( key );
    if ( retval != null ) {
      return retval.toString();
    }
    return null;
  }




  /**
   * Return the string representation of the object from this context with the 
   * given key taking case into account.
   * 
   * <p>This is a convenience method for calling {@code toString()} on the 
   * returned value after having checked for null.</p>
   * 
   * @param key the name of the object to return
   * @param usecase true to perform a case sensitive search, false to perform a 
   *        case insensitive search.
   * 
   * @return the string representation of the object with that name or null if 
   *         the named object is not found
   */
  public String getAsString( String key, boolean usecase ) {
    Object retval = get( key, usecase );
    if ( retval != null ) {
      return retval.toString();
    }
    return null;
  }




  /**
   * Place an object in this context with the given key
   * 
   * @param key the name of the object to place
   * @param value the object to place (null results in the object being removed)
   */
  public synchronized void set( String key, Object value ) {
    if ( key != null ) {
      if ( value != null ) {
        properties.put( key, value );
      } else {
        properties.remove( key );
      }
    }
  }




  public synchronized void setError( boolean flag ) {
    errorFlag = flag;
  }




  /**
   * Set the context in an error status with the given message.
   * 
   * @param msg The message to place in the context.
   */
  public synchronized void setError( String msg ) {
    errorFlag = true;
    status = ERROR_STATUS;
    message = msg;
  }




  public synchronized boolean isInError() {
    return errorFlag;
  }




  /**
   * Accessor to check if the context is not in error (i.e. fine).
   * 
   * <p>This is a convenience method to make code more readable</p>
   * 
   * @return true if the context is fine (without error) false if there is a problem.
   */
  public synchronized boolean isNotInError() {
    return !errorFlag;
  }




  /**
   * Access the current status token/tag of this context.
   * 
   * @return the status of this context
   */
  public synchronized String getStatus() {
    return status;
  }




  /**
   * Set a token/tag indicating the status of this context.
   * 
   * @param status the status to set
   */
  public synchronized void setStatus( String status ) {
    this.status = status;
  }




  /**
   * @return the message describing the status of this context
   */
  public synchronized String getMessage() {
    return message;
  }




  /**
   * @param message the message to set
   */
  public synchronized void setMessage( String message ) {
    this.message = message;
  }




  /**
   * @return the startTime
   */
  public synchronized long getStartTime() {
    return startTime;
  }




  /**
   * @param startTime the startTime to set
   */
  public synchronized void setStartTime( long startTime ) {
    this.startTime = startTime;
  }




  /**
   * @return the endTime
   */
  public synchronized long getEndTime() {
    return endTime;
  }




  /**
   * @param endTime the endTime to set
   */
  public synchronized void setEndTime( long endTime ) {
    this.endTime = endTime;
  }




  /**
   * @return the elapsed time in milliseconds from start to end (or now) or 0 if not started. 
   */
  public synchronized long getElapsed() {

    if ( startTime != 0 ) {
      if ( endTime != 0 ) {
        return endTime - startTime;
      } else {
        return System.currentTimeMillis() - startTime;
      }
    } else
      return 0;
  }




  /**
   * Set the start time to now.
   */
  public synchronized void start() {
    startTime = System.currentTimeMillis();
    fireStart( this );
  }




  /**
   * Set the end time to now.
   */
  public synchronized void end() {
    endTime = System.currentTimeMillis();
    fireEnd( this );
  }




  protected void fireStart( AbstractContext context ) {
    if ( parent != null )
      parent.fireStart( context );

    for ( ContextListener listener : listeners ) {
      listener.onStart( context );
    }
  }




  protected void fireEnd( AbstractContext context ) {
    if ( parent != null )
      parent.fireEnd( context );

    for ( ContextListener listener : listeners ) {
      listener.onEnd( context );
    }
  }




  /**
   * Fire a contextual event for all the listeners of this context and its 
   * parents.
   * 
   * @param event The event to send to context listeners - must not be null
   */
  public void fireEvent( ContextEvent event ) {
    Assert.notNull( event );
    if ( parent != null )
      parent.fireEvent( event );

    for ( ContextListener listener : listeners ) {
      listener.onEvent( event );
    }
  }




  public synchronized void setListeners( List<ContextListener> listeners ) {
    if ( listeners != null ) {
      this.listeners = listeners;
    }
  }




  public synchronized void addListener( ContextListener listener ) {
    if ( listener != null ) {
      if ( listeners == null ) {
        listeners = new ArrayList<ContextListener>();
      }
      listeners.add( listener );
    }
  }




  /**
   * @see coyote.loader.Context#dump()
   */
  @Override
  public String dump() {
    StringBuffer b = new StringBuffer( "Context Properties:" );
    b.append( StringUtil.LINE_FEED );
    for ( String key : properties.keySet() ) {
      b.append( "'" );
      b.append( key );
      b.append( "' = " );
      Object value = properties.get( key );
      if ( value != null )
        b.append( value.toString() );
      else
        b.append( "null" );
      b.append( StringUtil.LINE_FEED );
    }
    b.append( StringUtil.LINE_FEED );
    if ( symbols != null ) {
      b.append( "Symbol Table:" );
      b.append( StringUtil.LINE_FEED );
      b.append( symbols.dump() );
    } else {
      b.append( "No Symbol Table Found" );
    }
    return b.toString();
  }




  /**
   * @return the parent context, may be null.
   */
  public AbstractContext getParent() {
    return parent;
  }




  /**
   * @param context the parent context to set
   */
  public void setParent( AbstractContext context ) {
    if ( this != context ) {
      this.parent = context;
    }
  }




  /**
   * Add the given name value pairs to our symbol table.
   * 
   * @param map name name value pairs to merge.
   */
  public void merge( HashMap<String, String> map ) {
    getSymbols().merge( map );
  }




  /**
   * Add the given name value pair to the symbol table for template resolution.
   * 
   * @param name name of the symbol
   * @param value value of the symbol
   */
  @SuppressWarnings("unchecked")
  public void addSymbol( String name, String value ) {
    getSymbols().put( name, value );
  }




  /**
   * @see coyote.loader.Context#contains(java.lang.String)
   */
  @Override
  public boolean contains( String key ) {
    return properties.containsKey( key );
  }

}
