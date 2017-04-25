/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.testdata.format;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import coyote.testdata.DataSet;
import coyote.testdata.Row;


/**
 * 
 */
public class CSVFormatter extends AbstractFormatter {
  private static final SimpleDateFormat FORMATTER = new SimpleDateFormat( "yyyy/MM/dd" );




  /**
   * @see coyote.testdata.format.Formatter#format(coyote.testdata.DataSet)
   */
  @Override
  public String format( final DataSet dataset ) {
    final StringBuilder retval = new StringBuilder();
    // create the header to the dataset
    retval.append( processHeader( dataset ) );
    retval.append( "\r\n" );
    retval.append( processRows( dataset ) );
    return retval.toString();
  }




  /**
   * @param dataset
   * @return
   */
  private String processHeader( final DataSet dataset ) {
    final List<String> names = dataset.getColumnNames();
    final StringBuilder retval = new StringBuilder();
    if ( names.size() > 0 ) {
      for ( final String name : names ) {
        retval.append( name );
        retval.append( ',' );
      }
      retval.deleteCharAt( retval.length() - 1 );// remove last comma
    }
    return retval.toString();
  }




  /**
   * @param dataset
   * @return
   */
  private String processRows( final DataSet dataset ) {
    final StringBuilder retval = new StringBuilder();

    // List of order column names
    final List<String> names = dataset.getColumnNames();

    // for each one of the rows
    for ( int index = 0; index < dataset.size(); index++ ) {
      final Row row = dataset.getRow( index );

      // for each of the columns in that row
      for ( final String name : names ) {
        // the named value for that row
        Object obj = row.get( name );
        if ( obj == null ) {
          obj = "";
        }

        String token = null;
        if ( obj instanceof Date ) {
          token = FORMATTER.format( (Date)obj );
        } else {
          // try to convert it into a string
          token = obj.toString();
        }

        // escape any special characters

        // place quotes around it if necessary

        // other CSV formatting as necessary

        retval.append( token );
        retval.append( ',' );
      }
      if ( retval.length() > 0 ) {
        retval.deleteCharAt( retval.length() - 1 );// remove last comma
      }

      retval.append( "\r\n" );
    }

    return retval.toString();
  }

}
