/*
 * Copyright Stephan D. Cote' 2008 - All rights reserved.
 */
package coyote.mbus;

import java.util.Date;

import coyote.mbus.message.Message;
import coyote.mbus.network.MessageChannel;


/**
 * The MicroBusStepper class is used to test the basic operation of the MicroBus.
 * 
 * @author Stephan D. Cote' - Enterprise Architecture
 */
public class MicroBusStepper
{

  /**
   * @param args
   */
  public static void main( final String[] args ) throws Exception
  {
    MicroBus mbus = new MicroBus();
    mbus.enableLogging( true );
    
    //The mBus remains a private bus unless it is opened.
    mbus.open(); // opening the bus connects it to the network

    
    // Have the MicroBus create a channel on which we can use to send messages
    final MessageChannel channel = mbus.createChannel();
    
    Message msg;

    do
    {
      msg = new Message();
      msg.setGroup( "BEACON" );

      msg.add( "DATE", new Date().toString() );

      channel.send( msg );

      try
      {
        Thread.sleep( 10000 );
      }
      catch( final InterruptedException ignore )
      {
      }
    }
    while( true );
  }

}
