/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.util.List;

import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;


/**
 * Frame aggregators take in one or more frames and subsequently emit one or 
 * more frames representing the results of the aggregation.
 * 
 * <p>The most common use cases for aggregators are sorting, summary and 
 * analytic aggregators.
 *  
 * <p>Sorting aggregators take in all the frames and emit all collected frames 
 * in a specific order when the last frame flag is observed in the transaction 
 * context. 
 * 
 * <p>Summary aggregators will collect all the frames and emit a set of frames 
 * representing a summary of the data observered. An example of a summary 
 * aggregator will be to emit one frame representing the totals of specific 
 * fields in all the working frames observed. 
 * 
 * <p>Analytic aggregators process all observed frames and emit frames 
 * represent the results of analytic processing. An analytic aggregator may 
 * continually process frames and only emit a frame with a specific condition 
 * is observed. A rate of change aggregator will only emit a frame when the 
 * rate of change of some field breaches some configured threshhold.
 * 
 * <p>Aggregator are related to listeners in that they observed all frames. 
 * But unlike a listener, aggregators may filter out and completely change the 
 * working frame in the transaction context, where listeners normally do not 
 * alter data in the transaction context and only perform processing external 
 * to that context. Aggregators inherently control the flow of data to the 
 * writers where listeners do not.
 */
public interface FrameAggregator extends ConfigurableComponent {

  /**
   * Process the given frame for the purposes of creating one or more summary 
   * frames.
   * 
   * <p>Some aggregators will consume all the frames, storing them in memory 
   * or disk and emit them out at a later time in a particular order. Consider 
   * an aggregator which sorts all the frames. All frames will be heald until 
   * the transaction context indicates the last frame at which time the 
   * sorting aggregator will release all previously received frames in some 
   * logical order.
   * 
   * <p>If the transaction context indicates the last frame, the aggregator is 
   * expected to process the given frame and emit a summary frame.
   * 
   * @param frames the list of frames to process
   * @param txnContext the transaction context in which the frame is to be 
   *        processed
   * 
   * @return a frame representing the aggregation of prior frames, or null if 
   *         the given frame has been consumed as part of a later aggregated 
   *         frame.
   */
  public List<DataFrame> process(List<DataFrame> frames, TransactionContext txnContext);

}
