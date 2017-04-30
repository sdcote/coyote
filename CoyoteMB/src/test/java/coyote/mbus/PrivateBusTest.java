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

//import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coyote.mbus.message.Message;
import coyote.mbus.network.MessageChannel;


/**
 * 
 */
public class PrivateBusTest
{

  /**
   * This tests a single MicroBus instance running disconnected from the 
   * network but still passing messages to components in the JVM.
   */
  @Test
  public void test()
  {
    MicroBus sharedBus = new MicroBus();
    //sharedBus.enableLogging( true );

    // Some component in the JVM creates a message handler and registers it 
    // with the shared MicroBus by requesting the MicroBus create a channel to 
    // the message handler. 
    final MessageChannel alphaChannel = sharedBus.createChannel( new MessageHandler() );

    // Some component in the JVM creates a message handler and registers it 
    // with the shared MicroBus by requesting the MicroBus create a channel to 
    // the message handler. 
    final MessageHandler receiver = new MessageHandler();
    final MessageChannel betaChannel = sharedBus.createChannel( receiver );
    // Now it informs the channel it wants all messages which appear on the 
    // "SomeTopic" group
    betaChannel.join( "SomeTopic" );

    // The Alpha component creates a message for anything intreseted in 
    // "SomeTopic" messages... 
    Message msg = new Message();
    msg.setGroup( "SomeTopic" );
    msg.add( "MyField", "MyValue" );
    // ...and sends it
    alphaChannel.send( msg );

    // ... and it gets sent to the beta channel because it joined the 
    //     "SomeTopic" group and passed the message from the Alpha component to
    //     the receiver registered with the channel by the Beta component

    // The Beta component now just has to retrieve the message 
    int messagesWaiting = receiver.waitForMessage( 1000 );
    assertTrue( messagesWaiting == 1 );
    Message message = receiver.getNext();
    assertNotNull( message );
    //System.out.println( "Received the following message:\r\n" + message );

  }

}
