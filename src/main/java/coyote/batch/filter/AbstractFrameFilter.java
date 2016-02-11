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
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameFilter;
import coyote.batch.TransactionContext;
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




  public AbstractFrameFilter() {}




  /**
   * Constructor with a conditional expression
   * 
   * @param condition the conditional expression which must be met for this filter to fire
   */
  public AbstractFrameFilter( String condition ) {
    expression = condition;
  }




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

    // TODO: support the log flag to have the filter generate a log entry when fired...helps with debugging

  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}




  /**
   * @return the conditional expression which must be met for this filter to fire
   */
  public String getCondition() {
    return expression;
  }




  /**
   * @param condition the conditional expression which must be met for this filter to fire
   */
  public void setCondition( String condition ) {
    expression = condition;
  }




  /**
   * @see coyote.batch.FrameFilter#process(coyote.batch.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) {
    return false;
  }

}
