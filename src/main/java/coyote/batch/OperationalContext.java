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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;


/**
 * This is an operational context base class providing basic context functions.
 * 
 * <p>This operational context allows components in the job to share data 
 * amongst each other while remaining functionally separate.</p>
 */
public abstract class OperationalContext {
  public static final String ERROR_STATUS = "Error";
  protected String status = null;
  protected String message = null;
  protected long startTime = 0;
  protected long endTime = 0;
  protected final Map<String, Object> properties = new HashMap<String, Object>();
  protected OperationalContext parent = null;

  /** Flag indicating the context is in errorFlag */
  protected volatile boolean errorFlag = false;

  /** List of listeners which will do something when different events happen. */
  List<ContextListener> listeners = new ArrayList<ContextListener>();

  SymbolTable symbols = null;




  /**
   * @return the symbolTable for this context
   */
  public SymbolTable getSymbols() {
    return symbols;
  }




  /**
   * @param symbols the symbols to set in this context
   */
  public void setSymbols( SymbolTable symbols ) {
    this.symbols = symbols;
  }




  OperationalContext() {}




  public OperationalContext( List<ContextListener> listeners ) {
    setListeners( listeners );
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
  public Object get( String key, boolean usecase ) {
    if ( StringUtil.isNotBlank( key ) ) {
      if ( usecase ) {
        return properties.get( key );
      } else {
        for ( Map.Entry<String, Object> entry : properties.entrySet() ) {
          if ( key.equalsIgnoreCase( entry.getKey() ) ) {
            if ( entry.getValue() != null ) {
              return entry.getValue().toString();
            }
            // dont break; keep looking in case there is another non-null match
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
  public void set( String key, Object value ) {
    if ( key != null ) {
      if ( value != null ) {
        properties.put( key, value );
      } else {
        properties.remove( key );
      }
    }
  }




  public void setError( boolean flag ) {
    errorFlag = flag;
  }




  /**
   * Set the context in an error state with the given message.
   * 
   * @param msg The message to place in the context.
   */
  public void setError( String msg ) {
    errorFlag = true;
    status = ERROR_STATUS;
    message = msg;
  }




  public boolean isInError() {
    return errorFlag;
  }




  /**
   * Accessor to check if the context is not in error (i.e. fine).
   * 
   * <p>This is a convenience method to make code more readable</p>
   * 
   * @return true if the context is fine (without error) false if there is a problem.
   */
  public boolean isNotInError() {
    return !errorFlag;
  }




  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }




  /**
   * @param status the status to set
   */
  public void setStatus( String status ) {
    this.status = status;
  }




  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }




  /**
   * @param message the message to set
   */
  public void setMessage( String message ) {
    this.message = message;
  }




  /**
   * @return the startTime
   */
  public long getStartTime() {
    return startTime;
  }




  /**
   * @param startTime the startTime to set
   */
  public void setStartTime( long startTime ) {
    this.startTime = startTime;
  }




  /**
   * @return the endTime
   */
  public long getEndTime() {
    return endTime;
  }




  /**
   * @param endTime the endTime to set
   */
  public void setEndTime( long endTime ) {
    this.endTime = endTime;
  }




  /**
   * @return the elapsed time in milliseconds from start to end (or now) or 0 if not started. 
   */
  public long getElapsed() {

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
  public void start() {
    startTime = System.currentTimeMillis();
    fireStart( this );
  }




  /**
   * Set the end time to now.
   */
  public void end() {
    endTime = System.currentTimeMillis();
    fireEnd( this );
  }




  protected void fireStart( OperationalContext context ) {
    for ( ContextListener listener : listeners ) {
      listener.onStart( context );
    }
  }




  protected void fireEnd( OperationalContext context ) {
    for ( ContextListener listener : listeners ) {
      listener.onEnd( context );
    }
  }




  public void fireWrite( TransactionContext context ) {
    for ( ContextListener listener : listeners ) {
      listener.onWrite( context );
    }
  }




  public void fireRead( TransactionContext context ) {
    for ( ContextListener listener : listeners ) {
      listener.onRead( context );
    }
  }




  public void setListeners( List<ContextListener> listeners ) {
    if ( listeners != null ) {
      this.listeners = listeners;
    }
  }




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
    if ( symbols != null ) {
      b.append( StringUtil.LINE_FEED );
      b.append( "Symbol Table:" );
      b.append( StringUtil.LINE_FEED );
      b.append( symbols.dump() );
    }
    return b.toString();
  }




  /**
   * @return the parent context, may be null.
   */
  public OperationalContext getParent() {
    return parent;
  }




  /**
   * @param context the parent context to set
   */
  public void setParent( OperationalContext context ) {
    this.parent = context;
  }

}
