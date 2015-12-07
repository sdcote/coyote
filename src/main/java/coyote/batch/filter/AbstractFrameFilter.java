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
package coyote.batch.filter;

import java.io.IOException;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.Component;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameFilter;
import coyote.batch.TransformContext;
import coyote.batch.eval.EvaluationException;
import coyote.batch.eval.Evaluator;
import coyote.commons.StringUtil;


/**
 * 
 */
public abstract class AbstractFrameFilter extends AbstractConfigurableComponent implements FrameFilter, ConfigurableComponent {

  protected Evaluator evaluator = new Evaluator();
  protected String expression = null;
  
  
  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    evaluator.setContext( context );

    String token = findString( ConfigTag.CONDITION );
    if ( StringUtil.isNotBlank( token ) ) {
      expression = token.trim();

      try {
        evaluator.evaluateBoolean( expression );
      } catch ( EvaluationException e ) {
        context.setError( "Invalid boolean exception in Reject filter: " + e.getMessage() );
      }
    }

  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}

}
