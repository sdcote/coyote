/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and initial implementation
 */
package coyote.dx.web.worker;

import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.dx.web.InvocationException;
import coyote.dx.web.Method;
import coyote.dx.web.Parameters;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.marshal.MarshalException;
import coyote.dataframe.marshal.XMLMarshaler;


/**
 *
 */
public class SoapWorker extends AbstractWorker implements ResourceWorker {

  /** The logger this class uses */
  private static final Logger log = LoggerFactory.getLogger( SoapWorker.class );

  public static final String ENVELOPE = "Envelope";
  public static final String BODY = "Body";
  public static final String FAULT = "Fault";

  public static final String FAULTCODE = "faultcode";
  public static final String FAULTSTRING = "faultstring";
  public static final String DETAIL = "detail";

  public static final String CODE2 = "Code";
  public static final String REASON2 = "Reason";
  public static final String ROLE2 = "Role";
  public static final String DETAIL2 = "Detail";
  public static final String VALUE2 = "Value";
  public static final String TEXT2 = "Text";

  // TODO: re-factor these out of the class
  private boolean faultFlag = false;
  private String faultDetail = null;
  private String faultString = null;
  private String faultCode = null;




  public SoapWorker( final Resource instance ) {
    super( instance );
  }




  /**
   * SOAP Worker overrides / intercepts the request call so it can ensure the 
   * POST method is used.
   * 
   * @see coyote.dx.web.worker.AbstractWorker#request(coyote.dx.web.Parameters)
   */
  @Override
  public coyote.dx.web.Response request( final Parameters params ) throws InvocationException {
    // SOAP uses the POST method in all cases 
    params.setMethod( Method.POST );
    return super.request( params );
  }




  /**
   * Create a SOAP request from the given parameters and .
   *
   * @param params
   *
   * @return the soap envelop as a string
   */
  private String getRequestBody( final Parameters params ) {
    final StringBuffer b = new StringBuffer();
    Namespace nspace = params.getSoapNamespace();
    String soapOperation = params.getSoapOperation();

    // use the default namespace if the params did not contain one, may still be null
    if ( nspace == null ) {
      nspace = resource.getDefaultParameters().getSoapNamespace();
    }

    // use the default operation if the params did not contain one
    if ( StringUtil.isBlank( soapOperation ) ) {
      soapOperation = resource.getDefaultParameters().getSoapOperation();
    }

    b.append( "<soapenv:Envelope" );
    if ( nspace != null ) {
      b.append( " xmlns:" );
      b.append( nspace.getPrefix() );
      b.append( "=\"" );
      b.append( nspace.getUrl() );
      b.append( "\"" );
    }
    b.append( " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body>" );
    b.append( "<" );
    b.append( soapOperation );

    // Marshal the payload data frame to XML to represent the request
    if ( params.getPayload() != null ) {
      b.append( ">" );
      b.append( XMLMarshaler.marshal( params.getPayload() ) );
      b.append( "</" );
      b.append( soapOperation );
      b.append( ">" );
    } else {
      b.append( "/>" );
    }
    b.append( "</soapenv:Body></soapenv:Envelope>" );

    return b.toString();
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#marshalRequestBody(org.apache.http.client.methods.HttpEntityEnclosingRequestBase, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalRequestBody( final HttpEntityEnclosingRequestBase request, final Parameters params ) {
    // TODO: If there is data, make sure the Content-Type and Content-Encoding are set properly
    // request.setHeader( coyote.commons.network.http.HTTP.HDR_CONTENT_TYPE, "application/soap+xml; charset=UTF-8" );
  }




  /**
  * @see coyote.dx.web.worker.AbstractWorker#marshalResponseBody(coyote.dx.web.Response, org.apache.http.HttpResponse, coyote.dx.web.Parameters)
  */
  @Override
  public void marshalResponseBody( final Response workerResponse, final HttpResponse httpResponse, final Parameters params ) {
    try {
      final org.apache.http.entity.ContentType ctype = org.apache.http.entity.ContentType.getOrDefault( httpResponse.getEntity() );
      final String body = EntityUtils.toString( httpResponse.getEntity(), ctype.getCharset() );
      log.debug( this.getClass().getSimpleName() + " marshaling response body of '%s%s", body.substring( 0, body.length() > 500 ? 500 : body.length() ), body.length() <= 500 ? "'" : " ...'" );

      final DataFrame retval = new DataFrame();

      if ( body != null ) {
        final List<DataFrame> result = XMLMarshaler.marshal( body );

        if ( result != null ) {
          if ( result.size() > 0 ) {
            final DataFrame frame = result.get( 0 );

            if ( frame != null ) {
              final DataFrame soapEnvelope = frame.getAsFrame( SoapWorker.ENVELOPE );
              if ( soapEnvelope != null ) {
                final DataFrame soapBody = soapEnvelope.getAsFrame( SoapWorker.BODY );
                if ( soapBody != null ) {

                  // Check for SOAP faults
                  if ( soapBody.contains( SoapWorker.FAULT ) ) {
                    faultFlag = true;
                    // faults should be a complex object (i.e. frame)
                    final DataFrame soapFault = soapBody.getAsFrame( SoapWorker.FAULT );
                    if ( soapFault != null ) {
                      parseSoapFault( soapFault );
                    } else {
                      throw new MarshalException( "SOAP Fault must contain two or more child elements" );
                    }
                  } else {

                    // This is the data we need
                    for ( final DataField field : soapBody.getFields() ) {
                      if ( field.isFrame() ) {
                        //resultFrame.add( field );
                        break;
                      } else {
                        log.error( "Received element instead of object: " + frame.toString() );
                      }
                    }
                  }
                } else {
                  throw new MarshalException( "No SOAP Body" );
                }
              } else {
                throw new MarshalException( "No SOAP Envelope" );
              }
            } else {
              log.error( "First result frame is null" );
            }
          } else {
            log.warn( "Multiple Result Frames" );
          }
        } else {
          log.error( "No Response Body" );
        }
      }

      //        if ( isFault() ) {
      //          final DataFrame errorFrame = new DataFrame();
      //          errorFrame.put( FAULTCODE, soap.getFaultCode() );
      //          errorFrame.put( FAULTSTRING, soap.getFaultString() );
      //          errorFrame.put( DETAIL, soap.getFaultDetail() );
      //          retval.put( ERROR_FRAME, errorFrame );
      //        } else {
      //          // Put the result frame in the response field
      //          retval.put( RESULT_FRAME, soap.getResult() );
      //        }

    } catch ( final MarshalException e ) {
      log.error( e.getClass().getSimpleName() + ":" + e.getMessage() );
    } catch ( final DataFrameException e ) {
      log.error( e.getClass().getSimpleName() + ":" + e.getMessage() );
    } catch ( Exception e ) {
      log.error( e.getClass().getSimpleName() + ":" + e.getMessage() );
    }

  }




  /**
   * This will parse the data in the fault frame and populate the appropriate
   * fields.
   *
   * <p>This can get complex as 1.1 and 1.2 versions are markedly different.
   * That is why this is a separate method; to allow for more complex
   * processing without cluttering up the constructor.</p>
   *
   * @param soapFault The frame containing the SOAP fault data
   */
  private void parseSoapFault( final DataFrame soapFault ) {
    if ( soapFault != null ) {
      for ( final DataField field : soapFault.getFields() ) {
        if ( SoapWorker.DETAIL.equals( field.getName() ) ) {
          faultDetail = field.getStringValue();
        } else if ( SoapWorker.FAULTSTRING.equals( field.getName() ) ) {
          faultString = field.getStringValue();
        } else if ( SoapWorker.FAULTCODE.equals( field.getName() ) ) {
          faultCode = field.getStringValue();
        } else if ( SoapWorker.CODE2.equals( field.getName() ) ) {
          // this is a 1.2 fault and should contain a <env:Value>
          if ( field.isFrame() ) {
            final DataFrame codeframe = (DataFrame)field.getObjectValue();
            if ( codeframe.contains( SoapWorker.VALUE2 ) ) {
              System.err.println( codeframe.getAsString( SoapWorker.VALUE2 ) );
            } // code value
          } // isframe
        } else if ( SoapWorker.REASON2.equals( field.getName() ) ) {
          // this is a 1.2 fault and should contain a <env:Text xml:lang="en-US">
          if ( field.isFrame() ) {
            final DataFrame reasonframe = (DataFrame)field.getObjectValue();
            if ( reasonframe.contains( SoapWorker.TEXT2 ) ) {
              System.err.println( reasonframe.getAsString( SoapWorker.TEXT2 ) );
            } // reason text
          } // isframe
        } else if ( SoapWorker.ROLE2.equals( field.getName() ) ) {
          // this is a 1.2 fault and should contain a text for "Fault Role"
          System.err.println( field.getStringValue() );
        } else if ( SoapWorker.DETAIL2.equals( field.getName() ) ) {
          // this is a 1.2 fault and can contain anything
          // just use the string values of the child nodes as "Detail Entries"
          if ( field.isFrame() ) {
            final DataFrame detailframe = (DataFrame)field.getObjectValue();
            for ( final DataField fld : detailframe.getFields() ) {
              System.err.println( fld.getName() + " : " + fld.getStringValue() );
            } // for
          } // isframe
        } // if then else
      } // for soapfields
    } // soapFault !null
  }




  /**
   * @see coyote.dx.web.worker.ResourceWorker#send(coyote.dx.web.Parameters)
   */
  @Override
  public void send( final Parameters params ) {
    // TODO Auto-generated method stub

  }




  /**
   * @see coyote.dx.web.worker.ResourceWorker#setRequestHeaders(org.apache.http.HttpRequest, coyote.dx.web.Parameters)
   */
  @Override
  public void setRequestHeaders( final HttpRequest request, final Parameters params ) {
    request.setHeader( coyote.commons.network.http.HTTP.HDR_ACCEPT, "application/xml, text/plain, */*" );
  }

}
