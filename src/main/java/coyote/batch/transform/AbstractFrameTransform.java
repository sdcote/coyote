/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.transform;

import java.io.IOException;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameTransform;
import coyote.batch.TransformContext;


/**
 * Base class for frame transformers
 * 
 * <p>This class works like a clone operation except each field is checked for 
 * a name matching the pattern of a transform action. When it matches, the 
 * value is passed to the transform action for processing.</p>
 * 
 * <p>A common use case for frame transformation is encryption of data. Fields 
 * are stored and transferred in an encrypted format, but need to be decrypted 
 * before use.</p>
 * 
 * <p>Another use case for the transform is collecting metrics on the frames 
 * observed and aggregating values for post processing and context listeners to 
 * report.</p>
 */
public abstract class AbstractFrameTransform extends AbstractConfigurableComponent implements FrameTransform, ConfigurableComponent {

  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.setContext( context );

  }




  /**
   * Resolve the argument.
   * 
   * <p>This has the transform context resolve the argument.</p>
   * 
   * @param value the value to resolve (or use as a literal)
   * 
   * @return the resolved value of the argument. 
   */
  protected String resolveArgument( final String value ) {
    return context.resolveArgument( value );
  }

}
