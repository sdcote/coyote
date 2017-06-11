/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.i13n;

import coyote.dataframe.DataFrame;


/**
 * The Gauge interface models a component that is designed to be updated
 * regularly to show performance rates.
 */
public interface Gauge {

  /**
   * @return the average value per second
   */
  public abstract float getAvgValuePerSecond();




  public abstract float getElapsedSeconds();




  public abstract long getLastValuePerSecond();




  /**
   * @return Returns the maximum values per second for this gauge.
   */
  public abstract long getMaxValuePerSecond();




  /**
   * @return the accumulated values for the past 60 seconds.
   */
  public abstract long getMinuteTotal();




  /**
   * @return Returns the minimum values per second for this gauge.
   */
  public abstract long getMinValuePerSecond();




  public abstract String getName();




  /**
   * @return the sum of all values updated to this gauge.
   */
  public abstract long getTotal();




  public abstract float getValuePerMinute();




  public abstract float getValuePerSecond();




  public void reset();




  public void update( long val );




  /**
   * @return a frame representing this gauge
   */
  public DataFrame toFrame();

}