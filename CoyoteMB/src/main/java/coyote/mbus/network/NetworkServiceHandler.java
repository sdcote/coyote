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
package coyote.mbus.network;

import java.net.URI;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;


/**
 * This interface is designed to be a callback mechanism from the 
 * NetworkService. 
 * 
 * <p>Once the manager has determined that something has happened, it informs
 * the appropriate NetworkServiceHandler via this interface.</p> 
 * 
 * <p>The NetworkServiceHandler which is interested in being notified of 
 * packets relating to the SelectionKey should attach itself to the 
 * SelectionKey via the <code>attach()</code> method. The NetworkService will 
 * then call that SelectionKeyHandlers methods.</p>
 */
public interface NetworkServiceHandler {

  /**
   * Give the handler a chance to perform some processing prior to operation.
   * 
   * <p>This method will be called right after having the channel assigned and 
   * just prior to being added to the selector. This gives the handler a chance 
   * to perform some IO before the selection cycle.</p>
   */
  public void initialize();




  /**
   * Retrieve the network URI this handler is intended to service.
   * 
   * <p>It is expected that all URIs will contain &quot;udp&quot; or
   * &quot;tcp&quot; as the scheme portion of the URI so the NetworkService will
   * be able to determine the type of listener to bind.</p>
   *
   * @return A URI representing a scheme/protocol and an IP address and port.
   */
  public URI getServiceUri();




  /**
   * Set the IO channel for this handler to use to read and write.
   * @param channel  The channel on which to perform IO operations.
   */
  public void setChannel( AbstractSelectableChannel channel );




  /**
   * @return  The IO channel on which this handler reads and writes.
   */
  public AbstractSelectableChannel getChannel();




  /**
   * @return  The selection key with which the handler is registered with its   seletor.
   */
  public SelectionKey getKey();




  /**
   * Set the key used by the selector used for selection operations.
   * @param key  The SelectionKey that is used for I/O selection.
   */
  public void setKey( SelectionKey key );




  /**
   * Method which is called when the key becomes acceptable.
   *
   * @param key The key which is acceptable.
   */
  public void accept( SelectionKey key );




  /**
   * Method which is called when the key becomes connectable.
   *
   * @param key The key which is connectable.
   */
  public void connect( SelectionKey key );




  /**
   * Method which is called when the key becomes readable.
   *
   * @param key The key which is readable.
   */
  public void read( SelectionKey key );




  /**
   * Method which is called when the key becomes writable.
   *
   * @param key The key which is writable.
   */
  public void write( SelectionKey key );




  /**
   * Called to clean up resources used by the handler.
   */
  public void shutdown();

}
