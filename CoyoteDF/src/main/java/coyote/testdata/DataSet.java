/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.testdata;

import java.util.List;


/**
 * This represents a set of data with a Header and zero or more rows. 
 */
public interface DataSet {

  /**
   * Add the given dataset to this.
   * 
   * <p>Since rows are re-used and re-populated with data, this method makes a
   * deep copy (clone) of the rows in the given dataset and adds the cloned 
   * rows to this data set. This is to prevent any re-population of the dataset 
   * argument from effectively changing this datasets rows. Row references are 
   * not shared as a result of this operation. New rows are created.</p>
   * 
   * <p>Any new columns in the argument will be added to this dataset possibly 
   * adding new columns to this dataset if the column names do not match 
   * exactly.</p>
   * 
   * @param dataset
   */
  public void add( DataSet dataset );




  /**
   * @param name
   * @param generator
   */
  public void addColumn( String name, Generator generator );




  /**
   * Add the column to this dataset.
   * 
   * @param column The column to add
   */
  public void addColumn( Column column );




  /**
   * Arrange the column names given into the order they are presented.
   * 
   * <p>This allows for the ability to change the order in which the columns 
   * appear in the formatters and writers.</p>
   * 
   * <p><strong>NOTE</strong> This call will change the order of the under of 
   * the columns in the DataSet and affect the order in which the columns are 
   * processed. This is to say, the order in which the columns were originally 
   * added will be lost. Arranging the columns is permanent unless re-arranged 
   * at a later time. Fortunately this is a relatively inexpensive operation as
   * the data in the rows are unaffected, just the order in which the columns 
   * are processed is changed.</p>
   * 
   * @param columnNames a list of column names in the order they are to be processed in this dataset
   */
  public void arrangeColumns( String... columnNames );




  /**
   * Determine of the dataset contains the column of the given name.
   * 
   * @param name Name of the column to check
   * 
   * @return True if the dataset contains a column with the given name, false otherwise.
   */
  public boolean containsColumn( String name );




  /**
   * @return the (max) number of rows for this dataset.
   */
  public int getCapacity();




  /**
   * Retrieve the column with the given name.
   * 
   * @param name The name of the column to retrieve
   * 
   * @return The column with the given name or null if the column does not exist.
   */
  public Column getColumn( String name );




  /**
   * Accesses a mutable, ordered list of column names for this dataset.
   * 
   * <p>Names are returned in the order they were added. Removing items from 
   * this list has no effect on the actual columns in the dataset.</p>
   * 
   * @return An ordered list of each column name in this dataset
   */
  public List<String> getColumnNames();




  /**
   * Retrieve the dataset row at the given index.
   * 
   * @param index The zero based index of the row to retrieve
   * 
   * @return The Row at the given index.
   * 
   * @throws IllegalArgumentException if a request is made to populate a dataset beyond its row size or capacity.
   */
  public Row getRow( int index );




  /**
   * @return the list of rows in this dataset.
   */
  public List<Row> getRows();




  /**
   * Populate a specific row with new data.
   * 
   * <p>Existing data in that row will be re-populated with new data. This is a
   * subtle point. Rows are re-used and not re-created meaning data generated 
   * are placed into existing rows allowing the number of rows created to 
   * remain low.</p>
   * 
   * <p>If the row index is greater than the number of currently populated 
   * rows, additional rows will be created, but populated will nulls or empty 
   * values. For example, if a dataset with a capacity of two rows has no rows 
   * of data currently populated and a request is made to populate row 1, row 0
   * (the first row) will be populated with empty data and the second row (row 
   * 1) will be populated with new data.</p>
   * 
   * <p><strong>NOTE:</strong> making a call to populate a row beyond the 
   * dataset's capacity will result in an IllegalArgumentException being thrown
   * indicating the row argument is not valid.</p>
   *  
   * @param rowIndex the (zero-based) index of the row to populate 
   * 
   * @throws IllegalArgumentException if a request is made to populate a dataset beyond its row capacity.
   */
  public void populate( int rowIndex );




  /**
   * @param size the maximum number of rows for this dataset;
   */
  public void setCapacity( int size );




  /**
   * Access the number of rows currently in the dataset.
   * 
   * <p>This number is always less than or equal to the capacity of the 
   * dataset.</p>
   * 
   * @return the current size (number of rows) of the dataset.
   */
  public int size();




  /**
   * Add the given row to the dataset.
   * 
   * @param row the row to add.
   */
  public void add( Row row );

}
