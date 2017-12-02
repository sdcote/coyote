/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.aggregate;

import java.util.List;

import coyote.dataframe.DataFrame;
import coyote.dx.FrameAggregator;
import coyote.dx.context.TransactionContext;
import coyote.loader.log.Log;


/**
 * 
 */
public class DebugAggregator extends AbstractFrameAggregator implements FrameAggregator {

  /**
   * @param frames
   * @param txnContext
   * @return
   */
  @Override
  protected List<DataFrame> aggregate(List<DataFrame> frames, TransactionContext txnContext) {
    Log.debug("Aggregating " + frames.size() + " frames. LastFrame=" + txnContext.isLastFrame());
    return frames;
  }

}
