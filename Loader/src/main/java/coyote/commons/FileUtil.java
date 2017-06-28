/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import coyote.loader.log.Log;


/**
 * This is group of static functions to work with files.
 */
public final class FileUtil {

  /** The path separator for the current platform. Defaults to ':' */
  public static final String PATH_SEPARATOR = System.getProperty( "path.separator", ":" );

  /** The file separator for the current platform. Defaults to '/' */
  public static final String FILE_SEPARATOR = System.getProperty( "file.separator", "/" );

  /** The Unix separator character. */
  public static final char UNIX_SEPARATOR = '/';

  /** The Windows separator character. */
  public static final char WINDOWS_SEPARATOR = '\\';

  /** The absolute path to the home directory of the user running the VM. */
  public static final String HOME = System.getProperty( "user.home" );

  /** The home directory of the user running the VM. */
  public static final File HOME_DIR = new File( FileUtil.HOME );

  /** The file URI to the home directory of the user running the VM. */
  public static final URI HOME_DIR_URI = FileUtil.getFileURI( FileUtil.HOME_DIR );

  /** The absolute path to the current working directory of the VM. */
  public static final String CURRENT = System.getProperty( "user.dir" );

  /** The current working directory of the VM */
  public static final File CURRENT_DIR = new File( FileUtil.CURRENT );

  /** The file URI to the current working directory of the VM */
  public static final URI CURRENT_DIR_URI = FileUtil.getFileURI( FileUtil.CURRENT_DIR );

  /** Represents 1 Kilo Byte ( 1024 ). */
  public final static long ONE_KB = 1024L;

  /** Represents 1 Mega Byte ( 1024^2 ). */
  public final static long ONE_MB = FileUtil.ONE_KB * 1024L;

  /** Represents 1 Giga Byte ( 1024^3 ). */
  public final static long ONE_GB = FileUtil.ONE_MB * 1024L;

  /** Represents 1 Tera Byte ( 1024^4 ). */
  public final static long ONE_TB = FileUtil.ONE_GB * 1024L;

  /** Standard number formatter. */
  public static NumberFormat FILE_LENGTH_FORMAT = NumberFormat.getInstance();

  /** Standard byte formatter. */
  public static DecimalFormat byteFormat = new DecimalFormat( "0.00" );

  /** A useful default buffer size ({@value}) */
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  /** The file copy buffer size ({@value}) for file copies */
  private static final long BIG_BUFFER_SIZE = ONE_MB * 10;

  private static final int EOF = -1;

  /** The MD5 Message Digest we use to calculate file hashes */
  static MessageDigest md;
  static {
    try {
      md = MessageDigest.getInstance( "MD5" );
    } catch ( NoSuchAlgorithmException e ) {}
  }




  /**
   * Private constructor because everything is static
   */
  private FileUtil() {}




  /**
   * Tests the given string to make sure it is a fully-qualified file name to a
   * file which exists and can be read.
   *
   * @param filename the name of the file to validate
   *
   * @return A file reference which has been validated as absolute, existing and readable
   */
  public static File validateFileName( final String filename ) {
    if ( filename != null ) {
      final File tempfile = new File( filename );

      if ( tempfile.isAbsolute() && tempfile.exists() && tempfile.canRead() ) {
        return tempfile;
      } else {
        return null;
      }
    }

    return null;
  }




  /**
   * Checks to see if the given directory name exists and is a readable
   * directory.
   *
   * @param directory
   *
   * @return A directory reference which has been validated as absolute, existing and readable
   */
  public static File validateDirectory( final String directory ) {
    if ( directory != null ) {
      final File tempfile = new File( directory );

      if ( tempfile.exists() && tempfile.isDirectory() && tempfile.canRead() ) {
        return tempfile;
      } else {
        return null;
      }
    }

    return null;
  }




  /**
   * Opens a file, reads it and returns the data as a string and closes the
   * file.
   *
   * @param fname - file to open
   *
   * @return String representing the file data
   */
  public static String fileToString( final String fname ) {
    return FileUtil.fileToString( new File( fname ) );
  }




  /**
   * Opens a file, writes out the given string then closes the file.
   *
   * @param text - string to write
   * @param fname - file to open
   * 
   * @return boolean whether or not the operation worked
   */
  public static boolean stringToFile( final String text, final String fname ) {
    if ( fname != null ) {
      final File file = new File( fname );

      try {
        FileUtil.write( file, text.getBytes( StringUtil.ISO8859_1 ) );

        return true;
      } catch ( final Exception ex ) {}
    }

    return false;
  }




  /**
   * Opens a file, writes out the given string then closes the file.
   *
   * @param text - string to write
   * @param fname - file to open
   * @param chrset - the name of the character set to use
   * 
   * @return boolean whether or not the operation worked, could be an IO error 
   *         or a bad character set name.
   */
  public static boolean stringToFile( final String text, final String fname, final String chrset ) {
    if ( fname != null ) {
      final File file = new File( fname );

      try {
        FileUtil.write( file, text.getBytes( chrset ) );
        return true;
      } catch ( final Exception ex ) {}
    }

    return false;
  }




  /**
   * CleanDirs is a utility method for cleaning up temporary directories, used
   * by various methods to hold and process files on the file system.
   *
   * <p>CleanDir takes a directory name as an argument, and checks for any
   * empty directories within that directory. If it finds any, it remove the
   * empty directory and checks again until no empty directories are found.</p>
   *
   * @param fileName
   *
   * @throws IOException
   */
  public static void cleanDirs( String fileName ) throws IOException {
    if ( !fileName.endsWith( "/" ) ) {
      fileName = fileName + "/";
    }

    final Vector<String> contents = FileUtil.getDir( fileName );
    String oneItem = null;
    File oneFile = null;

    for ( final Enumeration<String> e = contents.elements(); e.hasMoreElements(); ) {
      oneItem = e.nextElement();
      oneFile = new File( fileName + oneItem );

      if ( oneFile.isDirectory() ) {
        // Try cleaning it
        FileUtil.cleanDirs( fileName + oneItem );

        // If it's now empty...
        if ( FileUtil.getDir( fileName + oneItem ).size() == 0 ) {
          oneFile = new File( fileName + oneItem );

          if ( !oneFile.delete() ) {
            System.err.println( "Unable to delete directory " + oneItem );
          }
        }
      }
    }
  }




  /**
   * Utility method to copy a file from one place to another and delete the
   * original.
   *
   * <p>If we fail, we throw IOException, also reporting what username this
   * process is running with to assist System Admins in setting appropriate
   * permissions.</p>
   *
   * @param sourceFile Source file pathname
   * @param destFile Destination file pathname
   * 
   * @throws IOException If the copy fails due to an I/O error
   */
  public static void copyFile( final String sourceFile, final String destFile ) throws IOException {
    FileUtil.copyFile( new File( sourceFile ), new File( destFile ) );
  }




  /**
   * Utility method to copy a file from one place to another 
   *
   * <p>If we fail, we throw IOException, also reporting what username this
   * process is running with to assist System Admins in setting appropriate
   * permissions.</p>
   *
   * @param sourceFile Source file pathname
   * @param destFile Destination file pathname
   * @throws IOException If the copy fails due to an I/O error
   */
  public static void copyFile( final File sourceFile, final File destFile ) throws IOException {
    int onechar = 0;

    if ( sourceFile == null ) {
      throw new IOException( "Source file is null - cannot copy." );
    }

    if ( destFile == null ) {
      throw new IOException( "Destination file is null - cannot copy." );
    }

    if ( sourceFile.compareTo( destFile ) == 0 ) {
      throw new IOException( "Cannot copy file '" + sourceFile + "' to itself" );
    }

    destFile.mkdirs();

    if ( destFile.exists() && !destFile.delete() ) {
      throw new IOException( "Unable to delete existing destination file '" + destFile + "'. Logged in as " + System.getProperty( "user.name" ) );
    }

    if ( !sourceFile.exists() ) {
      throw new IOException( "Source file " + sourceFile + " (" + sourceFile.getAbsolutePath() + ") does not exist. Cannot copy. Logged in as " + System.getProperty( "user.name" ) );
    }

    final FileOutputStream fout = new FileOutputStream( destFile );
    final BufferedOutputStream bout = new BufferedOutputStream( fout );
    final FileInputStream fin = new FileInputStream( sourceFile );
    final BufferedInputStream bin = new BufferedInputStream( fin );
    onechar = bin.read();

    while ( onechar != -1 ) {
      bout.write( onechar );

      onechar = bin.read();
    }

    bout.flush();
    bin.close();
    fin.close();

    if ( !destFile.exists() ) {
      throw new IOException( "File copy failed: destination file '" + destFile + "' does not exist after copy." );
    }
    // The below test is commented out because it does not
    // appear to work correctly under Windows NT and Windows 2000
    // if (sourceFile.length() != destFile.length())
    // {
    // throw new IOException("File copy complete, but source file was " +
    // sourceFile.length() + " bytes, destination now " + destFile.length()
    // + " bytes.");
    // }
  }




  /**
   * Get the base filename of a file (e.g. no directory or extension)
   *
   * <p>Returns "readme" from "T:\projects\src\readme.txt".</p>
   *
   * <p>NOTE: This method uses the default file separator for the system.</p>
   *
   * @param fileName Original pathname to get the base name from.
   *
   * @return The base file name
   */
  public static String getBase( final String fileName ) {
    String tempName1 = new String( "" );
    final StringTokenizer stk1 = new StringTokenizer( fileName, "/\\" );

    // Cruise through the string and eat up all the tokens before the last
    // directory delimiter
    while ( stk1.hasMoreTokens() ) {
      tempName1 = stk1.nextToken();
    }

    final StringTokenizer stk = new StringTokenizer( tempName1, "." );
    return stk.nextToken();
  }




  /**
   * Return a vector of the file/dir names in any give directory
   *
   * @param dirName
   *
   * @return TODO Complete Documentation
   *
   * @throws IOException If the given name is not a directory or if
   */
  public static Vector<String> getDir( final String dirName ) throws IOException {
    final File dirFile = new File( dirName );

    if ( !dirFile.isDirectory() ) {
      throw new IOException( "'" + dirName + "' is not a directory." );
    }

    final String[] dir = dirFile.list();

    if ( dir == null ) {
      throw new IOException( "Null array reading directory of " + dirName );
    }

    final Vector<String> fileList = new Vector<String>( 1 );
    String oneFileName = null;

    for ( int i = 0; i < dir.length; i++ ) {
      oneFileName = dir[i].trim();

      fileList.addElement( oneFileName );
    }

    return fileList;
  }




  /**
   * Get the file extension (after the ".")
   *
   * @param fileName Original full file name
   *
   * @return String Extension name
   *
   * @throws IOException If unable to allocate the file to get the extension
   */
  public static String getExtension( final String fileName ) throws IOException {
    final String tempName = new File( fileName ).getName();
    final StringTokenizer stk = new StringTokenizer( tempName, "." );
    stk.nextToken();

    if ( stk.hasMoreTokens() ) {
      return stk.nextToken();
    } else {
      return new String( "" );
    }
  }




  /**
   * Open a file , creating it as necessary, and changing its modification time.
   *
   * @param file
   */
  public final static void touch( final File file ) {
    try {
      FileUtil.append( file, new byte[0], false );
    } catch ( final IOException ioe ) {}
  }




  /**
   * Open a file , creating it as necessary, and changing its modification time.
   *
   * @param file
   * @param data
   * @param backup
   *
   * @throws IOException
   */
  public final static void append( final File file, final byte[] data, final boolean backup ) throws IOException {
    if ( !file.exists() || ( file.exists() && file.canWrite() ) ) {
      if ( !file.exists() ) {
        if ( file.getParentFile() != null ) {
          file.getParentFile().mkdirs();
        }
      } else if ( backup ) {
        FileUtil.createBackup( file );
      }

      RandomAccessFile seeker = null;

      try {
        seeker = new RandomAccessFile( file, "rw" );

        seeker.seek( seeker.length() );
        seeker.write( data );
        file.setLastModified( System.currentTimeMillis() );
      } catch ( final IOException ioe ) {
        throw ioe;
      }
      finally {
        // Attempt to close the data input stream
        try {
          // If it is open, close it
          if ( seeker != null ) {
            seeker.close();
          }
        } catch ( final Exception e ) {
          // Nevermind
        }
        finally {}
      }
    }

  }




  /**
   * Strip the path and suffix of a file name
   *
   * @param file Name of a file  "/usr/local/dbase/test.DBF"
   *
   * @return filename "test"
   */
  public final static String stripPathAndExtension( final String file ) {
    int begin = file.lastIndexOf( FileUtil.FILE_SEPARATOR );

    if ( begin < 0 ) {
      begin = 0;
    } else {
      begin++;
    }

    int end = file.lastIndexOf( "." );

    if ( end < 0 ) {
      end = file.length();
    }

    final String str = file.substring( begin, end );
    return str;
  }




  /**
   * Return a string representing the path of the given class name without any
   * extensions.
   *
   * <p>The returned string should represent a relative path to the base class
   * or source file with the simple adding of a &quot;.class&quot; or a
   * &quot;.java&quot; respectively.</p>
   *
   * @param classname The fully-qualified name of a class.
   *
   * @return A standard path structure to the base class, or the classname
   *         itself if no package information was found in the classname.
   */
  public final static String getJavaBasePath( final String classname ) {
    String base = classname;

    // Get the main body of the class name (no extension)
    if ( classname.endsWith( ".class" ) || classname.endsWith( ".java" ) || classname.endsWith( "." ) ) {
      base = classname.substring( 0, classname.lastIndexOf( '.' ) );
    }

    // remove any leading dots
    if ( base.charAt( 0 ) == '.' ) {
      base = base.substring( 1 );
    }

    // replace dots with path separators
    base = base.replace( '.', '/' );

    return base;
  }




  /**
   * Method getJavaClassFile
   *
   * @param classname
   *
   * @return TODO Complete Documentation
   */
  public final static File getJavaClassFile( final String classname ) {
    return FileUtil.getJavaClassFile( FileUtil.CURRENT_DIR, classname );
  }




  /**
   * Method getJavaClassFile
   *
   * @param dir
   * @param classname
   *
   * @return TODO Complete Documentation
   */
  public final static File getJavaClassFile( final File dir, final String classname ) {
    File retval = null;

    final String fil = FileUtil.getJavaBasePath( classname ) + ".class";

    // Make sure we have a parent directory
    if ( ( dir != null ) && dir.isDirectory() ) {
      retval = new File( dir, fil );
    } else {
      retval = new File( FileUtil.CURRENT_DIR, fil );
    }

    return retval;
  }




  /**
   * Method getJavaSourceFile
   *
   * @param classname
   *
   * @return TODO Complete Documentation
   */
  public final static File getJavaSourceFile( final String classname ) {
    return FileUtil.getJavaSourceFile( FileUtil.CURRENT_DIR, classname );
  }




  /**
   * Method getJavaSourceFile
   *
   * @param dir
   * @param classname
   *
   * @return TODO Complete Documentation
   */
  public final static File getJavaSourceFile( final File dir, final String classname ) {
    File retval = null;

    final String fil = FileUtil.getJavaBasePath( classname ) + ".java";

    // Make sure we have a parent directory
    if ( ( dir != null ) && dir.isDirectory() ) {
      retval = new File( dir, fil );
    } else {
      retval = new File( FileUtil.CURRENT_DIR, fil );
    }

    return retval;
  }




  /**
   * Get the path of a file name.
   *
   * <p>If the filename is relative, the result will be a relative path. If the
   * filename is absolute, the path returned will be absolute.</p>
   *
   * <p>The path separator for the current platform will be used as a path
   * delimiter of the returned path.</p>
   *
   * @param fileName Original pathname
   *
   * @return String Path portion of the pathname
   */
  public static String getPath( final String fileName ) {
    final StringBuffer path = new StringBuffer();

    if ( fileName.endsWith( "/" ) || fileName.endsWith( "\\" ) ) {
      // Already appears to be a path
      path.append( fileName );
    } else {
      if ( fileName.indexOf( ":" ) > 0 ) {
        // looks like a DOS path with a Drive designator, do not specify
        // root
      } else {
        if ( ( fileName.indexOf( "\\" ) == 0 ) || ( fileName.indexOf( "/" ) == 0 ) ) {
          // specify fully-qualified from root
          path.append( FileUtil.FILE_SEPARATOR );
        }
      }

      String token;
      final StringTokenizer stk = new StringTokenizer( fileName, "/\\" );

      while ( stk.hasMoreTokens() ) {
        token = stk.nextToken();

        if ( stk.hasMoreTokens() && ( token.length() > 0 ) ) {
          path.append( token );
          path.append( FileUtil.FILE_SEPARATOR );
        }
      }
    }

    return FileUtil.normalizeSlashes( path.toString() );
  }




  /**
   * Alternate method of getting a path still under test
   *
   * @param filename  the filename to query, null returns null
   * 
   * @return the path of the file, an empty string if none exists, null if invalid
   */
  public static String getPath2( String filename ) {
    return getPath( filename, 1 );
  }




  /**
   * Gets the name minus the path from a full filename.
   * 
   * <p>This method will handle a file in either Unix or Windows format. The 
   * text after the last forward or backslash is returned.<pre>
   * a/b/c.txt --&gt; c.txt
   * a.txt     --&gt; a.txt
   * a/b/c     --&gt; c
   * a/b/c/    --&gt; ""
   * </pre>
   * 
   * <p>The output will be the same irrespective of the machine on which the 
   * code is running.</p>
   *
   * @param filename the filename to query, null returns null
   * 
   * @return the name of the file without the path, or an empty string if none exists
   */
  public static String getName( String filename ) {
    if ( filename == null ) {
      return null;
    }
    int index = indexOfLastSeparator( filename );
    return filename.substring( index + 1 );
  }




  /**
   * Copy bytes from an {@code InputStream} to an {@code OutputStream}.
   * 
   * <p>This method buffers the input internally, so there is no need to use a
   * {@code BufferedInputStream}.</p>
   * 
   * <p>Large streams (over 2GB) will return a bytes copied value of {@code -1} 
   * after the copy has completed since the correct number of bytes cannot be 
   * returned as an int. For large streams use the {@code 
   * copyLarge(InputStream, OutputStream)} method.
   * 
   * @param input the {@code InputStream} to read from
   * @param output  the {@code OutputStream} to write to
   * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
   * 
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   */
  public static int copy( InputStream input, OutputStream output ) throws IOException {
    long count = copyLarge( input, output );
    if ( count > Integer.MAX_VALUE ) {
      return -1;
    }
    return (int)count;
  }




  /**
   * Copy bytes from a large (over 2GB) {@code InputStream} to an
   * {@code OutputStream}.
   * 
   * <p>This method buffers the input internally, so there is no need to use a
   * {@code BufferedInputStream}.<p>
   * 
   * @param input  the {@code InputStream} to read from
   * @param output  the {@code OutputStream} to write to
   * 
   * @return the number of bytes copied
   * 
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   */
  public static long copyLarge( InputStream input, OutputStream output ) throws IOException {
    return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE] );
  }




  /**
   * Copy bytes from a large (over 2GB) {@code InputStream} to an
   * {@code OutputStream}.
   * 
   * <p>This method uses the provided buffer, so there is no need to use a
   * {@code BufferedInputStream}.</p>
   * 
   * @param input  the {@code InputStream} to read from
   * @param output  the {@code OutputStream} to write to
   * @param buffer the buffer to use for the copy
   * 
   * @return the number of bytes copied
   * 
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   */
  public static long copyLarge( InputStream input, OutputStream output, byte[] buffer ) throws IOException {
    long count = 0;
    int n = 0;
    while ( EOF != ( n = input.read( buffer ) ) ) {
      output.write( buffer, 0, n );
      count += n;
    }
    return count;
  }




  /**
   * Does the work of getting the path.
   * 
   * @param filename  the filename
   * @param separatorAdd  0 to omit the end separator, 1 to return it
   * 
   * @return the path
   */
  private static String getPath( String filename, int separatorAdd ) {
    if ( filename == null ) {
      return null;
    }
    int prefix = getPrefixLength( filename );
    if ( prefix < 0 ) {
      return null;
    }
    int index = indexOfLastSeparator( filename );
    int endIndex = index + separatorAdd;
    if ( prefix >= filename.length() || index < 0 || prefix >= endIndex ) {
      return "";
    }
    return filename.substring( prefix, endIndex );
  }




  /**
   * Checks if the character is a separator.
   * 
   * @param ch  the character to check
   * @return true if it is a separator character
   */
  private static boolean isSeparator( char ch ) {
    return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
  }




  /**
   * Returns the length of the filename prefix, such as {@code C:/} or {@code ~/}.
   * <p>
   * This method will handle a file in either Unix or Windows format.
   * <p>
   * The prefix length includes the first slash in the full filename
   * if applicable. Thus, it is possible that the length returned is greater
   * than the length of the input string.
   * <pre>
   * Windows:
   * a\b\c.txt           --&gt; ""          --&gt; relative
   * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
   * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
   * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
   * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
   *
   * Unix:
   * a/b/c.txt           --&gt; ""          --&gt; relative
   * /a/b/c.txt          --&gt; "/"         --&gt; absolute
   * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
   * ~                   --&gt; "~/"        --&gt; current user (slash added)
   * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
   * ~user               --&gt; "~user/"    --&gt; named user (slash added)
   * </pre>
   * 
   * <p>The output will be the same irrespective of the machine that the code 
   * is running on. I.e., both Unix and Windows prefixes are matched 
   * regardless.</p>
   *
   * @param filename  the filename to find the prefix in, null returns -1
   * 
   * @return the length of the prefix, -1 if invalid or null
   */
  public static int getPrefixLength( String filename ) {
    if ( filename == null ) {
      return -1;
    }
    int len = filename.length();
    if ( len == 0 ) {
      return 0;
    }
    char ch0 = filename.charAt( 0 );
    if ( ch0 == ':' ) {
      return -1;
    }
    if ( len == 1 ) {
      if ( ch0 == '~' ) {
        return 2; // return a length greater than the input
      }
      return isSeparator( ch0 ) ? 1 : 0;
    } else {
      if ( ch0 == '~' ) {
        int posUnix = filename.indexOf( UNIX_SEPARATOR, 1 );
        int posWin = filename.indexOf( WINDOWS_SEPARATOR, 1 );
        if ( posUnix == -1 && posWin == -1 ) {
          return len + 1; // return a length greater than the input
        }
        posUnix = posUnix == -1 ? posWin : posUnix;
        posWin = posWin == -1 ? posUnix : posWin;
        return Math.min( posUnix, posWin ) + 1;
      }
      char ch1 = filename.charAt( 1 );
      if ( ch1 == ':' ) {
        ch0 = Character.toUpperCase( ch0 );
        if ( ch0 >= 'A' && ch0 <= 'Z' ) {
          if ( len == 2 || isSeparator( filename.charAt( 2 ) ) == false ) {
            return 2;
          }
          return 3;
        }
        return -1;

      } else if ( isSeparator( ch0 ) && isSeparator( ch1 ) ) {
        int posUnix = filename.indexOf( UNIX_SEPARATOR, 2 );
        int posWin = filename.indexOf( WINDOWS_SEPARATOR, 2 );
        if ( posUnix == -1 && posWin == -1 || posUnix == 2 || posWin == 2 ) {
          return -1;
        }
        posUnix = posUnix == -1 ? posWin : posUnix;
        posWin = posWin == -1 ? posUnix : posWin;
        return Math.min( posUnix, posWin ) + 1;
      } else {
        return isSeparator( ch0 ) ? 1 : 0;
      }
    }
  }




  /**
   * Returns the index of the last directory separator character.
   * <p>
   * This method will handle a file in either Unix or Windows format.
   * The position of the last forward or backslash is returned.
   * <p>
   * The output will be the same irrespective of the machine that the code is running on.
   * 
   * @param filename  the filename to find the last path separator in, null returns -1
   * @return the index of the last separator character, or -1 if there
   * is no such character
   */
  public static int indexOfLastSeparator( String filename ) {
    if ( filename == null ) {
      return -1;
    }
    int lastUnixPos = filename.lastIndexOf( UNIX_SEPARATOR );
    int lastWindowsPos = filename.lastIndexOf( WINDOWS_SEPARATOR );
    return Math.max( lastUnixPos, lastWindowsPos );
  }




  /**
   * Opens a file, reads it and returns the data as a string and closes the
   * file.
   *
   * @param file - file to open
   *
   * @return String representing the file data
   */
  public static String fileToString( final File file ) {
    try {
      final byte[] data = FileUtil.read( file );

      if ( data != null ) {
        // Attempt to return the string
        try {
          return new String( data, StringUtil.ISO8859_1 );
        } catch ( final UnsupportedEncodingException uee ) {
          // Send it back in default encoding
          return new String( data );
        }
      }
    } catch ( final Exception ex ) {}

    return null;
  }




  /**
   * Setup a working directory in the users home directory with the given name.
   *
   * @param dirname name of the directory to create
   * 
   * @return A reference to a file allowing access to the working directory
   */
  public static File initHomeWorkDirectory( String dirname ) {
    // if the name was null, create a directory named "wrk"
    if ( dirname == null ) {
      dirname = "wrk";
    }

    // setup a reference to the user's home directory
    return validateWorkDirectory( System.getProperty( "user.home" ) + System.getProperty( "file.separator" ) + dirname );
  }




  /**
   * Validate and return a file reference suitable for using as a working
   * directory.
   *
   * @param dirname name of the directory to create
   * 
   * @return A reference to a file representing the directory.
   * 
   * @throws IllegalArgumentException if the directory could not be made.
   */
  static File validateWorkDirectory( final String dirname ) throws IllegalArgumentException {
    // if the name was null, create a directory named "work"
    if ( dirname == null ) {
      return null;
    }

    // setup a reference to the new work directory
    File retval = new File( dirname );

    // If the given directory name is not absolute...
    if ( !retval.isAbsolute() ) {
      // ...prepend the current directory
      retval = new File( System.getProperty( "user.dir" ) + System.getProperty( "file.separator" ) + dirname );
    }

    // If the directory does not exist, create it
    if ( !retval.exists() ) {
      if ( !retval.mkdirs() ) {
        retval = null;

        throw new IllegalArgumentException( "Could not create\"" + retval + "\" as a working directory" );
      }
    }

    // Make sure we can write to it
    if ( retval.isDirectory() && retval.canWrite() ) {
      return retval;
    } else {
      return null;
    }
  }




  /**
   * Copy a file, then remove the original file
   *
   * @param sourceFile Original file name
   * @param destFile Destination file name
   *
   * @throws IOException If an I/O error occurs during the copy
   */
  public static void moveFile( final String sourceFile, final String destFile ) throws IOException {
    FileUtil.moveFile( new File( sourceFile ), new File( destFile ) );
  }




  /**
   * Copy a file, then remove the original file
   *
   * @param sourceFile Original file reference
   * @param destFile Destination file reference
   *
   * @throws IOException If an I/O error occurs during the copy
   */
  public static void moveFile( final File sourceFile, final File destFile ) throws IOException {
    if ( !sourceFile.canRead() ) {
      throw new IOException( "Cannot read source file '" + sourceFile + "'. Logged in as " + System.getProperty( "user.name" ) );
    }

    if ( !sourceFile.canWrite() ) {
      throw new IOException( "Cannot write to source file '" + sourceFile + "'. Logged in as " + System.getProperty( "user.name" ) + ". Cannot move without write permission to source file." );
    }

    FileUtil.copyFile( sourceFile, destFile );

    if ( !sourceFile.delete() ) {
      System.out.println( "Copy completed, but unable to delete source file '" + sourceFile + "'. Logged in as " + System.getProperty( "user.name" ) );
    }
  }




  /**
   * Take a prefix and a relative path and put the two together to make an
   * absolute path.
   *
   * @param prefix
   * @param originalPath
   *
   * @return TODO Complete Documentation
   */
  public static String makeAbsolutePath( String prefix, String originalPath ) {
    Assert.notBlank( originalPath, "Original path may not be blank here" );

    prefix = StringUtil.notNull( prefix );
    originalPath = originalPath.replace( '\\', '/' );
    prefix = prefix.replace( '\\', '/' );

    if ( originalPath.startsWith( "/" ) ) {
      return originalPath;
    }

    // Check for a drive specification for windows-type path
    if ( originalPath.substring( 1, 2 ).equals( ":" ) ) {
      return originalPath;
    }

    // Otherwise...Make sure the prefix ends with a "/"
    if ( !prefix.endsWith( "/" ) ) {
      prefix = prefix + "/";
    }

    // and put the two together
    return prefix + originalPath;
  }




  /**
   * This returns a URI for the given file.
   *
   * @param file from which to generate a URI
   *
   * @return the URI of the given file or null if a logic error occurred
   */
  public static URI getFileURI( final File file ) {
    final StringBuffer buffer = new StringBuffer( "file://" );

    final char[] chars = file.getAbsolutePath().trim().toCharArray();

    URI retval = null;

    if ( chars != null ) {
      if ( chars.length > 1 ) {
        // If there is a drive delimiter ':' in the second position, we assume
        // this is file is on a Windows system which does not return a leading /
        if ( chars[1] == ':' ) {
          buffer.append( "/" );
        }
      }

      for ( int i = 0; i < chars.length; i++ ) {
        final char c = chars[i];

        switch ( c ) {

          // Replace spaces
          case ' ':
            buffer.append( "%20" );
            continue;

          // Replace every Windows file separator
          case '\\':
            buffer.append( "/" );
            continue;

          default:
            buffer.append( c );
            continue;

        }
      }

      try {
        retval = new URI( buffer.toString() );
      } catch ( final URISyntaxException e ) {
        System.err.println( e.getMessage() );
      }
    }

    return retval;
  }




  /**
   * Scan the given directory for files containing the substrMatch
   * Small case extensions '.dbf' are recognized and returned as '.DBF'
   *
   * @param path eg "/usr/local/metrics"
   * @param suffix Case insensitive: eg ".DBF"
   * @param recurse set to true to recurse into all the child sub directories.
   *
   * @return a list of File objects representing the files in the given path with the given suffix
   */
  public final static List<File> getAllFiles( final String path, final String suffix, boolean recurse ) {
    File folder = new File( path );
    File[] listOfFiles = folder.listFiles();
    final List<File> list = new ArrayList<File>( 20 );

    String upperSuffix = null;

    if ( suffix != null )
      upperSuffix = suffix.toUpperCase();

    if ( listOfFiles != null ) {
      for ( int i = 0; i < listOfFiles.length; i++ ) {
        if ( listOfFiles[i].isFile() ) {
          if ( ( upperSuffix == null ) || listOfFiles[i].getName().toUpperCase().endsWith( upperSuffix ) ) {
            list.add( listOfFiles[i] );
          }
        } else if ( listOfFiles[i].isDirectory() ) {
          if ( recurse ) {
            try {
              list.addAll( getAllFiles( listOfFiles[i].getCanonicalPath(), suffix, recurse ) );
            } catch ( IOException e ) {
              e.printStackTrace();
            }
          }

        }
      }
    }
    return list;
  }




  /**
   * Delete a file without throwing any exceptions.
   *
   * @param file the file reference to delete
   *
   * @return true if the file was deleted or does not exist, false otherwise
   */
  public final static boolean deleteFile( File file ) {
    if ( file != null ) {
      // only delete a file that exists
      if ( file.exists() ) {
        // try the delete. If it fails, complain
        if ( !file.delete() ) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }




  /**
   * Delete a file without throwing any exceptions.
   *
   * @param fname the name of the file to delete
   *
   * @return true if the file was deleted or does not exist, false otherwise
   */
  public final static boolean deleteFile( final String fname ) {
    if ( StringUtil.isNotBlank( fname ) ) {
      return deleteFile( new File( fname ) );
    } else {
      return false;
    }
  }




  /**
   * Delete a file
   *
   * @param dataDir
   * @param fname
   *
   * @throws IOException
   * @throws NullPointerException
   */
  public final static void deleteFile( final String dataDir, final String fname ) throws NullPointerException, IOException {
    final File f = new File( dataDir + FileUtil.FILE_SEPARATOR + fname );

    // only delete a file that exists
    if ( f.exists() ) {
      // try the delete. If it fails, complain
      if ( !f.delete() ) {
        throw new IOException( "Could not delete file: " + dataDir + "/" + fname + "." );
      }
    }
  }




  /**
   * Performs a recursive delete of a directory and all its contents.
   *
   * @param dir
   *
   * @throws IOException
   */
  public static final void removeDir( final File dir ) throws IOException {
    final File[] list = dir.listFiles();

    if ( null != list ) {
      for ( int ii = 0; ii < list.length; ii++ ) {
        if ( list[ii].isDirectory() ) {
          FileUtil.removeDir( list[ii] );
        } else {
          if ( !list[ii].delete() ) {
            throw new IOException( "Unable to delete file " + list[ii].getAbsolutePath() );
          }
        }
      }
    }

    if ( !dir.delete() && dir.exists() ) {
      throw new IOException( "Unable to delete directory " + dir.getAbsolutePath() );
    }
  }




  /**
   * rename a file
   *
   * @param oldName
   * @param newName
   *
   * @return true if succeeded
   */
  public final static boolean renameFile( final String oldName, final String newName ) {
    final File f_old = new File( oldName );
    final File f_new = new File( newName );
    final boolean ret = f_old.renameTo( f_new );
    return ret;
  }




  /**
   * Open a file and return a DataInputStream object
   *
   * @param fn
   *
   * @return DataInpuStream - stream to use for file data
   */
  public static DataInputStream openInputFile( final String fn ) {
    FileInputStream fis = null;
    DataInputStream dis = null;
    BufferedInputStream bis = null;

    try {
      fis = new FileInputStream( fn );
    } catch ( final IOException e ) {
      return ( dis );
    }

    try {
      bis = new BufferedInputStream( fis );
      dis = new DataInputStream( bis );
    } catch ( final Exception e ) {
      try {
        fis.close();
      } catch ( final IOException e1 ) {}

      dis = null;

      return ( dis );
    }

    return ( dis );
  }




  /**
   * Method getFile
   *
   * @param filename
   *
   * @return TODO Complete Documentation
   */
  public static String getFile( final String filename ) {
    final String tmp = new String( filename );
    tmp.replace( '\\', '/' );

    final int i = tmp.lastIndexOf( '/' );
    return ( i != -1 ) ? tmp.substring( i + 1 ) : tmp;
  }




  /**
   * GetFileSize - returns the file size
   *
   * @param filename - file to size
   *
   * @return length of file
   */
  public static long getFileSize( final String filename ) {
    File f;

    try {
      f = new File( filename );

      if ( f.exists() ) {
        return f.length();
      }
    } catch ( final Exception e ) {}

    return -1;
  }




  /**
   * Clear a directory of all files
   * 
   * <p>Calling this with clrdir=false and clrsub=true will result in all files
   * in all the sub-directories of the given directory being deleted, but the
   * entire directory structure will remain on the file system.</p>
   *
   * <p>Calling this with clrdir=true and clrsub=true will result in all files
   * and all the sub-directories of the given directory being deleted as well as
   * the given directory.</p>
   *
   * <p>Calling this with clrdir=false and clrsub=false will result in all
   * files of the current directory being deleted with all the sub-directories
   * remaining untouched.</p>
   * 
   * @param dir The name of the directory to delete
   * @param clrdir Delete the directory after it has been cleared
   * @param clrsub Clear the sub-directories of this directory as well
   */
  public static void clearDir( final String dir, final boolean clrdir, final boolean clrsub ) {
    FileUtil.clearDir( new File( dir ), clrdir, clrsub );
  }




  /**
   * Clear a directory of all files
   *
   * <p>Calling this with clrdir=false and clrsub=true will result in all files
   * in all the sub-directories of the given directory being deleted, but the
   * entire directory structure will remain on the file system.</p>
   *
   * <p>Calling this with clrdir=true and clrsub=true will result in all files
   * and all the sub-directories of the given directory being deleted as well as
   * the given directory.</p>
   *
   * <p>Calling this with clrdir=false and clrsub=false will result in all
   * files of the current directory being deleted with all the sub-directories
   * remaining untouched.</p>
   *
   * @param dir The file reference to the directory to delete
   * @param clrdir Delete the directory after it has been cleared
   * @param clrsub Clear the sub-directories of this directory as well
   * 
   * @see #deleteDirectory(File)
   */
  public static void clearDir( final File dir, final boolean clrdir, final boolean clrsub ) {
    if ( !dir.isDirectory() ) {
      return;
    }

    // TODO: Calling this with clrdir=true and clrsub=false will result in all 
    // files of the current directory being deleted with all the sub-directories 
    // being moved to the parent directory. The orphans of the current directory 
    // should not be lost in the clearing operation.

    try {
      // Get a list of all the children in the current directory
      final String[] childlist = dir.list();

      // For each child in the directory
      for ( int i = 0; i < childlist.length; i++ ) {
        // Create a new file reference
        final File child = new File( dir, childlist[i] );

        // If it exists ( which is should )
        if ( child.exists() ) {
          // If the child is a file...
          if ( child.isFile() ) {
            // ...delete the file
            child.delete();
          } else {
            if ( clrsub ) {
              // .. otherwise recursively call this method to
              // delete the
              // directory
              FileUtil.clearDir( child, clrdir, clrsub );
            } else {
              // Force the deletion of the children
              FileUtil.clearDir( child, clrdir, true );

              // We need to make sure that moveFile will move
              // directories
              // before this call can be made:
              // moveFile(child,dir.getParentFile());
            }
          }
        }
      }

      // After all the contents are deleted, is we are to delete the directory
      if ( clrdir ) {
        // ...delete the directory itself
        dir.delete();
      }
    } catch ( final Exception e ) {}
  }




  /**
   * Method menu
   */
  private static void menu() {
    System.out.println( "1. copyFile" );
    System.out.println( "2. moveFile" );
    System.out.println( "3. getPath" );
    System.out.println( "4. getBase" );
    System.out.println( "5. getExtension" );
    System.out.println( "6. getDir" );
    System.out.println( "7. cleanDirs" );
    System.out.println( "8. makeAbsolutePath" );
    System.out.println( "0. quit" );
  }




  /**
   * Main method used for testing and command line operations
   *
   * @param args
   */
  public static void main( final String[] args ) {
    System.out.println( "FileUtil Test" );

    String command = new String( "" );
    final BufferedReader ds = new BufferedReader( new InputStreamReader( System.in ) );

    try {
      while ( !command.equals( "0" ) ) {
        FileUtil.menu();
        System.out.print( "Command==>" );

        command = ds.readLine();

        System.out.println( "" );
        System.out.println( "Command:" + command );

        if ( command.equals( "1" ) ) {
          System.out.println( "copyFile" );
          System.out.print( "sourceFile:" );

          final String sourceFile = ds.readLine();
          System.out.print( "destFile:" );

          final String destFile = ds.readLine();
          FileUtil.copyFile( sourceFile, destFile );
          System.out.println( "Copy Complete\n\n" );
        } else {
          if ( command.equals( "2" ) ) {
            System.out.println( "moveFile" );
            System.out.print( "sourceFile:" );

            final String sourceFile = ds.readLine();
            System.out.print( "destFile:" );

            final String destFile = ds.readLine();
            FileUtil.moveFile( sourceFile, destFile );
            System.out.println( "Move Complete\n\n" );
          } else {
            if ( command.equals( "3" ) ) {
              System.out.println( "getPath" );
              System.out.print( "fileName:" );

              final String fileName = ds.readLine();
              System.out.println( "Path:'" + FileUtil.getPath( fileName ) + "'\n\n" );
            } else {
              if ( command.equals( "4" ) ) {
                System.out.println( "getBase" );
                System.out.print( "fileName:" );

                final String fileName = ds.readLine();
                System.out.println( "Base:'" + FileUtil.getBase( fileName ) + "'\n\n" );
              } else {
                if ( command.equals( "5" ) ) {
                  System.out.println( "getExtension" );
                  System.out.print( "fileName:" );

                  final String fileName = ds.readLine();
                  System.out.println( "Extension:'" + FileUtil.getExtension( fileName ) + "'\n\n" );
                } else {
                  if ( command.equals( "6" ) ) {
                    System.out.println( "getDir" );
                    System.out.print( "dirName:" );

                    final String dirName = ds.readLine();
                    final Vector<String> v = FileUtil.getDir( dirName );

                    for ( final Enumeration<String> e = v.elements(); e.hasMoreElements(); ) {
                      System.out.println( "Item:'" + e.nextElement() + "'" );
                    }

                    System.out.println( "Directory Complete" );
                  } else {
                    if ( command.equals( "7" ) ) {
                      System.out.println( "cleanDirs" );
                      System.out.println( "dirName:" );

                      final String dirName = ds.readLine();
                      FileUtil.cleanDirs( dirName );
                    } else {
                      if ( command.equals( "8" ) ) {
                        System.out.println( "prefix:" );

                        final String prefix = ds.readLine();
                        System.out.println( "fileName:" );

                        final String fileName = ds.readLine();
                        System.out.println( "Converted name:" + FileUtil.makeAbsolutePath( prefix, fileName ) );
                      } else {
                        if ( !command.equals( "0" ) ) {
                          System.out.println( "Unknown command:" + command );
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch ( final Exception e ) {
      e.printStackTrace( System.out );
    }
  }




  /**
   * Make a copy of a file in the given file using a generation naming 
   * scheme effectively rotating the files.
   * 
   * <p>This task will create a copy of the file and append a number to the end 
   * of the file name. The latest backup is always 1; as all the files are 
   * named according to the age of their generation. For example FILE.1 is 
   * renamed to FILE.2 before the backup is created as FILE.1 so no data is 
   * lost.
   *
   * <p>If a limit is specified, then only that number of generations will be 
   * preserved. If limit is less than the current number of generations, the 
   * excess files will be removed. A limit of 0 (zero) is effectively 
   * interpreted as no limit. Any negative value is interpreted as zero. The 
   * default limit is zero.
   * 
   * @param targetFile
   * @param limit
   * @throws IOException
   */
  public static void createGenerationalBackup( File targetFile, int limit ) throws IOException {
    File target;
    File file;
    final String fileName = targetFile.getAbsolutePath();

    int generations = findGenerationSize( targetFile );

    if ( limit > 0 ) {
      if ( generations > limit ) {
        for ( int i = generations - 1; i >= limit; i-- ) {
          file = new File( fileName + '.' + i );
          if ( file.exists() ) {
            //System.out.println( "Deleting excess file " + file );
            file.delete();
          }
        }
      } else if ( generations == limit ) {
        file = new File( fileName + '.' + generations );
        if ( file.exists() ) {
          //System.out.println( "Deleting oldest file " + file );
          file.delete();
        }
      }
    }

    for ( int i = generations - 1; i >= 1; i-- ) {
      file = new File( fileName + "." + i );
      if ( file.exists() ) {
        target = new File( fileName + '.' + ( i + 1 ) );
        //System.out.println( "Renaming file " + file + " to " + target );
        file.renameTo( target );
      }
    }
    FileUtil.copyFile( targetFile.getAbsolutePath(), new File( targetFile.getAbsolutePath() + ".1" ).getAbsolutePath() );

  }




  /**
   * Determine how many generations currently exist for this file.
   * 
   * <p>If no generations exist, then 0 (zero) is returned.
   * 
   * @param file the file to check
   * 
   * @return the number of generations which exist for this file.
   */
  private static int findGenerationSize( File file ) {
    int i = 0;
    for ( i = 1; new File( file.getAbsolutePath() + "." + i ).exists(); i++ );
    return i;
  }




  /**
   * Create a backup of the file.
   * 
   * <p>This will create a file with the same name but with a .1 (one) 
   * appended to it. If that file exists, the number will be incremented until 
   * a numbered filename does not exist and then the file is copied to it. 
   * This results in the latest backup having the highest number.
   * 
   * <p>For a more traditional log rotation method, see 
   * {@link #createGenerationalBackup(File, int)}
   * 
   * @param file The file to backup
   *
   * @throws IOException if the file could not be copied.
   */
  public static void createBackup( final File file ) throws IOException {
    int i = findGenerationSize( file );
    FileUtil.copyFile( file.getAbsolutePath(), new File( file.getAbsolutePath() + "." + i ).getAbsolutePath() );
  }




  /**
   * Method write
   *
   * @param file
   * @param data
   *
   * @throws IOException
   */
  public static void write( final File file, final byte[] data ) throws IOException {
    FileUtil.write( file, data, false );
  }




  /**
   * Write the given data to the given file object creating it and it's parent
   * directiories as necessary.
   *
   * @param file The file reference to which the data will be written.
   * @param data The data to write to the file.
   * @param backup Flag indicating a generational backup of the data should be
   *        made before writing to the file.
   *
   * @throws IOException if there were problems with any of the operations
   *         involved with writing the data
   */
  public static void write( final File file, final byte[] data, final boolean backup ) throws IOException {
    if ( file == null ) {
      throw new IOException( "File reference was null" );
    }

    if ( !file.exists() || ( file.exists() && file.canWrite() ) ) {
      DataOutputStream dos = null;

      try {
        if ( file.exists() && backup ) {
          FileUtil.createBackup( file );
        }

        // Make sure the parent directories are present
        if ( file.getParent() != null ) {
          file.getParentFile().mkdirs();
        }

        if ( data.length > 0 ) {
          // Create an output stream
          dos = new DataOutputStream( new FileOutputStream( file ) );

          // Write the data to it
          dos.write( data );

          // Flush the buffers
          dos.flush();
        } else {
          FileUtil.touch( file );
        }

      } catch ( final EOFException eof ) {}
      finally {
        // Attempt to close the data input stream
        try {
          // If it is open, close it
          if ( dos != null ) {
            dos.close();
          }
        } catch ( final Exception e ) {
          // Nevermind
        }
        finally {}
      }
    }
  }




  /**
   * Create all directories required for this directory reference to be valid
   *
   * @param dir
   *
   * @throws IOException
   */
  public static void makeDirectory( final File dir ) throws IOException {
    if ( dir == null ) {
      throw new IOException( "File reference was null" );
    }

    if ( !dir.exists() || ( dir.exists() && dir.isFile() ) ) {
      // Make sure the parent directories are present
      if ( dir.getParent() != null ) {
        dir.getParentFile().mkdirs();
      }

      if ( !dir.mkdir() ) {
        throw new IOException( "Could not make directory" );
      }
    }
  }




  /**
   * Make a directory with the given name.
   *
   * <p>If the operation failed, a partial path may exist.</p>
   *
   * @param directory any valid directory path with slashes, back-slashes, 
   * relational dots and whatever.
   *
   * @return The file reference to the directory created, null if the operation
   *         failed in some way.
   */
  public static File makeDirectory( final String directory ) {
    File retval = null;

    if ( ( directory != null ) && ( directory.length() > 0 ) ) {
      final File tempfile = new File( FileUtil.normalizePath( directory ) );

      try {
        FileUtil.makeDirectory( tempfile );

        retval = tempfile;
      } catch ( final Exception e ) {
        retval = null;
      }
    }

    return retval;
  }




  /**
   * Make all the directories required to create the given file.
   * 
   * @param file the file reference
   * 
   * @throws IOException
   */
  public static void makeParentDirectory( final File file ) throws IOException {
    if ( file == null ) {
      throw new IOException( "File reference was null" );
    }

    if ( !file.exists() || ( file.exists() && file.isFile() ) ) {
      if ( file.getParent() != null ) {
        file.getParentFile().mkdirs();
      }
    }
  }




  /**
   * Make all the directories required to create the named file.
   *
   * <p>If the operation failed, a partial path may exist.</p>
   *
   * @param filename any valid file name with slashes, back-slashes, relational
   * dots and whatever.
   *
   * @return The file reference to the directory created, null if the operation
   *         failed in some way.
   */
  public static File makeParentDirectory( final String filename ) {
    File retval = null;

    if ( ( filename != null ) && ( filename.length() > 0 ) ) {
      final File tempfile = new File( FileUtil.normalizePath( filename ) );

      try {
        FileUtil.makeParentDirectory( tempfile );

        retval = tempfile;
      } catch ( final Exception e ) {
        retval = null;
      }
    }

    return retval;
  }




  /**
   * Read the entire file into memory as an array of bytes.
   *
   * @param file The file to read
   *
   * @return A byte array that contains the contents of the file.
   *
   * @throws IOException If problems occur.
   */
  public static byte[] read( final File file ) throws IOException {
    if ( file == null ) {
      throw new IOException( "File reference was null" );
    }

    if ( file.exists() && file.canRead() ) {
      DataInputStream dis = null;
      final byte[] bytes = new byte[new Long( file.length() ).intValue()];

      try {
        dis = new DataInputStream( new FileInputStream( file ) );

        dis.readFully( bytes );

        return bytes;
      } catch ( final Exception ignore ) {}
      finally {
        // Attempt to close the data input stream
        try {
          if ( dis != null ) {
            dis.close();
          }
        } catch ( final Exception ignore ) {}
      }
    }

    return null;
  }




  /**
   * Remove duplicate file separators, remove relation dots and correct all
   * file separators to those suitable for URI usage ('/').
   *
   * @param path The path to standardize
   *
   * @return The standardize path
   */
  public static String standardizePath( String path ) {
    path = FileUtil.normalizeSlashes( path );
    path = FileUtil.removeRelations( path );
    path = path.replace( File.separatorChar, '/' );

    return path;
  }




  /**
   * &quot;normalize&quot; the given absolute path.
   *
   * <p>This includes:
   * <ul>
   *   <li>Uppercase the drive letter if there is one.</li>
   *   <li>Remove redundant slashes after the drive spec.</li>
   *   <li>resolve all ./, .\, ../ and ..\ sequences.</li>
   *   <li>DOS style paths that start with a drive letter will have
   *     \ as the separator.</li>
   * </ul>
   * Unlike {@code File#getCanonicalPath()} it specifically doesn't
   * resolve symbolic links.
   *
   * @param path the path to be normalized
   *
   * @return the normalized version of the path.
   */
  public static File normalize( String path ) {
    final String orig = path;

    path = path.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );

    // make sure we are dealing with an absolute path
    final int colon = path.indexOf( ":" );

    if ( !path.startsWith( File.separator ) && ( colon == -1 ) ) {
      final String msg = path + " is not an absolute path";
      throw new RuntimeException( msg );
    }

    final boolean dosWithDrive = false;
    String root = null;
    // Eliminate consecutive slashes after the drive spec
    if ( path.length() == 1 ) {
      root = File.separator;
      path = "";
    } else if ( path.charAt( 1 ) == File.separatorChar ) {
      // UNC drive
      root = File.separator + File.separator;
      path = path.substring( 2 );
    } else {
      root = File.separator;
      path = path.substring( 1 );
    }

    final Stack<String> s = new Stack<String>();
    s.push( root );

    final StringTokenizer tok = new StringTokenizer( path, File.separator );

    while ( tok.hasMoreTokens() ) {
      final String thisToken = tok.nextToken();

      if ( ".".equals( thisToken ) ) {
        continue;
      } else if ( "..".equals( thisToken ) ) {
        if ( s.size() < 2 ) {
          throw new RuntimeException( "Cannot resolve path " + orig );
        } else {
          s.pop();
        }
      } else {
        // plain component
        s.push( thisToken );
      }
    }

    final StringBuffer sb = new StringBuffer();

    for ( int i = 0; i < s.size(); i++ ) {
      if ( i > 1 ) {
        // not before the filesystem root and not after it, since root
        // already contains one
        sb.append( File.separatorChar );
      }

      sb.append( s.elementAt( i ) );
    }

    path = sb.toString();

    if ( dosWithDrive ) {
      path = path.replace( '/', '\\' );
    }

    return new File( path );
  }




  /**
   * Remove duplicate file separators, remove relation dots and correct all
   * non-platform specific file separators to those of the current platform.
   *
   * @param path The path to normalize
   *
   * @return The normalized path
   */
  public static String normalizePath( String path ) {
    path = FileUtil.normalizeSlashes( path );
    path = FileUtil.removeRelations( path );

    return path;
  }




  /**
   * Replace all the file separator characters (either '/' or '\') with the
   * proper file separator for this platform.
   *
   * @param path
   *
   * @return normalized path
   */
  public static String normalizeSlashes( String path ) {
    if ( path == null ) {
      return null;
    } else {
      path = path.replace( '/', File.separatorChar );
      path = path.replace( '\\', File.separatorChar );

      return path;
    }
  }




  /**
   * Remove the current and parent directory relation references from the given
   * path string.
   *
   * <p>Takes a string like &quot;\home\work\bin\..\lib&quot; and returns a
   * path like &quot;\home\work\lib&quot;
   *
   * @param path The representative path with possible relational dot notation
   *
   * @return The representative path without the dots
   */
  public static String removeRelations( final String path ) {
    if ( path == null ) {
      return null;
    } else if ( path.length() == 0 ) {
      return path;
    } else {
      // Break the path into tokens and skip any '.' tokens
      final StringTokenizer st = new StringTokenizer( path, "/\\" );
      final String[] tokens = new String[st.countTokens()];

      int i = 0;

      while ( st.hasMoreTokens() ) {
        final String token = st.nextToken();

        if ( ( token != null ) && ( token.length() > 0 ) && !token.equals( "." ) ) {
          // if there is a reference to the parent, then just move
          // back to the
          // previous token in the list, which is this tokens parent
          if ( token.equals( ".." ) ) {
            if ( i > 0 ) {
              tokens[--i] = null;
            }
          } else {
            tokens[i++] = token;
          }
        }
      }

      // Start building the new path from the tokens
      final StringBuffer retval = new StringBuffer();

      // If the original path started with a file separator, then make
      // sure the
      // return value starts the same way
      if ( ( path.charAt( 0 ) == '/' ) || ( path.charAt( 0 ) == '\\' ) ) {
        retval.append( File.separatorChar );
      }

      // For each token in the path
      if ( tokens.length > 0 ) {
        for ( i = 0; i < tokens.length; i++ ) {
          if ( tokens[i] != null ) {
            retval.append( tokens[i] );
          }

          // if there is another token on the list, use the
          // platform-specific
          // file separator as a delimiter in the return value
          if ( ( i + 1 < tokens.length ) && ( tokens[i + 1] != null ) ) {
            retval.append( File.separatorChar );
          }
        }
      }

      if ( ( path.charAt( path.length() - 1 ) == '/' ) || ( ( path.charAt( path.length() - 1 ) == '\\' ) && ( retval.charAt( retval.length() - 1 ) != File.separatorChar ) ) ) {
        retval.append( File.separatorChar );
      }

      return retval.toString();
    }
  }




  /**
   * Method saveStreamToFile
   *
   * @param in
   * @param outFile
   *
   * @throws IOException
   */
  public static void saveStreamToFile( final InputStream in, final File outFile ) throws IOException {
    FileOutputStream out = null;

    try {
      out = new FileOutputStream( outFile );

      final byte[] buf = new byte[4096];
      int bytes_read;

      while ( ( bytes_read = in.read( buf ) ) != -1 ) {
        out.write( buf, 0, bytes_read );
      }
    }
    finally {
      if ( in != null ) {
        try {
          in.close();
        } catch ( final IOException e ) {}
      }

      if ( out != null ) {
        try {
          out.close();
        } catch ( final IOException e ) {}
      }
    }
  }




  /**
   * Formats the size as a most significant number of bytes.
   * 
   * <p>If the size is less than 1024 bytes, then the format will be in bytes, 
   * if less than a megabyte, then the format will be in KB and so on to TB for 
   * terabytes.</p>
   *
   * @param size the size (of the file?) to format
   *
   * @return A decimal with the most significant size as scale.
   */
  public static String formatSizeBytes( final double size ) {
    final StringBuffer buf = new StringBuffer( 16 );
    String text;
    double divider;

    if ( size < FileUtil.ONE_KB ) {
      text = "bytes";
      divider = 1.0;
    } else if ( size < FileUtil.ONE_MB ) {
      text = "KB";
      divider = FileUtil.ONE_KB;
    } else if ( size < FileUtil.ONE_GB ) {
      text = "MB";
      divider = FileUtil.ONE_MB;
    } else if ( size < FileUtil.ONE_TB ) {
      text = "GB";
      divider = FileUtil.ONE_GB;
    } else {
      text = "TB";
      divider = FileUtil.ONE_TB;
    }

    final double d = ( (double)size ) / divider;
    FileUtil.byteFormat.format( d, buf, new FieldPosition( 0 ) ).append( ' ' ).append( text );

    return buf.toString();
  }




  /**
   * Formats the size as a most significant number of bytes.
   * 
   * <p>If the size is less than 1024 bytes, then the format will be in bytes, 
   * if less than a megabyte, then the format will be in KB and so on to TB for 
   * terabytes.</p>
   *
   * @param number the number to format
   *
   * @return A decimal with the most significant size as scale.
   */
  public static String formatSizeBytes( final Number number ) {
    return FileUtil.formatSizeBytes( number.doubleValue() );
  }




  /**
   * Opens a {@link FileInputStream} for the specified file, providing better
   * error messages than simply calling {@code new FileInputStream(file)}.
   *
   * @param file  the file to open for input, must not be {@code null}
   * 
   * @return a new {@link FileInputStream} for the specified file
   * 
   * @throws FileNotFoundException if the file does not exist
   * @throws IOException if the file object is a directory
   * @throws IOException if the file cannot be read
   */
  public static FileInputStream openInputStream( final File file ) throws IOException {
    if ( file.exists() ) {
      if ( file.isDirectory() ) {
        throw new IOException( "File '" + file + "' exists but is a directory" );
      }
      if ( file.canRead() == false ) {
        throw new IOException( "File '" + file + "' cannot be read" );
      }
    } else {
      throw new FileNotFoundException( "File '" + file + "' does not exist" );
    }
    return new FileInputStream( file );
  }




  /**
   * Opens a {@link FileOutputStream} for the specified file, checking and
   * creating the parent directory if it does not exist.
   *
   * @param file  the file to open for output, must not be {@code null}
   * 
   * @return a new {@link FileOutputStream} for the specified file
   * 
   * @throws IOException if the file object is a directory
   * @throws IOException if the file cannot be written to
   * @throws IOException if a parent directory needs creating but that fails
   */
  public static FileOutputStream openOutputStream( final File file ) throws IOException {
    return openOutputStream( file, false );
  }




  /**
   * Opens a {@link FileOutputStream} for the specified file, checking and
   * creating the parent directory if it does not exist.
   *
   * @param file  the file to open for output, must not be {@code null}
   * @param append if {@code true}, then bytes will be added to the end of the file rather than overwriting
   * 
   * @return a new {@link FileOutputStream} for the specified file
   * 
   * @throws IOException if the file object is a directory
   * @throws IOException if the file cannot be written to
   * @throws IOException if a parent directory needs creating but that fails
   */
  public static FileOutputStream openOutputStream( final File file, final boolean append ) throws IOException {
    if ( file.exists() ) {
      if ( file.isDirectory() ) {
        throw new IOException( "File '" + file + "' exists but is a directory" );
      }
      if ( file.canWrite() == false ) {
        throw new IOException( "File '" + file + "' cannot be written to" );
      }
    } else {
      final File parent = file.getParentFile();
      if ( parent != null ) {
        if ( !parent.mkdirs() && !parent.isDirectory() ) {
          throw new IOException( "Directory '" + parent + "' could not be created" );
        }
      }
    }
    return new FileOutputStream( file, append );
  }




  /**
   * Interpret the filename as a file relative to the given file unless the 
   * filename already represents an absolute filename.
   *
   * @param baseDir The "reference" file for relative paths. This instance must be an absolute file and must not contain &quot;./&quot; or &quot;../&quot; or &quot;\&quot; instead of &quot;/&quot;. If it is null, this call is equivalent to {@code new java.io.File(filename)}.
   *
   * @param filename a file name
   *
   * @return an absolute file that doesn't contain &quot;./&quot; or &quot;../&quot; sequences and uses the correct separator for the current platform.
   */
  public static File resolveFile( final File baseDir, String filename ) {
    // If file is
    filename = filename.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );

    // deal with absolute files
    final int colon = filename.indexOf( ":" );
    if ( filename.startsWith( File.separator ) || ( colon > -1 ) ) {
      return FileUtil.normalize( filename );
    }

    if ( baseDir == null ) {
      return new File( filename );
    }

    File helpFile = new File( baseDir.getAbsolutePath() );
    final StringTokenizer tok = new StringTokenizer( filename, File.separator );

    while ( tok.hasMoreTokens() ) {
      final String part = tok.nextToken();

      if ( part.equals( ".." ) ) {
        helpFile = helpFile.getParentFile();

        if ( helpFile == null ) {
          final String msg = "The file or path you specified (" + filename + ") is invalid relative to " + baseDir.getPath();

          throw new RuntimeException( msg );
        }
      } else if ( part.equals( "." ) ) {
        // Do nothing here
      } else {
        helpFile = new File( helpFile, part );
      }
    }

    return new File( helpFile.getAbsolutePath() );
  }




  /**
   * Return the age of the given file or directory based on time last modified.
   *
   * <p>If the file is a directory, then the returned age will be the latest
   * modified time of all its children. The reasoning is if a sub-directory has
   * a file that was last modified 10 seconds ago, then the parent directory
   * has been logically modified (if only through its path) the same 10 seconds
   * ago.</p>
   *
   * <p>There is no way to tell if a file was recently modified and then
   * deleted from the directory unless the underlying operating system records
   * the activity in the last modified attribute of the directory entry.</p>
   *
   * @param file The file to query.
   *
   * @return The epoch time in milliseconds the file, or one of its children was last accessed, or -1 if the file does not exist ir is null.
   */
  public static long getFileAge( final File file ) {
    if ( ( file != null ) && file.exists() ) {
      if ( file.isDirectory() ) {
        long lastModified = file.lastModified();

        final String[] paths = file.list();

        if ( paths != null ) {
          for ( int i = 0; i < paths.length; i++ ) {
            final File fil = new File( file, paths[i] );
            final long age = FileUtil.getFileAge( fil );

            if ( age > lastModified ) {
              lastModified = age;
            }
          } // for each path in directory

        } // dir not empty

        return lastModified;
      } // is dir

      return file.lastModified();
    }

    return -1L;
  }




  public static String[] textToArray( final File file ) {
    final ArrayList<String> array = new ArrayList<String>();
    FileInputStream fin = null;
    String line;
    try {
      fin = new FileInputStream( file );
      final BufferedReader myInput = new BufferedReader( new InputStreamReader( fin ) );
      while ( ( line = myInput.readLine() ) != null ) {
        array.add( line );
      }
    } catch ( final Exception e ) {
      e.printStackTrace();
    }
    finally {
      try {
        fin.close();
      } catch ( final Exception e ) {}
    }

    final String[] retval = new String[array.size()];
    for ( int x = 0; x < retval.length; retval[x] = array.get( x++ ) ) {
      ;
    }

    return retval;
  }




  /**
  * Returns an Iterator for the lines in a {@code File}.
  * 
  * <p>This method opens an {@code InputStream} for the file.
  * When you have finished with the iterator you should close the stream
  * to free internal resources. This can be done by calling the
  * {@link LineIterator#close()} method.</p>
  * 
  * <p>If an exception occurs during the creation of the iterator, the
  * underlying stream is closed.
  *
  * @param file  the file to open for input, must not be {@code null}
  * 
  * @return an Iterator of the lines in the file, or null if an IO  or Runtime exception is thrown.
  */
  public static LineIterator lineIterator( final File file ) {
    InputStream in = null;
    try {
      in = openInputStream( file );
      return lineIterator( in );
    } catch ( final Exception ex ) {
      close( in );
    }
    return null;
  }




  public static LineIterator lineIterator( final InputStream input ) throws IOException {
    return new LineIterator( new InputStreamReader( input ) );
  }




  public static LineIterator lineIterator( final Reader reader ) {
    return new LineIterator( reader );
  }




  /**
   * Close the Reader quietly consuming any exceptions.
   * 
   * @param reader the Reader to close
   */
  public static void close( final Reader reader ) {
    close( (Closeable)reader );
  }




  /**
   * Close the Writer quietly consuming any exceptions.
   * 
   * @param writer the writer to close
   */
  public static void close( final Writer writer ) {
    close( (Closeable)writer );
  }




  /**
   * Close the input stream quietly consuming any exceptions.
   * 
   * @param is the input stream to close
   */
  public static void close( final InputStream is ) {
    close( (Closeable)is );
  }




  /**
   * Close the output stream quietly consuming any exceptions.
   * 
   * @param os the output stream to close
   */
  public static void close( final OutputStream os ) {
    close( (Closeable)os );
  }




  /**
   * Close the given group of closable objects quietly consuming 
   * any exceptions.
   * 
   * @param closeables the closable objects to close
   */
  public static void close( final Closeable... closeables ) {
    if ( closeables == null ) {
      return;
    }
    for ( final Closeable closeable : closeables ) {
      close( closeable );
    }
  }




  /**
   * Closes a {@code Closeable} object quietly consuming any exceptions thrown. 
   * @param closeable the object to close, may be null or already closed
   */
  public static void close( final Closeable closeable ) {
    try {
      if ( closeable != null )
        closeable.close();
    } catch ( final IOException ignore ) {}
  }




  /**
   * Return the MD5 hash for the given file.
   * 
   * <p>This reads in the file and computes the MD5 hash for its contents. The
   * hash can be compared to the hash values of other files to determine if 
   * their contents are equivalent.  It is possible to detect duplicate files 
   * by computing the hash values for all the files in a given set and testing 
   * their equivalence.</p> 
   * 
   * @param file The file to read and process.
   * 
   * @return the hash value of the file
   */
  public static byte[] getHash( File file ) {
    synchronized( md ) {
      byte[] dataBytes = new byte[1024];

      if ( file != null && file.exists() && file.canRead() && file.isFile() ) {
        try (FileInputStream fis = new FileInputStream( file );) {
          md.reset();
          int nread = 0;
          while ( ( nread = fis.read( dataBytes ) ) != -1 ) {
            md.update( dataBytes, 0, nread );
          } ;
        } catch ( Exception e ) {
          e.printStackTrace();
        }

        byte[] mdbytes = md.digest();

        return mdbytes;
      } else {
        return new byte[0];
      }
    }
  }




  /**
   * Copy the given source file to the target directory.
   * 
   * <p>The source file name will not be changed and should appear in the 
   * target directory.</p>
   * 
   * <p>The directory will be created if necessary.</p>
   * 
   * @param src the source file
   * @param tgt the directory name of the destination
   * 
   * @throws IOException If the copy fails due to an I/O error
   */
  public static void copyFileToDir( String src, String tgt ) throws IOException {
    // make the directory specified by the target string
    File targetDir = FileUtil.makeDirectory( tgt );

    // get a file reference to the source file
    File sourceFile = new File( src );

    // Create a target file reference
    File targetfile = new File( targetDir.getAbsolutePath(), sourceFile.getName() );

    // copy the source file to the calculated target file
    FileUtil.copyFile( sourceFile, targetfile );
  }




  /**
   * @param src
   * @param tgt
   * @throws IOException 
   */
  public static void copyDirectory( String src, String tgt ) throws IOException {
    FileUtil.copyDirectory( new File( src ), new File( tgt ) );
  }




  /**
   * Copy one directory to another.
   * 
   * @param srcDir
   * @param destDir
   * 
   * @throws IOException if the source directory is invalid (!exists,!Directory or !Readable)
   */
  public static void copyDirectory( final File srcDir, final File destDir ) throws IOException {
    if ( srcDir.exists() && srcDir.isDirectory() && srcDir.canRead() ) {
      if ( destDir != null ) {
        copyDirectory( srcDir, destDir, true );
      } else {
        throw new IOException( "No destination directory specified" );
      }
    } else {
      throw new IOException( String.format( "Cannot read from source directory '%s'", srcDir.getAbsolutePath() ) );
    }
  }




  /**
   * Copy one directory to another.
   *
   * @param srcDir the validated source directory, must not be {@code null}
   * @param destDir the validated destination directory, must not be {@code null}
   * @param keepDate whether to preserve the file date
   * 
   * @throws IOException if an error occurs
   */
  private static void copyDirectory( final File srcDir, final File destDir, final boolean keepDate ) throws IOException {
    // recurse
    final File[] srcFiles = srcDir.listFiles();
    if ( srcFiles == null ) { // null if abstract pathname does not denote a directory, or if an I/O error occurs
      throw new IOException( "Failed to list contents of " + srcDir );
    }
    if ( destDir.exists() ) {
      if ( destDir.isDirectory() == false ) {
        throw new IOException( "Destination '" + destDir + "' exists but is not a directory" );
      }
    } else {
      if ( !destDir.mkdirs() && !destDir.isDirectory() ) {
        throw new IOException( "Destination '" + destDir + "' directory cannot be created" );
      }
    }
    if ( destDir.canWrite() == false ) {
      throw new IOException( "Destination '" + destDir + "' cannot be written to" );
    }
    for ( final File srcFile : srcFiles ) {
      final File dstFile = new File( destDir, srcFile.getName() );
      if ( srcFile.isDirectory() ) {
        copyDirectory( srcFile, dstFile, keepDate );
      } else {
        copyFile( srcFile, dstFile, keepDate );
      }
    }

    // Do this last, as the above has probably affected directory metadata
    if ( keepDate ) {
      destDir.setLastModified( srcDir.lastModified() );
    }
  }




  /**
   * Copies one file to another.
   * 
   * <p>This tracks the original file length and throws an IOException if the 
   * output file length is different from the original file length. This has 
   * been known to fail on Window NT and 2000 Server for no apparent reason.</p>
   * 
   * <p>This may also fail with an IllegalArgumentException "Negative size" if 
   * the input file is truncated part way through copying the data and the new 
   * file size is less than the current position. This has been seen to happen 
   * when log files are being copied and log rotation occurs.</p>
   *
   * @param srcFile the validated source file, must not be {@code null}
   * @param destFile the validated destination file, must not be {@code null}
   * @param keepDate whether to preserve the file date
   * 
   * @throws IOException if an error occurs, if the output file length is not 
   *         the same as the input file length after the copy completes
   * @throws IllegalArgumentException "Negative size" if the file is truncated 
   *         so that the size is less than the position
   */
  private static void copyFile( final File srcFile, final File destFile, final boolean keepDate ) throws IOException {
    if ( destFile.exists() && destFile.isDirectory() ) {
      throw new IOException( "Destination '" + destFile + "' exists but is a directory" );
    }

    FileInputStream fis = null;
    FileOutputStream fos = null;
    FileChannel input = null;
    FileChannel output = null;
    try {
      fis = new FileInputStream( srcFile );
      fos = new FileOutputStream( destFile );
      input = fis.getChannel();
      output = fos.getChannel();
      final long size = input.size();
      long pos = 0;
      long count = 0;
      while ( pos < size ) {
        final long remain = size - pos;
        count = remain > BIG_BUFFER_SIZE ? BIG_BUFFER_SIZE : remain;
        final long bytesCopied = output.transferFrom( input, pos, count );
        if ( bytesCopied == 0 ) { //  can happen if file is truncated after caching the size
          break; // ensure we don't loop forever
        }
        pos += bytesCopied;
      }
    }
    finally {
      close( output, fos, input, fis );
    }

    final long srcLen = srcFile.length();
    final long dstLen = destFile.length();
    if ( srcLen != dstLen ) {
      throw new IOException( "Failed to copy full contents from '" + srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen );
    }
    if ( keepDate ) {
      destFile.setLastModified( srcFile.lastModified() );
    }
  }




  /**
   * Convenience method for getting a file reference to "user.dir".
   * 
   * @return a file reference to the currently set working directory
   */
  public static File getCurrentWorkingDirectory() {
    return new File( System.getProperty( "user.dir" ) );
  }




  /**
   * Get a listing of all the files in this directory which matches the given 
   * pattern.
   * 
   * <p>All the files in the directory will be returned.</p>
   * 
   * <p>Only files are returned; directories are not included in the list.</p>
   * 
   * @param directory the directory to scan to start 
   * 
   * @return a list of file references to discovered files matching the given name pattern.
   */
  public static List<File> getFiles( File directory ) {
    return getFiles( directory, null, false );
  }




  /**
   * Get a listing of all the files in this directory which matches the given 
   * pattern.
   * 
   * <p>All the files in the directory will be returned.</p>
   * 
   * <p>Only files are returned; directories are not included in the list.</p>
   * 
   * @param directory the directory to scan to start 
   * @param recurse true to include all sub-directories as well
   * 
   * @return a list of file references to discovered files matching the given name pattern.
   */
  public static List<File> getFiles( File directory, boolean recurse ) {
    return getFiles( directory, null, recurse );
  }




  /**
   * Get a listing of all the files in this directory which matches the given 
   * pattern.
   * 
   * <p>The pattern is a regular expression (regex) which is applied to the 
   * entire file path of the discovered file. If the path of the file matches, 
   * the file is placed in the list of return values.</p>
   * 
   * <p>Only files are returned; directories are not included in the list.</p>
   * 
   * @param directory the directory to scan to start 
   * @param pattern the regex pattern to match, if null, no files will be excluded
   * 
   * @return a list of file references to discovered files matching the given name pattern.
   */
  public static List<File> getFiles( File directory, String pattern ) {
    return getFiles( directory, pattern, false );
  }




  /**
   * Get a listing of all the files in this directory which matches the given 
   * pattern.
   * 
   * <p>The pattern is a regular expression (regex) which is applied to the 
   * entire file path of the discovered file. If the path of the file matches, 
   * the file is placed in the list of return values.</p>
   * 
   * <p>Only files are returned; directories are not included in the list.</p>
   * 
   * @param directory the directory to scan to start 
   * @param pattern the regex pattern to match, if null, no files will be excluded
   * @param recurse true to include all sub-directories as well
   * 
   * @return a list of file references to discovered files matching the given name pattern.
   */
  public static List<File> getFiles( File directory, String pattern, boolean recurse ) {
    final List<File> list = new ArrayList<File>();

    Pattern regex = null;

    if ( pattern != null && pattern.trim().length() > 0 ) {
      regex = Pattern.compile( pattern );
    }

    File[] listOfFiles = directory.listFiles();
    if ( listOfFiles != null ) {

      for ( int i = 0; i < listOfFiles.length; i++ ) {
        if ( listOfFiles[i].isFile() ) {
          // This is where we do pattern checks on the entire file path
          if ( regex != null ) {
            if ( regex.matcher( listOfFiles[i].getAbsolutePath() ).matches() ) {
              list.add( listOfFiles[i] );
            }
          } else {
            list.add( listOfFiles[i] );
          }
        } else if ( listOfFiles[i].isDirectory() ) {
          if ( recurse ) {
            list.addAll( getFiles( listOfFiles[i], pattern, recurse ) );
          }
        } // isFile || dir
      } // for
    } // !null

    return list;
  }




  /**
   * Deletes a directory and all of its contents.
   * 
   * <p>Simple,Quick, No Frills approach to wiping out a directory. See 
   * {@linkplain #clearDir(File, boolean, boolean)} for a method with more 
   * frills.</p>
   * 
   * @param dir the reference to a directory or a file
   * 
   * @return true if everything was deleted, false if at least one item could not be deleted.
   * 
   * @see #clearDir(File, boolean, boolean)
   */
  public static boolean deleteDirectory( File dir ) {
    boolean retval = true;
    File[] currList;
    Stack<File> stack = new Stack<File>();
    stack.push( dir );

    // while we still have things to delete
    while ( !stack.isEmpty() ) {

      // if the next item is a directory
      if ( stack.lastElement().isDirectory() ) {
        // get a listing of all the files and directories
        currList = stack.lastElement().listFiles();

        // place everything found on the stack
        if ( currList.length > 0 ) {
          for ( File curr : currList ) {
            stack.push( curr );
          }
        } else {
          // there is nothing in this directory so it can be deleted
          if ( !stack.pop().delete() )
            retval = false;
        }
      } else {
        // this is a file, so delete it
        if ( !stack.pop().delete() )
          retval = false;
      }
    }
    return retval;
  }




  /**
   * This will copy files and directories from one directory to another.
   * 
   * @param source directory from which the files are to be read
   * @param target directory to which files are to be written
   * @param pattern regex pattern to match on the fully qualified filename
   * @param recurse true to also search the sub-directories of the source directory
   * @param preserveHierarchy true to maintain the hierarchy in the target directory for recursive searches
   * @param keepDate true to keep the data of the target file the same as the source file, false to keep the current date/time
   * @param overwrite true to overwrite files in the target directory with the same name if they exist
   * @param rename true to rename the file in the target directory if it already exists and overwrite is false, false to ignore name collisions
   * 
   * @return true if all files were successfully copied, false if even one file was skipped due to overwrite=false and rename=false settings
   * 
   * @throws IOException if there were major problems with any of the file operations
   */
  public static boolean copyDirectory( String source, String target, String pattern, boolean recurse, boolean preserveHierarchy, boolean keepDate, boolean overwrite, boolean rename ) throws IOException {
    return FileUtil.copyDirectory( new File( source ), new File( target ), pattern, recurse, preserveHierarchy, keepDate, overwrite, rename );
  }




  /**
   * This will copy files and directories from one directory to another.
   * 
   * @param source directory from which the files are to be read
   * @param target directory to which files are to be written
   * @param pattern regex pattern to match on the fully qualified filename
   * @param recurse true to also search the sub-directories of the source directory
   * @param preserveHierarchy true to maintain the hierarchy in the target directory for recursive searches
   * @param keepDate true to keep the date of the target file the same as the source file, false to keep the current date/time
   * @param overwrite true to overwrite files in the target directory with the same name if they exist
   * @param rename true to rename the file in the target directory if it already exists and overwrite is false, false to ignore name collisions
   * 
   * @return true if all files were successfully copied, false if even one file was skipped due to overwrite=false and rename=false settings
   * 
   * @throws IOException if there were major problems with any of the file operations
   */
  public static boolean copyDirectory( File source, File target, String pattern, boolean recurse, boolean preserveHierarchy, boolean keepDate, boolean overwrite, boolean rename ) throws IOException {
    boolean retval = true;

    // if we have a pattern
    if ( StringUtil.isNotBlank( pattern ) ) {

      List<File> foundFiles = FileUtil.getFiles( source, pattern, recurse );

      if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
        Log.debug( "Found " + foundFiles.size() + " files with a pattern of '" + pattern + "' in " + source.getAbsolutePath() + " - resurse:" + recurse );
      }

      if ( recurse ) {
        // collect files matching the pattern from potentially many different directories
        String src = source.getAbsolutePath();
        String tgt = target.getAbsolutePath();

        if ( preserveHierarchy ) {

          // for each of the found files
          for ( File file : foundFiles ) {
            // replace the source directory path with the target directory path
            File tgtFile = new File( file.getAbsolutePath().replace( src, tgt ) );

            // make sure the parent directories exist
            tgtFile.getParentFile().mkdirs();
            // copy the file preserving the date if necessary
            copyFile( file, tgtFile, keepDate );

          }

        } else {
          if ( !target.exists() ) {
            if ( !target.mkdirs() ) {
              throw new IOException( "Could not create target directory: " + target.getAbsolutePath() );
            }
          }

          Set<String> targets = new HashSet<String>();

          for ( File srcFile : foundFiles ) {
            String tgtName = srcFile.getName();
            File tgtFile = new File( tgt, tgtName );

            if ( tgtFile.exists() ) {
              if ( overwrite ) {
                copyFile( srcFile, tgtFile, keepDate );
                targets.add( tgtName );
              } else {
                // we are not allowed to overwrite files so see if we can 
                // rename it
                if ( rename ) {
                  // create a generational target name
                  int generation = 1;
                  while ( targets.contains( tgtName ) ) {
                    tgtName = formatGenerationalName( tgtName, generation++ );
                  }
                  tgtFile = new File( tgt, tgtName );
                  copyFile( srcFile, tgtFile, keepDate );
                  targets.add( tgtName );

                } else {
                  // The target file already exists, but we are not allowed to 
                  // overwrite it and we are not allow to create a new file 
                  // with a different name so don't copy the file and set the 
                  // return flag to false indicating that at least one file did 
                  // not get copied.
                  retval = false;
                }
              }

            } else {
              copyFile( srcFile, tgtFile, keepDate );
              targets.add( tgtName );
            }
          }
        }

      } else {

        // make sure the target directory exists
        if ( !target.exists() ) {
          if ( !target.mkdirs() ) {
            throw new IOException( "Could not create target directory: " + target.getAbsolutePath() );
          }
        }

        String tgt = target.getAbsolutePath();

        // one directory's contents onto another no name collisions
        for ( File srcFile : foundFiles ) {
          String tgtName = srcFile.getName();
          File tgtFile = new File( tgt, tgtName );

          if ( tgtFile.exists() ) {
            if ( overwrite ) {
              copyFile( srcFile, tgtFile, keepDate );
            } else {
              if ( rename ) {
                // create a generational target name
                int generation = 1;
                while ( tgtFile.exists() ) {
                  tgtFile = new File( tgt, formatGenerationalName( tgtName, generation++ ) );
                }
                copyFile( srcFile, tgtFile, keepDate );
              } else {
                // set the return flag to false indicating that at least one 
                // file did not get copied.
                retval = false;
              } // rename
            } // overwrite
          } else {
            // just copy it
            copyFile( srcFile, tgtFile, keepDate );
          } // target exists
        } // for each found file

      } // recurse

    } else {
      // simple directory copy
      FileUtil.copyDirectory( source, target, keepDate );
    }

    return retval;
  }




  /**
   * Construct a generational name from the given filename using the given int 
   * as the generation.
   * 
   * <p>This is designed to be called thusly:<pre>
   * int generation = 1;
   * while(&lt;some_test&gt; )){
   *   tgtFile = formatGenerationalName(tgtFile,generation++);
   * }</pre>
   * 
   * @param tgtFile
   * @param generation
   * 
   * @return the generational name, never null, but may be blank 
   */
  public static String formatGenerationalName( String tgtFile, int generation ) {
    StringBuffer b = new StringBuffer();
    if ( StringUtil.isNotBlank( tgtFile ) ) {
      String[] tokens = tgtFile.split( "\\.(?=[^\\.]+$)" );
      if ( tokens.length == 2 ) {
        b.append( tokens[0] );
        b.append( '.' );
        b.append( generation );
        b.append( '.' );
        b.append( tokens[1] );
      } else if ( tokens.length == 1 ) {
        b.append( tokens[0] );
        b.append( '.' );
        b.append( generation );
      }
    }

    return b.toString();
  }

}
