/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
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

/**
 * This is the interface for a generic event
 */
public interface ContextEvent {

  /**
   * @return the time in milliseconds when the event occurred (not fired)
   */
  public long getTimestamp();




  /**
   * Access the subject of the event.
   * 
   * <p>This has meaning only for those listeners expecting events of a 
   * particular type. Those listeners can then cast the returned object into 
   * the appropriate type.</p>
   * 
   * <p>The subject is not guaranteed to be immutable unless the implementation
   * takes measures to insure immutability.</p>
   *  
   * @return the subject of the event, may be null.
   */
  public Object getSubject();




  /**
   * Access the context which is the source of the event.
   * 
   * <p>Even though a listener may only be registered with the root (parent) 
   * context and fired from the root, the context returned will be the context
   * in which the event occurred. This allows a listener to be registered with 
   * the root and still receive events for any child context.</p>
   * 
   * @return the context from which this event was fired.
   */
  public AbstractContext getContext();

}
