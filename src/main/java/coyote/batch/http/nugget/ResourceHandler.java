package coyote.batch.http.nugget;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.nugget.DefaultHandler;
import coyote.commons.network.http.nugget.Error404UriHandler;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.commons.network.http.nugget.UriResource;
import coyote.loader.log.Log;


/**
 * Servers resources from the class path and not the file system.
 * 
 * <p>All data must be a resource in the class path. This handler does not 
 * serve anything which is not in the packaged application. This is arguably 
 * more secure than serving from the file system in that the user does not have 
 * the opportunity to introduce sensitive data or links to other resources.
 * 
 * <p>The root of the class path uses is "content" by default but can be 
 * changed
 * 
 * addRoute( "/(.)+", ResourceHandler.class, "content" );
 * 
 */
public class ResourceHandler extends DefaultHandler {

  private static final String ROOT_URL = "/";
  private static final String DEFAULT_ROOT = "content";
  private boolean redirectOnIndexedDir = true;

  // The class loader object associated with this Class
  ClassLoader cLoader = this.getClass().getClassLoader();




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
  public Response get( final UriResource uriResource, final Map<String, String> urlParams, final IHTTPSession session ) {

    showRequest( uriResource, session );

    //redirectOnIndexedDir = uriResource.initParameter( 0, Boolean.class );

    final String baseUri = uriResource.getUri(); // the regex matcher URL

    String coreRequest = HTTPDRouter.normalizeUri( session.getUri() );

    // find the portion of the URI which differs from the base
    for ( int index = 0; index < Math.min( baseUri.length(), coreRequest.length() ); index++ ) {
      if ( baseUri.charAt( index ) != coreRequest.charAt( index ) ) {
        coreRequest = HTTPDRouter.normalizeUri( coreRequest.substring( index ) );
        break;
      }
    }

    // Retrieve the base directory in the classpath for our search
    String parentdirectory = uriResource.initParameter( 0, String.class );
    if ( StringUtil.isBlank( parentdirectory ) ) {
      parentdirectory = DEFAULT_ROOT;
    }

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

    // A blank request or one ending with a path delimiter indicates a request 
    // for our root or some other directory; see if there is an index file in 
    // the requested directory and send a (301) redirect if so.
    if ( StringUtil.isBlank( coreRequest ) || coreRequest.endsWith( "/" ) ) {
      localPath = getDirectoryIndexRequest( localPath );

      // If we did not get a new local path, it means there is no index file in 
      // the directory
      if ( StringUtil.isBlank( localPath ) ) {
        if ( StringUtil.isBlank( coreRequest ) ) {
          Log.append( HTTPD.EVENT, "There does not appear to be an index file in the content root (" + parentdirectory + ") of the classpath." );
        }
        Log.append( HTTPD.EVENT, "404 NOT FOUND - '" + coreRequest + "'" );
        return new Error404UriHandler().get( uriResource, urlParams, session );
      } else {
        if ( redirectOnIndexedDir ) {
          // We need to send a 301, indicating the new (proper) URL to use
          String redirectlocation = localPath.replace( parentdirectory.substring( 0, parentdirectory.length() - 1 ), "" ); // YUCK!!!
          Response redirect = HTTPD.newFixedLengthResponse( Status.REDIRECT, "text/plain", null );
          redirect.addHeader( "Location", redirectlocation );
          return redirect;
        } else {
          // hide the fact that we are serving the index page and just serve 
          // the index page
          try {
            return HTTPD.newChunkedResponse( Status.OK, HTTPD.getMimeTypeForFile( localPath ), cLoader.getResourceAsStream( localPath ) );
          } catch ( final Exception ioe ) {
            return HTTPD.newFixedLengthResponse( Status.REQUEST_TIMEOUT, "text/plain", null );
          }
        }
      }
    } else {
      // See if the requested resource exists
      URL rsc = cLoader.getResource( localPath );

      // if we have no URL, the class loader could not find the resource 
      if ( rsc == null ) {
        Log.append( HTTPD.EVENT, "404 NOT FOUND - '" + coreRequest + "' LOCAL: " + localPath );
        return new Error404UriHandler().get( uriResource, urlParams, session );
      } else {
        // Success - Found the resource - 
        try {
          return HTTPD.newChunkedResponse( Status.OK, HTTPD.getMimeTypeForFile( localPath ), cLoader.getResourceAsStream( localPath ) );
        } catch ( final Exception ioe ) {
          return HTTPD.newFixedLengthResponse( Status.REQUEST_TIMEOUT, "text/plain", null );
        }
      }
    }
  }




  private void showRequest( UriResource uriResource, IHTTPSession session ) {
    Map<String, String> header = session.getHeaders();
    Map<String, String> parms = session.getParms();
    String uri = session.getUri();

    final String baseUri = uriResource.getUri();

    session.getQueryParameterString();

    // Print 
    if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
      StringBuffer b = new StringBuffer( "DEBUG: " );

      b.append( session.getMethod() + " '" + uri + "' \r\n" );

      Iterator<String> e = header.keySet().iterator();
      while ( e.hasNext() ) {
        String value = e.next();
        b.append( "   HDR: '" + value + "' = '" + header.get( value ) + "'\r\n" );
      }
      e = parms.keySet().iterator();
      while ( e.hasNext() ) {
        String value = e.next();
        b.append( "   PRM: '" + value + "' = '" + parms.get( value ) + "'\r\n" );
      }
      Log.append( HTTPD.EVENT, b.toString() );
    }

    Log.append( HTTPD.EVENT, "Servicing request for " + baseUri );

    String realUri = HTTPDRouter.normalizeUri( session.getUri() );
    Log.append( HTTPD.EVENT, "Servicing request for real URI  '" + realUri + "'" );

    for ( int index = 0; index < Math.min( baseUri.length(), realUri.length() ); index++ ) {
      if ( baseUri.charAt( index ) != realUri.charAt( index ) ) {
        realUri = HTTPDRouter.normalizeUri( realUri.substring( index ) );
        break;
      }
    }
    Log.append( HTTPD.EVENT, "Processed request for real URI '" + realUri + "'" );
  }




  /**
   * Use the class loader to find the named resource
   * 
   * @param name full path name to the resource to load
   * 
   * @return An input stream for reading the resource, or {@code null} if the 
   *         resource could not be found
   */
  protected BufferedInputStream resourceToInputStream( final String name ) {
    return new BufferedInputStream( ClassLoader.getSystemResourceAsStream( name ) );
  }




  @Override
  public IStatus getStatus() {
    return Status.INTERNAL_ERROR; // this should never be called
  }




  @Override
  public String getText() {
    return Status.INTERNAL_ERROR.getDescription(); // this should never be called
  }




  @Override
  public String getMimeType() {
    return "text/plain"; // this should never be called
  }

}