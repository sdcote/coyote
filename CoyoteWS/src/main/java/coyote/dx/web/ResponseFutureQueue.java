/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.dx.web;

/**
 * Blocking queue of ResponseFuture. 
 *
 * <p>Implemented as circular buffer in an array of ResponseFuture instances. 
 * Synchronized on the array to avoid double synchronization.
 */
public class ResponseFutureQueue {

  /** The array we used to store the object references */
  private ResponseFuture[] slots;

  /** The size of the array (it's capacity) */
  private int capacity;

  /** Number of objects currently in the array */
  private int size = 0;

  /** The element that is next to be retrieved */
  private int head = 0;

  /** The end of the current array of available objects */
  private int tail = 0;




  /**
   * Constructor.
   *
   * @param size
   */
  public ResponseFutureQueue( int size ) {
    capacity = size;

    if ( size == 0 ) {
      capacity = 255;
    }

    slots = new ResponseFuture[capacity];
  }




  /**
   * Returns how many objects are in the array.
   *
   * @return the current size of the queue
   */
  public int size() {
    return size;
  }




  /**
   * Returns how much space is available in the queue.
   *
   * @return the current available capacity of the queue
   */
  public int space() {
    return capacity - size;
  }




  /**
   * Returns how loaded the queue as a percentage.
   *
   * @return the current load percentage of the queue
   */
  public float load() {
    if ( capacity > 0 ) {
      return (float)size / (float)capacity;
    } else {
      return 1;
    }
  }




  /**
   * Returns if the queue is empty or not.
   *
   * @return True if there is nothing to get() false if there is something to
   *         get()
   */
  public boolean isEmpty() {
    if ( ( capacity - size ) == 0 ) {
      return true;
    } else {
      return false;
    }
  }




  /**
   * Returns the total capacity of the queue.
   *
   * @return the maximum size of the queue
   */
  public int capacity() {
    return capacity;
  }




  /**
   * Clears out the queue.
   */
  public void clear() {
    synchronized( slots ) {
      for ( int i = 0; i < capacity; i++ ) {
        slots[i] = null;
      }

      size = 0;
      head = 0;
      tail = 0;
    }
  }




  /**
   * Put object in queue.
   *
   * <p>Block indefinitely if the queue is full.
   *
   * @param o Object to place in the queue
   *
   * @throws InterruptedException
   */
  public void put( ResponseFuture o ) throws InterruptedException {
    synchronized( slots ) {
      while ( size == capacity ) {
        slots.wait();
      }

      slots[tail] = o;

      if ( ++tail == capacity ) {
        tail = 0;
      }

      size++;

      slots.notify();
    }
  }




  /**
   * Put object in queue.
   *
   * <p>Block for time-out if the queue is full.
   *
   * @param timeout If timeout expires, throw InterruptedException
   * @param o Object
   * @exception InterruptedException Timeout expired or otherwise interrupted
   */
  public void put( ResponseFuture o, int timeout ) throws InterruptedException {
    synchronized( slots ) {
      if ( size == capacity ) {
        slots.wait( timeout );

        if ( size == capacity ) {
          throw new InterruptedException( "Timed out" );
        }
      }

      slots[tail] = o;

      if ( ++tail == capacity ) {
        tail = 0;
      }

      size++;

      slots.notify();
    }
  }




  /**
   * Get object from queue.
   *
   * <p>Block indefinitely if there are no objects to get.
   *
   * @return The next object in the queue.
   *
   * @throws InterruptedException
   */
  public ResponseFuture get() throws InterruptedException {
    synchronized( slots ) {
      while ( size == 0 ) {
        slots.wait();
      }

      ResponseFuture o = slots[head];

      if ( ++head == capacity ) {
        head = 0;
      }

      if ( size == capacity ) {
        slots.notify();
      }

      size--;

      return o;
    }
  }




  /**
   * Get from queue.
   *
   * <p>Block for time-out if there are no objects to get.
   *
   * @param millis the time to wait for a job
   * 
   * @return The next object in the queue, or null if timed-out.
   *
   * @throws InterruptedException
   */
  public ResponseFuture get( long millis ) throws InterruptedException {
    synchronized( slots ) {
      if ( size == 0 ) {
        slots.wait( millis );
      }

      if ( size == 0 ) {
        return null;
      }

      ResponseFuture o = slots[head];

      if ( ++head == capacity ) {
        head = 0;
      }

      if ( size == capacity ) {
        slots.notify();
      }

      size--;

      return o;
    }
  }




  /**
   * Peek at the first element in queue but does not remove it.
   *
   * <p>Block indefinitely if there are no objects to peek.
   *
   * @return The next object in the queue, or null if queue is empty.
   *
   * @throws InterruptedException
   */
  public ResponseFuture peek() throws InterruptedException {
    synchronized( slots ) {
      if ( size == 0 ) {
        slots.wait();
      }

      if ( size == 0 ) {
        return null;
      }

      return slots[head];
    }
  }




  /**
   * Peek at the first element in queue but does not remove it.
   *
   * <p>Block for time-out if there are no objects to peek.
   *
   * @param millis the time to wait for a job
   *
   * @return The next object in the queue, or null if timed-out or queue is
   *         empty.
   *
   * @throws InterruptedException
   */
  public ResponseFuture peek( int millis ) throws InterruptedException {
    synchronized( slots ) {
      if ( size == 0 ) {
        slots.wait( (long)millis );
      }

      if ( size == 0 ) {
        return null;
      }

      return slots[head];
    }
  }
}
