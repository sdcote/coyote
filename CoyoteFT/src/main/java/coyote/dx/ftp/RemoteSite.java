/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx.ftp;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import coyote.commons.Assert;
import coyote.commons.UriUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.marshal.JSONMarshaler;


/**
 * This represents a FTP or SFTP connection.
 * 
 * <p>This is a facade to file transfer protocols allowing code to remain 
 * stable while the underlying implementations evolve.</p> 
 */
public class RemoteSite extends DataFrame {

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

  public static String FTP = "ftp";
  public static String SFTP = "sftp";

  public static final String HOST_FIELD = "hostname";
  public static final String PASS_FIELD = "password";
  public static final String PORT_FIELD = "port";
  public static final String PROTOCOL_FIELD = "protocol";
  public static final String PROXY_HOST_FIELD = "proxyHost";
  public static final String PROXY_PORT_FIELD = "proxyPort";
  public static final String PROXY_USER_FIELD = "proxyUser";
  public static final String USER_FIELD = "username";
  public static final String PROXY_PASS_FIELD = "proxyPassword";

  private volatile boolean opened = false;




  public RemoteSite() {
    setProtocol( FTP );
  }




  /**
   * Construct a RemoteSite from the given URI
   * 
   * <p><strong>NOTE:</strong> URI are often encoded to represent special 
   * characters. This has implications for passwords which contain special 
   * characters. If a password with special characters (:, #, &#38;, $, et. al.) 
   * is represented in the URI, it should be encoded to ensure proper 
   * representation. THis method assumes the username and password portion may 
   * be encoded and will attempt to perform a URL decode.</p>
   * 
   * @param uri The URI to parse to configure this RemoteSite instance
   */
  public RemoteSite( URI uri ) {
    Assert.notNull( uri, "URI cannot be null" );

    // Set the host and port from the URI
    setHost( UriUtil.getHost( uri ) );
    setPort( UriUtil.getPort( uri ) );

    // User names may contain special characters and might be URL encoded
    String username = UriUtil.getUser( uri );
    if ( username != null ) {
      setUsername( UriUtil.decodeString( username ) );
    }
    // Passwords often contain special characters and might be URL encoded too
    String password = UriUtil.getPassword( uri );
    if ( password != null ) {
      setPassword( UriUtil.decodeString( password ) );
    }

    // Set the protocol this remote site instance is to use
    if ( uri.getScheme() != null ) {
      setProtocol( uri.getScheme() );
    } else {
      setProtocol( FTP );
    }

  }




  public String getHost() {
    return getAsString( HOST_FIELD );
  }




  public void setHost( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( HOST_FIELD, value );
  }




  public String getPassword() {
    return getAsString( PASS_FIELD );
  }




  public void setPassword( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PASS_FIELD, value );
  }




  public int getPort() {
    try {
      return getAsInt( PORT_FIELD );
    } catch ( DataFrameException e ) {
      return 0;
    }
  }




  public void setPort( int value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PORT_FIELD, value );
  }




  public String getProtocol() {
    return getAsString( PROTOCOL_FIELD );
  }




  public void setProtocol( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PROTOCOL_FIELD, value );
  }




  public String getProxyHost() {
    return getAsString( PROXY_HOST_FIELD );
  }




  public void setProxyHost( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PROXY_HOST_FIELD, value );
  }




  public int getProxyPort() {
    try {
      return getAsInt( PROXY_PORT_FIELD );
    } catch ( DataFrameException e ) {
      return getProtocolPort( getProtocol() );
    }
  }




  public void setProxyPort( int value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PROXY_PORT_FIELD, value );
  }




  public String getProxyUser() {
    return getAsString( PROXY_USER_FIELD );
  }




  public void setProxyUser( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PROXY_USER_FIELD, value );
  }




  public String getUsername() {
    return getAsString( USER_FIELD );
  }




  public void setUsername( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( USER_FIELD, value );
  }




  public String getProxyPassword() {
    return getAsString( PROXY_PASS_FIELD );
  }




  public void setProxyPassword( String value ) {
    if ( opened )
      throw new IllegalStateException( "Cannot change open connection" );
    super.put( PROXY_PASS_FIELD, value );
  }




  public static int getProtocolPort( String protocolname ) {
    if ( protocolname != null ) {
      if ( FTP.equalsIgnoreCase( protocolname.trim() ) ) {
        return FTPUtil.DEFAULT_PORT;
      } else if ( SFTP.equalsIgnoreCase( protocolname.trim() ) ) {
        return SFTPUtil.DEFAULT_PORT;
      }
    }
    return 0;
  }




  /**
   * Sort the file entries by mtime, oldest first.
   * 
   * <p>This is often the preferred way to sort listings as older files tend 
   * to scroll off the top of the screen leaning the most recent nearest the 
   * command prompt.</p>
   * 
   * @param source The list of FileEntries to sort.
   * 
   * @return a new list of file entries
   */
  public List<RemoteFile> sortMtime( final List<RemoteFile> source ) {

    final List<RemoteFile> list = new ArrayList<RemoteFile>();
    for ( final RemoteFile entry : source ) {
      list.add( entry );
    }

    for ( int i = list.size() - 1; i >= 0; i-- ) {
      for ( int j = 0; j < i; j++ ) {
        if ( list.get( j ).getMtime() > list.get( j + 1 ).getMtime() ) {
          final RemoteFile temp = list.get( j );
          list.set( j, list.get( j + 1 ) );
          list.set( j + 1, temp );
        }
      }
    }
    return list;
  }




  /**
   * Sort the file entries by atime, oldest first.
   * 
   * <p>This is often the preferred way to sort listings as less frequently 
   * accessed  files tend to scroll off the top of the screen leaning the 
   * most recently accessed nearest the command prompt.</p>
   * 
   * @param source The list of FileEntries to sort.
   * 
   * @return a new list of file entries
   */
  public List<RemoteFile> sortAtime( final List<RemoteFile> source ) {

    final List<RemoteFile> list = new ArrayList<RemoteFile>();
    for ( final RemoteFile entry : source ) {
      list.add( entry );
    }

    for ( int i = list.size() - 1; i >= 0; i-- ) {
      for ( int j = 0; j < i; j++ ) {
        if ( list.get( j ).getAtime() > list.get( j + 1 ).getAtime() ) {
          final RemoteFile temp = list.get( j );
          list.set( j, list.get( j + 1 ) );
          list.set( j + 1, temp );
        }
      }
    }
    return list;
  }




  /**
   * Return a list of file on this remote site
   * 
   * @param directory the directory to list
   * 
   * @return a list of RemoteFile object representing the contents of the remote directory
   * 
   * @throws FileTransferException if problems were encountered during retrieval
   */
  public List<RemoteFile> listFiles( final String directory ) throws FileTransferException {
    if ( !opened )
      open();

    if ( FTP.equalsIgnoreCase( getProtocol() ) ) {
      return FTPUtil.listFiles( this, directory );
    } else if ( SFTP.equalsIgnoreCase( getProtocol() ) ) {
      return SFTPUtil.listFiles( this, directory );
    }

    return null;
  }




  /**
   * Return the attribute of the file on the remote system.
   * 
   * @param filename can be a file or a directory
   * 
   * @return file statistics for the named file or null if that file does not exist
   * 
   * @throws FileTransferException if connection problems occurred
   */
  public FileAttributes getAttributes( final String filename ) throws FileTransferException {
    if ( !opened )
      open();

    if ( FTP.equalsIgnoreCase( getProtocol() ) ) {
      return FTPUtil.getAttributes( this, filename );
    } else if ( SFTP.equalsIgnoreCase( getProtocol() ) ) {
      return SFTPUtil.getAttributes( this, filename );
    }

    return null;
  }




  /**
   * This retrieves a file from this remote site to a local file.
   * 
   * @param remote the path to the remote file to retrieve
   * @param local path to the local file
   * 
   * @return true if the file is transferred, false if it did not.
   */
  public boolean retrieveFile( String remote, String local ) {
    if ( !opened )
      open();
    if ( FTP.equalsIgnoreCase( getProtocol() ) ) {
      return FTPUtil.retrieveFile( this, remote, local );
    } else if ( SFTP.equalsIgnoreCase( getProtocol() ) ) {
      return SFTPUtil.retrieveFile( this, remote, local );
    }
    return false;
  }




  /**
   * Publish a local file to this site to the remote location
   * 
   * @param local fully qualified path name of the file to publish
   * @param remote fully qualified pathname on the remote site
   * 
   * @return true if the file transfer succeeded, false otherwise.
   */
  public boolean publishFile( String local, String remote ) {
    if ( !opened )
      open();
    if ( FTP.equalsIgnoreCase( getProtocol() ) ) {
      return FTPUtil.publishFile( this, local, remote );
    } else if ( SFTP.equalsIgnoreCase( getProtocol() ) ) {
      return SFTPUtil.publishFile( this, local, remote );
    }
    return false;
  }




  public void open() {
    opened = true;
  }




  public void close() {
    opened = false;
    if ( FTP.equalsIgnoreCase( getProtocol() ) ) {
      FTPUtil.close( this );
    } else if ( SFTP.equalsIgnoreCase( getProtocol() ) ) {
      SFTPUtil.close( this );
    }
  }




  /**
   * This is a very simple string representation of this remote site.
   * 
   * <p>This will not expose the password or proxy password fields. If they 
   * exist, they will show as "-HIDDEN-".</p>
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return sanitize( (DataFrame)this.clone() ).toString();
  }




  /**
   * JSON representation of this remote site.
   * 
   * <p>This will not expose the password or proxy password fields. If they 
   * exist, they will show as "-HIDDEN-".</p>
   * 
   * @return single line of this site in JSON
   */
  public String toFormattedString() {
    return JSONMarshaler.toFormattedString( sanitize( (DataFrame)this.clone() ) );
  }




  /**
   * Clone the given frame and overwrite the password fields to protect 
   * sensitive data.
   *  
   * @param frame The frame to close
   * 
   * @return a copy of the given frame with the sensitive fields "-HIDDEN-" 
   *         from view
   */
  private DataFrame sanitize( DataFrame frame ) {
    DataFrame retval = (DataFrame)this.clone();
    if ( retval.contains( PASS_FIELD ) ) {
      retval.put( PASS_FIELD, "-HIDDEN-" );
    }

    if ( retval.contains( PROXY_PASS_FIELD ) ) {
      retval.put( PROXY_PASS_FIELD, "-HIDDEN-" );
    }

    return retval;
  }




  /**
   * This retrieves a directory from this remote site to a local directory.
   * 
   * @param remote the path to the remote directory to retrieve
   * @param local path to the local directory
   * @param pattern RegEx to be used in selecting files (null results in everything)
   * @param recurse flag indicating sub directories are to be included
   * @param preserve flag indicating the hierarchy of the recursed directories should be preserved
   * @param delete flag indicating sucessfully retried files should be deleted from the server
   * 
   * @return true if the directory is transferred, false if it did not.
   */
  public boolean retrieveDirectory( String remote, String local, String pattern, boolean recurse, boolean preserve, boolean delete ) {
    if ( !opened )
      open();
    if ( FTP.equalsIgnoreCase( getProtocol() ) ) {
      return FTPUtil.retrieveDirectory( this, remote, local, pattern, recurse, preserve, delete );
    } else if ( SFTP.equalsIgnoreCase( getProtocol() ) ) {
      return SFTPUtil.retrieveDirectory( this, remote, local, pattern, recurse, preserve, delete );
    }
    return false;
  }

}
