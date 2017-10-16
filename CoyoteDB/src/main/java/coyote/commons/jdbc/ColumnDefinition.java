/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.jdbc;

/**
 * This holds details about a column in a table.
 */
public class ColumnDefinition {
  private long length;
  private boolean mandatory;
  private String name;
  private boolean nullable;
  private int position;
  private boolean primaryKey;
  private boolean readOnly;
  private String remarks;
  private ColumnType type;
  private boolean unique;




  public ColumnDefinition(final String name, final ColumnType type) {
    this(name, type, type.getLength(), false, false, false, false, false, null, 0);
  }




  public ColumnDefinition(final String name, final ColumnType type, final int len) {
    this(name, type, len, false, false, false, false, false, null, 0);
  }




  public ColumnDefinition(final String name, final ColumnType type, final int len, final boolean nullable, final boolean ro, final boolean req, final boolean key, final boolean unique, final String remarks, final int pos) {
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
   * @return the length
   */
  public long getLength() {
    return length;
  }




  /**
   * @return the name
   */
  public String getName() {
    return name;
  }




  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }




  /**
   * @return the remarks
   */
  public String getRemarks() {
    return remarks;
  }




  /**
   * @return the type
   */
  public ColumnType getType() {
    return type;
  }




  /**
   * @return the mandatory
   */
  public boolean isMandatory() {
    return mandatory;
  }




  /**
   * @return the nullable
   */
  public boolean isNullable() {
    return nullable;
  }




  /**
   * @return the primaryKey
   */
  public boolean isPrimaryKey() {
    return primaryKey;
  }




  /**
   * @return the readOnly
   */
  public boolean isReadOnly() {
    return readOnly;
  }




  /**
   * @return the unique
   */
  public boolean isUnique() {
    return unique;
  }




  public ColumnDefinition setLength(final long length) {
    this.length = length;
    return this;
  }




  public ColumnDefinition setMandatory(final boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }




  public ColumnDefinition setName(final String name) {
    this.name = name;
    return this;
  }




  public ColumnDefinition setNullable(final boolean nullable) {
    this.nullable = nullable;
    return this;
  }




  public ColumnDefinition setPosition(final int position) {
    this.position = position;
    return this;
  }




  public ColumnDefinition setPrimaryKey(final boolean primaryKey) {
    this.primaryKey = primaryKey;
    return this;
  }




  public ColumnDefinition setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }




  public ColumnDefinition setRemarks(final String remarks) {
    this.remarks = remarks;
    return this;
  }




  public ColumnDefinition setType(final ColumnType type) {
    this.type = type;
    return this;
  }




  public ColumnDefinition setUnique(final boolean unique) {
    this.unique = unique;
    return this;
  }

}
