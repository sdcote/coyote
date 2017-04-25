/*
 *
 */
package coyote.dx.web.worker;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.dx.web.InvocationException;
import coyote.dx.web.Method;
import coyote.dx.web.Parameters;
import coyote.dx.web.Resource;
import coyote.dx.web.decorator.RequestDecorator;
import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.loader.log.Log;


/**
 * This is the base class for all workers.
 */
public abstract class AbstractWorker implements ResourceWorker {
  /**
   * Runnable executor of the HTTP request.
   */
  protected class RequestRunner implements Runnable {
    HttpRequest request;
    coyote.dx.web.Response response;
    Parameters parameters;




    protected RequestRunner( final HttpRequest request, final coyote.dx.web.Response response, final Parameters params ) {
      this.request = request;
      this.response = response;
      parameters = params;
    }




    @Override
    public void run() {
      Log.debug( "Running request in " + Thread.currentThread().getName() );

      decorate( request );

      response.transactionStart();
      response.requestStart();

      try (CloseableHttpResponse httpResponse = httpClient.execute( target, request, localContext )) {
        response.requestEnd();

        final int status = httpResponse.getStatusLine().getStatusCode();
        response.setHttpStatusCode( status );
        response.setHttpStatusPhrase( httpResponse.getStatusLine().getReasonPhrase() );

        log.debug( "Request:\r\n    %s\r\nResponse:\r\n    %s", request.toString(), httpResponse.getStatusLine().toString() );
        if ( ( status >= 200 ) && ( status < 300 ) ) {
          log.debug( "Success - %s", httpResponse.getStatusLine().toString() );
        } else if ( ( status >= 300 ) && ( status < 400 ) ) {
          final String errmsg = "Unexpected Response - " + httpResponse.getStatusLine().toString();
          log.debug( errmsg );
          // Status of a 301 or a 302, look for a Location: header in the response and use that URL
          response.setLink( httpResponse.getFirstHeader( "Location" ).getValue() );
        } else if ( ( status >= 400 ) && ( status < 500 ) ) {
          final String errmsg = "Access error - " + httpResponse.getStatusLine().toString();
          log.debug( errmsg );
        } else if ( status >= 500 ) {
          final String errmsg = "Server error - " + httpResponse.getStatusLine().toString();
          log.debug( errmsg );
        }

        if ( httpResponse.getEntity() != null ) {
          response.parseStart();
          try {
            marshalResponseBody( response, httpResponse, parameters );
          } catch ( final Exception e ) {
            Log.error( e.toString() );
          }
          finally {
            response.parseEnd();
          }
        } else {
          log.debug( "The response did not contain a body" );
        }

      } catch ( final ClientProtocolException e1 ) {
        response.requestEnd();
        log.error( e1.getMessage() );
      } catch ( final IOException e1 ) {
        response.requestEnd();
        log.error( e1.getMessage() );
      }
      finally {
        response.transactionEnd();
      }

      // set key performance metrics
      response.setRequestStart( response.getRequestStart() );
      response.setRequestEnd( response.getRequestEnd() );
      response.setParseStart( response.getParseStart() );
      response.setParseEnd( response.getParseEnd() );
      response.setTransactionStart( response.getTransactionStart() );
      response.setTransactionEnd( response.getTransactionEnd() );

      // add some HTTP status information
      response.setHttpStatusCode( response.getHttpStatusCode() );
      response.setHttpStatusPhrase( response.getHttpStatusPhrase() );

      // mark the end of the operation
      response.operationEnd();
      response.setComplete( true );
    }

  }

  protected static final String RESULT_FRAME = "result";

  protected static final String ERROR_FRAME = "error";

  protected static final String STATUS_FIELD = "status";

  protected static final String ERROR_MESSAGE_FIELD = "message";

  protected static final String ERROR_DETAIL_FIELD = "detail";
  /** The logger this class uses */
  protected static final Logger log = LoggerFactory.getLogger( AbstractWorker.class );
  protected Resource resource;

  // This is the persistent http client we will use to send all our requests
  protected CloseableHttpClient httpClient;
  // Create a context in which we will execute our request
  protected final HttpClientContext localContext = HttpClientContext.create();

  // HTTP Client configuration settings
  protected RequestConfig config;

  // The host of our targeted resource
  protected HttpHost target = null;




  /**
   * This is is the default constructor for all workers.
   *
   * @param resource
   */
  public AbstractWorker( final Resource resource ) {
    this.resource = resource;

    // This lays open several components of the underlying HTTP client library
    // so as to allow configuration of many different aspects of the connection
    // process

    // Define and configure the Connection Manager
    final PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager();
    connMgr.closeIdleConnections( 15, TimeUnit.MINUTES );

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // our own keep-alive strategy
    final ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
      @Override
      public long getKeepAliveDuration( final HttpResponse response, final HttpContext context ) {
        // Honor 'keep-alive' header
        final HeaderElementIterator it = new BasicHeaderElementIterator( response.headerIterator( HTTP.CONN_KEEP_ALIVE ) );
        while ( it.hasNext() ) {
          final HeaderElement he = it.nextElement();
          final String param = he.getName();
          final String value = he.getValue();
          if ( ( value != null ) && param.equalsIgnoreCase( "timeout" ) ) {
            try {
              return Long.parseLong( value ) * 1000;
            } catch ( final NumberFormatException ignore ) {}
          }
        }
        // HttpHost target = (HttpHost)context.getAttribute( HttpClientContext.HTTP_TARGET_HOST );
        // we could also check for other parameters such as hostname and return
        // a different value for each server or domain.
        // for right now just keep it at 10 minutes (600 seconds) for long queries
        return 600 * 1000;
      }
    };
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    // Define and configure the client
    httpClient = HttpClientBuilder.create().setConnectionManager( connMgr ).setKeepAliveStrategy( myStrategy ).build();

    // Setup our HTTP fixtures
    target = new HttpHost( resource.getHost(), resource.getPort(), resource.getScheme() );

    // Now set timeouts
    final int soTimeout = 15 * 60 * 1000;
    final int connTimeout = 15 * 60 * 1000;
    final int rqTimeout = 15 * 60 * 1000;

    // If there are both proxy host and port values, configure the request
    // to use them
    if ( resource.getProxySettings() != null ) {
      final HttpHost proxy = new HttpHost( resource.getProxySettings().getHost(), resource.getProxySettings().getPort() );
      config = RequestConfig.copy( RequestConfig.DEFAULT ).setSocketTimeout( soTimeout ).setConnectTimeout( connTimeout ).setConnectionRequestTimeout( rqTimeout ).setProxy( proxy ).build();
      log.debug( "Connecting to resource " + target + " via " + proxy );
    } else {
      config = RequestConfig.copy( RequestConfig.DEFAULT ).setSocketTimeout( soTimeout ).setConnectTimeout( connTimeout ).setConnectionRequestTimeout( rqTimeout ).build();
      log.debug( "Connecting to resource " + target );
    }

    // If we have credentials, set them in the local client context
    if ( ( resource.getAuthenticator().getUsername() != null ) && ( resource.getAuthenticator().getPassword() != null ) ) {
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

      // If we have proxy credentials, add them
      if ( resource.getProxySettings() != null ) {
        // NTLM Credentials
        if ( StringUtil.isNotBlank( resource.getProxySettings().getDomain() ) ) {
          credentialsProvider.setCredentials( new AuthScope( resource.getProxySettings().getHost(), resource.getProxySettings().getPort(), null, "NTLM" ), new NTCredentials( resource.getProxySettings().getUsername(), resource.getProxySettings().getPassword(), null, resource.getProxySettings().getDomain() ) );
          log.debug( "Adding proxy NTLM credentials for %s", resource.getProxySettings().getUsername() );
        }
      }

      // Now set the credentials for the target host:port - Do not specify
      // a scheme...it results in a 401
      credentialsProvider.setCredentials( new AuthScope( target.getHostName(), target.getPort() ), new UsernamePasswordCredentials( resource.getAuthenticator().getUsername(), resource.getAuthenticator().getPassword() ) );
      log.debug( "Adding basic auth credential support for %s", resource.getAuthenticator().getUsername() );

      // place the credentials provider in the client context
      localContext.setCredentialsProvider( credentialsProvider );

    }

  }




  @Override
  public void close() throws IOException {
    log.debug( "Closing Worker..." );
    try {
      httpClient.close();
    } catch ( final IOException e ) {
      log.warn( e.getMessage() );
    }
    finally {
      log.debug( "...Worker closed" );
    }
  }




  /**
   * This method enriches the given HTTP Request with any additional headers
   * required by the SOA environment.
   *
   * <p>This will modify the given request object. Decorators are not limited
   * to headers, but can modify any part of the request.</p>
   *
   * @param request the request to query and update if necessary.
   */
  public void decorate( final HttpMessage request ) {
    for ( final RequestDecorator decorator : resource.getRequestDecorators() ) {
      decorator.process( request );
    }
  }




  /**
   * @see coyote.dx.web.worker.ResourceWorker#marshalRequestBody(org.apache.http.client.methods.HttpEntityEnclosingRequestBase, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalRequestBody( final HttpEntityEnclosingRequestBase request, final Parameters params ) {}




  /**
   * @see coyote.dx.web.worker.ResourceWorker#marshalResponseBody(coyote.dx.web.Response, org.apache.http.HttpResponse, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalResponseBody( final coyote.dx.web.Response workerResponse, final HttpResponse httpResponse, final Parameters params ) {
    try {
      final byte[] byteContent = EntityUtils.toByteArray( httpResponse.getEntity() );
      workerResponse.setData( byteContent );
    } catch ( final IOException e ) {
      log.warn( e.getMessage() );
    }
  }




  /**
   * This performs a basic HTTP request-response exchange.
   *
   * @see coyote.dx.web.worker.ResourceWorker#request(coyote.dx.web.Parameters)
   */
  @Override
  public coyote.dx.web.Response request( final Parameters params ) throws InvocationException {

    final coyote.dx.web.Response retval = new coyote.dx.web.Response();
    retval.operationStart();

    log.debug( "Request %s", params );
    log.debug( "Sending to endpoint: %s", resource.getFullURI().toString() );

    if ( params.getMethod() == null ) {
      params.setMethod( resource.getDefaultParameters().getMethod() );
      if ( params.getMethod() == null ) {
        params.setMethod( Method.GET );
        Log.notice( "No HTTP method specified and no default set: using HTTP " + params.getMethod() );
      } else {
        Log.debug( "Using default HTTP method of " + params.getMethod() );
      }
    }

    final HttpRequest request;

    // Create the appropriate request based on the method set in the parameters
    switch ( params.getMethod() ) {
      case GET:
        final HttpGet grqst = new HttpGet( resource.getFullURI().toString() );
        grqst.setConfig( config );
        request = grqst;
        break;
      case POST:
        final HttpPost prqst = new HttpPost( resource.getFullURI().toString() );
        prqst.setConfig( config );
        marshalRequestBody( prqst, params );
        request = prqst;
        break;
      case DELETE:
        final HttpDelete drqst = new HttpDelete( resource.getFullURI().toString() );
        drqst.setConfig( config );
        request = drqst;
        break;
      case PUT:
        final HttpPut urqst = new HttpPut( resource.getFullURI().toString() );
        urqst.setConfig( config );
        marshalRequestBody( urqst, params );
        request = urqst;
        break;
      case OPTIONS:
        final HttpOptions orqst = new HttpOptions( resource.getFullURI().toString() );
        orqst.setConfig( config );
        request = orqst;
        break;
      case HEAD:
        final HttpHead hrqst = new HttpHead( resource.getFullURI().toString() );
        hrqst.setConfig( config );
        request = hrqst;
        break;
      case TRACE:
        final HttpTrace trqst = new HttpTrace( resource.getFullURI().toString() );
        trqst.setConfig( config );
        request = trqst;
        break;
      default:
        final HttpGet rqst = new HttpGet( resource.getFullURI().toString() );
        rqst.setConfig( config );
        request = rqst;
        break;
    }

    setRequestHeaders( request, params );

    // execute the request in a separate thread
    final Thread thread = new Thread( new RequestRunner( request, retval, params ) );
    thread.start();

    // return the results of processing the request
    return retval;

  }




  /**
   * Empty, no-op implementation.
   *
   * @see coyote.dx.web.worker.ResourceWorker#send(coyote.dx.web.Parameters)
   */
  @Override
  public void send( final Parameters params ) {}




  /**
   * @see coyote.dx.web.worker.ResourceWorker#setRequestHeaders(org.apache.http.HttpRequest, coyote.dx.web.Parameters)
   */
  @Override
  public void setRequestHeaders( final HttpRequest request, final Parameters params ) {
    String headerValue = params.getAcceptHeaderValue();
    if ( headerValue != null ) {
      request.setHeader( coyote.commons.network.http.HTTP.HDR_ACCEPT, headerValue );
    } else {
      request.setHeader( coyote.commons.network.http.HTTP.HDR_ACCEPT, MimeType.ANY.getType() );
    }
  }

}
