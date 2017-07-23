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
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;


/**
 * Filters are called to reject specific records. This is commonly performed 
 * with one or more Reject filters. If not specifically rejected, the record 
 * passes. 
 * 
 * The following is an example of a configuration to accept only a specific 
 * case and reject all others:<pre>
 * "Filter": {
 *     "Accept": { "Condition": "match(Working.Record Type,LN)" }
 *     "Reject": { "note": "This filter will result is all other record types being rejected." }
 * }</pre>
 * There can be multiple Accept filters to accept multiple conditions with the 
 * final Reject eliminating all others.
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
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    evaluator.setContext( context );

    String token = getConfiguration().getString( ConfigTag.CONDITION );
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
   * @see coyote.dx.FrameFilter#process(coyote.dx.context.TransactionContext)
   */
  @Override
  public boolean process( TransactionContext context ) {
    return false;
  }

}
