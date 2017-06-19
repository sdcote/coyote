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
package coyote.dx.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.dx.FrameReader;
import coyote.dx.FrameValidator;
import coyote.dx.FrameWriter;


/**
 * This is an operational context base class providing basic context functions.
 * 
 * <p>This operational context allows components in the job to share data 
 * amongst each other while remaining functionally separate.</p>
 */
public abstract class OperationalContext {
  public static final String ERROR_STATUS = "Error";
  protected String status = null;
  protected String errorMessage = null;
  protected volatile long startTime = 0;
  protected volatile long endTime = 0;
  protected final Map<String, Object> properties = new HashMap<String, Object>();
  protected OperationalContext parent = null;
  protected long currentFrame = 0;

  /** Flag indicating the context is in errorFlag */
  protected volatile boolean errorFlag = false;

  /** List of listeners which will do something when different events happen. */
  List<ContextListener> listeners = new ArrayList<ContextListener>();

  protected SymbolTable symbols = null;




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
   * <p>This support the automatic decryption of encrypted values if the name 
   * of the retrieved object starts with the encryption prefix. This allows 
   * the value to remain encrypted in memory and only exposed during the brief 
   * time it is used. NOTE, the decrypted value will remain in the heap until 
   * garbage collection so there is still some exposure. 
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
   * @param errMsg The error message to place in the context.
   */
  public void setError( String errMsg ) {
    errorFlag = true;
    status = ERROR_STATUS;
    errorMessage = errMsg;
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
  public String getErrorMessage() {
    return errorMessage;
  }




  /**
   * Set the error message in the context.
   * 
   * <p>This does NOT set the context in an error state. It just allows setting 
   * a more detailed message after the fact.
   * 
   * @param errMsg the message to set
   */
  public void setErrorMessage( String errMsg ) {
    this.errorMessage = errMsg;
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
    if ( parent != null )
      parent.fireStart( context );

    for ( ContextListener listener : listeners ) {
      listener.onStart( context );
    }
  }




  protected void fireEnd( OperationalContext context ) {
    if ( parent != null )
      parent.fireEnd( context );

    for ( ContextListener listener : listeners ) {
      listener.onEnd( context );
    }
  }




  public void fireWrite( TransactionContext context, FrameWriter writer ) {
    if ( parent != null )
      parent.fireWrite( context, writer );

    for ( ContextListener listener : listeners ) {
      listener.onWrite( context, writer );
    }
  }




  public void fireRead( TransactionContext context, FrameReader reader ) {
    if ( parent != null )
      parent.fireRead( context, reader );

    for ( ContextListener listener : listeners ) {
      listener.onRead( context, reader );
    }
  }




  /**
   * Fire an event indicating validation failed in the given context for the 
   * given reason.
   * @param validator 
   * 
   * @param msg error message indicating why the validation failed.
   */
  public void fireValidationFailed( FrameValidator validator, String msg ) {
    if ( parent != null )
      parent.fireValidationFailed( validator, msg );

    for ( ContextListener listener : listeners ) {
      listener.onValidationFailed( this, validator, msg );
    }

  }




  public void fireFrameValidationFailed( TransactionContext txnContext ) {
    if ( parent != null )
      parent.fireFrameValidationFailed( txnContext );

    for ( ContextListener listener : listeners ) {
      listener.onFrameValidationFailed( txnContext );
    }

  }




  public void setListeners( List<ContextListener> listeners ) {
    if ( listeners != null ) {
      this.listeners = listeners;
    }
  }




  public void addListener( ContextListener listener ) {
    if ( listener != null ) {
      if ( listeners == null ) {
        listeners = new ArrayList<ContextListener>();
      }
      listeners.add( listener );
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
    if ( this != context ) {
      this.parent = context;
    }
  }




  /**
   * @return the row (current frame number in the sequence)
   */
  public long getRow() {
    return currentFrame;
  }




  /**
   * This sets the number of the current frame in the sequence.
   * @param row the row (frame sequence) to set
   */
  public void setRow( long row ) {
    this.currentFrame = row;
  }




  /**
   * Merge the properties in the given source context into this one, over-
   * writing any existing properties with the same key.
   * 
   * @param source context from which the data is to be read.
   */
  public void merge( OperationalContext source ) {
    if ( source != null ) {
      for ( String key : source.properties.keySet() ) {
        Object value = source.properties.get( key );
        if ( value != null ) {
          properties.put( key, value );
        }
      }
    }
  }

}
