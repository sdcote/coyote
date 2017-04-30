/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.web.decorator;

import java.util.UUID;

import org.apache.http.HttpMessage;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * Generates a random UUID as the message identifier.
 * 
 * <p>Useful for auditing in web service infrastructures.</p>
 */
public class MessageIdentifier extends AbstractDecorator implements RequestDecorator {
  private String headerName = "X-Message-Id";




  public MessageIdentifier() {}




  /**
   * @see coyote.dx.web.decorator.RequestDecorator#process(org.apache.http.HttpMessage)
   */
  @Override
  public void process( HttpMessage request ) {
    request.setHeader( headerName, UUID.randomUUID().toString() );
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
   * @see coyote.dx.web.decorator.AbstractDecorator#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) {
    super.setConfiguration( frame );

    //look for a header parameter so we can change the name of the header to populate.
    for ( DataField field : frame.getFields() ) {
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( HEADER ) ) {
        headerName = field.getStringValue();
      }
    } // for each field

  }

}
