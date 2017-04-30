/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.testdata.credential;

import coyote.testdata.GenerationStrategy;
import coyote.testdata.Row;


/**
 * 
 */
public class UsernameNameStrategy implements GenerationStrategy {

  /**
   * Generate a user name from a users first and last name.
   * 
   * p>This method will remove any punctuation and/or whitespace from a the 
   * users first and last name prior to creation.</p>
   * 
   * <p>Generated user names will always be their first initial + last name not 
   * exceeding 8 characters</p>
   * 
   * @param firstName of the user
   * @param lastName of the user
   * 
   * @return a string containing the new user name
   */
  public static String generateUserName( final String firstName, final String lastName ) {

    // remove any punctuation, spaces, etc..
    final String fName = firstName.replaceAll( "\\p{Punct}|\\p{Space}", "" );
    String lName = lastName.replaceAll( "\\p{Punct}|\\p{Space}", "" );

    if ( lName.length() > 7 ) {
      lName = lName.substring( 0, 7 );
    }

    final String userName = fName.substring( 0, 1 ) + lName;

    return userName.toLowerCase();
  }

  String[] firstFields = { "firstname", "first_name", "fname", "given_name", "givenname", "first_nm", "firstnm", "frstnm" };

  String[] lastFields = { "lastname", "last_name", "lname", "family_name", "familyname", "surname", "last_nm", "lastnm", "lstnm" };




  public UsernameNameStrategy() {

  }




  /**
   * @see coyote.testdata.GenerationStrategy#getData(coyote.testdata.Row)
   */
  @Override
  public Object getData( final Row row ) {
    final String firstName = locateName( firstFields, row );
    final String lastName = locateName( lastFields, row );

    return generateUserName( firstName, lastName );
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
