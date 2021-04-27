package coyote.dx.ftp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;

import coyote.commons.Assert;
import coyote.commons.StringUtil;
import coyote.loader.log.Log;


public class FTPUtil {

  // Cached connections
  private static final Map<String, FTPClient> clientCache = new HashMap<String, FTPClient>();

  public static final int DEFAULT_PORT = 21;




  public static FTPClient getClient( final String user, final String password, final int port, final String host ) throws FileTransferException {

    synchronized( clientCache ) {

      final String key = ( "user:" + user + "_host:" + host + "_port:" + port );

      final FTPClient ftp = clientCache.get( key );

      if ( ( ftp != null ) && ftp.isConnected() ) {
        return ftp;
      } else {
        final FTPClient ftpClient = FTPUtil.openNewClient( host, port, user, password );
        clientCache.put( key, ftpClient );
        return ftpClient;
      }
    }
  }




  private static FTPClient openNewClient( String host, int port, String user, String password ) {
    final FTPClient ftp;

    // retrieve proxy data from system properties
    if ( StringUtil.isNotBlank( System.getProperty( RemoteSite.PROXY_HOST ) ) ) {
      Log.debug( "Using HTTP proxy server: " + System.getProperty( RemoteSite.PROXY_HOST ) );

      // See if NTLM support is needed by the existence of a domain...this does 
      // not always work as the NTLM proxy protocol is not widely supported by 
      // products outside of Micro$oft
      String proxyuser = null;
      if ( StringUtil.isNotBlank( System.getProperty( RemoteSite.PROXY_DOMAIN ) ) ) {
        proxyuser = System.getProperty( RemoteSite.PROXY_DOMAIN ) + "\\" + System.getProperty( RemoteSite.PROXY_USER );
      } else {
        proxyuser = System.getProperty( RemoteSite.PROXY_USER );
      }

      // Create a proxy-aware FTP client
      ftp = new FTPHTTPClient( System.getProperty( RemoteSite.PROXY_HOST ), Integer.parseInt( System.getProperty( RemoteSite.PROXY_PORT ) ), proxyuser, System.getProperty( RemoteSite.PROXY_PASSWORD ) );
    } else {
      // Create a standard FTP client
      ftp = new FTPClient();
    }

    try {
      int reply;
      if ( port > 0 ) {
        ftp.connect( host, port );
      } else {
        ftp.connect( host );
      }
      Log.debug( "Connected to " + host + " on " + ( port > 0 ? port : ftp.getDefaultPort() ) );

      // After connection attempt, you should check the reply code to verify
      // success.
      reply = ftp.getReplyCode();

      if ( !FTPReply.isPositiveCompletion( reply ) ) {
        ftp.disconnect();
        Log.error( "FTP server refused connection. reply code: " + reply );
      } else {
        if ( !ftp.login( user, password ) ) {
          ftp.logout();
          Log.error( "Login failed" );
          if ( ftp.isConnected() ) {
            try {
              ftp.disconnect();
            } catch ( IOException f ) {
              // do nothing
            }
          }
          return null;
        }

        Log.debug( "Remote system is " + ftp.getSystemType() );

        ftp.setFileType( FTP.BINARY_FILE_TYPE );

        ftp.enterLocalPassiveMode();

      }
    } catch ( IOException e ) {
      if ( ftp.isConnected() ) {
        try {
          ftp.disconnect();
        } catch ( IOException ignore ) {}
      }
      Log.error( "Could not connect to server." );
      e.printStackTrace();
    }

    return ftp;
  }




  public static List<RemoteFile> listFiles( RemoteSite cfg, String dir ) {

    FTPClient ftp;
    final List<RemoteFile> fileListings = new ArrayList<RemoteFile>();

    try {
      ftp = FTPUtil.getClient( cfg.getUsername(), cfg.getPassword(), cfg.getPort(), cfg.getHost() );

      for ( FTPFile file : ftp.listFiles( dir ) ) {

        final FileAttributes attr = new FileAttributes();

        // set the last modified time (FTP does not always report access time)
        attr.setModifiedTime( (int)( file.getTimestamp().getTimeInMillis() / 1000 ) );

        // Set the file size
        attr.setSize( file.getSize() );

        try {
          attr.setUID( Integer.parseInt( file.getUser() ) );
        } catch ( NumberFormatException e ) {
          attr.setUser( file.getUser() );
        }

        try {
          attr.setGID( Integer.parseInt( file.getGroup() ) );
        } catch ( NumberFormatException e ) {
          attr.setGroup( file.getGroup() );
        }

        //        attr.setFLAGS( lsEntry.getAttrs().getFlags() );
        //        attr.setPERMISSIONS( lsEntry.getAttrs().getPermissions() );

        // Add a new RemoteFile to the list
        fileListings.add( new RemoteFile( dir + "/" + file.getName(), attr ) );

      }
    } catch ( FileTransferException | IOException e ) {
      e.printStackTrace();
    }

    return fileListings;
  }




  public static void close( RemoteSite cfg ) {
    FTPClient client;
    try {
      client = FTPUtil.getClient( cfg.getUsername(), cfg.getPassword(), cfg.getPort(), cfg.getHost() );
      if ( client != null ) {
        try {
          client.logout();
          client.disconnect();
        } catch ( IOException ignore ) {}
      }
    } catch ( FileTransferException ignore ) {}
  }




  /**
   * 
   * @param cfg the remote site with
   * @param remote Name of the remote file to retrieve
   * @param local name of the local file to which the file is to be written
   * 
   * @return true when the file transfers successfully, false if not.
   */
  public static boolean retrieveFile( RemoteSite cfg, String remote, String local ) {
    boolean retval = false;
    FTPClient ftp;

    try {
      ftp = FTPUtil.getClient( cfg.getUsername(), cfg.getPassword(), cfg.getPort(), cfg.getHost() );

      OutputStream output;

      output = new FileOutputStream( local );

      retval = ftp.retrieveFile( remote, output );

      output.close();

    } catch ( FileTransferException | IOException e ) {
      e.printStackTrace();
    }
    return retval;
  }




  public static FileAttributes getAttributes( RemoteSite site, String filename ) throws FileTransferException {
    Assert.notNull( site, "FTP remote site cannot be null" );
    Assert.notNull( filename, "FTP filename cannot be null" );
    //RemoteFile retval = null;

    throw new UnsupportedOperationException( "FTP stat is not supported" );
    //return retval;
  }




  public static boolean publishFile( RemoteSite remoteSite, String local, String remote ) {
    throw new UnsupportedOperationException( "FTP publish is not supported" );
  }




  public static boolean retrieveDirectory( RemoteSite remoteSite, String remote, String local, String pattern, boolean recurse, boolean preserve, boolean delete ) {
    // TODO Auto-generated method stub
    return false;
  }

}
