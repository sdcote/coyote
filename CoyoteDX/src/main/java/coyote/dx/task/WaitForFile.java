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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import coyote.commons.FileUtil;
import coyote.commons.Glob;
import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;


/**
 * This task will wait for a file to arrive and to be readable.
 * 
 * Absolute file
 * 
 * Glob patterns /usr/var/inbound/orders*.dat will look in "/usr/var/inbound/" 
 * for any files matching the pattern of "orders*.dat" 
 * 
 * watch a directory:
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
public class WaitForFile extends AbstractFileTask {
  private static final long WAIT_TIME = 1000;
  Glob globber = null;




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    final String pattern = getString( ConfigTag.FILE );
    globber = new Glob( pattern );

    File directoryToWatch = getDirectory();
    if ( directoryToWatch.exists() ) {
      // First get a list of all the files in the directory and look for matches
      File[] files = directoryToWatch.listFiles();

      for ( File file : files ) {
        Log.debug( "Checking " + file.getName() );
        if ( globber.isFileMatched( file.getName() ) ) {
          Log.debug( "Found a matching file :" + file.getAbsolutePath() );
          return;
        }
      }

      // If no match, setup a directory watch
      WatchService watchService;
      try {
        watchService = FileSystems.getDefault().newWatchService();

        Path path = Paths.get( directoryToWatch.getAbsolutePath() );
        Log.debug( "Watching " + path.getFileName() );
        path.register( watchService, StandardWatchEventKinds.ENTRY_CREATE );
        WatchKey key;
        try {
          while ( ( key = watchService.take() ) != null ) {
            for ( WatchEvent<?> event : key.pollEvents() ) {
              String filename = event.context().toString();
              Log.trace( event.kind() + ": " + filename );
              if ( StandardWatchEventKinds.ENTRY_CREATE.equals( event.kind() ) ) {
                if ( globber.isFileMatched( filename ) ) {
                  File matchedFile = new File( filename );
                  Log.debug( "Found " + matchedFile.getAbsolutePath() );

                  if ( !matchedFile.canWrite() ) {
                    Log.debug( "Waiting for file to become available for processing" );
                    // use write permission because it is a better judge of a file being closed and complete
                    while ( !matchedFile.canWrite() ) {
                      try {
                        Thread.sleep( WAIT_TIME );
                      } catch ( InterruptedException ignore ) {
                        getContext().setError( "Interrupted while waiting for file to be available" );
                        return;
                      }
                    } // wait for availability
                  } // available
                  Log.debug( "File ready for processing: " + matchedFile.getAbsolutePath() );
                  return;
                } // file match 
              }
            }
            key.reset();
          }
        } catch ( InterruptedException e ) {
          getContext().setError( "Interrupted while waiting for file" );
          return;
        }
      } catch ( IOException e ) {
        throw new TaskException( "Problems monitoring directory: " + directoryToWatch.getAbsolutePath(), e );
      }
    } else {
      throw new TaskException( "Invalid directory to watch: " + directoryToWatch.getAbsolutePath() );
    }

  }




  private File getDirectory() {
    File retval = null;
    final String directory = getString( ConfigTag.DIRECTORY );
    if ( StringUtil.isBlank( directory ) ) {
      final String filename = getString( ConfigTag.FILE );
      String parent = FileUtil.getPath( filename );
      if ( StringUtil.isNotBlank( parent ) ) {
        retval = new File( parent );
      } else {
        retval = FileUtil.getCurrentWorkingDirectory();
      }
    } else {
      retval = new File( directory );
    }
    return retval;
  }




  /**
   * Test of the WatchService concept.
   * @param args
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main( String[] args ) throws IOException, InterruptedException {
    WatchService watchService = FileSystems.getDefault().newWatchService();

    Path path = Paths.get( System.getProperty( "user.dir" ) );
    System.out.println( "Watching " + path.getFileName() );

    path.register( watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY );

    WatchKey key;
    while ( ( key = watchService.take() ) != null ) {
      for ( WatchEvent<?> event : key.pollEvents() ) {
        System.out.println( "Event kind:" + event.kind() + ". File affected: " + event.context() + "." );
      }
      key.reset();
    }
  }
  
}
