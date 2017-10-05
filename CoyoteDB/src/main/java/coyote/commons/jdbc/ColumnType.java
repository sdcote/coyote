/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.jdbc;

import coyote.commons.StringUtil;


/**
 * The supported data types for database columns.
 */
public enum ColumnType {
  /** . */
  BOOLEAN("BOL", 1),
  /** . */
  BYTE("S8", 8),
  /** . */
  DATE("DAT", 64),
  /** . */
  DOUBLE("DBL", 64),
  /** . */
  FLOAT("FLT", 64),
  /** . */
  INT("S32", 32),
  /** . */
  LONG("S64", 64),
  /** . */
  SHORT("S16", 16),
  /** String. */
  STRING("STR", 0);

  private int length;
  private String name;




  /**
   * Get the column type by name.
   *
   * @param name name of the column to return
   *
   * @return the column type with the given mane or null if not found.
   */
  public static ColumnType getColumnTypeByName(final String name) {
    ColumnType retval = null;
    if (name != null) {
      for (final ColumnType type : ColumnType.values()) {
        if (StringUtil.equalsIgnoreCase(name, type.toString())) {
          retval = type;
          break;
        }
      }
    }
    return retval;
  }




  private ColumnType(final String name, final int length) {
    this.name = name;
    this.length = length;
  }




  public int getLength() {
    return length;
  }




  public String getName() {
    return name;
  }




  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}
