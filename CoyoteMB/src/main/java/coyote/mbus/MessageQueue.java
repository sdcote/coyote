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

import java.util.LinkedList;

import coyote.mbus.message.Message;


/**
 * MessageQueue models a queue that holds messages for handling by another 
 * component, like a dispatcher.
 */
public class MessageQueue {

  /**
   * Field list
   */
  LinkedList list = new LinkedList();




  /**
   * Constructor MessageQueue
   */
  public MessageQueue() {}




  /**
   * Adds the given Message to the end of the queue as in an append operation.
   *
   * @param msg
   */
  public void add( final Message msg ) {
    synchronized( list ) {
      list.add( msg );
      list.notify();
    }
  }




  /**
   * Adds the given Message to the top of the queue.
   * <p>
   * This will result in this message being retrieved when
   *
   * @param message
   */
  public void addFirst( final Message message ) {
    synchronized( list ) {
      list.addFirst( message );
      list.notify();
    }
  }




  /**
   * Remove all the data in the queue.
   */
  public void clear() {
    list.clear();
  }




  /**
   * Performs a blocking retrieval operation on the queue.
   * 
   * <p>Block indefinitely if there are no messages to get.</p>
   *
   * @return The next Message in the queue.
   *
   * @throws InterruptedException
   */
  public Message get() throws InterruptedException {
    synchronized( list ) {
      while ( list.size() == 0 ) {
        list.wait();
      }

      return (Message)list.removeFirst();
    }
  }




  /**
   * Performs a blocking retrieval operation on the queue.
   * 
   * <p>Block only for time-out if there are no messages to get.</p>
   *
   * @param millis the time to wait for a Message in milliseconds.
   *
   * @return The next Message in the queue, or null if timed-out.
   *
   * @throws InterruptedException
   */
  public Message get( final long millis ) throws InterruptedException {
    synchronized( list ) {
      if ( list.size() == 0 ) {
        list.wait( millis );
      }

      if ( list.size() == 0 ) {
        return null;
      }

      return (Message)list.removeFirst();
    }
  }




  /**
   * Return the number of messages in the queue
   *
   * @return the number of message entries in the queue
   */
  public int size() {
    return list.size();
  }

}
