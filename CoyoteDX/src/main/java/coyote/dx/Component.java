/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx;

import java.io.Closeable;

import coyote.dx.context.TransformContext;


/**
 * 
 */
public interface Component extends Closeable {

  /**
   * Open the component using the given transformation context.
   * 
   * <p>Components can use the data in the context to alter their operation at 
   * the last moment and initialize themselves using data from pervious 
   * operations.</p>
   *  
   * @param context The transformation context in which this component should 
   *        be opened.
   */
  public void open( TransformContext context );




  /**
   * Accessor to the operational context of this component.
   * 
   * @return the transform context in which this component operates.
   */
  public TransformContext getContext();

}
