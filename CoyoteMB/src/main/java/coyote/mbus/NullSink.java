/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
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
 * An implementation of a message sink which does nothing with the message.
 * 
 * <p>The purpose of this class is to create valid message channels without
 * running the risk of messages being queues up with nothing to process
 * them.</p>
 */
public class NullSink implements MessageSink {

  /**
   * Completely ignore the message.
   * 
   * @see coyote.mbus.MessageSink#onMessage(coyote.mbus.message.Message)
   */
  @Override
  public void onMessage( Message msg ) {
    // Do Nothing
  }

}
