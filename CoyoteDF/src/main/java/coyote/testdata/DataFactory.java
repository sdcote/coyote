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

/**
 * 
 */
public class DataFactory {

  /**
   * @return the created dataset
   */
  public static DataSet createDataSet() {
    return new DefaultDataSet();
  }




  /**
   * Create a new dataset using the given dataset as a model.
   * 
   * <p>This will create a new dataset and populate it with clones of the 
   * columns. The columns are not shared between the two datasets.</p>
   * 
   * @param model The dataset to use a a model for the new dataset
   * 
   * @return a new dataset with no rows and a capacity set to zero.
   */
  public static DataSet createDataSet( DataSet model ) {
    DataSet retval = DataFactory.createDataSet();
    
    // Clone the columns for the new dataset
    for ( String columnName : model.getColumnNames() ) {
      Column column = model.getColumn( columnName );
      retval.addColumn( (Column)column.clone() );
    }

    return retval;
  }




  /**
   * Create a new dataset which can hold the given number of rows.
   * 
   * @param size the number of rows in this dataset.
   * 
   * @return a new empty dataset.
   */
  public static DataSet createDataSet( final int size ) {
    final DataSet retval = new DefaultDataSet();
    retval.setCapacity( size );
    return retval;
  }




  /**
   * This will populate the dataset to capacity with new data.
   * 
   * <p>This method can be called multiple times on the same dataset and result
   * in the existing data being over-written. No new rows will be created, only
   * new data placed in the existing rows.</p>
   * 
   * @param dataset The set of data to populate
   */
  public static void populate( final DataSet dataset ) {
    if ( dataset.getCapacity() > 0 ) {
      for ( int x = 0; x < dataset.getCapacity(); dataset.populate( x++ ) ) {
        ;
      }
    } else {
      // just one row gets populated
      dataset.setCapacity( 1 );
      dataset.populate( 0 );
    }

  }




  /**
   * @param dataset The set of data to populate
   * @param count this sets the new maximum size of the dataset
   */
  public static void populate( final DataSet dataset, final int count ) {
    dataset.setCapacity( count );
    DataFactory.populate( dataset );
  }




  /**
   * Private constructor; no instances necessary.
   */
  private DataFactory() {

  }

}
