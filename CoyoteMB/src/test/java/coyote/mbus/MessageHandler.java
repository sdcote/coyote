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

import java.util.ArrayList;

import coyote.mbus.message.Message;


/**
 * This is a message sink channels can use to process messages.
 */
class MessageHandler implements MessageSink
{
  ArrayList<Message> messageList = new ArrayList<Message>();




  public int messageCount()
  {
    synchronized( messageList )
    {
      return messageList.size();
    }
  }




  public Message getNext()
  {
    synchronized( messageList )
    {
      if( messageList.size() > 0 )
        return messageList.remove( 0 );
      else
        return null;
    }
  }




  /**
   * @see coyote.mbus.MessageSink#onMessage(coyote.mbus.message.Message)
   */
  public void onMessage( Message msg )
  {
    synchronized( messageList )
    {
      messageList.add( msg );
    }
  }




  /**
   * Wait for messages to be received from the message bus for a specific 
   * amount of time.
   * 
   * <p>This method is designed to give the bus a change to deliver messages in 
   * a multi-threaded, networked environment.</p>
   * 
   * @param timeout how long to wait for a message before timing-out. 0
   * 
   * @return THe number of messages waiting to be retrieved.
   */
  public int waitForMessage( long timeout )
  {
    // determine the timeout sentinel value
    final long tout = System.currentTimeMillis() + timeout;
    final Object opLock = new Object();
    int retval = 0;

    // While we have not reached the sentinel time
    while( tout > System.currentTimeMillis() )
    {
      // make a check to see if anything has been placed in the message list 
      retval = messageCount();

      // If there is a message to process, return with the number of messages 
      // ready for processing
      if( retval > 0 )
        return retval;

      // wait on the operational lock object
      synchronized( opLock )
      {
        try
        {
          opLock.wait( 10 );
        }
        catch( final Throwable t )
        {
        }
      }

    } // while time-out not reached

    return retval;
  }
}
