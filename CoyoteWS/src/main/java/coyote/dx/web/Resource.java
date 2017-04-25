/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx.web;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import coyote.dx.web.auth.AuthenticationException;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.NullAuthenticator;
import coyote.dx.web.decorator.RequestDecorator;
import coyote.dx.web.worker.ResourceWorker;
import coyote.commons.StringUtil;


/**
 * Represents a resource on the web which can be contacted for data or to 
 * perform some processing.
 */
public class Resource implements Closeable {

  /** The Uniform Resource Locator for the resource this instance represents */
  private URI baseurl;

  /** optional decorators which enrich the requests before they are submitted */
  private final List<RequestDecorator> requestDecorators = new ArrayList<RequestDecorator>();

  /** The worker which handles the specific protocol for the resource  */
  private volatile ResourceWorker worker;

  /** The default request parameters (can be overridden on the request) */
  private final Parameters defaultParameters;

  /** The component which handles authentication with the resource */
  private Authenticator authenticator = new NullAuthenticator();

  /** Any proxy settings this resource should use */
  private final Proxy proxy;

  /** The optional variable portion of the resource URL which will be appended to the end of the URL to contain dynamic data between calls. */
  private volatile String requestPath = null;




  /**
   * Simple constructor which only wraps the URL.
   * 
   * @param url Complete URL to the physical resource
   * 
   * @throws IOException if the URL is invalid
   */
  public Resource( final String url ) throws IOException {
    this( url, new Parameters(), null );
  }




  /**
   * Constructor which wraps the URL and specifies a proxy to access resource.
   * 
   * @param url Complete URL to the physical resource
   * @param proxy The HTTP Proxy workers should use to access the resource
   * 
   * @throws IOException if the URL is invalid
   */
  public Resource( String url, Proxy proxy ) throws IOException {
    this( url, new Parameters(), proxy );
  }




  /**
   * Simple constructor which only wraps the URL.
   * 
   * @param url Complete URL to the physical resource
   * @param parameters the default parameters this resource will use for all requests 
   *        unless overridden by request arguments 
   * 
   * @throws IOException if the URL is invalid
   */
  public Resource( final String url, final Parameters parameters ) throws IOException {
    this( url, parameters, null );
  }




  /**
   * Primary constructor.
   * 
   * @param url Complete URL to the physical resource
   * @param parameters the default parameters this resource will use for all requests 
   *        unless overridden by request arguments
   * @param prxy The HTTP Proxy workers should use to access the resource
   * 
   * @throws IOException if the URL is invalid
   */
  public Resource( final String url, final Parameters parameters, final Proxy prxy ) throws IOException {

    if ( parameters != null ) {
      defaultParameters = parameters;
    } else {
      defaultParameters = new Parameters();
    }

    // If there is no proxy defined, check to see if the JRE has proxy settings 
    // defined by creating a new proxy and checking the value of proxy host set
    // by system properties. If there are proxy settings in the system 
    // properties, use them
    if ( prxy == null ) {
      // create an empty proxy object
      Proxy proxyCheck = new Proxy();

      // check to see if system properties have set the proxy host
      if ( StringUtil.isNotBlank( proxyCheck.getHost() ) ) {
        proxy = proxyCheck;
      } else {
        proxy = null;
      }
    } else {
      proxy = prxy;
    }

    // Make sure the URL ends with a slash
    try {
      baseurl = new URI( url );
    } catch ( final URISyntaxException e ) {
      throw new IOException( e );
    }

  }




  /**
   * Open the resource, performing any required pre-authentication operations
   * 
   * @throws IOException if authentication failed
   * @throws AuthenticationException if authentication initialization fails
   */
  public void open() throws IOException, AuthenticationException {
    // Perform the appropriate authentication processing now
    if ( authenticator != null ) {
      authenticator.init( this );
    }
  }




  /**
   * Close all resources (i,e, the worker) allocated to this resource.
   * 
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    if ( worker != null ) {
      worker.close();
    }
  }




  /**
   * @return the defaultParameters
   */
  public Parameters getDefaultParameters() {
    return defaultParameters;
  }




  /**
   * @return return the hostname based on the currently set URL
   */
  public String getHost() {
    return baseurl.getHost();
  }




  /**
   * @return return the port used to connect to the resource based on the currently set URL
   */
  public int getPort() {
    int retval = baseurl.getPort();
    if ( retval < 1 ) {
      if ( "HTTP".equalsIgnoreCase( baseurl.getScheme() ) ) {
        retval = 80;
      } else if ( "HTTPS".equalsIgnoreCase( baseurl.getScheme() ) ) {
        retval = 443;

      }
    }
    return retval;
  }




  /**
   * @return return the scheme used to connect to the resource based on the currently set URL
   */
  public String getScheme() {
    return baseurl.getScheme();
  }




  /**
   * @return return the currently set URI
   */
  public URI getURI() {
    return baseurl;
  }




  /**
   * @return return the currently set URI with the currently set path appended
   */
  public URI getFullURI() {
    if ( StringUtil.isNotBlank( requestPath ) ) {
      try {
        return new URI( baseurl + requestPath );
      } catch ( URISyntaxException e ) {
        // Should not happen since setPath performs this check
        e.printStackTrace();
      }
    }
    return baseurl;
  }




 



  /**
   * Add the given request decorator to the list of decorators.
   * 
   * <p>This allows for the modification of the HTTP request message just 
   * before it is sent. A common use case is the creation and population of 
   * specific headers in the request message such as a message identifier or 
   * key used by the web service infrastructure.</p>
   * 
   * @param decorator the component to add to the list of decorators used by 
   *        workers to this resource.
   */
  public void addRequestDecorator( RequestDecorator decorator ) {
    if ( decorator != null ) {
      requestDecorators.add( decorator );
    }
  }




  /**
   * @return the list of decorators to be used to enrich / transform the HTTP 
   *         requests before they are sent. 
   */
  public List<RequestDecorator> getRequestDecorators() {
    return requestDecorators;
  }




  /**
   * @return the proxy settings this resource should use
   */
  public Proxy getProxySettings() {
    return proxy;
  }




  /**
   * Invoke the resource.
   * 
   * @return a response which contains the results of resource processing 
   * 
   * @throws InvocationException 
   */
  public Response request() throws InvocationException {
    return request( defaultParameters );
  }




  /**
   * Invoke the resource using the given parameters to alter the resources 
   * operation.
   * 
   * <p>The parameters include the request payload, URI parameters and any 
   * protocol-specific settings to tweak. SOAP requests will always have 
   * parameters as the request must contain a payload, contain a target name 
   * space and an operation being invoked.</p> 
   * 
   * @param params the variables of the resource invocation request
   * 
   * @return a response which contains the results of resource processing 
   * 
   * @throws InvocationException 
   */
  public Response request( Parameters params ) throws InvocationException {
    return getWorker( params ).request( params );
  }




  /**
   * Return the ResourceWorker for the exchange type in the given parameters.
   * 
   * @param params The parameters containing the exchange type
   * 
   * @return ResourceWorker for the exchange type in the given parameters or 
   *         the worker for the exchange type in the default parameters if the 
   *         parameters are null or there is no exchange type set in the given 
   *         parameters.
   */
  private ResourceWorker getWorker( Parameters params ) {
    if ( params != null && params.getExchangeType() != null ) {
      return params.getExchangeType().getWorker( this );
    } else {
      return getWorker( defaultParameters );
    }
  }




  /**
   * Asynchronous sending of a message with no expectations of a return value.
   * 
   * @param params the variables of the event being sent
   */
  public void send( Parameters params ) {
    getWorker( params ).send( params );
  }




  /**
   * Set the resources Authenticator.
   * 
   * <p>The provided authenticator is initialized at this point and will 
   * potentially communicate to the resource to authenticate.</p>
   * 
   * <p>If the given reference is null, a {@code NullAuthenticator} will be set
   * to ensure components expecting an authenticator do not receive a null 
   * reference. This resource of an Authenticator will not perform any 
   * authentication functions.</p>
   *  
   * @param auth The Authenticator to set.
   */
  public void setAuthenticator( Authenticator auth ) {
    if ( auth == null ) {
      authenticator = new NullAuthenticator();
    } else {
      authenticator = auth;
    }
  }




  /**
   * @return the authenticator, should never be null
   */
  public Authenticator getAuthenticator() {
    return authenticator;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer( "WebResource: " );
    b.append( baseurl );
    if ( StringUtil.isNotBlank( requestPath ) ) {
      b.append( " path: " );
      b.append( requestPath );
    }
    return b.toString();
  }




  /**
   * @return the path portion to add to the end of the URL
   */
  public String getPath() {
    return requestPath;
  }




  /**
   * Set a path to append to the request URL.
   * 
   * <p>This allows the setting of an optional variable portion of the resource 
   * URL which will be appended to the end of the URL to contain dynamic data 
   * between calls. It allows the caller to replace values in the path to make
   * ReST calls more dynamic.
   * 
   * <p>For example, a ReSTful call may contain the user id to retrieve:<pre>
   * https://myserver.moc/api/user/123/view</pre>
   * <p>The caller may want to replace {@code 123} with the actual identifier 
   * between calls. If the resource URL is {@code https://myserver.moc/api} the
   * path of {@code /user/123/view} can be determined at runtime and even 
   * between multiple calls.
   * 
   *  <p>The request path is always appended to the base resource URL if it 
   *  exists. This allows a single resource to be used for many different 
   *  requests by simply changing the path. For example, a resource can be 
   *  created for "https://docs.example.com" and the path be set for each of 
   *  the documents it servers. It can be used to retrieve story cards {@code 
   *  setPath("/story/1234.md")} or other resources like graphics {@code 
   *  setPath("/flows/1234.svg")}. All the setting in the resource remain the 
   *  same, only the path changes.
   *  
   *  <p>It bears repeating that this is completely optional and the base URL 
   *  specified as part of the constructor can contain the entire resource 
   *  URL: {@code resource("https://docs.example.com/story/1234.md")}
   *  
   * @param path the variable portion of the path to set
   * 
   * @throws URISyntaxException if the path causes the request URI to be come invalid. 
   */
  public void setPath( String path ) throws URISyntaxException {
    // perform a test to make sure the result is valid
    if ( StringUtil.isNotBlank( path ) ) {
      new URI( baseurl + requestPath );
    }
    requestPath = path;
  }

}
