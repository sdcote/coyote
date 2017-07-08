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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
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
 * <p>This looks in a specific directory for files. It does not recurse into
 * subdirectories so it is easier to add backup files to subdirectories 
 * without triggering this task. This makes it easier to look for files in a 
 * directory, process them and place them in a subdirectory for easier file 
 * management.
 * 
 * <p>If a directory is configured that directory will be used. If there is no 
 * directory configured, the filename will be used to determine the directory 
 * so it should be an absolute path followed by a file glob expression.
 * 
 * <p>Directory and File<pre>
 * "WaitForFile": {"directory": "[#$wrkdir#]","filename": "*.csv"}</pre> 
 * 
 * <p>Absolute file<pre>
 * "WaitForFile": {"filename": "[#$wrkdir#][#$FS#]*.csv"}</pre> 
 * Glob patterns /usr/var/inbound/orders*.dat will look in "/usr/var/inbound/" 
 * for any files matching the pattern of "orders*.dat" 
 * 
 * <p><strong>NOTE:</string> Just because the file is there does not mean it 
 * is ready for processing. A file can appear in the file system but still be 
 * locked and unavailable to the data transfer job. It is possible to create a 
 * file in the directory and start streaming data to it over a period of time.
 * This is common in SCP and FTP files. The best approach is to perform some 
 * atomic operation such as a copy or move at the file system level once the 
 * data in the file has been completely written to it. DO NOT try to read FTP 
 * files from their incoming directory unless you know for sure the FTP server
 * only created the file after it has been closed. Always move the files into 
 * the watched directory with atomic file system operations after the file has 
 * been closed.  
 */
public class WaitForFile extends AbstractFileTask {
  private static final long WAIT_TIME = 3000;
  Glob globber = null;




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    File directoryToWatch = null;

    final String directory = getString( ConfigTag.DIRECTORY );
    final String pattern = getString( ConfigTag.FILE );

    // Determine our watch directory
    if ( StringUtil.isNotBlank( directory ) ) {
      directoryToWatch = new File( directory );
      if ( !directoryToWatch.isDirectory() ) {
        getContext().setError( "Configured directory is not valid: " + directoryToWatch.getAbsolutePath() );
        return;
      }
    } else {
      Log.debug( "No directory specified in configuration, using filename pattern" );
      String parent = FileUtil.getPath( pattern );
      if ( StringUtil.isNotBlank( parent ) ) {
        directoryToWatch = new File( parent );
        if ( !directoryToWatch.isDirectory() ) {
          getContext().setError( "Parent directory of filename is not valid: '" + parent + "' (" + directoryToWatch.getAbsolutePath() + ")" );
          return;
        }
      } else {
        Log.debug( "No directory specified in filename, using current working directory" );
        directoryToWatch = FileUtil.getCurrentWorkingDirectory();
      }
    }

    if ( !directoryToWatch.exists() ) {
      getContext().setError( "Configured directory does not exist: " + directoryToWatch.getAbsolutePath() );
      return;
    }
    if ( !directoryToWatch.canRead() ) {
      getContext().setError( "Configured directory is not readable: " + directoryToWatch.getAbsolutePath() );
      return;
    }

    String base = FileUtil.getFile( pattern );
    globber = new Glob( base );

    if ( directoryToWatch.exists() ) {
      // First get a list of all the files in the directory and look for matches
      File[] files = directoryToWatch.listFiles();

      for ( File file : files ) {
        Log.debug( "Checking " + file.getName() + " against " + globber );
        if ( globber.isFileMatched( file.getName() ) ) {
          Log.debug( "Found a matching file :" + file.getAbsolutePath() );
          waitForAvailable( file );
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
                  waitForAvailable( matchedFile );
                  return;
                }
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




  /**
   * Make an attempt to ensure the file is available for reading.
   * 
   * <p>This is a blocking call and will only return one it is possible to 
   * open the file for RW access.
   * 
   * @param file the file to check
   */
  private void waitForAvailable( File file ) {
    if ( isLocked( file ) ) {
      Log.debug( "Waiting for file to become available for processing" );
      while ( isLocked( file ) ) {
        try {
          Thread.sleep( WAIT_TIME );
        } catch ( InterruptedException ignore ) {
          getContext().setError( "Interrupted while waiting for file to be available" );
          return;
        }
      }
    }
    Log.debug( "File ready for processing: " + file.getAbsolutePath() );
  }




  /**
   * Best effort guess as to whether or not the given file is locked.
   * 
   * <p>Not all Java implementations or operating systems support file locking 
   * in a uniform manner so this may not work in all situations.
   * 
   * <p>Returns false if the file does not exist as the file technically does 
   * not have a lock.
   * 
   * @param file the file to test
   * 
   * @return false only if this process can obtain a lock on this file, 
   *         otherwise this returns true indicating either the file is locked 
   *         by another process or an error occurred in trying to obtain a 
   *         lock on the file indicating a logical lock on the file.
   */
  private boolean isLocked( File file ) {
    if ( file == null || !file.exists() ) {
      return false;
    }

    try (FileChannel channel = new RandomAccessFile( file, "rw" ).getChannel()) {
      FileLock lock = null;
      try {
        lock = channel.tryLock();
        return false;
      } catch ( OverlappingFileLockException inner ) {
        inner.printStackTrace();
        return true;
      }
      finally {
        if ( lock != null ) {
          lock.release();
        }
      }
    } catch ( Exception e ) {
      return true;
    }

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
