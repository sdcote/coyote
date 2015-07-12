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
package coyote.commons;

import java.sql.Timestamp;


/**
 * 
 */
public class JdbcUtil {

  private JdbcUtil() {}




  public static Timestamp getCurrentTimeStamp() {
    return getTimeStamp( new java.util.Date() );
  }




  public static Timestamp getTimeStamp( java.util.Date date ) {
    return new Timestamp( date.getTime() );
  }

}
