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
 * This class represents a field in a dataset.
 * 
 * <p>As datasets are composed of rows, this class represents the named column 
 * of each row.</p>
 * 
 * <p>Columns are composed of a name and an associated data generator. The
 * generator for a column is called to generate data for that field of the row 
 * by the populate method of the DataFactory.</p> 
 */
public class Column implements Cloneable {
  private String _name = null;
  private Generator _generator = null;




  public Object clone() {
    return new Column( _name, _generator );// not a deep copy!
  }




  Column( final String name, final Generator generator ) {
    setName( name );
    setGenerator( generator );
  }




  /**
   * @return the _generator used to generate data for this column
   */
  public Generator getGenerator() {
    return _generator;
  }




  /**
   * @return the _name of the column
   */
  public String getName() {
    return _name;
  }




  /**
   * @param generator the generator to set to this column
   */
  public void setGenerator( final Generator generator ) {
    _generator = generator;
  }




  /**
   * @param name the name of the column to set 
   */
  public void setName( final String name ) {
    _name = name;
  }

}
