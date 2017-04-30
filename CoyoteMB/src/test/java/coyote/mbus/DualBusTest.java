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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.mbus.message.Message;
import coyote.mbus.network.MessageChannel;


/**
 * 
 */
public class DualBusTest
{
  static MicroBus alphaBus = null;
  static MicroBus betaBus = null;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception
  {
    // Setup 2 busses which use the same multicast port
    alphaBus = new MicroBus();
    alphaBus.setPort( 12345 );
    //alphaBus.enableLogging(false );
    betaBus = new MicroBus();
    betaBus.setPort( 12345 );
    //betaBus.enableLogging( true );
  }




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception
  {
    alphaBus.open();
    alphaBus.waitForBus( 5000 );
    betaBus.open();
    betaBus.waitForBus( 5000 );
  }




  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception
  {
    alphaBus.close();
    betaBus.close();
  }




  /**
   * This tests the scenario of two separate MicroBus instances communicating
   * with each other using the network. 
   */
  @Test
  public void test()
  {
    // Mimic a system running in its own JVM. It could be on the same host or a
    // different host on the network it uses an instance of the MicroBus to 
    // communicate with other components
    final MessageHandler alphaHandler = new MessageHandler();
    final MessageChannel alphaChannel = alphaBus.createChannel( alphaHandler );
    alphaChannel.join( "OpenTopic" );
    alphaChannel.join( "AlphaTopic" );

    // Mimic another system running in a different JVM. It creates its own
    // MicroBus connected to the network.
    final MessageHandler betaHandler = new MessageHandler();
    final MessageChannel betaChannel = betaBus.createChannel( betaHandler );
    betaChannel.join( "OpenTopic" );
    betaChannel.join( "BetaTopic" );

    // Now we model the alpha system creating a message it intends to broadcast 
    // on and "Open Topic". It does not know who is listening or even where it
    // is; it just creates a message destine to the "OpenTopic" group.
    Message msg = new Message();
    msg.setGroup( "OpenTopic" );
    msg.add( "MyField", "MyValue" );

    // The alpha system sends it on its message channel...
    alphaChannel.send( msg );

    // The message has probably already been delivered to bete system's message 
    // handler, so now the beta system just needs to retrieve it
    betaHandler.waitForMessage( 1000 );

  }

}
