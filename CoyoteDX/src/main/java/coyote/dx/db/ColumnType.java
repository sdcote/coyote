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

package coyote.dx.db;

/**
 * The supported data types for database columns.
 */
public enum ColumnType {
  /** String. */
  STRING("STR", 0),
  /** . */
  BOOLEAN("BOL", 1),
  /** . */
  BYTE("S8", 8),
  /** . */
  SHORT("S16", 16),
  /** . */
  INT("S32", 32),
  /** . */
  LONG("S64", 64),
  /** . */
  FLOAT("FLT", 64),
  /** . */
  DOUBLE("DBL", 64),
  /** . */
  DATE("DAT", 64);

  private String name;
  private int length;




  private ColumnType(String name, int length) {
    this.name = name;
    this.length = length;
  }




  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }




  /**
   * Get the column type by name.
   * 
   * @param name name of the column to return
   * 
   * @return the column type with the given mane or null if not found.
   */
  public static ColumnType getColumnTypeByName(String name) {
    ColumnType retval = null;
    if (name != null) {
      for (final ColumnType type : ColumnType.values()) {
        if (name.equalsIgnoreCase(type.toString())) {
          retval = type;
          break;
        }
      }
    }
    return retval;
  }




  public String getName() {
    return name;
  }




  public int getLength() {
    return length;
  }

}
