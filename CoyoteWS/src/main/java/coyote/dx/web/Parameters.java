/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web;

import java.util.ArrayList;
import java.util.List;

import coyote.commons.network.MimeType;
import coyote.dataframe.DataFrame;
import coyote.dx.web.worker.Namespace;


/**
 * Request parameters.
 * 
 * <p>Any thing which affects the rules for message exchanges are specified 
 * here. This is where the worker gets all the operational directives for
 * calling the web service.
 * 
 * <p>The method of the request (GET,POST, PUT, etc.) is specified here.
 * 
 * <p>All request headers are specified here.
 * 
 * <p>Since SOAP does things a little differently than a standard ReST request, 
 * SOAP specific settings are placed here to ensure that the proper underlying 
 * worker is invoked. Setting the SOAP Namespace and/or SOAP Operation will 
 * result in the SOAP worker handling the message exchange, ensuring headers 
 * are set correctly and the response body/payload is marshaled as expected. 
 * 
 * <p>These will over-ride those set in the resource protocol settings.
 */
public class Parameters {

  /** The name of the SOAP operation being invoked */
  protected String soapOperation = null;

  /** The namespace for SOAP requests */
  protected Namespace soapNamespace = null;

  /** The HTTP method used in communicating with the resource */
  protected Method method = null;

  /** The type of HTTP exchange handled by resource (e.g. JSON) */
  protected ExchangeType exchangeType = ExchangeType.HTML;

  /** The message (e.g. request) payload */
  private DataFrame payload = null;

  /** The message (e.g. request) body */
  private String body = null;

  /** The list of MIME types to be accepted as a response to the exchange. */
  private final List<MimeType> acceptTypes = new ArrayList<MimeType>();

  /** This is the type of content the request contains. */
  private MimeType contentType = null;




  /**
   * Default constructor
   */
  public Parameters() {}




  /**
   * Create a SOAP namespace with a prefix and the URL describing the prefix.
   * 
   * <p>This is retrieved from the "targetNamespace" in the WSDL.
   * 
   * @param prefix the prefix to use
   * @param url the URL describing the XML tags being used in the SOAP request
   * 
   * @return this parameters object for chaining
   */
  public Parameters setSoapNamespace( final String prefix, final String url ) {
    soapNamespace = new Namespace( prefix, url );
    return this;
  }




  /**
   * @return the namespace data for SOAP requests, or null if not set
   */
  public Namespace getSoapNamespace() {
    return soapNamespace;
  }




  /**
   * @param operation the name of the SOAP operation being invoked
   * 
   * @return this parameters object for chaining
   */
  public Parameters setSoapOperation( final String operation ) {
    soapOperation = operation;
    return this;
  }




  /**
   * @return The name of the SOAP operation to invoke or null if not set
   */
  public String getSoapOperation() {
    return soapOperation;
  }




  /**
   * Set the given data frame as the request payload.
   * 
   * <p>If using SOAP, this frame will be surrounded with the SOAP operation 
   * (if set).
   * 
   * @param frame the payload (message) to be sent
   * 
   * @return this parameters object for chaining
   */
  public Parameters setPayload( DataFrame frame ) {
    if ( frame != null ) {
      payload = (DataFrame)frame.clone();
    } else {
      payload = null;
    }
    return this;
  }




  /**
   * @return the list of data frames representing the request payload.
   */
  public DataFrame getPayload() {
    return payload;
  }




  /**
   * @return the HTTP method current set (default is GET)
   */
  public Method getMethod() {
    return method;
  }




  /**
   * @param method the HTTP method to use when using HTTP based services
   * 
   * @return this parameters object for chaining
   */
  public Parameters setMethod( Method method ) {
    this.method = method;
    return this;
  }




  /**
   * @return the formatting of the data exchanged (default is JSON)
   */
  public ExchangeType getExchangeType() {
    return exchangeType;
  }




  /**
   * @param type the formatting of the data exchanged (either JSON or XML)
   * 
   * @return this parameters object for chaining
   */
  public Parameters setExchangeType( ExchangeType type ) {
    exchangeType = type;
    return this;
  }




  /**
   * Add the given MimeType to the list of those to be accepted.
   * 
   * <p>The Accept header value will contain a list of all the accept types in 
   * the order they were added.
   * 
   * @param type the MimeType to add
   * 
   * @return this parameters object for chaining
   * 
   * @see #getAcceptHeaderValue()
   */
  public Parameters addAcceptType( MimeType type ) {
    acceptTypes.add( type );
    return this;
  }




  /**
   * @return the accept MimeTypes for this set of parameters
   */
  public List<MimeType> getAcceptTypes() {
    return acceptTypes;
  }




  /**
   * @param mimeTypeList the acceptTypes to set
   * 
   * @see #getAcceptHeaderValue()
   */
  public Parameters setAcceptTypes( List<MimeType> mimeTypeList ) {
    if ( mimeTypeList != null ) {
      acceptTypes.clear();
      for ( MimeType type : mimeTypeList ) {
        acceptTypes.add( type );
      }
    }
    return this;
  }




  /**
   * Remove any and all MimeTypes from the list of those to be accepted.
   */
  public void clearAcceptTypes() {
    acceptTypes.clear();
  }




  /**
   * Clear out the existing accept types and make this the only accept type.
   * 
   * @param type the MIME type to accept.
   * 
   * @return this parameters object for chaining
   * 
   * @see #getAcceptHeaderValue()
   */
  public Parameters setAcceptType( MimeType type ) {
    acceptTypes.clear();
    if ( type != null ) {
      acceptTypes.add( type );
    }
    return this;
  }




  /**
   * Return the value of the Accept header based on the currently set accepted MIME types.
   * 
   * <p>They are output in the order they were added.
   * 
   * @return the value to place in the HTTP Accept: header or null if there are no accept MIME types set.
   */
  public String getAcceptHeaderValue() {
    if ( acceptTypes.size() > 0 ) {
      StringBuffer b = new StringBuffer();
      for ( int x = 0; x < acceptTypes.size(); x++ ) {
        b.append( acceptTypes.get( x ).getType() );
        if ( x + 1 < acceptTypes.size() ) {
          b.append( "; " );
        }
      }
      return b.toString();
    }
    return null;
  }




  /**
   * @return the body to be sent in the request, or null if there is no body.
   */
  public String getBody() {
    return body;
  }




  /**
   * Set the body of the request.
   * 
   * <p>If both a body and a payload exists, the body will be appended to the payload.
   * @param body
   */
  public void setBody( String body ) {
    this.body = body;
  }




  /**
   * @return the type of content the request contains
   */
  public MimeType getContentType() {
    return contentType;
  }




  /**
   * @param type the type of data in the request body
   */
  public void setContentType( MimeType type ) {
    this.contentType = type;
  }

}
