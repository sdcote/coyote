/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.ConfigTag;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;


/**
 * Base class for all frame writers
 */
public abstract class AbstractFrameWriter extends AbstractConfigurableComponent implements FrameWriter {
  protected Evaluator evaluator = new Evaluator();
  protected String expression = null;




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.context = context;

    // Look for a conditional statement the writer may use to control if it is 
    // to write the record or not
    expression = getConfiguration().getString( ConfigTag.CONDITION );
    if ( StringUtil.isNotBlank( expression ) ) {
      expression = expression.trim();

      try {
        evaluator.evaluateBoolean( expression );
      } catch ( final IllegalArgumentException e ) {
        context.setError( "Invalid boolean expression in writer: " + e.getMessage() );
      }
    }

  }




  /**
   * @return the target URI to which the writer will write
   */
  public String getTarget() {
    return configuration.getAsString( ConfigTag.TARGET );
  }




  /**
   * Set the URI to where the write will write its data.
   * 
   * @param value the URI to where the writer should write its data
   */
  public void setTarget( final String value ) {
    configuration.put( ConfigTag.TARGET, value );
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}




  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {}

}
