/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http.responder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;


/**
 * Generic responder to retrieve the requested page from a file root.
 * 
 * <p>The first initialization parameter is the directory from which the files 
 * are to be served.</p>
 */
public class StaticPageResponder extends DefaultResponder {

  private static String[] getPathArray( final String uri ) {
    final String array[] = uri.split( "/" );
    final ArrayList<String> pathArray = new ArrayList<String>();

    for ( final String s : array ) {
      if ( s.length() > 0 ) {
        pathArray.add( s );
      }
    }

    return pathArray.toArray( new String[] {} );

  }




  protected BufferedInputStream fileToInputStream( final File fileOrdirectory ) throws IOException {
    return new BufferedInputStream( new FileInputStream( fileOrdirectory ) );
  }




  @Override
  public Response get( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {
    final String baseUri = resource.getUri();
    String realUri = HTTPDRouter.normalizeUri( session.getUri() );
    for ( int index = 0; index < Math.min( baseUri.length(), realUri.length() ); index++ ) {
      if ( baseUri.charAt( index ) != realUri.charAt( index ) ) {
        realUri = HTTPDRouter.normalizeUri( realUri.substring( index ) );
        break;
      }
    }

    // Start with the root directory as set in our init parameter
    File requestedFile = resource.initParameter( File.class );

    // TODO: this has a smell, redesign
    for ( final String pathPart : getPathArray( realUri ) ) {
      requestedFile = new File( requestedFile, pathPart );
    }

    // if they asked for a directory, look for an index file
    if ( requestedFile.isDirectory() ) {
      requestedFile = new File( requestedFile, "index.html" );
      // if that does not exist, look for the DOS version of the index file
      if ( !requestedFile.exists() ) {
        requestedFile = new File( requestedFile.getParentFile(), "index.htm" );
      }
    }

    // if the file does not exist or is not a file...
    if ( !requestedFile.exists() || !requestedFile.isFile() ) {
      // throw a 404 at them
      return new Error404Responder().get( resource, urlParams, session );
    } else {

      // return the found file
      try {
        return Response.createChunkedResponse( getStatus(), HTTPD.getMimeTypeForFile( requestedFile.getName() ), fileToInputStream( requestedFile ) );
      } catch ( final IOException ioe ) {
        return Response.createFixedLengthResponse( Status.REQUEST_TIMEOUT, MimeType.TEXT.getType(), null );
      }
    }
  }




  @Override
  public String getMimeType() {
    throw new IllegalStateException( "This method should not be called" );
  }




  @Override
  public IStatus getStatus() {
    return Status.OK;
  }




  @Override
  public String getText() {
    throw new IllegalStateException( "This method should not be called" );
  }
}