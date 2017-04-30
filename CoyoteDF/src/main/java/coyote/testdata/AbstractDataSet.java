/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.testdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This is the base class for all dataset implementations.
 */
public abstract class AbstractDataSet implements DataSet {
  protected int _capacity = 0;
  List<Row> rows = new ArrayList<Row>();
  List<Column> columns = new ArrayList<Column>();




  /**
   * @see coyote.testdata.DataSet#add(coyote.testdata.DataSet)
   */
  @Override
  public void add( final DataSet dataset ) {

    for ( final Row row : dataset.getRows() ) {
      add( row );
    }

    // Make sure we include the newly added rows to our capacity if necessary.
    if ( rows.size() > _capacity ) {
      _capacity = rows.size();
    }

    // add any missing columns
    for ( final String name : dataset.getColumnNames() ) {
      if ( !containsColumn( name ) ) {
        columns.add( dataset.getColumn( name ) );
      }
    }
  }




  /**
   * @see coyote.testdata.DataSet#add(coyote.testdata.Row)
   */
  @Override
  public void add( Row row ) {
    // make a copy of the row to add as it may be re-populated which would 
    // change this reference as well
    rows.add( (Row)row.clone() );
  }




  /**
   * @see coyote.testdata.DataSet#addColumn(java.lang.String, coyote.testdata.Generator)
   */
  @Override
  public void addColumn( final String name, final Generator generator ) {
    if ( ( name != null ) && ( generator != null ) && ( name.trim().length() > 0 ) ) {
      columns.add( new Column( name, generator ) );
    }

  }




  /**
   * @see coyote.testdata.DataSet#addColumn(coyote.testdata.Column)
   */
  @Override
  public void addColumn( Column column ) {
    if ( column != null && column.getName() != null ) {
      columns.add( column );
    }

  }




  /**
   * @see coyote.testdata.DataSet#arrangeColumns(java.lang.String[])
   */
  @Override
  public void arrangeColumns( final String... columnNames ) {
    final List<Column> newOrder = new ArrayList<Column>();

    for ( final String name : columnNames ) {
      for ( int index = 0; index < columns.size(); index++ ) {
        if ( name.equals( columns.get( index ).getName() ) ) {
          newOrder.add( columns.remove( index ) );
          break; // get the next column name
        }
      }
    }

    // now add the remaining columns into the new order list
    final Iterator<Column> it = columns.iterator();
    while ( it.hasNext() ) {
      newOrder.add( it.next() );
      it.remove();
    }

    columns = newOrder;
  }




  /**
   * @see coyote.testdata.DataSet#containsColumn(java.lang.String)
   */
  @Override
  public boolean containsColumn( final String name ) {
    if ( getColumn( name ) != null ) {
      return true;
    } else {
      return false;
    }
  }




  /**
   * @see coyote.testdata.DataSet#getCapacity()
   */
  @Override
  public int getCapacity() {
    return _capacity;
  }




  /**
   * @see coyote.testdata.DataSet#getColumn(java.lang.String)
   */
  @Override
  public Column getColumn( final String name ) {
    if ( name != null ) {
      for ( final Column column : columns ) {
        if ( name.equals( column.getName() ) ) {
          return column;
        }
      }
    }
    return null;
  }




  /**
   * @see coyote.testdata.DataSet#getColumnNames()
   */
  @Override
  public List<String> getColumnNames() {
    final ArrayList<String> names = new ArrayList<String>();

    for ( final Column column : columns ) {
      names.add( column.getName() );
    }
    return names;
  }




  /**
   * @see coyote.testdata.DataSet#getRow(int)
   */
  @Override
  public Row getRow( final int index ) {
    if ( index >= rows.size() ) {
      throw new IllegalArgumentException( "Row index exceeds dataset size" );
    }
    return rows.get( index );
  }




  /**
   * @see coyote.testdata.DataSet#getRows()
   */
  @Override
  public List<Row> getRows() {
    return rows;
  }




  /**
   * @see coyote.testdata.DataSet#populate(int)
   */
  @Override
  public void populate( final int rowIndex ) {
    if ( rowIndex >= _capacity ) {
      throw new IllegalArgumentException( "Row index exceeds dataset capacity" );
    }

    // add the number of rows necessary 
    while ( rows.size() <= rowIndex ) {
      rows.add( new DefaultRow() );
    }

    // now populate the specific row
    populate( rows.get( rowIndex ) );
  }




  /**
   * Populate each column with data for the given row
   * 
   * @param row The row to populate with data
   */
  protected void populate( final Row row ) {
    if ( row != null ) {
      for ( final Column column : columns ) {
        column.getGenerator().generateData( column.getName(), row );
      }
    }
  }




  /**
   * @see coyote.testdata.DataSet#setCapacity(int)
   */
  @Override
  public void setCapacity( final int size ) {
    // If we are being asked to reduce the capacity of the dataset... 
    if ( size < rows.size() ) {
      // remove rows from the dataset to release resources
      int count = 1;
      for ( final Iterator<Row> it = rows.iterator(); it.hasNext(); count++ ) {
        if ( count > size ) {
          it.remove();
        }
      }
    }
    _capacity = size;
  }




  /**
   * @see coyote.testdata.DataSet#size()
   */
  @Override
  public int size() {
    return rows.size();
  }
}
