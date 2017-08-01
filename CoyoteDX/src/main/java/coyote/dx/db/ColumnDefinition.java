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
 * This holds details about a column in a table.
 */
public class ColumnDefinition {
  private String name;
  private ColumnType type;
  private int length;
  private boolean nullable;
  private boolean readOnly;
  private boolean mandatory;
  private boolean primaryKey;
  private boolean unique;
  private String remarks;
  private int position;




  public ColumnDefinition(String name, ColumnType type) {
    this(name, type, type.getLength(), false, false, false, false, false, null, 0);
  }




  public ColumnDefinition(String name, ColumnType type, int len) {
    this(name, type, len, false, false, false, false, false, null, 0);
  }




  public ColumnDefinition(String name, ColumnType type, int len, boolean nullable, boolean ro, boolean req, boolean key, boolean unique, String remarks, int pos) {
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




  public ColumnDefinition setName(String name) {
    this.name = name;
    return this;
  }




  /**
   * @return the type
   */
  public ColumnType getType() {
    return type;
  }




  public ColumnDefinition setType(ColumnType type) {
    this.type = type;
    return this;
  }




  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }




  public ColumnDefinition setLength(int length) {
    this.length = length;
    return this;
  }




  /**
   * @return the readOnly
   */
  public boolean isReadOnly() {
    return readOnly;
  }




  public ColumnDefinition setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }




  /**
   * @return the mandatory
   */
  public boolean isMandatory() {
    return mandatory;
  }




  public ColumnDefinition setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }




  /**
   * @return the primaryKey
   */
  public boolean isPrimaryKey() {
    return primaryKey;
  }




  public ColumnDefinition setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
    return this;
  }




  /**
   * @return the unique
   */
  public boolean isUnique() {
    return unique;
  }




  public ColumnDefinition setUnique(boolean unique) {
    this.unique = unique;
    return this;
  }




  /**
   * @return the nullable
   */
  public boolean isNullable() {
    return nullable;
  }




  public ColumnDefinition setNullable(boolean nullable) {
    this.nullable = nullable;
    return this;
  }




  /**
   * @return the remarks
   */
  public String getRemarks() {
    return remarks;
  }




  public ColumnDefinition setRemarks(String remarks) {
    this.remarks = remarks;
    return this;
  }




  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }




  public ColumnDefinition setPosition(int position) {
    this.position = position;
    return this;
  }

}
