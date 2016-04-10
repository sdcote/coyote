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
package coyote.commons.jdbc;


/**
 * This holds details about a column in a table
 */
public class ColumnDefinition {
  private final String name;
  private final ColumnType type;
  private final int length;
  private final boolean nullable;
  private final boolean readOnly;
  private final boolean mandatory;
  private final boolean primaryKey;
  private final boolean unique;
  private final String remarks;
  private final int position;




  public ColumnDefinition( String name, ColumnType type ) {
    this( name, type, type.getLength(), false, false, false, false, false, null, 0 );
  }




  public ColumnDefinition( String name, ColumnType type, int len ) {
    this( name, type, len, false, false, false, false, false, null, 0 );
  }




  public ColumnDefinition( String name, ColumnType type, int len, boolean nullable, boolean ro, boolean req, boolean key, boolean unique, String remarks, int pos ) {
    this.name = name;
    this.type = type;
    length = len;
    this.nullable = nullable;
    readOnly = ro;
    mandatory = req;
    primaryKey = key;
    this.unique = unique;
    this.remarks = remarks;
    position = pos;
  }




  /**
   * @return the name
   */
  public String getName() {
    return name;
  }




  /**
   * @return the type
   */
  public ColumnType getType() {
    return type;
  }




  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }




  /**
   * @return the readOnly
   */
  public boolean isReadOnly() {
    return readOnly;
  }




  /**
   * @return the mandatory
   */
  public boolean isMandatory() {
    return mandatory;
  }




  /**
   * @return the primaryKey
   */
  public boolean isPrimaryKey() {
    return primaryKey;
  }




  /**
   * @return the unique
   */
  public boolean isUnique() {
    return unique;
  }




  /**
   * @return the nullable
   */
  public boolean isNullable() {
    return nullable;
  }




  /**
   * @return the remarks
   */
  public String getRemarks() {
    return remarks;
  }




  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }

}
