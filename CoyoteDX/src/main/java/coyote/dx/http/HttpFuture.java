/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http;

import coyote.commons.network.http.Response;
import coyote.dataframe.DataFrame;


/**
 * Future class which allow requesting threads to wait for completion of
 * processing in other threads before sending a response.
 */
public class HttpFuture {
  private final Object mutex = new Object();
  private volatile Response response = null;
  private DataFrame frame = null;
  private String method = null;




  public DataFrame getDataFrame() {
    return frame;
  }




  /**
   * @return the frame
   */
  public DataFrame getFrame() {
    return frame;
  }




  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }




  public Response getResponse(final long timeout) {
    synchronized (mutex) {
      final long expiry = System.currentTimeMillis() + timeout;
      while (response == null && System.currentTimeMillis() < expiry) {
        try {
          mutex.wait(10);
        } catch (final InterruptedException ignore) {
          // don't care, simply time-out
        }
      }
      return response;
    }

  }




  public boolean isComplete() {
    synchronized (mutex) {
      return (response != null);
    }
  }




  /**
   * @param frame the frame to set
   */
  public void setFrame(final DataFrame frame) {
    this.frame = frame;
  }




  public void setMethod(final String method) {
    this.method = method;
  }




  public void setResponse(final Response result) {
    synchronized (mutex) {
      response = result;
      mutex.notifyAll();
    }
  }

}