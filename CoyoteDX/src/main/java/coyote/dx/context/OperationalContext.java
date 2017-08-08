/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
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
import coyote.dx.Symbols;


/**
 * This is an operational context base class providing basic context functions.
 * 
 * <p>This operational context allows components in the job to share data 
 * amongst each other while remaining functionally separate.</p>
 */
public abstract class OperationalContext {
  protected String state = null;
  protected StringBuffer errorMessage = null;
  protected volatile long startTime = 0;
  protected volatile long endTime = 0;
  protected final Map<String, Object> properties = new HashMap<String, Object>();
  protected OperationalContext parent = null;
  protected volatile long currentFrame = 0;

  /** Flag indicating the context is in error */
  protected volatile boolean errorFlag = false;

  /** List of listeners which will do something when different events happen. */
  protected List<ContextListener> listeners = new ArrayList<ContextListener>();

  protected SymbolTable symbols = null;




  /**
   * @return the symbolTable for this context
   */
  public SymbolTable getSymbols() {
    return symbols;
  }




  /**
   * Set the given symbol table in this context.
   * 
   * @param symbols the symbols to set in this context
   */
  public void setSymbols(SymbolTable symbols) {
    this.symbols = symbols;
  }




  public OperationalContext() {}




  public OperationalContext(List<ContextListener> listeners) {
    setListeners(listeners);
  }




  /**
   * Return the object from this context with the given (case sensitive) key.
   * 
   * @param key the name of the object to return
   * 
   * @return the object with that name or null if the named object is not found
   */
  public Object get(String key) {
    return get(key, true);
  }




  /**
   * Performs a search for a property with the given name taking case into 
   * account.
   * 
   * <p>If this finds a key match and its value is null, it will continue 
   * looking in case there is another key with a non-null value.
   * 
   * @param key the key for which to search
   * @param usecase true to indicate case sensitive, false to perform a case 
   *        insensitive search.
   * 
   * @return the value of the property, or null if the property was not found 
   *         with the given key of if the key was null or blank.
   */
  public Object get(String key, boolean usecase) {
    if (StringUtil.isNotBlank(key)) {
      if (usecase) {
        return properties.get(key);
      } else {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
          if (StringUtil.equalsIgnoreCase(key, entry.getKey()) && entry.getValue() != null) {
            return entry.getValue().toString();
          } // if match and value not null else keep looking
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
  public String getAsString(String key) {
    Object retval = get(key);
    if (retval != null) {
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
  public String getAsString(String key, boolean usecase) {
    Object retval = get(key, usecase);
    if (retval != null) {
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
  public void set(String key, Object value) {
    if (key != null) {
      if (value != null) {
        properties.put(key, value);
      } else {
        properties.remove(key);
      }
    }
  }




  public void setError(boolean flag) {
    errorFlag = flag;
  }




  /**
   * Set the context in an error state with the given message.
   * 
   * <p>The message will be prepended with the state of the context.
   * 
   * @param errMsg The error message to place in the context.
   */
  public void setError(String errMsg) {
    errorFlag = true;
    setErrorMessage("[" + state + "] " + errMsg);
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
   * @return the textual description of the state of the context.
   */
  public String getState() {
    return state;
  }




  /**
   * The state is a textual description of the state if the context.
   * 
   * <p>This is most used in error reporting to determine what state the 
   * context was in when the error occurred.
   *  
   * @param state the textual description of the state of the context to set
   */
  public void setState(String state) {
    this.state = state;
  }




  /**
   * @return the message
   */
  public String getErrorMessage() {
    if (errorMessage != null) {
      return errorMessage.toString();
    } else {
      return null;
    }
  }




  /**
   * Set the error message in the context.
   * 
   * <p>This does NOT set the context in an error state. It just allows setting 
   * a more detailed message after the fact.
   * 
   * @param errMsg the message to set
   */
  public void setErrorMessage(String errMsg) {
    if (errorMessage == null) {
      errorMessage = new StringBuffer(errMsg);
    } else {
      errorMessage.append("\n");
      errorMessage.append(errMsg);
    }
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
  public void setStartTime(long startTime) {
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
  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }




  /**
   * @return the elapsed time in milliseconds from start to end (or now) or 0 if not started. 
   */
  public long getElapsed() {

    if (startTime != 0) {
      if (endTime != 0) {
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
    fireStart(this);
  }




  /**
   * Set the end time to now.
   */
  public void end() {
    endTime = System.currentTimeMillis();
    if (symbols != null) {
      if (StringUtil.isNotBlank(getState())) {
        symbols.put(Symbols.CONTEXT_STATUS, getState());
      } else {
        symbols.put(Symbols.CONTEXT_STATUS, "No status information found in context");
      }

      if (StringUtil.isNotBlank(getErrorMessage())) {
        symbols.put(Symbols.CONTEXT_ERROR, getErrorMessage());
      } else {
        symbols.put(Symbols.CONTEXT_ERROR, "No error message found in context");
      }
    }

    fireEnd(this);
  }




  protected void fireStart(OperationalContext context) {
    if (parent != null)
      parent.fireStart(context);

    for (ContextListener listener : listeners) {
      listener.onStart(context);
    }
  }




  protected void fireEnd(OperationalContext context) {
    if (parent != null)
      parent.fireEnd(context);

    for (ContextListener listener : listeners) {
      listener.onEnd(context);
    }
  }




  public void fireWrite(TransactionContext context, FrameWriter writer) {
    if (parent != null)
      parent.fireWrite(context, writer);

    for (ContextListener listener : listeners) {
      listener.onWrite(context, writer);
    }
  }




  public void fireRead(TransactionContext context, FrameReader reader) {
    if (parent != null)
      parent.fireRead(context, reader);

    for (ContextListener listener : listeners) {
      listener.onRead(context, reader);
    }
  }




  /**
   * Fire an event indicating validation failed in the given context for the 
   * given reason.
   * 
   * <p>This method is called by FrameValidators to inform interested 
   * components when frames fail validation checks and to provide a detailed 
   * message as to why the validation failed, what the values were which 
   * failed and possibly guidelines on how to locate and correct the error.
   * 
   * @param validator the validator generating the event
   * @param msg error message indicating details why the validation failed.
   */
  public void fireValidationFailed(FrameValidator validator, String msg) {
    if (parent != null)
      parent.fireValidationFailed(validator, msg);

    for (ContextListener listener : listeners) {
      listener.onValidationFailed(this, validator, msg);
    }

  }




  public void fireFrameValidationFailed(TransactionContext txnContext) {
    if (parent != null)
      parent.fireFrameValidationFailed(txnContext);

    for (ContextListener listener : listeners) {
      listener.onFrameValidationFailed(txnContext);
    }

  }




  public void setListeners(List<ContextListener> listeners) {
    if (listeners != null) {
      this.listeners = listeners;
    }
  }




  public void addListener(ContextListener listener) {
    if (listener != null) {
      if (listeners == null) {
        listeners = new ArrayList<ContextListener>();
      }
      listeners.add(listener);
    }
  }




  public String dump() {
    StringBuffer b = new StringBuffer("Context Properties:");
    b.append(StringUtil.LINE_FEED);
    for (String key : properties.keySet()) {
      b.append("'");
      b.append(key);
      b.append("' = ");
      Object value = properties.get(key);
      if (value != null)
        if (value instanceof Object[]) {
          b.append('[');
          Object[] arry = (Object[])value;
          for (int x = 0; x < arry.length; x++) {
            if (value != null) {
              b.append(arry[x].toString());
            }
            if (x + 1 < arry.length) {
              b.append(", ");
            }
          }
          b.append(']');
        } else {
          b.append(value.toString());
        }
      else
        b.append("null");
      b.append(StringUtil.LINE_FEED);
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
  public void setParent(OperationalContext context) {
    if (this != context) {
      this.parent = context;
    }
  }




  /**
   * @return the row (current frame number in the sequence)
   */
  public long getRow() {
    if (parent != null) {
      return parent.currentFrame;
    } else {
      return currentFrame;
    }
  }




  /**
   * This sets the number of the current frame in the sequence.
   * @param row the row (frame sequence) to set
   */
  public void setRow(long row) {
    if (parent != null) {
      parent.currentFrame = row;
    } else {
      this.currentFrame = row;
    }
  }




  /**
   * Merge the properties in the given source context into this one, over-
   * writing any existing properties with the same key.
   * 
   * @param source context from which the data is to be read.
   */
  public void merge(OperationalContext source) {
    if (source != null) {
      for (String key : source.properties.keySet()) {
        Object value = source.properties.get(key);
        if (value != null) {
          properties.put(key, value);
        }
      }
    }
  }




  /**
   * Return a shallow copy of the properties in this context as a map.
   * 
   * <p>Note: this only retrieves the references in the properties map. This 
   * does not retrieve the symbol table values, sart time, state, error 
   * message, end time, current frame, or listeners.
   * 
   * <p>The map is mutable in so far as objects can be added and removed 
   * without affecting the contents of the context.
   * 
   * <p>Changing the property value changes both values as this is a shallow 
   * copy of the properties map in this context.
   * 
   * @return a shallow copy of the properties in this context.
   */
  public Map<String, Object> toMap() {
    final Map<String, Object> retval = new HashMap<String, Object>();
    for (String key : properties.keySet()) {
      Object value = properties.get(key);
      if (value != null) {
        retval.put(key, value);
      }
    }
    return retval;
  }




  /**
   * Returns the value as a map.
   * 
   * <p>If the value is a scalar, it will be placed in a map with its name as 
   * the key. If it is a map, it will be returned as a shallow copy of the 
   * that map.
   * 
   * @param name Name of the property to retrieve
   * 
   * @return the data as a map or a shallow copy of the map; will never return null.
   */
  public Map<String, Object> getAsMap(String name) {
    Map retval = new HashMap();
    Object obj = properties.get(name);
    if (obj != null) {
      if (obj instanceof Map) {
        for (Object key : ((Map)obj).keySet()) {
          Object value = ((Map)obj).get(key);
          if (value != null) {
            retval.put(key.toString(), value);
          }
        }
      } else {
        retval.put(name, obj);
      }
    }
    return retval;
  }

}
