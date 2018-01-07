/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.aggregate;

import java.util.ArrayList;
import java.util.List;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameAggregator;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;


/**
 * One of the more generic aggregators, simply limits the size of the output 
 * frames.
 * 
 * <p>No grouping or sorting is performed. This simply keeps the last (limit)
 * number of frames.
 * 
 * <p>A sample configuration is as follows:<pre>
 * "Aggregator": { "class": "Size", "limit": 24 }</pre>
 */
public class Size extends AbstractFrameAggregator implements FrameAggregator {

  private static final String LIMIT = "Limit";
  private int limit = 0;
  private List<DataFrame> frameList = new ArrayList<>();




  /**
   * @see coyote.dx.aggregate.AbstractFrameAggregator#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    String value = getString(LIMIT);
    if (StringUtil.isNotBlank(value)) {
      try {
        limit = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        Log.error("Could not parse limit config (" + LIMIT + ") into an integer - value: '" + value + "' - no limit will be imposed");
      }
    }
  }




  /**
   * @see coyote.dx.aggregate.AbstractFrameAggregator#aggregate(java.util.List, coyote.dx.context.TransactionContext)
   */
  @Override
  protected List<DataFrame> aggregate(List<DataFrame> frames, TransactionContext txnContext) {
    List<DataFrame> retval = new ArrayList<>();
    for (int x = 0; x < frames.size(); x++) {
      frameList.add(frames.get(x));
      while (limit > 0 && frameList.size() > limit) {
        frameList.remove(0);
      }
    }

    if (txnContext.isLastFrame()) {
      retval = frameList;
    }

    return retval;
  }

}
