/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.jdbc;

import java.util.ArrayList;
import java.util.List;

import coyote.commons.StringUtil;


/**
 * This models a (database) table definition.
 */
public class TableDefinition {
  private String catalogName;
  private final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
  private int majorVersion;
  private int minorVersion;
  private String productName;
  private String productVersion;
  private String schemaName;
  private String tableName;




  /**
   * Default constructor specifying a tablename.
   *
   * @param name The name of the database table this models
   */
  public TableDefinition(final String name) {
    tableName = name;
  }




  /**
   * Add a column definition to the current list.
   *
   * <p>This will sort the columns by their ordinal position if specified. If
   * equal to another, the column is placed in the order it was added.</p>
   *
   * @param col The column definition to add
   */
  public void addColumn(final ColumnDefinition col) {
    if (col != null) {
      for (int x = 0; x < columns.size(); x++) {
        if (columns.get(x).getPosition() > col.getPosition()) {
          columns.add(x, col);
          return;
        }
      }
      columns.add(col);
    }
  }




  /**
   * Convenience method to add a column with a specified name and type.
   *
   * @param name The name of the column to add
   * @param type The data type of the column
   */
  public void addColumn(final String name, final ColumnType type) {
    columns.add(new ColumnDefinition(name, type));
  }




  /**
   * Convenience method to add a column with a specified name, type and length.
   *
   * @param name The name of the column to add
   * @param type The data type of the column
   * @param len The maximum length of the data this column will hold
   */
  public void addColumn(final String name, final ColumnType type, final int len) {
    columns.add(new ColumnDefinition(name, type, len));
  }




  /**
   * Return the named column definition from the table definition based on a
   * case in-sensitive search.
   *
   * @param name The name of the column to return
   *
   * @return the named column or null if not found.
   *
   * @see #getColumn(String)
   */
  public ColumnDefinition findColumn(final String name) {
    for (final ColumnDefinition column : columns) {
      if (StringUtil.equalsIgnoreCase(column.getName(), name)) {
        return column;
      }
    }
    return null;
  }




  /**
   * @return the name of the catalog in which this table exists
   */
  public String getCatalogName() {
    return catalogName;
  }




  /**
   * Return the named column definition from the table definition.
   *
   * @param name The name of the column to return
   *
   * @return the named column or null if not found.
   *
   * @see #findColumn(String)
   */
  public ColumnDefinition getColumn(final String name) {
    for (final ColumnDefinition column : columns) {
      if (column.getName().equals(name)) {
        return column;
      }
    }
    return null;
  }




  /**
   * @return the reference to the list of column in this table.
   */
  public List<ColumnDefinition> getColumns() {
    return columns;
  }




  /**
   * @return the major version number of the database product hosting this table
   */
  public int getMajorVersion() {
    return majorVersion;
  }




  /**
   * @return the minor version number of the database product hosting this table
   */
  public int getMinorVersion() {
    return minorVersion;
  }




  /**
   * @return the name of the table
   */
  public String getName() {
    return tableName;
  }




  /**
   * @param name the name of the table to set
   */
  public void setName(String name) {
    this.tableName = name;
  }




  /**
   * @return the name of the database product hosting this table
   */
  public String getProductName() {
    return productName;
  }




  /**
   * @return the version of the database product hosting this table
   */
  public String getProductVersion() {
    return productVersion;
  }




  /**
   * @return the name of the schema in which this table exists
   */
  public String getSchemaName() {
    return schemaName;
  }




  /**
   * @param name the catalog name to set
   */
  public void setCatalogName(final String name) {
    catalogName = name;
  }




  /**
   * @param version the major version number of the database product hosting this table
   */
  public void setMajorVersion(final int version) {
    majorVersion = version;
  }




  /**
   * @param version the minor version number of the database product hosting this table
   */
  public void setMinorVersion(final int version) {
    minorVersion = version;
  }




  /**
   * @param name the name of the database product hosting this table (e.g. ORACLE, H2, etc.)
   */
  public void setProductName(final String name) {
    productName = name;
  }




  /**
   * @param version the version of the database product hosting this table
   */
  public void setProductVersion(final String version) {
    productVersion = version;
  }




  /**
   * @param name the schema name to set
   */
  public void setSchemaName(final String name) {
    schemaName = name;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    if (catalogName != null) {
      b.append(catalogName);
      b.append('.');
    }
    if (schemaName != null) {
      b.append(schemaName);
      b.append('.');
    }
    b.append(tableName);
    b.append("\r\n");
    for (final ColumnDefinition column : columns) {
      b.append(column.getName());
      b.append(' ');
      b.append(column.getType().getName());
      b.append('(');
      b.append(column.getLength());
      b.append(')');
      b.append("\r\n");
    }
    return b.toString();
  }

}
