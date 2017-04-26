/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus;

import coyote.mbus.message.Message;


/**
 * Object that implement this interface are enabled to be used as a call-back
 * for MessageChannels
 */
public interface MessageSink {

  /**
   * Allow messages to be sent to the object implementing this interface.
   *
   * <p>This is a call-back method that usually results when something places a
   * message in a Message Channel with which we are registered.</p>
   *
   * @param msg The message to receive and process.
   * 
   * @throws IllegalStateException if the system is in the process of shutting-down
   */
  public void onMessage( Message msg );
}
