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

import coyote.testdata.transform.DataTransform;


/**
 * 
 */
public interface Generator {

  /**
   * Add a data generation strategy used in this generator.
   * 
   * @param strategy The strategy this generator should use to create data. 
   */
  public void addStrategy( GenerationStrategy strategy );




  /**
   * Add the given transform to the list of transforms used by this generator.
   * 
   * <p>Once datum is generated, it is passed to each transform in the order 
   * they were added to this generator and altered to ensure the generated data 
   * meets the expectations of the caller.</p> 
   * 
   * @param transform
   */
  public void addTransform( DataTransform transform );




  /**
   * Generate data and place it in the given row.
   * 
   * @param name The name of the column to populate with generated data
   * @param row The row to populate with data.
   */
  public void generateData( String name, Row row );




  /**
   * Obtain a reference to the list if generation strategies in this generator.
   * 
   * <p>Changing the strategies from this list and altering this list will 
   * result in affecting the behavior of this generator.</p>
   * 
   * @return The reference to the strategy list for this generator. 
   */
  public List<GenerationStrategy> getStrategies();




  /**
   * Remove a data generation strategy from this generator.
   * 
   * <p>Removal of all strategies from a generator will result in null values 
   * being generated or by poorly written generators, a null pointer exception 
   * may be thrown. Care should be taken not to leave a generator without 
   * generation strategies.</p> 
   * @param strategy
   */
  public void removeStrategy( GenerationStrategy strategy );

}
