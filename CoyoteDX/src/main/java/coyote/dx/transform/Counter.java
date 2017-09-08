/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * Place a sequential number in the named field.
 * 
 * <p>This can be configured thusly:<pre>
 * "Counter": { "field": "RecordNumber"}
 * "Counter": { "field": "RecordNumber", "start": 10000}
 * "Counter": { "field": "RecordNumber", "step": 5}
 * "Counter": { "field": "RecordNumber", "stop": 99999999}</pre>
 */
public class Counter extends AbstractFieldTransform implements FrameTransform {
  private static final String STEP = "step";
  private long step = 1L;
  private long start = 0L;
  private long stop = Long.MAX_VALUE;
  private long counter = 0;




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (cfg.containsIgnoreCase(STEP)) {
      try {
        setStep(cfg.getLong(STEP));
      } catch (Throwable ball) {
        throw new ConfigurationException("Invalid long value");
      }
    }

    if (cfg.containsIgnoreCase(ConfigTag.START)) {
      try {
        setStart(cfg.getLong(ConfigTag.START));
      } catch (Throwable ball) {
        throw new ConfigurationException("Invalid long value");
      }
    }

    if (cfg.containsIgnoreCase(ConfigTag.STOP)) {
      try {
        setStop(cfg.getLong(ConfigTag.STOP));
      } catch (Throwable ball) {
        throw new ConfigurationException("Invalid long value");
      }
    }
  }




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    counter = start;
  }




  /**
   * @see coyote.dx.transform.AbstractFieldTransform#performTransform(coyote.dataframe.DataFrame)
   */
  @Override
  protected DataFrame performTransform(DataFrame frame) throws TransformException {
    DataFrame retval = frame;
    retval.put(getFieldName(), counter);
    if (stop - step > counter) {
      counter += step;
    } else {
      counter = start;
    }
    return retval;
  }




  private void setStep(long step) {
    this.step = step;
  }




  private void setStart(long start) {
    this.start = start;
  }




  private void setStop(long stop) {
    this.stop = stop;
  }

}
