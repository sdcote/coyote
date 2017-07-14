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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTP;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Retrieve the data via the given source URL and place it in the job 
 * directory unless a file or directory is specified.
 * 
 * <p>The standard use case is a simple HTTP Get from some site. The filename 
 * will be that of the file and it will be placed in the Job Directory.
 * 
 * <p>If a file name is specified, the file will be named matching that 
 * filename argument. Otherwise, it will have the name name as it is 
 * specified on the URL. If the response contains the "Content-Disposition" 
 * header containing the filename, that filename will be used. This allows for 
 * web service calls which return files.
 * 
 * <p>The directory argument will be used to determine the directory into 
 * which the file is placed, unless the filename is absolute, in which case 
 * the directory name is ignored.
 * 
 * <p>If the filename is relative and no directory name is specified, the 
 * current job directory will be used.
 */
public class WebGet extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String source = getString( ConfigTag.SOURCE );
    final String filename = getString( ConfigTag.FILE ); // optional
    final String directory = getString( ConfigTag.DIRECTORY ); // optional

    URL url = null;
    try {
      url = new URL( source );
    } catch ( MalformedURLException e ) {
      final String msg = LogMsg.createMsg( CDX.MSG, "WebGet could not parse the URL: {0}", source ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }
    }

    HttpURLConnection httpConn = null;
    int responseCode = 0;
    try {
      httpConn = (HttpURLConnection)url.openConnection();
      responseCode = httpConn.getResponseCode();
    } catch ( IOException e ) {
      final String msg = LogMsg.createMsg( CDX.MSG, "WebGet could not connect to server: {0} - Reason: {1}", source, e.getMessage() ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }
    }

    if ( responseCode == HttpURLConnection.HTTP_OK ) {

      String fileName = "";
      if ( StringUtil.isBlank( filename ) ) {
        String disposition = httpConn.getHeaderField( HTTP.HDR_CONTENT_DISPOSITION );

        if ( disposition != null ) {
          int index = disposition.indexOf( "filename=" );
          if ( index > 0 ) {
            fileName = disposition.substring( index + 10, disposition.length() - 1 );
          }
        } else {
          fileName = FileUtil.getFile( source );
        }
      } else {
        fileName = filename;
      }

      File targetFile = new File( fileName );

      if ( !targetFile.isAbsolute() ) {
        if ( StringUtil.isNotBlank( directory ) ) {
          targetFile = new File( directory, fileName );
        } else {
          // use the JobDir
          targetFile = new File( getJobDir(), fileName );
        }
      }

      Log.debug( "WebGet retrieving " + source + " to " + targetFile.getAbsolutePath() + ", Content Type: " + httpConn.getContentType() + ", Content Length: " + httpConn.getContentLength() );

      try {
        int bytesTotal = 0;
        try (InputStream inputStream = httpConn.getInputStream(); FileOutputStream outputStream = new FileOutputStream( targetFile )) {
          int bytesRead = -1;
          byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
          while ( ( bytesRead = inputStream.read( buffer ) ) != -1 ) {
            outputStream.write( buffer, 0, bytesRead );
            bytesTotal += bytesRead;
          }
        }
        Log.debug( "Downloaded " + FileUtil.formatSizeBytes( bytesTotal ) + " from " + source + " to " + targetFile.getAbsolutePath() );
      } catch ( IOException e ) {
        final String msg = LogMsg.createMsg( CDX.MSG, "WebGet could not retrieve file from server: {0} - {1}", e.getClass().getSimpleName(), e.getMessage() ).toString();
        Log.error( msg );
        if ( haltOnError ) {
          getContext().setError( msg );
          return;
        }
      }
    } else {
      final String msg = LogMsg.createMsg( CDX.MSG, "WebGet could not retrieve file from server - Response: {0} for {1}", responseCode, source ).toString();
      Log.error( msg );
      if ( haltOnError ) {
        getContext().setError( msg );
        return;
      }
    }
  }

}
