/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;


/**
 * 
 */
public class HttpFutureTest {

  
  @Test
  public void test() {
    HttpFuture future = new HttpFuture();
    assertFalse(future.isComplete());
    assertNull(future.getResponse(500));
    new Thread(new Completer(future, 3000)).start();
    assertNotNull(future.getResponse(5000));
    assertTrue(future.isComplete());
  }




  /**
   * Somthing which runs in a different thread and completes the HttpFuture
   */
  private class Completer implements Runnable {
    HttpFuture future;
    long wait;

    Completer(HttpFuture future, long wait) {
      this.future = future;
      this.wait = wait;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(wait);
      } catch (InterruptedException ignore) {
        // don't care
      }
      future.setResponse(new Response(Status.OK, MimeType.ANY.getType(), null, 0L));
    }

  }
}
