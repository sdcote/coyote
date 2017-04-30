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
package coyote.dx.filter;

import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameFilter;
import coyote.dx.TransactionContext;
import coyote.dx.TransformContext;
import coyote.dx.eval.Evaluator;


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
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    evaluator.setContext( context );

    String token = findString( ConfigTag.CONDITION );
    if ( StringUtil.isNotBlank( token ) ) {
      expression = token.trim();

      try {
        evaluator.evaluateBoolean( expression );
      } catch ( IllegalArgumentException e ) {
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
   * @see coyote.dx.FrameFilter#process(coyote.dx.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) {
    return false;
  }

}
