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
package coyote.dx.web.decorator;

import org.apache.http.HttpMessage;

import coyote.commons.ByteUtil;
import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.Loader;


/**
 * When the user agent wants to send the server authentication credentials it 
 * may use the Authorization field.
 * 
 * The Authorization field is constructed as follows:<ul>
 * <li>Username and password are combined into a string "username:password". 
 * Note that username cannot contain the ":" character.</li>
 * <li>The resulting string is then encoded using the RFC2045-MIME variant of 
 * Base64, except not limited to 76 char/line</li>
 * <li>The authorization method and a space i.e. "Basic " is then put before 
 * the encoded string.</li></ul>
 * 
 * <p>For example, if the user agent uses 'Aladdin' as the username and 
 * 'open sesame' as the password then the field is formed as follows:<pre>
 * Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==</pre>
 * 
 */
public class BasicAuth extends AbstractDecorator implements RequestDecorator {
  protected static final String DATA = "data";
  protected static final String USERNAME = "username";
  protected static final String PASSWORD = "password";
  protected static final String ENC_DATA = Loader.ENCRYPT_PREFIX + "data";
  protected static final String ENC_USERNAME = Loader.ENCRYPT_PREFIX + "username";
  protected static final String ENC_PASSWORD = Loader.ENCRYPT_PREFIX + "password";
  protected static final String BASIC = "Basic";
  protected static final String DEFAULT_HEADER = "Authorization";

  private String headerName = DEFAULT_HEADER;
  private String headerData = "";
  private String username = null;
  private String password = null;




  public BasicAuth() {}




  public BasicAuth( String user, String pswd ) {
    setUsername( user );
    setPassword( pswd );
  }




  /**
   * 
   */
  void calculateHeaderData() {
    if ( StringUtil.isNotBlank( username ) || StringUtil.isNotBlank( password ) ) {
      StringBuffer b = new StringBuffer();

      if ( StringUtil.isNotBlank( username ) ) {
        b.append( username );
      }
      b.append( ":" );

      if ( StringUtil.isNotBlank( password ) ) {
        b.append( password );
      }

      headerData = BASIC + " " + ByteUtil.toBase64( StringUtil.getBytes( b.toString() ) );
    }
  }




  /**
   * @return the headerName
   */
  public String getHeaderName() {
    return headerName;
  }




  /**
   * @param headerName the headerName to set
   */
  public void setHeaderName( String headerName ) {
    this.headerName = headerName;
  }




  /**
   * @return the headerData
   */
  public String getHeaderData() {
    return headerData;
  }




  /**
   * @param headerData the headerData to set
   */
  public void setHeaderData( String headerData ) {
    this.headerData = headerData;
  }




  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }




  /**
   * @param username the username to set
   */
  public void setUsername( String username ) {
    this.username = username;
    headerData = null; // force recalculation of data
  }




  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }




  /**
   * @param password the password to set
   */
  public void setPassword( String password ) {
    this.password = password;
    headerData = null; // force recalculation of data
  }




  /**
   * @see coyote.dx.web.decorator.AbstractDecorator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) {
    super.setConfiguration( frame );

    //look for all configuration parameters
    for ( DataField field : frame.getFields() ) {
      if ( field.getName() != null ) {
        if ( field.getName().equalsIgnoreCase( HEADER ) ) {
          setHeaderName( field.getStringValue() );
        } else if ( field.getName().equalsIgnoreCase( DATA ) ) {
          setHeaderData( field.getStringValue() );
        } else if ( field.getName().equalsIgnoreCase( ENC_DATA ) ) {
          setHeaderData( CipherUtil.decryptString( field.getStringValue() ) );
        } else if ( field.getName().equalsIgnoreCase( USERNAME ) ) {
          setUsername( field.getStringValue() );
        } else if ( field.getName().equalsIgnoreCase( PASSWORD ) ) {
          setPassword( field.getStringValue() );
        } else if ( field.getName().equalsIgnoreCase( ENC_USERNAME ) ) {
          setUsername( CipherUtil.decryptString( field.getStringValue() ) );
        } else if ( field.getName().equalsIgnoreCase( ENC_PASSWORD ) ) {
          setPassword( CipherUtil.decryptString( field.getStringValue() ) );
        }
      } // header name ! null
    } // for each field

  }




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process( HttpMessage request ) {

    // if we don't have any data to place in the header...
    if ( StringUtil.isBlank( headerData ) ) {
      // ... calculate it from the username and password
      calculateHeaderData();
    }

    // Set the header if data exists
    if ( StringUtil.isNotBlank( headerData ) ) {
      request.setHeader( headerName, headerData );
    }
  }

}
