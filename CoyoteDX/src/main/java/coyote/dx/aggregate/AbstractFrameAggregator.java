/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.aggregate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameAggregator;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractFrameAggregator extends AbstractConfigurableComponent implements FrameAggregator {
  protected Evaluator evaluator = new Evaluator();
  protected static final List<DataFrame> EMPTY_LIST = new ArrayList<DataFrame>();




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.context = context;

    // set the transform context in the evaluator so it can resolve variables
    evaluator.setContext(context);
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // no-op implementation
  }




  /**
   * Return the conditional expression from the configuration.
   * 
   * @return the condition which must evaluate to true before the aggregator 
   *         is to execute.
   */
  public String getCondition() {
    if (configuration.containsIgnoreCase(ConfigTag.CONDITION)) {
      return configuration.getString(ConfigTag.CONDITION);
    }
    return null;
  }




  /**
   * @see coyote.dx.FrameAggregator#process(java.util.List, coyote.dx.context.TransactionContext)
   */
  @Override
  public List<DataFrame> process(List<DataFrame> frames, TransactionContext txnContext) {
    List<DataFrame> retval = null;
    if (isEnabled()) {
      if (getCondition() != null) {
        try {
          if (evaluator.evaluateBoolean(getCondition())) {
            retval = aggregate(frames, txnContext);
          } else {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Aggregator.boolean_evaluation_false", getCondition()));
            }
          }
        } catch (final IllegalArgumentException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Aggregator.boolean_evaluation_error", getCondition(), e.getMessage()));
        }
      } else {
        retval = aggregate(frames, txnContext);
      }
    }
    return retval;
  }




  /**
   * Perform the actual aggregation.
   * 
   * @param frames frames to aggregate
   * @param txnContext the transaction context
   * 
   * @return a list of dataframes representing the aggregation. This may be empty but never null.
   */
  protected abstract List<DataFrame> aggregate(List<DataFrame> frames, TransactionContext txnContext);

}
