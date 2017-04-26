/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.mbus;

import coyote.mbus.network.MessageChannel;


/**
 * The MessageQueueChannel class models a component which models a queue by 
 * synchronizing the processing of received messages with other queued message 
 * channels on the bus.
 */
public class MessageQueueChannel extends MessageChannel {

  /**
   * 
   */
  public MessageQueueChannel( final MessageChannel channel ) {
    // TODO Auto-generated constructor stub
  }




  /**
   * @param sink
   */
  public MessageQueueChannel( final MessageSink sink ) {
    super( sink );
    // TODO Auto-generated constructor stub
  }




  /**
   * @param args
   */
  public static void main( final String[] args ) {
    // TODO Auto-generated method stub

  }

}
