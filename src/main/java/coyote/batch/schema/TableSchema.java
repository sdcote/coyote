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
  private final String tableName;
  private final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();




  public TableSchema( String name ) {
    tableName = name;
  }




  public void addColumn( ColumnDefinition col ) {
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
   * @return the name
   */
  public String getName() {
    return tableName;
  }

}
