package coyote.dx.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import coyote.commons.Assert;
import coyote.commons.CollectionUtil;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.CFT;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


public class SFTPUtil {

  public static final int DEFAULT_PORT = 22;

  /** System property which specifies the user name for the proxy server */
  public static final String PROXY_USER = "http.proxyUser";

  /** System property which specifies the user password for the proxy server */
  public static final String PROXY_PASSWORD = "http.proxyPassword";

  /** System property which specifies the proxy server host name */
  public static final String PROXY_HOST = "http.proxyHost";

  /** System property which specifies the port of the proxy server */
  public static final String PROXY_PORT = "http.proxyPort";

  /** System property which specifies the domain of the authentication (NTLM) */
  public static final String PROXY_DOMAIN = "http.proxyDomain";

  // Cached connections
  private static final Map<String, ChannelSftp> connectionCache = new HashMap<String, ChannelSftp>();




  /**
   * Returned a cached connection or create and cache a connection for the 
   * given arguments.
   * 
   * @param user name of the user
   * @param password password of the user
   * @param port port on the host
   * @param host host name
   * 
   * @return an open connection to the SFTP server
   * 
   * @throws FileTransferException If a connection could not be made
   */
  public static ChannelSftp getConnection( final String user, final String password, final int port, final String host ) throws FileTransferException {

    synchronized( connectionCache ) {

      final String key = ( "user:" + user + "_host:" + host + "_port:" + port );

      final ChannelSftp cachedChannelSftp = connectionCache.get( key );

      if ( ( cachedChannelSftp != null ) && !cachedChannelSftp.isClosed() && cachedChannelSftp.isConnected() ) {
        // returned cached connection
        return cachedChannelSftp;
      } else {

        // clean up the disconnected channel
        if ( cachedChannelSftp != null ) {
          try {
            if ( cachedChannelSftp.getSession() != null ) {
              cachedChannelSftp.getSession().disconnect();
            }
            cachedChannelSftp.exit();
          } catch ( Exception ignore ) {}
        }

        // create a new connection
        final ChannelSftp channelSftp = SFTPUtil.openNewConnection( host, port, user, password );

        // cache it for later
        connectionCache.put( key, channelSftp );
        return channelSftp;
      }
    }
  }




  /**
   * Removes the connection matching the arguments.
   * 
   * @param user name of the user
   * @param password password of the user
   * @param port port on the host
   * @param host host name
   * 
   * @return the cached connection or null if not connection matching the given arguments were found.
   */
  public static ChannelSftp removeConnection( final String user, final String password, final int port, final String host ) {
    synchronized( connectionCache ) {
      return connectionCache.remove( "user:" + user + "_host:" + host + "_port:" + port );
    }
  }




  /**
  * Open SFTP Channel using the authentication provided
  *
  * @param hostName
  * @param portNumber
  * @param userName
  * @param password
  * 
  * @return a connection to the FTP server
  * 
  * @throws FileTransferException
  */
  private static ChannelSftp openNewConnection( final String hostName, final int portNumber, final String userName, final String password ) throws FileTransferException {
    Assert.notNull( hostName, "SFTP hostname cannot be null" );
    Assert.notNull( userName, "SFTP username cannot be null" );
    Assert.notNull( password, "SFTP password cannot be null" );

    Log.debug( "Opening connection to ftp server [host=" + hostName + ", port=" + portNumber + ", userName=" + userName + "]" );

    // if there is no port number, default to port 22
    int port = portNumber > 0 ? portNumber : DEFAULT_PORT;

    Session session = null;
    ChannelSftp channelSftp = null;
    try {
      final JSch jsch = new JSch();
      session = jsch.getSession( userName, hostName, port );

      final String proxyUser = System.getProperty( PROXY_USER );
      final String proxyPass = System.getProperty( PROXY_PASSWORD );
      final String proxyHost = System.getProperty( PROXY_HOST );
      final String proxyPort = System.getProperty( PROXY_PORT );
      final String proxyDomain = System.getProperty( PROXY_DOMAIN );

      // Support for proxy if we have a proxy host and port
      if ( StringUtil.isNotBlank( proxyPort ) && StringUtil.isNotBlank( proxyHost ) ) {

        // create a new proxy
        ProxyHTTP proxy = new ProxyHTTP( proxyHost, Integer.parseInt( proxyPort ) );

        // Setup proxy authentication if there is a username and password
        if ( StringUtil.isNotBlank( proxyUser ) && StringUtil.isNotBlank( proxyPass ) ) {

          // if there is a domain specified, put the username in the format of 
          // a "Down-Level Logon Name"...
          if ( StringUtil.isNotBlank( proxyDomain ) ) {
            String ntlmUser = proxyDomain + "\\" + proxyUser;
            proxy.setUserPasswd( ntlmUser, proxyPass );
          } else {
            // otherwise just use the usename and password
            proxy.setUserPasswd( proxyUser, proxyPass );
          }
        } // if auth

        // set the proxy in the session
        session.setProxy( proxy );
      }

      session.setPassword( password );
      session.setUserInfo( new LocalUserInfo() );
      session.connect();

      channelSftp = (ChannelSftp)session.openChannel( "sftp" );
      channelSftp.connect();

      return channelSftp;
    } catch ( final Exception exception ) {
      try {
        if ( channelSftp != null ) {
          channelSftp.disconnect();
          channelSftp.getSession().disconnect();
        }
      } catch ( final Exception ee ) {
        Log.debug( "Error disconnection:" + ee.getMessage() );
      }
      try {
        if ( session != null ) {
          session.disconnect();
        }
      } catch ( final Exception ee ) {
        Log.debug( "Error disconnection:" + ee.getMessage() );
      }

      final String message = ( "Error opening connection to ftp server [host=" + hostName + ", port=" + portNumber + ", userName=" + userName + "] - " );
      throw new FileTransferException( message + exception.getMessage() );
    }
  }




  /**
   * Get a list of files in the given directory on a remote site.
   * 
   * @param site The remote site on which the directory exists
   * @param directory the name of the directory to query
   * 
   * @return a List of RemoteFile objects representing files on the remote site
   * in the given directory.
   * 
   * @throws FileTransferException if the SFTP connection threw an exception 
   * during processing
   */
  public static List<RemoteFile> listFiles( RemoteSite site, final String directory ) throws FileTransferException {
    Assert.notNull( site, "SFTP remote site cannot be null" );
    Assert.notNull( directory, "SFTP directory cannot be null" );

    ChannelSftp channelSftp = SFTPUtil.getConnection( site.getUsername(), site.getPassword(), site.getPort(), site.getHost() );
    Log.debug( LogMsg.createMsg( CFT.MSG, "Listing remote files in directory={}", directory ) );

    final List<RemoteFile> fileListings = new ArrayList<RemoteFile>();

    try {
      channelSftp.ls( directory, new LsEntrySelector() {
        @Override
        public int select( final LsEntry lsEntry ) {
          final FileAttributes attr = new FileAttributes();
          attr.setAccessTime( lsEntry.getAttrs().getATime() );
          attr.setModifiedTime( lsEntry.getAttrs().getMTime() );
          attr.setFLAGS( lsEntry.getAttrs().getFlags() );
          attr.setPERMISSIONS( lsEntry.getAttrs().getPermissions() );
          attr.setSize( lsEntry.getAttrs().getSize() );
          attr.setUID( lsEntry.getAttrs().getUId() );
          attr.setGID( lsEntry.getAttrs().getGId() );
          RemoteFile rfile = new RemoteFile( directory + "/" + lsEntry.getFilename(), attr );
          fileListings.add( rfile ); // add the remote file to the list
          return LsEntrySelector.CONTINUE;
        }
      } );
    } catch ( final SftpException e ) {
      final String msg = String.format( "Error listing directory '%s'", directory );
      Log.error( msg );
      throw new FileTransferException( msg, e );
    }

    return fileListings;

  }




  /**
   * Retrieve a list of files from the given directory.
   * 
   * @param channelSftp the channel to use
   * @param directory the absolute path of the directory to list
   * @param pattern the regex pattern to match the full filename (absolute 
   *        path), or null to accept everything
   * @param recurse flag indicating recursive calls to sub directories
   * 
   * @return a list of files from the directory
   * 
   * @throws FileTransferException
   */
  @SuppressWarnings("unchecked")
  private static List<RemoteFile> listFiles( final ChannelSftp channelSftp, final String directory, final String pattern, final boolean recurse ) throws FileTransferException {
    Assert.notNull( channelSftp );
    Assert.notNull( directory );

    Pattern regex = null;
    if ( StringUtil.isNotBlank( pattern ) ) {
      regex = Pattern.compile( pattern );
    }

    Log.debug( "Listing remote files [directory=" + directory + ", recurse=" + recurse + "]" );

    final List<RemoteFile> fileListings = new ArrayList<RemoteFile>();

    Vector<LsEntry> entries;
    try {
      entries = channelSftp.ls( directory );// unchecked

      String absolutePath = null;

      for ( LsEntry entry : entries ) {
        absolutePath = directory + "/" + entry.getFilename();
        // if there is no pattern, or there is a pattern and it matches
        if ( regex == null || ( regex != null && regex.matcher( absolutePath ).matches() ) ) {

          Buffer buf = new Buffer();
          buf.putInt( entry.getAttrs().getFlags() );
          buf.putLong( entry.getAttrs().getSize() );
          buf.putInt( entry.getAttrs().getUId() );
          buf.putInt( entry.getAttrs().getGId() );
          buf.putInt( entry.getAttrs().getPermissions() );
          buf.putInt( entry.getAttrs().getATime() );
          buf.putInt( entry.getAttrs().getMTime() );
          RemoteFile rfile = new RemoteFile( absolutePath, FileAttributes.getAttributes( buf ) );
          fileListings.add( rfile ); // add the remote file to the list
          if ( recurse && rfile.isDirectory() ) {
            fileListings.addAll( listFiles( channelSftp, rfile.getAbsolutePath(), pattern, recurse ) );
          }
        } else {
          Log.debug( "Regex '" + pattern + "' did not match " + absolutePath );
        }
      }

    } catch ( final SftpException e ) {
      final String message = ( "Error listing directory, [directory:" + directory + "]" );
      throw new FileTransferException( message, e );
    }

    return fileListings;
  }




  /**
   * Return the file attributes for the given file on the given remote site.
   * 
   * @param site the remote site to query
   * @param filename the name of the file (or directory) to query
   * 
   * @return a file attributes object for the given file or null if the file/directory does not exist or another file
   * 
   * @throws FileTransferException if there was a problem connection to the remote site
   */
  public static FileAttributes getAttributes( RemoteSite site, String filename ) throws FileTransferException {
    Assert.notNull( site, "SFTP remote site cannot be null" );
    Assert.notNull( filename, "SFTP filename cannot be null" );

    ChannelSftp channelSftp = null;

    try {
      channelSftp = SFTPUtil.getConnection( site.getUsername(), site.getPassword(), site.getPort(), site.getHost() );
    } catch ( Exception e1 ) {
      throw e1;
    }

    if ( channelSftp != null ) {
      Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.getting_file_attributes", filename ) );

      try {
        SftpATTRS stats = channelSftp.stat( filename );

        // create an array of bytes to be parsed by the FileAttributes 
        Buffer buf = new Buffer();
        buf.putInt( stats.getFlags() );
        buf.putLong( stats.getSize() );
        buf.putInt( stats.getUId() );
        buf.putInt( stats.getGId() );
        buf.putInt( stats.getPermissions() );
        buf.putInt( stats.getATime() );
        buf.putInt( stats.getMTime() );

        return FileAttributes.getAttributes( buf );

      } catch ( final SftpException e ) {
        Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.error_getting_attributes", filename, e.getClass().getSimpleName(), e.getMessage() ) );
      }
    }
    return null;
  }




  public static boolean retrieveFile( RemoteSite site, String remote, String local ) {
    try {
      transferToLocal( getConnection( site.getUsername(), site.getPassword(), site.getPort(), site.getHost() ), remote, local );
      return true;
    } catch ( final FileTransferException e ) {
      Log.error( e.getMessage() );
      return false;
    }
  }




  /**
   * This retrieves a directory from the given remote site to a local directory.
   * 
   * @param site the remote sire to query
   * @param remoteDir the path to the remote directory to retrieve
   * @param localDir path to the local directory
   * @param pattern RegEx to be used in selecting files (null results in everything)
   * @param recurse flag indicating sub directories are to be included
   * @param preserve flag indicating the hierarchy of the recursed directories should be preserved
   * @param delete flag indicating successfully retrieved files should be deleted from the server
   * 
   * @return true if the directory is transferred, false if it did not.
   */
  public static boolean retrieveDirectory( RemoteSite site, String remoteDir, String localDir, String pattern, boolean recurse, boolean preserve, boolean delete ) {
    Assert.notNull( site );
    Assert.notNull( remoteDir );
    Assert.notNull( localDir );

    boolean retval = false;

    Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.transferring_directory", remoteDir, localDir, pattern, recurse, preserve, delete ) );

    File directory = new File( localDir );

    if ( directory.exists() ) {
      if ( !directory.isDirectory() ) {
        Log.error( LogMsg.createMsg( CFT.MSG, "SFTP.file_reference_is_not_directory", directory.getAbsolutePath() ) );
        return false;
      }
    } else {
      try {
        FileUtil.makeDirectory( directory );
        Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.created_local_directory", directory.getAbsolutePath() ) );
      } catch ( IOException e ) {
        Log.error( LogMsg.createMsg( CFT.MSG, "SFTP.could_not_create_directory", directory ), e );
        return false;
      }
    }

    // This is a list of files copied successfully
    List<String> remoteFilesCopied = new ArrayList<String>();

    // reference to our SFTP connection
    ChannelSftp channelSftp = null;

    try {
      channelSftp = getConnection( site.getUsername(), site.getPassword(), site.getPort(), site.getHost() );
      Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.retrieving_from_remote", remoteDir ) );

      // Get a listing of all the files
      final List<RemoteFile> fileListings = listFiles( channelSftp, remoteDir, pattern, recurse );

      if ( fileListings.size() > 0 ) {
        String localname = null;
        for ( RemoteFile remoteFile : fileListings ) {
          if ( !remoteFile.isDirectory() ) {

            if ( preserve ) {
              if ( remoteFile.getAbsolutePath().startsWith( remoteDir ) ) {
                String core = remoteFile.getAbsolutePath().substring( remoteDir.length() + 1, remoteFile.getAbsolutePath().length() );
                localname = directory.getAbsolutePath() + FileUtil.FILE_SEPARATOR + core;

              } else {
                localname = directory.getAbsolutePath() + FileUtil.FILE_SEPARATOR + remoteFile.getName();
              }
            } else {
              localname = directory.getAbsolutePath() + FileUtil.FILE_SEPARATOR + remoteFile.getName();
            }

            localname = FileUtil.normalizePath( localname );
            transferToLocal( channelSftp, remoteFile.getAbsolutePath(), localname );
            remoteFilesCopied.add( remoteFile.getAbsolutePath() );
          }

        }
      } else {
        Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.no_matching_files_found", remoteDir, pattern, recurse ) );
      }
      // we made it all the way through
      retval = true;

    } catch ( FileTransferException e ) {
      e.printStackTrace();
    }
    finally {
      if ( delete && channelSftp != null ) {
        for ( String remoteFile : remoteFilesCopied ) {
          try {
            deleteRemoteFile( channelSftp, remoteFile );
          } catch ( FileTransferException e ) {
            Log.error( LogMsg.createMsg( CFT.MSG, "SFTP.could_not_delete_file", remoteFile, e.getMessage() ) );
          }
        }

      }
    }

    return retval;
  }




  /**
   * Delete the remote file from the FTP channel
   *
   * @param channelSftp
   * @param file
   * 
   * @throws FileTransferException
   */
  private static void deleteRemoteFile( final ChannelSftp channelSftp, final String file ) throws FileTransferException {
    Assert.notNull( channelSftp );
    Assert.notNull( file );

    Log.debug( "Deleting remote file [file=" + file + "]" );

    try {
      channelSftp.rm( file );
    } catch ( final SftpException sftpException ) {
      throw new FileTransferException( LogMsg.createMsg( CFT.MSG, "SFTP.error_removing_file", file ).toString(), sftpException );
    }
  }




  public static boolean publishFile( RemoteSite site, String local, String remote ) {
    try {
      transferToRemote( getConnection( site.getUsername(), site.getPassword(), site.getPort(), site.getHost() ), local, remote );
      return true;
    } catch ( final FileTransferException e ) {
      Log.error( e.getMessage() );
      return false;
    }
  }




  /**
  * Transfer Remote file to the local file system
  *
  * @param channelSftp
  * @param sourceFile
  * @param targetFile
  * 
  * @throws FileTransferException
  */
  private static void transferToLocal( final ChannelSftp channelSftp, final String sourceFile, final String targetFile ) throws FileTransferException {
    Assert.notNull( channelSftp );
    Assert.notNull( sourceFile );
    Assert.notNull( targetFile );

    Log.debug( "Transferring file to local [sourceFile=" + sourceFile + ", destinationPath=" + targetFile + "]" );
    FileUtil.makeParentDirectory( targetFile );
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream( targetFile );
      channelSftp.get( sourceFile, fileOutputStream );
    } catch ( final SftpException sftpException ) {
      final String message = ( "Error transfering files from ftp to local [sourceFile=" + sourceFile + ", destinationPath=" + targetFile + "]" );
      throw new FileTransferException( message, sftpException );
    } catch ( final FileNotFoundException fileNotFoundException ) {
      final String message = ( "Error transfering files from ftp to local [sourceFile=" + sourceFile + ", destinationPath=" + targetFile + "]" );
      throw new FileTransferException( message, fileNotFoundException );
    }
    finally {
      FileUtil.close( fileOutputStream );
    }
  }




  /**
   * Transfer the file from local System to the Remote Ftp Channel
   *
   * @param channelSftp
   * @param localFile
   * @param remoteFile
   * 
   * @throws FileTransferException
   */
  private static void transferToRemote( final ChannelSftp channelSftp, final String localFile, final String remoteFile ) throws FileTransferException {
    Assert.notNull( channelSftp );
    Assert.notNull( localFile );
    Assert.notNull( remoteFile );

    Log.debug( "Moving file to remote [sourcePath=" + localFile + ", remoteDestinationPath=" + remoteFile + "]" );

    String remoteDestinationDirectory = FileUtil.getPath( remoteFile );
    // If we are running on Windows, make sure the file separators are proper; 
    // it's the Internet after all
    remoteDestinationDirectory = remoteDestinationDirectory.replace( '\\', '/' );

    final String remoteDestinationFileName = FileUtil.getName( remoteFile );
    Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.destination_directory", remoteDestinationDirectory ) );
    Log.debug( LogMsg.createMsg( CFT.MSG, "SFTP.destination_file", remoteDestinationFileName ) );

    OutputStream outputStream = null;
    InputStream inputStream = null;
    try {

      // Create our directory if it doesn't exist
      SFTPUtil.createRemoteDirectory( channelSftp, remoteDestinationDirectory );
      // TODO: Maybe a check should be performed first?

      // change to that remote directory
      channelSftp.cd( remoteDestinationDirectory );

      outputStream = channelSftp.put( remoteDestinationFileName );
      inputStream = new FileInputStream( localFile );
      FileUtil.copy( inputStream, outputStream );
    } catch ( final SftpException | IOException e ) {
      //e.printStackTrace();
      final String message = ( "Error transferring file to sftp [sourcePath=" + localFile + ", remoteDestinationPath=" + remoteFile + "] - " + e.getMessage() );
      throw new FileTransferException( message, e );
    }
    finally {
      FileUtil.close( inputStream );
      FileUtil.close( outputStream );
    }
  }




  /**
   * Close and remove all connections for this remote site.
   *  
   * @param site the remote site to which all connections should be closed.
   */
  public static void close( RemoteSite site ) {
    try {
      ChannelSftp channelSftp = removeConnection( site.getUsername(), site.getPassword(), site.getPort(), site.getHost() );
      if ( channelSftp != null ) {
        if ( channelSftp.getSession() != null ) {
          channelSftp.getSession().disconnect();
        }
        channelSftp.exit();
      }
    } catch ( JSchException ignore ) {}
  }




  /**
   * Create the specified directory on the remote Ftp channel
   *
   * @param channelSftp the channel on which to issue the command
   * @param directory fully qualified directory path from root
   */
  private static void createRemoteDirectory( final ChannelSftp channelSftp, final String directory ) {
    Assert.notNull( channelSftp );
    Assert.notNull( directory );

    Log.debug( "Creating remote directory [directory=" + directory + "]" );

    try {
      channelSftp.cd( "/" );
      channelSftp.mkdir( directory );
    } catch ( final SftpException exception ) {
      Log.debug( "Could not create directory: " + directory + " - " + exception.getMessage() + " (maybe it already exists)" );
    }
  }




  /**
   * Close all the connections in the connection cache.
   */
  public static void clearConnectionCache() {
    synchronized( connectionCache ) {

      if ( CollectionUtil.isEmpty( connectionCache ) ) {
        return;
      }

      for ( final ChannelSftp channelSftp : connectionCache.values() ) {
        try {
          channelSftp.getSession().disconnect();
          channelSftp.exit();
        } catch ( final Exception ignore ) {}
      }

      connectionCache.clear();
    }
  }




  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

  //

  // Everything below is currently being reviewed for applicability

  //

  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
  // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /

  private static InputStream openFileInputStream( final ChannelSftp channelSftp, final String filePath ) throws FileTransferException {
    Assert.notNull( channelSftp );
    Assert.notNull( filePath );

    Log.debug( "Opening remote input stream [filePath=" + filePath + "]" );

    try {
      final InputStream inputStream = channelSftp.get( filePath );
      return inputStream;
    } catch ( final SftpException sftpException ) {
      final String message = ( "Error loading file from ftp [settingsFilePath=" + filePath + "]" );
      throw new FileTransferException( message, sftpException );
    }
  }




  /**
  * Return file listings for active accounts
  *
  * @param fileFilter
  * 
  * @return non-null file listings
  */
  private List<RemoteFile> listFiles( final ChannelSftp channelSftp, final String inDirectory, final SuffixFileFilter fileFilter ) {
    Assert.notNull( fileFilter );

    final List<RemoteFile> fileListing = new ArrayList<RemoteFile>();

    List<RemoteFile> accountFileListings = new ArrayList<RemoteFile>();
    try {
      accountFileListings = filterFiles( channelSftp, inDirectory, fileFilter );
    } catch ( final FileTransferException e ) {
      Log.error( "Problem listing files - " + e.getMessage() );
    }

    if ( CollectionUtil.isNotEmpty( accountFileListings ) ) {
      fileListing.addAll( accountFileListings );
    } else {
      Log.info( "No files found " );
    }

    return fileListing;
  }




  /**
   * Get a list of files in the given directory on a SFTP account which match
   * the given filter.
   *
   * @param channelSftp The connection to the SFTP server
   * @param directory the name of the directory to query
   * @param jobConfiguration the job configuration related to this request
   * @param fileFilter the object which will be used to filter out unwanted
   * filenames
   *
   * @return a List of FileListing objects representing files on the SFTP site
   * matching the given filter.
   *
   * @throws FileTransferException if the SFTP connection threw an exception during
   * processing
   */
  private List<RemoteFile> filterFiles( final ChannelSftp channelSftp, final String directory, final SuffixFileFilter fileFilter ) throws FileTransferException {
    Assert.notNull( channelSftp );
    Assert.notNull( directory );

    Log.debug( "Filtering remote files [directory=" + directory + ", fileFilter=" + fileFilter + "]" );

    final List<RemoteFile> fileListings = new ArrayList<RemoteFile>();
    try {
      // get a listing of all the files in the given directory on the SFTP
      // account filtering out all unwanted files using the given file filter
      channelSftp.ls( directory, new LsEntrySelector() {
        @Override
        public int select( final LsEntry lsEntry ) {

          // Create a remote file so the file filter will be able to query the entry
          final FileAttributes attr = new FileAttributes();
          attr.setAccessTime( lsEntry.getAttrs().getATime() );
          attr.setModifiedTime( lsEntry.getAttrs().getMTime() );
          attr.setFLAGS( lsEntry.getAttrs().getFlags() );
          attr.setPERMISSIONS( lsEntry.getAttrs().getPermissions() );
          attr.setSize( lsEntry.getAttrs().getSize() );
          attr.setUID( lsEntry.getAttrs().getUId() );
          attr.setGID( lsEntry.getAttrs().getGId() );
          RemoteFile rfile = new RemoteFile( directory + "/" + lsEntry.getFilename(), attr );

          // if there is no file filter or if the file filter accepts the file 
          if ( ( fileFilter == null ) || fileFilter.accept( rfile ) ) {
            fileListings.add( rfile ); // add the remote file to the list
          }
          return LsEntrySelector.CONTINUE;
        }
      } );
    } catch ( final SftpException e ) {
      final String message = ( "Error listing directory, [directory:" + directory + "]" );
      throw new FileTransferException( message, e );
    }

    return fileListings;
  }

  //

  //

  /**
   * 
   */
  static class LocalUserInfo implements UserInfo {

    @Override
    public String getPassword() {
      return null;
    }




    @Override
    public boolean promptYesNo( final String str ) {
      return true;
    }




    @Override
    public String getPassphrase() {
      return null;
    }




    @Override
    public boolean promptPassphrase( final String message ) {
      return true;
    }




    @Override
    public boolean promptPassword( final String message ) {
      return true;
    }




    @Override
    public void showMessage( final String message ) {
      // No message
    }
  }

}
