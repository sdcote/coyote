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

import java.util.ArrayList;
import java.util.List;


/**
 * This models a (database) table schema.
 */
public class TableSchema {
  private String catalogName;
  private String schemaName;
  private final String tableName;
  private final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();




  public TableSchema( String name ) {
    tableName = name;
  }




  public void addColumn( ColumnDefinition col ) {
    for ( int x = 0; x < columns.size(); x++ ) {
      if ( columns.get( x ).getPosition() > col.getPosition() ) {
        columns.add( x, col );
        return;
      }
    }
    columns.add( col );
  }




  public void addColumn( String name, ColumnType type, int len ) {
    columns.add( new ColumnDefinition( name, type, len ) );
  }




  public void addColumn( String name, ColumnType type ) {
    columns.add( new ColumnDefinition( name, type ) );
  }




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

}
