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
 * 
 */
public interface ContextListener {

  /**
   * Signal when a context has started - i.e. logical operation has begun.
   * 
   * <p>The concept of starting can be different depending on the type of 
   * context it is. For some it means when the context was created, but it 
   * generally means that point in time just after initialization and just 
   * before operation is to begin.</p>
   * 
   * <p>It is possible that events may have been previously fired from the 
   * given context as initialization activities may have generated events. This 
   * event signifies that all initialization is complete and the operational 
   * phase is about to commence.</p>
   * 
   * @param context the context which is starting.
   */
  void onStart( Context context );




  /**
   * Signal when a context has ended - i.e. logical operation has completed.
   * 
   * <p>The concept of ending can be different depending on the type of 
   * context it is. It generally represents that point in time just after 
   * operation and just before termination activities begin.</p>
   * 
   * <p>Generally speaking, when this event is fired, the listener can assume 
   * that context is no longer operational. Other events may be fired during 
   * the termination phase of components in the context, but operation has 
   * logically ended.</p>  
   * 
   * @param context the context which has ended.
   */
  void onEnd( Context context );




  /**
   * Used to indicate an event in a context has occurred.
   * 
   * <p>It is expected that listeners will check the type of event being given 
   * and process it accordingly. This allows components to generate any subtype 
   * of an event appropriate for its domain.</p>
   * 
   * @param event the event which occurred.
   */
  void onEvent( ContextEvent event );

}
