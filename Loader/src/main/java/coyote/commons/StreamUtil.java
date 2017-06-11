/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * StreamUtil defines a set of static methods for manipulating streams.
 */
public final class StreamUtil {

  /** Field ENCODING */
  static final String ENCODING = "encoding=";

  /** Size of block used in IO (4096 bytes) */
  static final int CHUNK_SIZE = 4096;

  /** Empty byte array */
  static final byte NO_BYTES[] = new byte[0];

  private static final String fileIOEncoding = "8859_1";




  /**
   * Copy the bytes from the given inputstream to the given output stream and
   * return the number of bytes copied.
   *
   * <p>WARNING: All data is read into memory first, then sent to the output
   * stream so this method is dependent on available memory.</p>
   *
   * @param from The input stream to read from.
   * @param to The output stream to write to.
   *
   * @return TODO: Finish documentation the number of bytes copied
   *
   * @throws IOException
   */
  public static final int copy( final InputStream from, final OutputStream to ) throws IOException {
    // write the data
    final byte[] buffer = loadBytes( from );
    to.write( buffer );
    to.flush();

    // to know how many bytes were copied
    return buffer.length;
  }




  /**
   * Method copy
   *
   * @param inputstream
   * @param outputstream
   * @param i
   * @param j
   *
   * @throws IOException
   */
  public static void copy( final InputStream inputstream, final OutputStream outputstream, final int i, final int j ) throws IOException {
    final byte bytes[] = new byte[j];
    int l;

    for ( int k = 0; k < i; k += l ) {
      l = Math.min( j, i - k );

      readFully( inputstream, bytes, 0, l );
      outputstream.write( bytes, 0, l );
    }
  }




  /**
   * Method getBufferedPrintWriter
   *
   * @param outputstream
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static PrintWriter getBufferedPrintWriter( final OutputStream outputstream ) throws IOException {
    return new PrintWriter( new BufferedWriter( new OutputStreamWriter( outputstream, fileIOEncoding ) ) );
  }




  /**
   * Method getBufferedReader
   *
   * @param inputstream
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static BufferedReader getBufferedReader( final InputStream inputstream ) throws IOException {
    return new BufferedReader( new InputStreamReader( inputstream, fileIOEncoding ) );
  }




  /**
   * Method getCharBufferedPrintWriter
   *
   * @param outputstream
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static PrintWriter getCharBufferedPrintWriter( final OutputStream outputstream ) throws IOException {
    return new PrintWriter( new BufferedWriter( new OutputStreamWriter( outputstream ) ) );
  }




  /**
   * Method getCharBufferedReader
   *
   * @param inputstream
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static BufferedReader getCharBufferedReader( final InputStream inputstream ) throws IOException {
    return new BufferedReader( new InputStreamReader( inputstream ) );
  }




  /**
   * Method getEncoding
   *
   * @param bytes
   *
   * @return TODO: Finish documentation
   */
  public static String getEncoding( final byte bytes[] ) {
    if ( isUTF16( bytes ) ) {
      return "UTF-16";
    }

    final String s = new String( bytes );
    final int i = s.indexOf( ENCODING );

    if ( i == -1 ) {
      return null;
    } else {
      final int j = i + ENCODING.length() + 1;
      final char c = s.charAt( j - 1 );
      final int k = s.indexOf( c, j );
      return s.substring( j, k );
    }
  }




  /**
   * Method getReader
   *
   * @param file
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static Reader getReader( final File file ) throws IOException {
    final FileInputStream fileinputstream = new FileInputStream( file );
    final byte bytes[] = readUpTo( fileinputstream, 100 );
    fileinputstream.close();

    final String s = normalizeEncoding( getEncoding( bytes ) );
    return new InputStreamReader( new FileInputStream( file ), s );
  }




  /**
   * Method getReader
   *
   * @param inputstream
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static Reader getReader( InputStream inputstream ) throws IOException {
    if ( !( inputstream instanceof BufferedInputStream ) ) {
      inputstream = new BufferedInputStream( inputstream );
    }

    inputstream.mark( 100 );

    final byte bytes[] = readUpTo( inputstream, 100 );
    inputstream.reset();

    final String s = normalizeEncoding( getEncoding( bytes ) );
    return new InputStreamReader( inputstream, s );
  }




  /**
   * Method getURL
   *
   * @param file
   *
   * @return TODO: Finish documentation
   *
   * @throws MalformedURLException
   */
  public static URL getURL( final File file ) throws MalformedURLException {
    String s = file.getAbsolutePath();

    if ( File.separatorChar != '/' ) {
      s = s.replace( File.separatorChar, '/' );
    }

    if ( !s.startsWith( "/" ) ) {
      s = "/".concat( String.valueOf( s ) );
    }

    if ( !s.endsWith( "/" ) && file.isDirectory() ) {
      s = String.valueOf( s ).concat( "/" );
    }

    return new URL( "file", "", s );
  }




  /**
   * Method getWriter
   *
   * @param file
   * @param encoding
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  public static Writer getWriter( final File file, final String encoding ) throws UnsupportedEncodingException, IOException {
    return getWriter( ( ( new FileOutputStream( file ) ) ), encoding );
  }




  /**
   * Method getWriter
   *
   * @param outputstream
   * @param encoding
   *
   * @return TODO: Finish documentation
   *
   * @throws UnsupportedEncodingException
   */
  public static Writer getWriter( final OutputStream outputstream, String encoding ) throws UnsupportedEncodingException {
    encoding = normalizeEncoding( encoding );

    return new OutputStreamWriter( outputstream, encoding );
  }




  /**
   * Copy an input stream completely to an output stream.
   *
   * @param in The stream to read.
   * @param out The stream to write.
   *
   * @throws IOException If problems occur.
   */
  public static void inputStreamToOutputStream( final InputStream in, final OutputStream out ) throws IOException {
    byte[] buffer = null;
    boolean streaming = true;

    while ( streaming ) {
      buffer = new byte[CHUNK_SIZE];

      final int count = in.read( buffer );

      if ( count == -1 ) {
        streaming = false;
      } else {
        if ( count < CHUNK_SIZE ) {
          streaming = false;
        }

        out.write( buffer, 0, count );
      }
    }
  }




  /**
   * Method isUTF16
   *
   * @param abyte0
   *
   * @return TODO: Finish documentation
   */
  public static boolean isUTF16( final byte abyte0[] ) {
    if ( abyte0.length < 2 ) {
      return false;
    } else {
      return ( ( abyte0[0] == -1 ) && ( abyte0[1] == -2 ) ) || ( ( abyte0[0] == -2 ) && ( abyte0[1] == -1 ) );
    }
  }




  /**
   * Method isUTF8
   *
   * @param string
   *
   * @return TODO: Finish documentation
   */
  public static boolean isUTF8( final String string ) {
    return ( string == null ) || string.equalsIgnoreCase( "UTF-8" ) || string.equalsIgnoreCase( "UTF8" );
  }




  /**
   * Method listFiles
   *
   * @param file
   *
   * @return TODO: Finish documentation
   */
  public static File[] listFiles( final File file ) {
    final String as[] = file.list();

    if ( as == null ) {
      return null;
    }

    final int i = as.length;
    final File afile[] = new File[i];

    for ( int j = 0; j < i; j++ ) {
      afile[j] = new File( file.getPath(), as[j] );
    }

    return afile;
  }




  /**
   * Reads all bytes from the given stream and returns a byte array with the
   * results
   *
   * @param is the stream from which to read
   *
   * @return The bytes that were read in from the input stream
   *
   * @throws IOException if any problem occurred.
   */
  public static final byte[] loadBytes( final InputStream is ) throws IOException {
    // read in the entry data
    int count = 0;
    byte[] buffer = new byte[0];
    final byte[] chunk = new byte[4096];

    while ( ( count = is.read( chunk ) ) >= 0 ) {
      final byte[] t = new byte[buffer.length + count];
      System.arraycopy( buffer, 0, t, 0, buffer.length );
      System.arraycopy( chunk, 0, t, buffer.length, count );

      buffer = t;
    }

    return buffer;
  }




  /**
   * Method loadResource
   *
   * @param s
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static byte[] loadResource( final String s ) throws IOException {
    final URL url = new URL( s );
    final InputStream inputstream = url.openStream();
    final byte data[] = readFully( inputstream );
    inputstream.close();

    return data;
  }




  /**
   * Reads all the characters from the given Reader and generates a String
   *
   * @param reader
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static final String loadString( final Reader reader ) throws IOException {
    // read in the entry data
    int count = 0;
    char[] buffer = new char[0];
    final char[] chunk = new char[4096];

    while ( ( count = reader.read( chunk ) ) >= 0 ) {
      final char[] t = new char[buffer.length + count];
      System.arraycopy( buffer, 0, t, 0, buffer.length );
      System.arraycopy( chunk, 0, t, buffer.length, count );

      buffer = t;
    }

    return new String( buffer );
  }




  /**
   * Method normalizeEncoding
   *
   * @param encoding
   *
   * @return TODO: Finish documentation
   */
  public static String normalizeEncoding( final String encoding ) {
    return ( ( encoding != null ) && !encoding.equalsIgnoreCase( "UTF-8" ) ) ? encoding : "UTF8";
  }




  /**
   * Method readFully
   *
   * @param file
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static byte[] readFully( final File file ) throws IOException {
    final RandomAccessFile randomaccessfile = new RandomAccessFile( file, "r" );
    final byte bytes[] = new byte[(int)randomaccessfile.length()];
    randomaccessfile.readFully( bytes );
    randomaccessfile.close();

    return bytes;
  }




  /**
   * Method readFully
   *
   * @param inputstream
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static byte[] readFully( final InputStream inputstream ) throws IOException {
    final byte bytes[] = new byte[CHUNK_SIZE];
    final ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();

    do {
      final int i = inputstream.read( bytes, 0, bytes.length );

      if ( i != -1 ) {
        bytearrayoutputstream.write( bytes, 0, i );
      } else {
        return bytearrayoutputstream.toByteArray();
      }
    }
    while ( true );
  }




  /**
   * Method readFully
   *
   * @param inputstream
   * @param bytes
   * @param i
   * @param j
   *
   * @throws IOException
   */
  public static void readFully( final InputStream inputstream, final byte bytes[], final int i, final int j ) throws IOException {
    int l;

    for ( int k = 0; k < j; k += l ) {
      l = inputstream.read( bytes, i + k, j - k );

      if ( l < 0 ) {
        throw new EOFException( String.valueOf( ( new StringBuffer( "expected " ) ).append( j ).append( " bytes of content, got " ).append( k ) ) );
      }
    }
  }




  /**
   * Method readFully
   *
   * @param inputstream
   * @param i
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static byte[] readFully( final InputStream inputstream, final int i ) throws IOException {
    if ( i <= 0 ) {
      return NO_BYTES;
    } else {
      final byte bytes[] = new byte[i];
      readFully( inputstream, bytes, 0, i );

      return bytes;
    }
  }




  /**
   * Construct a string by reading bytes in from the given inputstream until
   * the LF sequence is observed.
   *
   * <p>All CR characters will be ignored and stripped from the returned
   * string.</p>
   *
   * <p>This will NOT work on Macintosh files which only use CR as a line
   * terminator.</p>
   *
   * @param inputstream The stream to read
   *
   * @return the string read in without any CR or LF characters, null if the
   *         stream is EOF or closed
   *         
   * @throws IOException
   */
  public static String readLine( final InputStream inputstream ) throws IOException {
    final StringBuffer stringbuffer = new StringBuffer();

    do {
      final int i = inputstream.read();

      if ( i == -1 ) {
        return ( stringbuffer.length() != 0 ) ? stringbuffer.toString() : null;
      }

      // line-feeds represent the end of line
      if ( i == 10 ) {
        return stringbuffer.toString();
      }

      // Ignore carriage returns
      if ( i != 13 ) {
        stringbuffer.append( (char)i );
      }
    }
    while ( true );
  }




  /**
   * Method readUpTo
   *
   * @param inputstream
   * @param bytes
   * @param i
   * @param j
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static int readUpTo( final InputStream inputstream, final byte bytes[], final int i, final int j ) throws IOException {
    int k = 0;

    do {
      if ( k >= j ) {
        break;
      }

      final int l = inputstream.read( bytes, i + k, j - k );

      if ( l < 0 ) {
        break;
      }

      k += l;
    }
    while ( true );

    return k;
  }




  /**
   * Method readUpTo
   *
   * @param inputstream
   * @param i
   *
   * @return TODO: Finish documentation
   *
   * @throws IOException
   */
  public static byte[] readUpTo( final InputStream inputstream, final int i ) throws IOException {
    if ( i <= 0 ) {
      return NO_BYTES;
    }

    byte bytes[] = new byte[i];
    final int j = readUpTo( inputstream, bytes, 0, i );

    if ( j < i ) {
      final byte morebytes[] = bytes;
      bytes = new byte[j];

      System.arraycopy( morebytes, 0, bytes, 0, j );
    }

    return bytes;
  }




  /**
   * Save the given text out to the given file.
   *
   * @param filename
   * @param s1
   * @param textOut
   * @param s3
   *
   * @throws IOException
   */
  public static void saveFile( final String filename, final String s1, final String textOut, final String s3 ) throws IOException {
    if ( ( filename != null ) && ( filename.length() > 0 ) ) {
      final File file = new File( filename );
      file.mkdirs();
    }

    final File file1 = new File( String.valueOf( ( new StringBuffer( String.valueOf( filename ) ) ).append( s1 ).append( s3 ) ) );
    System.out.println( "write file ".concat( String.valueOf( file1 ) ) );

    // Delete it if it exists
    if ( file1.exists() ) {
      file1.delete();
    }

    final FileOutputStream fileoutputstream = new FileOutputStream( file1 );
    fileoutputstream.write( textOut.getBytes() );
    fileoutputstream.close();
  }




  /**
   * Method toString
   *
   * @param abyte0
   *
   * @return TODO: Finish documentation
   *
   * @throws UnsupportedEncodingException
   */
  public static String toString( final byte abyte0[] ) throws UnsupportedEncodingException {
    return isUTF16( abyte0 ) ? new String( abyte0, "UTF-16" ) : new String( abyte0, "UTF8" );
  }




  /**
   * Private constructor because everything is static
   */
  private StreamUtil() {}

}