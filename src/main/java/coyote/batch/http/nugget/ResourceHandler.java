package coyote.batch.http.nugget;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import coyote.batch.Service;
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
 */
public class ResourceHandler extends DefaultHandler {
  
  private static final String DEFAULT_ROOT = "content";

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
  public Response get( final UriResource uriResource, final Map<String, String> urlParams, final IHTTPSession session ) {


    // The first init parameter should be the content root namespace in the class path for all our lookups.
    String root = uriResource.initParameter( 0, String.class );
    if(StringUtil.isBlank( root )){
      root = DEFAULT_ROOT;
    }
    
    
    
    Map<String, String> header = session.getHeaders();
    Map<String, String> parms = session.getParms();
    String uri = session.getUri();
    

    
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

    final String baseUri = uriResource.getUri();
    Log.append( HTTPD.EVENT, "Servicing request for " + baseUri );

    String realUri = HTTPDRouter.normalizeUri( session.getUri() );
    Log.append( HTTPD.EVENT, "Servicing request for real URI " + realUri );

    for ( int index = 0; index < Math.min( baseUri.length(), realUri.length() ); index++ ) {
      if ( baseUri.charAt( index ) != realUri.charAt( index ) ) {
        realUri = HTTPDRouter.normalizeUri( realUri.substring( index ) );
        break;
      }
    }
    Log.append( HTTPD.EVENT, "Processed request for real URI " + realUri );

    // HTTP/1.1 302 Found
    // Location: http://www.iana.org/domains/example/

    // the the input stream to the named resource or null if not found
    BufferedInputStream bis = resourceToInputStream( realUri );

    if ( bis == null ) {
      return new Error404UriHandler().get( uriResource, urlParams, session );
    } else {
      return HTTPD.newChunkedResponse( getStatus(), HTTPD.getMimeTypeForFile( realUri ), bis );
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