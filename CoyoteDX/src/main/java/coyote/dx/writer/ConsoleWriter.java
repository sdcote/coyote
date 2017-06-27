/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.CSVMarshaler;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.ConfigTag;


/**
 * 
 */
public class ConsoleWriter extends AbstractFrameWriter {
  private final String JSON_FORMAT = "json";
  private final String XML_FORMAT = "xml";
  private final String CSV_FORMAT = "csv";




  public String getFormat() {
    if ( configuration.containsIgnoreCase( ConfigTag.FORMAT ) ) {
      return configuration.getString( ConfigTag.FORMAT );
    }
    return JSON_FORMAT;
  }




  private boolean isIndented() {
    if ( configuration.containsIgnoreCase( ConfigTag.INDENT ) ) {
      return configuration.getBoolean( ConfigTag.INDENT );
    }
    return false;
  }




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {
    if ( isIndented() ) {
      if ( JSON_FORMAT.equalsIgnoreCase( getFormat() ) ) {
        System.out.println( JSONMarshaler.toFormattedString( frame ) );
      } else if ( XML_FORMAT.equalsIgnoreCase( getFormat() ) ) {
        System.out.println( XMLMarshaler.toFormattedString( frame ) );
      } else if ( CSV_FORMAT.equalsIgnoreCase( getFormat() ) ) {
        System.out.println( CSVMarshaler.marshal( frame ) );
      } else {
        System.out.println( "Don't know how to format data into '" + getFormat() + "'" );
      }
    } else {
      if ( JSON_FORMAT.equalsIgnoreCase( getFormat() ) ) {
        System.out.println( JSONMarshaler.marshal( frame ) );
      } else if ( XML_FORMAT.equalsIgnoreCase( getFormat() ) ) {
        System.out.println( XMLMarshaler.marshal( frame ) );
      } else if ( CSV_FORMAT.equalsIgnoreCase( getFormat() ) ) {
        System.out.println( CSVMarshaler.marshal( frame ) );
      } else {
        System.out.println( "Don't know how to format data into '" + getFormat() + "'" );
      }
    }
  }

}
