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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import coyote.testdata.transform.DataTransform;


/**
 * 
 */
public abstract class AbstractGenerator implements Generator {

  private final List<DataTransform> transforms = new ArrayList<DataTransform>();
  protected Random random = new Random();
  private final List<GenerationStrategy> strategies = new ArrayList<GenerationStrategy>();




  @Override
  public void addStrategy( final GenerationStrategy strategy ) {
    strategies.add( strategy );
  }




  /**
   * @see coyote.testdata.Generator#addTransform(coyote.testdata.transform.DataTransform)
   */
  @Override
  public void addTransform( final DataTransform transform ) {
    transforms.add( transform );
  }




  /**
   * Apply each of the transforms to the given value in the order they were 
   * added to this generator.
   *  
   * @param value The value to transform
   * 
   * @return the transformed value
   */
  protected Object applyTransforms( final Object value ) {
    Object retval = value;
    for ( final DataTransform transformer : transforms ) {
      retval = transformer.transform( retval );
    }
    return retval;
  }




  /**
   * Perform the basic pattern of generating data using one of the set 
   * generation strategies and apply all the transforms in order before setting 
   * the data in the given row with the given name.
   * 
   * @see coyote.testdata.Generator#generateData(java.lang.String, coyote.testdata.Row)
   */
  @Override
  public void generateData( final String name, final Row row ) {

    Object retval = null;

    // If there is only one strategy (the norm)
    if ( strategies.size() == 1 ) {
      // use it to generate the data
      retval = strategies.get( 0 ).getData( row );
    } else if ( strategies.size() > 0 ) {
      // if there are multiple strategies, then choose one at random
      retval = strategies.get( random.nextInt( strategies.size() ) ).getData( row );
    }

    // Transform the returned data value
    retval = applyTransforms( retval );

    // set the generated data in the row
    row.set( name, retval );

  }




  @Override
  public List<GenerationStrategy> getStrategies() {
    return strategies;
  }




  @Override
  public void removeStrategy( final GenerationStrategy strategy ) {
    strategies.remove( strategy );
  }




  /**
   * Set the seed for the random number generator.
   * 
   * <p>This can both help ensure more randomness or, if set to a known value,
   * introduce some predictability for testing. Either way, no guarantees can 
   * be made as to the results of setting the seed as it depends on the 
   * implementation in the JVM.</p>
   *  
   * @param seed The new seed value to set in the random number generator.
   */
  protected void setRandomSeed( final long seed ) {
    random.setSeed( seed );
  }
}
