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
import coyote.commons.StringUtil;
import coyote.commons.template.Template;


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
   * Resolve the argument.
   * 
   * <p>This will try to retrieve the value from the transform context using 
   * the given value as it may be a reference to a context property.</p>
   * 
   * <p>If no value was found in the look-up, then the value is treated as a 
   * literal and will be returned as the argument.</p>
   * 
   * <p>Regardless of whether or not the value was retrieved from the 
   * transform context as a reference value, the value is resolved as a 
   * template using the symbol table in the transform context. This allows for 
   * more dynamic values during the operation of the entire transformation 
   * process.</p>
   * 
   * @param value the value to resolve (or use as a literal)
   * 
   * @return the resolved value of the argument. 
   */
  protected String resolveArgument( String value ) {
    String retval = null;

    // lookup the value in the transform context
    String cval = context.getAsString( value );

    // If the lookup failed, just use the value
    if ( StringUtil.isBlank( cval ) ) {
      cval = value;
    }

    // in case it is a template, resolve it to the context's symbol table
    if ( StringUtil.isNotBlank( cval ) ) {
      retval = Template.resolve( cval, context.getSymbols() );
    }
    return retval;
  }

  
  
  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.setContext( context );
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}

}
