/*
 * Copyright (c) 2002 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import coyote.commons.network.MimeType;
import coyote.loader.log.Log;


/**
 * HTTP response. Return one of these from serve().
 */
public class Response implements Closeable {

  /**
   * HTTP status code after processing, e.g. "200 OK", Status.OK
   */
  private IStatus status;

  /**
   * MIME type of content, e.g. "text/html"
   */
  private String mimeType;

  /**
   * Data of the response, may be null.
   */
  private InputStream data;

  private long contentLength;

  /**
   * Headers for the HTTP response. Use addHeader() to add lines. the
   * lowercase map is automatically kept up to date.
   */
  @SuppressWarnings("serial")
  private final Map<String, String> header = new HashMap<String, String>() {

    @Override
    public String put( final String key, final String value ) {
      lowerCaseHeader.put( key == null ? key : key.toLowerCase(), value );
      return super.put( key, value );
    };
  };

  /**
   * copy of the header map with all the keys lowercase for faster
   * searching.
   */
  private final Map<String, String> lowerCaseHeader = new HashMap<String, String>();

  /**
   * The request method that spawned this response.
   */
  private Method requestMethod;

  /**
   * Use chunkedTransfer
   */
  private boolean chunkedTransfer;

  private boolean encodeAsGzip;

  private boolean keepAlive;




  /**
   * Creates a fixed length response if totalBytes&gt;=0, otherwise chunked.
   */
  public Response( final IStatus status, final String mimeType, final InputStream data, final long totalBytes ) {
    this.status = status;
    this.mimeType = mimeType;
    if ( data == null ) {
      this.data = new ByteArrayInputStream( new byte[0] );
      contentLength = 0L;
    } else {
      this.data = data;
      contentLength = totalBytes;
    }
    chunkedTransfer = contentLength < 0;
    keepAlive = true;
  }




  /**
   * Adds given line to the header.
   */
  public void addHeader( final String name, final String value ) {
    header.put( name, value );
  }




  @Override
  public void close() throws IOException {
    if ( data != null ) {
      data.close();
    }
  }




  /**
   * Indicate to close the connection after the Response has been sent.
   * 
   * @param close {@code true} to hint connection closing, {@code false} to let
   *         connection be closed by client.
   */
  public void closeConnection( final boolean close ) {
    if ( close ) {
      header.put( HTTP.HDR_CONNECTION.toLowerCase(), HTTP.CLOSE );
    } else {
      header.remove( HTTP.HDR_CONNECTION.toLowerCase() );
    }
  }




  public InputStream getData() {
    return data;
  }




  public String getHeader( final String name ) {
    return lowerCaseHeader.get( name.toLowerCase() );
  }




  public String getMimeType() {
    return mimeType;
  }




  public Method getRequestMethod() {
    return requestMethod;
  }




  public IStatus getStatus() {
    return status;
  }




  /**
   * @return {@code true} if connection is to be closed after this
   *         Response has been sent.
   */
  public boolean isCloseConnection() {
    return HTTP.CLOSE.equals( getHeader( HTTP.HDR_CONNECTION.toLowerCase() ) );
  }




  @SuppressWarnings("static-method")
  protected void printHeader( final PrintWriter pw, final String key, final String value ) {
    pw.append( key ).append( ": " ).append( value ).append( "\r\n" );
  }




  /**
   * Sends given response to the socket.
   */
  protected void send( final OutputStream outputStream ) {
    final SimpleDateFormat gmtFrmt = new SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US );
    gmtFrmt.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

    try {
      if ( status == null ) {
        throw new Error( "send(): Status can't be null." );
      }
      final PrintWriter pw = new PrintWriter( new BufferedWriter( new OutputStreamWriter( outputStream, new ContentType( mimeType ).getEncoding() ) ), false );
      pw.append( "HTTP/1.1 " ).append( status.getDescription() ).append( " \r\n" );
      if ( mimeType != null ) {
        printHeader( pw, HTTP.HDR_CONTENT_TYPE, mimeType );
      }
      if ( getHeader( HTTP.HDR_DATE.toLowerCase() ) == null ) {
        printHeader( pw, HTTP.HDR_DATE, gmtFrmt.format( new Date() ) );
      }
      for ( final Entry<String, String> entry : header.entrySet() ) {
        printHeader( pw, entry.getKey(), entry.getValue() );
      }
      if ( getHeader( HTTP.HDR_CONNECTION.toLowerCase() ) == null ) {
        printHeader( pw, HTTP.HDR_CONNECTION, ( keepAlive ? HTTP.KEEP_ALIVE : HTTP.CLOSE ) );
      }
      if ( getHeader( HTTP.HDR_CONTENT_LENGTH.toLowerCase() ) != null ) {
        encodeAsGzip = false;
      }
      if ( encodeAsGzip ) {
        printHeader( pw, HTTP.HDR_CONTENT_ENCODING, HTTP.GZIP );
        setChunkedTransfer( true );
      }
      long pending = data != null ? contentLength : 0;
      if ( ( requestMethod != Method.HEAD ) && chunkedTransfer ) {
        printHeader( pw, HTTP.HDR_TRANSFER_ENCODING, HTTP.CHUNKED );
      } else if ( !encodeAsGzip ) {
        pending = sendContentLengthHeaderIfNotAlreadyPresent( pw, pending );
      }
      pw.append( "\r\n" );
      pw.flush();
      sendBodyWithCorrectTransferAndEncoding( outputStream, pending );
      outputStream.flush();
      HTTPD.safeClose( data );
    } catch ( final IOException ioe ) {
      Log.append( HTTPD.EVENT, "ERROR: Could not send response to the client", ioe );
    }
  }




  /**
   * Sends the body to the specified OutputStream. The pending parameter
   * limits the maximum amounts of bytes sent unless it is -1, in which
   * case everything is sent.
   * 
   * @param outputStream the OutputStream to send data to
   * @param pending -1 to send everything, otherwise sets a max limit to the 
   *        number of bytes sent
   *
   * @throws IOException if something goes wrong while sending the data.
   */
  private void sendBody( final OutputStream outputStream, long pending ) throws IOException {
    final long BUFFER_SIZE = 16 * 1024;
    final byte[] buff = new byte[(int)BUFFER_SIZE];
    final boolean sendEverything = pending == -1;
    while ( ( pending > 0 ) || sendEverything ) {
      final long bytesToRead = sendEverything ? BUFFER_SIZE : Math.min( pending, BUFFER_SIZE );
      final int read = data.read( buff, 0, (int)bytesToRead );
      if ( read <= 0 ) {
        break;
      }
      outputStream.write( buff, 0, read );
      if ( !sendEverything ) {
        pending -= read;
      }
    }
  }




  private void sendBodyWithCorrectEncoding( final OutputStream outputStream, final long pending ) throws IOException {
    if ( encodeAsGzip ) {
      final GZIPOutputStream gzipOutputStream = new GZIPOutputStream( outputStream );
      sendBody( gzipOutputStream, -1 );
      gzipOutputStream.finish();
    } else {
      sendBody( outputStream, pending );
    }
  }




  private void sendBodyWithCorrectTransferAndEncoding( final OutputStream outputStream, final long pending ) throws IOException {
    if ( ( requestMethod != Method.HEAD ) && chunkedTransfer ) {
      final ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream( outputStream );
      sendBodyWithCorrectEncoding( chunkedOutputStream, -1 );
      chunkedOutputStream.finish();
    } else {
      sendBodyWithCorrectEncoding( outputStream, pending );
    }
  }




  protected long sendContentLengthHeaderIfNotAlreadyPresent( final PrintWriter pw, final long defaultSize ) {
    final String contentLengthString = getHeader( HTTP.HDR_CONTENT_LENGTH.toLowerCase() );
    long size = defaultSize;
    if ( contentLengthString != null ) {
      try {
        size = Long.parseLong( contentLengthString );
      } catch ( final NumberFormatException ex ) {
        Log.append( HTTPD.EVENT, "ERROR: content-length was not a number " + contentLengthString );
      }
    }
    pw.print( HTTP.HDR_CONTENT_LENGTH + ": " + size + "\r\n" );
    return size;
  }




  public void setChunkedTransfer( final boolean chunkedTransfer ) {
    this.chunkedTransfer = chunkedTransfer;
  }




  public void setData( final InputStream data ) {
    this.data = data;
  }




  public void setGzipEncoding( final boolean encodeAsGzip ) {
    this.encodeAsGzip = encodeAsGzip;
  }




  public void setKeepAlive( final boolean useKeepAlive ) {
    keepAlive = useKeepAlive;
  }




  public void setMimeType( final String mimeType ) {
    this.mimeType = mimeType;
  }




  public void setRequestMethod( final Method requestMethod ) {
    this.requestMethod = requestMethod;
  }




  public void setStatus( final IStatus status ) {
    this.status = status;
  }




  /**
   * Create a response with unknown length (using HTTP 1.1 chunking).
   */
  public static Response createChunkedResponse( final IStatus status, final String mimeType, final InputStream data ) {
    return new Response( status, mimeType, data, -1 );
  }




  /**
   * Create a response with known length.
   */
  public static Response createFixedLengthResponse( final IStatus status, final String mimeType, final InputStream data, final long totalBytes ) {
    return new Response( status, mimeType, data, totalBytes );
  }




  /**
   * Create a text response with known length.
   */
  public static Response createFixedLengthResponse( final IStatus status, final String mimeType, final String txt ) {
    ContentType contentType = new ContentType( mimeType );
    if ( txt == null ) {
      return createFixedLengthResponse( status, mimeType, new ByteArrayInputStream( new byte[0] ), 0 );
    } else {
      byte[] bytes;
      try {
        final CharsetEncoder newEncoder = Charset.forName( contentType.getEncoding() ).newEncoder();
        if ( !newEncoder.canEncode( txt ) ) {
          contentType = contentType.tryUTF8();
        }
        bytes = txt.getBytes( contentType.getEncoding() );
      } catch ( final UnsupportedEncodingException e ) {
        Log.append( HTTPD.EVENT, "encoding problem", e );
        bytes = new byte[0];
      }
      return createFixedLengthResponse( status, contentType.getContentTypeHeader(), new ByteArrayInputStream( bytes ), bytes.length );
    }
  }




  /**
   * Create a text response with known length.
   */
  public static Response createFixedLengthResponse( final String msg ) {
    return createFixedLengthResponse( Status.OK, MimeType.HTML.getType(), msg );
  }




  /**
   * Add and possibly overwrite all the headers of the given map to the 
   * response headers map.
   * 
   * @param responseHeaders the map of headers to place in the output 
   */
  public void addHeaders( Map<String, String> responseHeaders ) {
    if ( responseHeaders != null ) {
      for ( Map.Entry<String, String> entry : responseHeaders.entrySet() ) {
        addHeader( entry.getKey(), entry.getValue() );
      }
    }
  }

}