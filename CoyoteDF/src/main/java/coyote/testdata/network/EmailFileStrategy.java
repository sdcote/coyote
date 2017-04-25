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
package coyote.testdata.network;

import coyote.testdata.DataFileStrategy;
import coyote.testdata.Row;


/**
 * 
 */
public class EmailFileStrategy extends DataFileStrategy {
  private static final String FILENAME = "emaildomain";

  String[] userFields = { "username", "user_name", "accountname", "accountname" };




  public EmailFileStrategy() {

    // Load data from a file if it exists
    super.loadData( FILENAME );

  }




  /**
   * @see coyote.testdata.GenerationStrategy#getData(coyote.testdata.Row)
   */
  @Override
  public Object getData( final Row row ) {

    String username = locateName( userFields, row );
    if ( ( username == null ) || ( username.trim().length() == 0 ) ) {
      username = "usr" + random.nextInt( 1000 );
    }

    final String domain = data.get( random.nextInt( data.size() ) );

    final StringBuffer buffer = new StringBuffer();
    buffer.append( username );
    buffer.append( '@' );
    buffer.append( domain );

    // return the generated value
    return buffer.toString();
  }




  /**
   * Look for a column name which matches one of the names in the array and 
   * return the value of that column.
   * 
   * <p>This performs a case-insensitive search for column names in the given 
   * row. If a match is found, the string value of that columns value is 
   * returned.</p>
   * 
   * @param fields The array of names for which to search. 
   * @param row The row in which to search.
   * 
   * @return The string value of the matched column.
   */
  private String locateName( final String[] fields, final Row row ) {

    for ( final String field : fields ) {
      for ( final String column : row.getColumnNames() ) {
        if ( field.equalsIgnoreCase( column ) ) {
          final Object obj = row.get( column );
          if ( obj != null ) {
            return obj.toString();
          } else {
            break; // could there be another match? - doubtful 
          }
        }
      }
    }
    return null;
  }

}
