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
package coyote.testdata.transform;

/**
 * Transforms normally take an object and transform it.
 */
public interface DataTransform {

  /**
   * Transform the given object.  
   * 
   * <p>If the transformation strategy cannot transform the object, for whatever 
   * reason, the original object reference is returned.</p>
   * 
   * @param value the object to transform
   * 
   * @return the transformed bject
   */
  public Object transform( Object value );

}
