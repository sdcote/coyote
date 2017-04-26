package coyote.commons.network.mqtt.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


/**
 * An implementation of the {@link ClientCache} interface that provides
 * file based persistence.
 * 
 * A directory is specified when the Persistence object is created. When the persistence
 * is then opened (see {@link #open(String, String)}), a sub-directory is made beneath the base
 * for this client ID and connection key. This allows one persistence base directory
 * to be shared by multiple clients.
 * 
 * The sub-directory's name is created from a concatenation of the client ID and connection key
 * with any instance of '/', '\\', ':' or ' ' removed.
 */
public class FileCache implements ClientCache {
  private static final String MESSAGE_FILE_EXTENSION = ".msg";
  private static final String MESSAGE_BACKUP_FILE_EXTENSION = ".mbu";
  private static final String LOCK_FILENAME = ".lck";

  private final File dataDir;
  private File clientDir = null;
  private FileLock fileLock = null;

  private static final FilenameFilter FILE_FILTER = new FilenameFilter() {
    @Override
    public boolean accept( final File dir, final String name ) {
      return name.endsWith( MESSAGE_FILE_EXTENSION );
    }
  };




  public FileCache() {
    this( System.getProperty( "user.dir" ) );
  }




  /**
   * Create an file-based persistent data store within the specified directory.
   * 
   * @param directory the directory to use.
   */
  public FileCache( final String directory ) {
    dataDir = new File( directory );
  }




  /**
   * Checks whether the persistence has been opened.
   * @throws CacheException if the persistence has not been opened.
   */
  private void checkIsOpen() throws CacheException {
    if ( clientDir == null ) {
      throw new CacheException();
    }
  }




  @Override
  public void clear() throws CacheException {
    checkIsOpen();
    final File[] files = getFiles();
    for ( final File file : files ) {
      file.delete();
    }
  }




  @Override
  public void close() throws CacheException {

    synchronized( this ) {
      // checkIsOpen();
      if ( fileLock != null ) {
        fileLock.release();
      }

      if ( getFiles().length == 0 ) {
        clientDir.delete();
      }
      clientDir = null;
    }
  }




  @Override
  public boolean containsKey( final String key ) throws CacheException {
    checkIsOpen();
    final File file = new File( clientDir, key + MESSAGE_FILE_EXTENSION );
    return file.exists();
  }




  @Override
  public Cacheable get( final String key ) throws CacheException {
    checkIsOpen();
    Cacheable result;
    try {
      final File file = new File( clientDir, key + MESSAGE_FILE_EXTENSION );
      final FileInputStream fis = new FileInputStream( file );
      final int size = fis.available();
      final byte[] data = new byte[size];
      int read = 0;
      while ( read < size ) {
        read += fis.read( data, read, size - read );
      }
      fis.close();
      result = new CachedData( key, data, 0, data.length, null, 0, 0 );
    } catch ( final IOException ex ) {
      throw new CacheException( ex );
    }
    return result;
  }




  private File[] getFiles() throws CacheException {
    checkIsOpen();
    final File[] files = clientDir.listFiles( FILE_FILTER );
    if ( files == null ) {
      throw new CacheException();
    }
    return files;
  }




  private boolean isSafeChar( final char c ) {
    return Character.isJavaIdentifierPart( c ) || ( c == '-' );
  }




  /**
   * Returns all of the persistent data from the previously specified persistence directory.
   * 
   * @return all of the persistent data from the persistence directory.
   * 
   * @throws CacheException
   */
  @Override
  public Enumeration keys() throws CacheException {
    checkIsOpen();
    final File[] files = getFiles();
    final Vector result = new Vector( files.length );
    for ( final File file : files ) {
      final String filename = file.getName();
      final String key = filename.substring( 0, filename.length() - MESSAGE_FILE_EXTENSION.length() );
      result.addElement( key );
    }
    return result.elements();
  }




  @Override
  public void open( final String clientId, final String theConnection ) throws CacheException {

    if ( dataDir.exists() && !dataDir.isDirectory() ) {
      throw new CacheException();
    } else if ( !dataDir.exists() ) {
      if ( !dataDir.mkdirs() ) {
        throw new CacheException();
      }
    }
    if ( !dataDir.canWrite() ) {
      throw new CacheException();
    }

    final StringBuffer keyBuffer = new StringBuffer();
    for ( int i = 0; i < clientId.length(); i++ ) {
      final char c = clientId.charAt( i );
      if ( isSafeChar( c ) ) {
        keyBuffer.append( c );
      }
    }
    keyBuffer.append( "-" );
    for ( int i = 0; i < theConnection.length(); i++ ) {
      final char c = theConnection.charAt( i );
      if ( isSafeChar( c ) ) {
        keyBuffer.append( c );
      }
    }

    synchronized( this ) {
      if ( clientDir == null ) {
        final String key = keyBuffer.toString();
        clientDir = new File( dataDir, key );

        if ( !clientDir.exists() ) {
          clientDir.mkdir();
        }
      }

      try {
        fileLock = new FileLock( clientDir, LOCK_FILENAME );
      } catch ( final Exception e ) {
        //throw new MqttPersistenceException(MqttPersistenceException.REASON_CODE_PERSISTENCE_IN_USE);
      }

      // Scan the directory for .backup files. These will
      // still exist if the JVM exited during addMessage, before
      // the new message was written to disk and the backup removed.
      restoreBackups( clientDir );
    }
  }




  /**
   * Writes the specified persistent data to the previously specified 
   * persistence directory.
   * 
   * @param message
   * 
   * @throws CacheException
   */
  @Override
  public void put( final String key, final Cacheable message ) throws CacheException {
    checkIsOpen();
    final File file = new File( clientDir, key + MESSAGE_FILE_EXTENSION );
    final File backupFile = new File( clientDir, key + MESSAGE_FILE_EXTENSION + MESSAGE_BACKUP_FILE_EXTENSION );

    if ( file.exists() ) {
      // Backup the existing file so the overwrite can be rolled-back 
      final boolean result = file.renameTo( backupFile );
      if ( !result ) {
        backupFile.delete();
        file.renameTo( backupFile );
      }
    }
    try {
      final FileOutputStream fos = new FileOutputStream( file );
      fos.write( message.getHeaderBytes(), message.getHeaderOffset(), message.getHeaderLength() );
      if ( message.getPayloadBytes() != null ) {
        fos.write( message.getPayloadBytes(), message.getPayloadOffset(), message.getPayloadLength() );
      }
      fos.getFD().sync();
      fos.close();
      if ( backupFile.exists() ) {
        // The write has completed successfully, delete the backup 
        backupFile.delete();
      }
    } catch ( final IOException ex ) {
      throw new CacheException( ex );
    }
    finally {
      if ( backupFile.exists() ) {
        // The write has failed - restore the backup
        final boolean result = backupFile.renameTo( file );
        if ( !result ) {
          file.delete();
          backupFile.renameTo( file );
        }
      }
    }
  }




  /**
   * Deletes the data with the specified key from the previously specified persistence directory.
   */
  @Override
  public void remove( final String key ) throws CacheException {
    checkIsOpen();
    final File file = new File( clientDir, key + MESSAGE_FILE_EXTENSION );
    if ( file.exists() ) {
      file.delete();
    }
  }




  /**
   * Identifies any backup files in the specified directory and restores them
   * to their original file. This will overwrite any existing file of the same
   * name. This is safe as a stray backup file will only exist if a problem
   * occured whilst writing to the original file.
   * @param dir The directory in which to scan and restore backups
   */
  private void restoreBackups( final File dir ) throws CacheException {
    final File[] files = dir.listFiles( new FileFilter() {
      @Override
      public boolean accept( final File f ) {
        return f.getName().endsWith( MESSAGE_BACKUP_FILE_EXTENSION );
      }
    } );
    if ( files == null ) {
      throw new CacheException();
    }

    for ( final File file : files ) {
      final File originalFile = new File( dir, file.getName().substring( 0, file.getName().length() - MESSAGE_BACKUP_FILE_EXTENSION.length() ) );
      final boolean result = file.renameTo( originalFile );
      if ( !result ) {
        originalFile.delete();
        file.renameTo( originalFile );
      }
    }
  }
}
