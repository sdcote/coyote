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

import java.net.URL;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.loader.log.Log;


/**
 * This responder retrieves the requested page from the class path via the class 
 * loader.
 * 
 * <p>This is useful when there is no file system from which to serve content 
 * such as container-ized applications or when the server is not allowed to 
 * access the file system for security reasons. The ensures that only specific,
 * intended content is served.</p>
 *
 *
 * addRoute( "/(.)+", ClassloadingResponder.class, "/content" );
 */
public class ClassloadingResponder extends DefaultResponder {

  // the ClassLoader object associated with this Class
  ClassLoader cLoader = this.getClass().getClassLoader();




  /**
   * retrieve the requested resource from the class path.
   *  
   * @see coyote.commons.network.http.responder.DefaultResponder#get(coyote.commons.network.http.responder.Resource, java.util.Map, coyote.commons.network.http.IHTTPSession)
   */
  @Override
  public Response get( final Resource resource, final Map<String, String> urlParams, final IHTTPSession session ) {

    final String baseUri = resource.getUri(); // the regex matcher URL

    String coreRequest = HTTPDRouter.normalizeUri( session.getUri() );

    // find the portion of the URI which differs from the base
    for ( int index = 0; index < Math.min( baseUri.length(), coreRequest.length() ); index++ ) {
      if ( baseUri.charAt( index ) != coreRequest.charAt( index ) ) {
        coreRequest = HTTPDRouter.normalizeUri( coreRequest.substring( index ) );
        break;
      }
    }

    // Retrieve the base directory in the classpath for our search
    String parentdirectory = resource.initParameter( String.class );

    // make sure we are configured with a properly formatted parent directory
    if ( !parentdirectory.endsWith( "/" ) ) {
      parentdirectory = parentdirectory.concat( "/" );
    }
    if ( parentdirectory.startsWith( "/" ) ) {
      parentdirectory = parentdirectory.substring( 1 );
    }

    // add our configured parent directory to the real request. This is the
    // actual local resource for which we are looking:
    String localPath = parentdirectory + coreRequest;

    // A blank request indicates a request for our root directory; see if there 
    // is an index file in the root
    if ( StringUtil.isBlank( coreRequest ) || coreRequest.endsWith( "/" ) ) {
      localPath = getDirectoryIndexRequest( localPath );

      // If we did not get a new local path, it means there is no index file in 
      // the directory
      if ( StringUtil.isBlank( localPath ) ) {
        if ( StringUtil.isBlank( coreRequest ) ) {
          Log.append( HTTPD.EVENT, "There does not appear to be an index file in the content root (" + parentdirectory + ") of the classpath." );
        }
        Log.append( HTTPD.EVENT, "404 NOT FOUND - '" + coreRequest + "'" );
        return new Error404Responder().get( resource, urlParams, session );
      }
    }

    // See if this resource exists
    URL rsc = cLoader.getResource( localPath );

    if ( rsc == null ) {
      // couldn't find the resource
      Log.append( HTTPD.EVENT, "404 NOT FOUND - '" + coreRequest + "' LOCAL: " + localPath );
      return new Error404Responder().get( resource, urlParams, session );
    } else {
      // Success - Found the resource - 
      // Hopefully it is not a directory...
      // <sigh/> not sure how to detect those with a class loader TODO 
      try {
        return Response.createChunkedResponse( getStatus(), HTTPD.getMimeTypeForFile( localPath ), cLoader.getResourceAsStream( localPath ) );
      } catch ( final Exception ioe ) {
        return Response.createFixedLengthResponse( Status.REQUEST_TIMEOUT, MimeType.TEXT.getType(), null );
      }
    }
  }




  /**
   * Treat the given request path as a directory and try different options to pull an index request.
   * 
   * @param path
   * 
   * @return the new request which will return one of the index files
   */
  private String getDirectoryIndexRequest( String path ) {
    if ( StringUtil.isBlank( path ) ) {
      path = "/";
    }
    if ( !path.endsWith( "/" ) ) {
      path = path.concat( "/" );
    }

    // look for the standard index file name
    String retval = path.concat( "index.html" );
    if ( cLoader.getResource( retval ) != null ) {
      return retval; // found it
    } else {
      retval = path.concat( "index.htm" );
      if ( cLoader.getResource( retval ) != null ) {
        return retval; // found DOS index file
      } else {
        return null; // did not find either
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