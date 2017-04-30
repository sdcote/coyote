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

import java.util.List;


/**
 * 
 */
public interface Row extends Cloneable {

  public Object clone();




  /**
   * Determine if the row contains the column with the given name,
   * 
   * @param name The name of the column to find.
   * 
   * @return True if the named column exists, fales otherwise.
   */
  public boolean containsColumn( String name );




  /**
   * Retrieve the value of the given column name.
   * 
   * @param name The name of the column to retrieve.
   * 
   * @return The value currently set in the named column, null if no value has been set of if no column with that name is present in the row.
   */
  public Object get( String name );




  /**
   * @return the list of column names for this row.
   */
  public List<String> getColumnNames();




  /**
   * Return the value of the named row as a string.
   * 
   * <p>If the column exists and there is no value, an empty string will be 
   * returned. If the column does not exist, then a null reference will be 
   * returned.</p> 
   * 
   * @param name The name of the column to query
   * 
   * @return The value of the named column, null if the named column does not exist. 
   */
  public String getStringValue( String name );




  /**
   * Set the value of the given column name.
   * 
   * @param name The name of the column to set.
   * @param value The value to set in the specified column
   */
  public void set( String name, Object value );

}
