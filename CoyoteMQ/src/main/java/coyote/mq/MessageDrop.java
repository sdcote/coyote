/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mq;

/**
 * This is a data type which is designed to hold one message in a thread safe 
 * way for another thread to retrieve.
 * 
 * <p>This ensures that a delivery thread does not send an acknowledgement of 
 * delivery to the broker until the data has been retrieved. The data is 
 * effectively staged for retrieval and only acknowledges delivery when some
 * other thread notifies it has retrieved the data. 
 * 
 * <p>This class is designed to be used by only 2 threads; one delivering data 
 * and the other retrieving data. 
 */
public class MessageDrop {
  private volatile byte[] message = null;




  /**
   * Leave data in the message drop and wait for it to be retrieved by another 
   * thread.
   * 
   * @param data the message data to leave
   * 
   * @throws InterruptedException 
   */
  public synchronized void leave( byte[] data ) throws InterruptedException {
    message = data;
    notifyAll(); // notify thread waiting for data in waitForDelivery
    waitForRetrieval();
  }




  /**
   * Check to see if data exists in the message drop.
   * 
   * @return true if there is data in the drop to be retrieved, false if the 
   *         message drop is empty.
   */
  public boolean hasData() {
    return message != null;
  }




  /**
   * Retrieve the data left by other threads, notifying any waiting threads 
   * that the data has been taken.
   * 
   * @return the data left by another thread.
   */
  public synchronized byte[] take() {
    try {
      byte[] retval = message;
      message = null;
      return retval;
    }
    finally {
      notifyAll(); // notify thread waiting for data in waitForRetrieval
    }
  }




  /**
   * Wait for the data to clear.
   * 
   * <p>This is called by the delivery thread and will block until notified 
   * and there is no data.
   * 
   * @throws InterruptedException 
   */
  public synchronized void waitForRetrieval() throws InterruptedException {
    while ( this.hasData() ) {
      wait();
    }
  }




  /**
   * Wait indifinitely for data to arrive.
   * 
   * @throws InterruptedException
   */
  public synchronized void waitForDelivery() throws InterruptedException {
    waitForDelivery( Long.MAX_VALUE );
  }




  /**
   * Wait for the delivery of a message, but only for the given number of 
   * milliseconds.
   * 
   * <p>A call to {@link #hasData()} will indicate if a time-out has occurred 
   * or if data has been received.
   * 
   * @param millis How many milliseconds to wait before returning.
   * 
   * @throws InterruptedException
   */
  public synchronized void waitForDelivery( long millis ) throws InterruptedException {
    long timeout = System.currentTimeMillis() + millis;
    while ( !this.hasData() ) {
      wait( 10 );
      if ( System.currentTimeMillis() > timeout )
        return;
    }

  }

}
