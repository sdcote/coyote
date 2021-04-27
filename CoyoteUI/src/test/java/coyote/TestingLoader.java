/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote;

import coyote.dx.Service;
import coyote.loader.Loader;
import coyote.loader.log.Log;


/**
 * This mimics a Service loader in CoyoteDX.
 */
public class TestingLoader extends Service implements Loader {
  Thread executor = null;




  /**
   * run the loader in the background
   */
  public void background() {
    executor = new Thread(new Executor(this));
    executor.setDaemon(true);
    executor.start();
    System.out.println("Running in " + executor.getName());
  }




  /**
   * @see coyote.loader.thread.ThreadJob#terminate()
   */
  @Override
  public void terminate() {
    Log.info("Terminating");
    super.terminate();
  }

  /**
   * Thing that runs the loader in a separate thread.
   */
  class Executor implements Runnable {
    Loader loader;




    public Executor(TestingLoader loader) {
      this.loader = loader;
    }




    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      loader.start();
    }
  }

}
