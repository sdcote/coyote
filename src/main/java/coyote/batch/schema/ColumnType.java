/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.schema;

/**
 * 
 */
public enum ColumnType {
  /** String */
  STRING( "STR", 0),
  /**  */
  BOOLEAN( "BOL", 1),
  /**  */
  BYTE( "S8", 8),
  /**  */
  SHORT( "S16", 16),
  /**  */
  INT( "S32", 32),
  /**  */
  LONG( "S64", 64),
  /**  */
  FLOAT( "FLT", 64),
  /**  */
  DOUBLE( "DBL", 64),
  /**  */
  DATE( "DAT", 64);

  private String name;
  private int length;




  private ColumnType( String s, int l ) {
    name = s;
    length = l;
  }




  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }




  public static ColumnType getColumnTypeByName( String name ) {
    if ( name != null ) {
      for ( ColumnType type : ColumnType.values() ) {
        if ( name.equalsIgnoreCase( type.toString() ) ) {
          return type;
        }
      }
    }
    return null;
  }




  public int getLength() {
    return length;
  }

}
