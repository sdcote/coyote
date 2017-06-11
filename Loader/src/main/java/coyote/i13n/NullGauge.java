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
 * The NullGauge class models a gauge that does nothing.
 */
public class NullGauge implements Gauge {

  protected String name = null;
  protected static final DataFrame EMPTY_FRAME = new DataFrame();




  public NullGauge( final String name ) {
    this.name = name;
  }




  /**
   * @see coyote.i13n.Gauge#getAvgValuePerSecond()
   */
  @Override
  public float getAvgValuePerSecond() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getElapsedSeconds()
   */
  @Override
  public float getElapsedSeconds() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getLastValuePerSecond()
   */
  @Override
  public long getLastValuePerSecond() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getMaxValuePerSecond()
   */
  @Override
  public long getMaxValuePerSecond() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getMinuteTotal()
   */
  @Override
  public long getMinuteTotal() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getMinValuePerSecond()
   */
  @Override
  public long getMinValuePerSecond() {
    return 0;
  }




  /**
   * @see  coyote.i13n.Gauge#getName()
   */
  @Override
  public String getName() {
    return name;
  }




  /**
   * @see coyote.i13n.Gauge#getTotal()
   */
  @Override
  public long getTotal() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getValuePerMinute()
   */
  @Override
  public float getValuePerMinute() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#getValuePerSecond()
   */
  @Override
  public float getValuePerSecond() {
    return 0;
  }




  /**
   * @see coyote.i13n.Gauge#reset()
   */
  @Override
  public void reset() {}




  /**
   * <p>Last profiler metric: 0.000443 ms per call - 2,257,336cps
   *
   * @see coyote.i13n.Gauge#update(long)
   */
  @Override
  public void update( final long val ) {}




  /**
   * @see coyote.i13n.Gauge#toFrame()
   */
  @Override
  public DataFrame toFrame() {
    return EMPTY_FRAME;
  }

}
