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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SecurityResponseException;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.auth.Auth;
import coyote.commons.network.http.auth.AuthProvider;
import coyote.loader.log.Log;


/**
 * 
 */
public class Resource {

  private static final Pattern PARAM_PATTERN = Pattern.compile( "(?<=(^|/)):[a-zA-Z0-9_-]+(?=(/|$))" );

  private static final String PARAM_MATCHER = "([A-Za-z0-9\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=\\s]+)";

  // default empty parameter map
  private static final Map<String, String> EMPTY = Collections.unmodifiableMap( new HashMap<String, String>() );

  private final String uri;

  private final Pattern uriPattern;

  final int priority;

  // The class to use handling the URI
  private final Class<?> responderClass;

  // the initialization parameters for the responder
  private final Object[] initParameter;

  private final List<String> uriParams = new ArrayList<String>();

  private final AuthProvider authProvider;

  private final String description;




  /**
   * Create a URI Resource
   * 
   * @param uri the 
   * @param priority
   * @param responder
   * @param authProvider
   * @param initParameter
   */
  public Resource( final String uri, final int priority, final Class<?> responder, final AuthProvider authProvider, final Object... initParameter ) {
    this.responderClass = responder;
    this.authProvider = authProvider;
    this.initParameter = initParameter;
    if ( uri != null ) {
      this.uri = HTTPDRouter.normalizeUri( uri );
      uriPattern = createUriPattern();
    } else {
      uriPattern = null;
      this.uri = null;
    }

    // prioritize this resource based on the number of parameters; the fewer 
    // the parameters, the higher the priority (is found first)
    this.priority = priority + ( uriParams.size() * 1000 );

    // build our description once and reuse it (for toString())
    StringBuilder b = new StringBuilder( "Resource{uri='" );
    b.append( ( uri == null ? "/" : uri ) );
    b.append( "', Parts=" );
    b.append( uriParams );
    b.append( "} Responder=" );
    b.append( ( responder == null ? "NULL" : responder.getSimpleName() ) );
    description = b.toString();
  }




  private Pattern createUriPattern() {
    String patternUri = uri;
    Matcher matcher = PARAM_PATTERN.matcher( patternUri );
    int start = 0;
    while ( matcher.find( start ) ) {
      uriParams.add( patternUri.substring( matcher.start() + 1, matcher.end() ) );
      patternUri = new StringBuilder( patternUri.substring( 0, matcher.start() ) )//
          .append( PARAM_MATCHER )//
          .append( patternUri.substring( matcher.end() ) ).toString();
      start = matcher.start() + PARAM_MATCHER.length();
      matcher = PARAM_PATTERN.matcher( patternUri );
    }
    return Pattern.compile( patternUri );
  }




  public String getUri() {
    return uri;
  }




  /**
   * @return the number of initialization parameters set in the resource.
   */
  public int getInitParameterLength() {
    return initParameter.length;
  }




  /**
   * Cast the first initialization parameter to the given class.
   * 
   * @param paramClazz the class to perform the cast
   * 
   * @return the first parameter as an object of the given class
   * 
   * @throws ClassCastException if the cast fails
   */
  public <T> T initParameter( final Class<T> paramClazz ) {
    return initParameter( 0, paramClazz );
  }




  /**
   * Cast the initialization parameter at the given index to the given class.
   * 
   * @param parameterIndex the 0-based index of the parameter to retrieve and cast
   * @param paramClazz the class to perform the cast
   * 
   * @return the given parameter as an object of the given class
   * 
   * @throws ClassCastException if the cast fails
   */
  public <T> T initParameter( final int parameterIndex, final Class<T> paramClazz ) {
    if ( initParameter.length > parameterIndex ) {
      return paramClazz.cast( initParameter[parameterIndex] );
    }
    Log.append( HTTPD.EVENT, "ERROR: init parameter index not available " + parameterIndex );
    return null;
  }




  /**
   * See if the URL matches this resources RegEx pattern, if it does, return 
   * the parameters parsed from this URI based on the routing pattern.
   *  
   * @param url the URL to match
   * 
   * @return parameters pulled from the URL based on this resource's matching 
   *     pattern (may be empty) or null if the URL did not match at all.
   */
  public Map<String, String> match( final String url ) {
    final Matcher matcher = uriPattern.matcher( url );
    if ( matcher.matches() ) {
      if ( uriParams.size() > 0 ) {
        final Map<String, String> result = new HashMap<String, String>();
        for ( int i = 1; i <= matcher.groupCount(); i++ ) {
          result.put( uriParams.get( i - 1 ), matcher.group( i ) );
        }
        return result;
      } else {
        return EMPTY;
      }
    }
    return null;
  }




  public Response process( final Map<String, String> urlParams, final IHTTPSession session ) throws SecurityResponseException {
    String error = "Error: Problems while processing URI resource";
    Auth authAnnotation = null;

    if ( responderClass != null ) {
      try {
        final Object object = responderClass.newInstance();

        // Check for a class level Auth annotation which is applied to all methods
        if ( responderClass.isAnnotationPresent( Auth.class ) ) {
          authAnnotation = (Auth)responderClass.getAnnotation( Auth.class );
        }

        // If this is a URI Responder, have it process the request
        if ( object instanceof Responder ) {
          final Responder responder = (Responder)object;

          // determine which method to call
          Class[] params = { Resource.class, Map.class, IHTTPSession.class };
          Method method = null;
          switch ( session.getMethod() ) {
            case GET:
              method = responderClass.getMethod( "get", params );
              break;
            case POST:
              method = responderClass.getMethod( "post", params );
              break;
            case PUT:
              method = responderClass.getMethod( "put", params );
              break;
            case DELETE:
              method = responderClass.getMethod( "delete", params );
              break;
            default:
              method = responderClass.getMethod( "other", String.class, Resource.class, Map.class, IHTTPSession.class );
          }

          // Check for method level annotation which will override any class level annotation
          if ( method.isAnnotationPresent( Auth.class ) ) {
            authAnnotation = (Auth)method.getAnnotation( Auth.class );
          }

          // If there is an Auth annotation present, perform authentication and authorization
          if ( authAnnotation != null ) {
            if ( authProvider != null ) {
              if ( authAnnotation.requireSSL() && !authProvider.isSecureConnection( session ) ) {
                // RFC and OWASP differ in their recommendations. I prefer OWASP's version - don't respond to the request and just drop the connection.
                throw new SecurityResponseException( "Resource requires secure connection" );
              }
              if ( !authProvider.isAuthenticated( session ) ) {
                if ( authAnnotation.required() ) {
                  return Response.createFixedLengthResponse( Status.UNAUTHORIZED, MimeType.TEXT.getType(), "Authentication Required" );
                }
              }
              if ( StringUtil.isNotBlank( authAnnotation.groups() ) && !authProvider.isAuthorized( session, authAnnotation.groups() ) ) {
                return Response.createFixedLengthResponse( Status.FORBIDDEN, MimeType.TEXT.getType(), "Not Authorized" );
              }
            } else {
              // should never happen, but who knows?
              Log.append( HTTPD.EVENT, "ERROR: No Authentication Provider Set: while processing for '" + session.getUri() + "' from " + session.getRemoteIpAddress() + ":" + session.getRemoteIpPort() );
              Log.error( "No Authentication Provider Set in Server: check HTTP log for more details" );
              return Response.createFixedLengthResponse( Status.INTERNAL_ERROR, MimeType.TEXT.getType(), "Server Error" );
            }
          }

          // All auth checks have passed, invoke processing
          switch ( session.getMethod() ) {
            case GET:
            case POST:
            case PUT:
            case DELETE:
              return (Response)method.invoke( responder, this, urlParams, session );
            default:
              return (Response)method.invoke( responder, session.getMethod().toString(), this, urlParams, session );
          }
        } else {
          // This is some other object...display it generically
          return Response.createFixedLengthResponse( Status.OK, MimeType.TEXT.getType(),
              new StringBuilder( "Return: " ).append( responderClass.getCanonicalName() ).append( ".toString() -> " ).append( object ).toString() );
        }
      } catch ( final Exception e ) {
        error = "Error: " + e.getClass().getName() + " : " + e.getMessage();
        Log.append( HTTPD.EVENT, error, e );
        if ( e instanceof SecurityResponseException ) {
          throw (SecurityResponseException)e;
        }
      }
    }
    return Response.createFixedLengthResponse( Status.INTERNAL_ERROR, MimeType.TEXT.getType(), error );
  }




  @Override
  public String toString() {
    return description;
  }

}