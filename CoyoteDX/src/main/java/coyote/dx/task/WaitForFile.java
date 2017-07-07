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
package coyote.dx.task;

import java.io.File;

import coyote.dx.TaskException;


/**
 * This task will wait for a file to arrive and to be readable.
 * 
 * Absolute file
 * 
 * File patterns /usr/var/inbound/orders*.dat will look in "/usr/var/inbound/" 
 * for any files matching the pattern of "orders*.dat" 
 * 
 * watch a directory:
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
public class WaitForFile extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    File fileToCopy = new File( getFile() );
    int sleepTime = 1000;
    while ( !fileToCopy.canWrite() ) {
      try {
        Thread.sleep( sleepTime );
      } catch ( InterruptedException ignore ) {
        getContext().setError( "Interrupted while waiting for file" );
        return;
      }
    }

    // File is ready!

  }




  private String getFile() {
    return "SomeFileName.txt";
  }

}
