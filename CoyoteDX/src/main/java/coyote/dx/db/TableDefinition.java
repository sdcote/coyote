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

import java.util.ArrayList;
import java.util.List;


/**
 * This models a (database) table definition.
 */
public class TableDefinition {
  private String catalogName;
  private String schemaName;
  private final String tableName;
  private final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();




  /**
   * Default constructor specifying a tablename.
   * 
   * @param name The name of the database table this models
   */
  public TableDefinition( String name ) {
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
  public void addColumn( ColumnDefinition col ) {
    if ( col != null ) {
      for ( int x = 0; x < columns.size(); x++ ) {
        if ( columns.get( x ).getPosition() > col.getPosition() ) {
          columns.add( x, col );
          return;
        }
      }
      columns.add( col );
    }
  }



/**
 * Convenience method to add a column with a specified name, type and length.
 * 
 * @param name The name of the column to add
 * @param type The data type of the column
 * @param len The maximum length of the data this column will hold 
 */
  public void addColumn( String name, ColumnType type, int len ) {
    columns.add( new ColumnDefinition( name, type, len ) );
  }




  /**
   * Convenience method to add a column with a specified name and type.
   * 
   * @param name The name of the column to add
   * @param type The data type of the column
   */
  public void addColumn( String name, ColumnType type ) {
    columns.add( new ColumnDefinition( name, type ) );
  }



/**
 * @return the reference to the list of column in this table.
 */
  public List<ColumnDefinition> getColumns() {
    return columns;
  }




  /**
   * @return the name of the table
   */
  public String getName() {
    return tableName;
  }




  /**
   * @return the name of the catalog in which this table exists
   */
  public String getCatalogName() {
    return catalogName;
  }




  /**
   * @param name the catalog name to set
   */
  public void setCatalogName( String name ) {
    this.catalogName = name;
  }




  /**
   * @return the name of the schema in which this table exists
   */
  public String getSchemaName() {
    return schemaName;
  }




  /**
   * @param name the schema name to set
   */
  public void setSchemaName( String name ) {
    this.schemaName = name;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    if ( catalogName != null ) {
      b.append( catalogName );
      b.append( '.' );
    }
    if ( schemaName != null ) {
      b.append( schemaName );
      b.append( '.' );
    }
    b.append( tableName );
    b.append( "\r\n" );
    b.append( "================" );
    b.append( "\r\n" );
    for ( ColumnDefinition column : columns ) {
      b.append( column.getName() );
      b.append( ' ' );
      b.append( column.getType().getName() );
      b.append( '(' );
      b.append( column.getLength() );
      b.append( ')' );
      b.append( "\r\n" );
    }
    b.append( "================" );

    return b.toString();
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
  public ColumnDefinition getColumn( String name ) {
    for ( ColumnDefinition column : columns ) {
      if ( column.getName().equals( name ) ) {
        return column;
      }
    }
    return null;
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
  public ColumnDefinition findColumn( String name ) {
    for ( ColumnDefinition column : columns ) {
      if ( column.getName().equalsIgnoreCase( name ) ) {
        return column;
      }
    }
    return null;
  }

}
