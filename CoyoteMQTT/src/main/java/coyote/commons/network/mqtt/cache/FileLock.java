package coyote.commons.network.mqtt.cache;

/**
 * This is used to obtain a lock that can be used to prevent other MQTT clients
 * using the same persistent store. If the lock is already held then an 
 * exception is thrown. 
 * 
 * <p>Some Java runtimes such as JME MIDP do not support file locking or even 
 * the Java classes that support locking. The class is coded to both compile 
 * and work on all Java runtimes. In Java runtimes that do not support locking 
 * it will look as though a lock has been obtained but in reality no lock has 
 * been obtained.</p> 
 */
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

import coyote.commons.network.mqtt.MQTT;


public class FileLock {
  private File lockFile;
  private RandomAccessFile file;
  private Object fileLock;




  /**
   * Creates an NIO FileLock on the specified file if supported by the runtime.
   *  
   * @param clientDir the a File of the directory to contain the lock file. 
   * @param lockFilename name of the the file to lock
   * 
   * @throws Exception if the lock could not be obtained for any reason
   */
  public FileLock( final File clientDir, final String lockFilename ) throws Exception {
    // Create a file to obtain a lock on. 
    lockFile = new File( clientDir, lockFilename );
    if ( MQTT.isClassAvailable( "java.nio.channels.FileLock" ) ) {
      try {
        file = new RandomAccessFile( lockFile, "rw" );
        Method m = file.getClass().getMethod( "getChannel", new Class[] {} );
        final Object channel = m.invoke( file, new Object[] {} );
        m = channel.getClass().getMethod( "tryLock", new Class[] {} );
        fileLock = m.invoke( channel, new Object[] {} );
      } catch ( final NoSuchMethodException nsme ) {
        fileLock = null;
      } catch ( final IllegalArgumentException iae ) {
        fileLock = null;
      } catch ( final IllegalAccessException iae ) {
        fileLock = null;
      }
      if ( fileLock == null ) {
        // Lock not obtained
        release();
        throw new Exception( "Problem obtaining file lock" );
      }
    }
  }




  /**
   * Releases the lock.
   */
  public void release() {
    try {
      if ( fileLock != null ) {
        final Method m = fileLock.getClass().getMethod( "release", new Class[] {} );
        m.invoke( fileLock, new Object[] {} );
        fileLock = null;
      }
    } catch ( final Exception e ) {
      // Ignore exceptions
    }
    if ( file != null ) {
      try {
        file.close();
      } catch ( final IOException e ) {}
      file = null;
    }

    if ( ( lockFile != null ) && lockFile.exists() ) {
      lockFile.delete();
    }
    lockFile = null;
  }

}
