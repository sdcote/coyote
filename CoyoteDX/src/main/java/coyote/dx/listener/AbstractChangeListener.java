/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.Decimal;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Base class for change listeners which need a previous value cache.
 *
 * <p>Consider a listener which is monitoring for a rate of change over a
 * number of periods. This class handles the previous period values in a
 * uniform manner.
 * 
 * <p>The {@code field} configuration parameter is the required minimum
 * configuration attribute as it directs the listener to which file to sample.
 * This field must always contain a numeric value.
 * 
 * <p>This listener supports grouping in that separate lists of samples will 
 * be tracked mased on the value of a spacific field. By specifyinf a field 
 * name in the {@code group} configuration attribute, the listener will track
 * data in a group named by that fields value. For example, if a data transfer 
 * job is streaming in metrics from a device, and the identifier of that 
 * device is specified in the {@code ID} field, metrics will be collected and
 * tracked separately for each identified device. <tt>"Group":"ID"</tt> 
 * instructs the listener to keep a separate list of samples for each device 
 * identifier in the {@code ID} field. When grouping is enabled, global values
 * are not tracked as this would double the memory requirements. If a complete
 * sample list is required, a separate listener with no grouping should be 
 * used with no grouping specified.
 */
public abstract class AbstractChangeListener extends AbstractMonitoringListener implements ContextListener {

  private static final Decimal EA_FACTOR = Decimal.valueOf("0.25");
  private static final String DEFAULT_GROUP = "DEFAULT";
  private int maxSampleSize = Integer.MAX_VALUE;
  private String fieldName = null;
  private String groupingFieldName = null;
  private final Map<String, List<Decimal>> sampleMap = new HashMap<>();


  



  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(final Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    String fname = getConfiguration().getString(ConfigTag.FIELD);
    if (StringUtil.isBlank(fname)) {
      fname = getConfiguration().getString(ConfigTag.TARGET);
    }
    if (StringUtil.isNotBlank(fname)) {
      setFieldName(fname.trim());
    }

    String gname = getConfiguration().getString(ConfigTag.GROUP);
    if (StringUtil.isNotBlank(gname)) {
      setGroupingFieldName(gname.trim());
    }

  }





  /**
   * @param value the value to add to the sample list.
   *
   * @return the value removed from the sample list due to size limitation.
   *         May be null if there is no maximum size, the maximum size has
   *          not been reached or the value eas null.
   */
  private Decimal add(final Decimal value, final String group) {
    Decimal retval = null;
    if (value != null) {
      final List<Decimal> samples = getOrCreateSampleList(group);
      samples.add(value);
      while (samples.size() > maxSampleSize) {
        retval = samples.remove(0);
      }
    }
    return retval;
  }




  /**
   * Retrieve the average of the samples decaying the significance of each
   * older sample by the given factor.
   *
   * <p>This gives an average of all the samples weighing more recent samples
   * higher than older values. This is commonly referred to as an exponential
   * moving average or EMA. This is in contrast with a simple moving average
   * or SMA where each sample is weighted the same regardless of its age.
   *
   * <p>This returns the exponential average over the existing samples. The
   * most recent value is not affected by the given decay factor.
   *
   * @return the average over the past samples decaying the significance of
   *         each by 25%. The decay factor will never reach 1.
   */
  protected Decimal getExponentialAverage() {
    return getExponentialAverage(EA_FACTOR, DEFAULT_GROUP);
  }




  protected Decimal getExponentialAverage(final Decimal decayFactor) {
    return getExponentialAverage(decayFactor, DEFAULT_GROUP);
  }




  /**
   * Retrieve the average of the samples decaying the significance of each
   * older sample by the given factor.
   *
   * <p>This gives an averate of all the samples weighing more recent samples
   * higher than older values. This is commonly referred to as an exponential
   * moving average or EMA. This is in contrast with a simple moving average
   * or SMA.
   *
   * <p>This returns the exponential average over the existing samples. The
   * most recent value is not affected by the given decay factor.
   *
   * @param decayFactor the decay rate for each successive prior sample.
   *
   * @return the average over the past samples decaying the significance of
   *         each. The decay factor will never reach 1.
   */
  protected Decimal getExponentialAverage(final Decimal decayFactor, final String group) {
    Decimal retval = null;
    final List<Decimal> samples = getOrCreateSampleList(group);
    for (int x = samples.size() - 1; x >= 0; x--) {
      final Decimal value = samples.get(x);
      if (retval == null) {
        retval = value;
        continue;
      } else {
        retval = retval.plus(decayFactor.multipliedBy(value.minus(retval)));
      }
    }
    return retval;
  }




  /**
   * @return the name of the field being sampled
   */
  protected String getFieldName() {
    return fieldName;
  }




  /**
   * @return the groupingFieldName
   */
  protected String getGroupingFieldName() {
    return groupingFieldName;
  }




  /**
   * @return the maximum size of the sample array
   */
  public int getMaximumSampleSize() {
    return maxSampleSize;
  }




  /**
   * @param groupName
   * @return
   */
  private List<Decimal> getOrCreateSampleList(final String groupName) {
    List<Decimal> retval = null;
    if (StringUtil.isNotEmpty(groupName)) {
      retval = sampleMap.get(groupName);
      if (retval == null) {
        retval = new ArrayList<Decimal>();
        sampleMap.put(groupName, retval);
      }
    }
    return retval;
  }




  protected Decimal getSample(final int index, final String group) {
    Decimal retval = null;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (index < samples.size()) {
      retval = samples.get(index);
    }
    return retval;
  }




  /**
   * @return the current size of the sample array
   */
  public int getSampleSize(final String group) {
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples != null) {
      return samples.size();
    } else {
      return 0;
    }
  }




  protected Decimal getSimpleAverage() {
    return getSimpleAverage(DEFAULT_GROUP);
  }




  protected Decimal getSimpleAverage(final String group) {
    Decimal retval = Decimal.ZERO;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples.size() > 0) {
      Decimal sum = Decimal.ZERO;
      for (final Decimal value : samples) {
        sum = sum.plus(value);
      }
      retval = sum.dividedBy(Decimal.valueOf(samples.size()));
    }
    return retval;
  }




  protected Decimal getMinimum() {
    return getMinimum(DEFAULT_GROUP);
  }




  protected Decimal getMinimum(final String group) {
    Decimal retval = Decimal.NaN;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples.size() > 0) {
      Decimal min = Decimal.NaN;
      for (final Decimal value : samples) {
        if (min.isNaN() || value.isLessThan(min)) {
          min = value;
        }
      }
      retval = min;
    }
    return retval;
  }




  protected Decimal getMaximum() {
    return getMaximum(DEFAULT_GROUP);
  }




  protected Decimal getMaximum(final String group) {
    Decimal retval = Decimal.NaN;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples.size() > 0) {
      Decimal max = Decimal.NaN;
      for (final Decimal value : samples) {
        if (max.isNaN() || value.isGreaterThan(max)) {
          max = value;
        }
      }
      retval = max;
    }
    return retval;
  }




  protected Decimal getTotal() {
    return getTotal(DEFAULT_GROUP);
  }




  protected Decimal getTotal(final String group) {
    Decimal retval = Decimal.ZERO;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples.size() > 0) {
      Decimal sum = Decimal.ZERO;
      for (final Decimal value : samples) {
        sum = sum.plus(value);
      }
      retval = sum;
    }
    return retval;
  }




  /**
   * @see coyote.dx.listener.AbstractListener#onMap(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onMap(final TransactionContext txnContext) {
    
    
    if (isEnabled()) {
      if (getCondition() != null) {
        try {
          if (evaluator.evaluateBoolean(getCondition())) {
            sample(txnContext);
            localSample(txnContext);
          } else {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Task.boolean_evaluation_false", getCondition()));
            }
          }
        } catch (final IllegalArgumentException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Task.boolean_evaluation_error", getCondition(), e.getMessage()));
        }
      } else {
        sample(txnContext);
        localSample(txnContext);
      }
    }
    
    
  }




  /**
   * @param txnContext
   */
  protected void sample(TransactionContext txnContext) {
    // to be overridden by subclasses
  }





  /**
   * @param txnContext
   */
  private void localSample(TransactionContext txnContext) {
    if (getFieldName() != null) {
      try {
        if (StringUtil.isNotEmpty(getGroupingFieldName())) {
          String group = txnContext.getTargetFrame().getAsString(getGroupingFieldName());
          if (group != null) {
            sample(txnContext.getTargetFrame().getAsString(getFieldName()), group);
          }
        } else {
          sample(txnContext.getTargetFrame().getAsString(getFieldName()));
        }
      } catch (final Throwable ball) {
        Log.error(ExceptionUtil.stackTrace(ball));
      }
    }
  }
  
  
  





  /**
   * @see coyote.dx.listener.AbstractListener#preload(coyote.dataframe.DataFrame)
   */
  @Override
  public void preload(final DataFrame frame) {
    if (getFieldName() != null) {
      try {
        if (StringUtil.isNotEmpty(getGroupingFieldName())) {
          String group = frame.getAsString(getGroupingFieldName());
          if (group != null) {
            sample(frame.getAsString(getFieldName()), group);
          }
        } else {
          sample(frame.getAsString(getFieldName()));
        }
      } catch (final Throwable ball) {
        Log.error(ExceptionUtil.stackTrace(ball));
      }
    }
  }




  /**
   * @param value the value to add to the sample list.
   *
   * @return the value removed from the sample list due to size limitation.
   *         May be null if there is no maximum size, the maximum size has
   *          not been reached or the value eas null.
   */
  protected Decimal sample(final Decimal value, final String group) {
    return add(value, group);
  }




  /**
   * @param value the value to add to the sample list.
   *
   * @return the value removed from the sample list due to size limitation.
   *         May be null if there is no maximum size, the maximum size has
   *          not been reached or the value eas null.
   */
  protected Decimal sample(final double value) {
    return add(Decimal.valueOf(value), DEFAULT_GROUP);
  }




  protected Decimal sample(final double value, final String group) {
    return add(Decimal.valueOf(value), group);
  }




  /**
   * @param value the value to add to the sample list.
   *
   * @return the value removed from the sample list due to size limitation.
   *         May be null if there is no maximum size, the maximum size has
   *          not been reached or the value eas null.
   */
  protected Decimal sample(final int value) {
    return add(Decimal.valueOf(value), DEFAULT_GROUP);
  }




  protected Decimal sample(final int value, final String group) {
    return add(Decimal.valueOf(value), group);
  }




  /**
   * @param value the value to add to the sample list.
   *
   * @return the value removed from the sample list due to size limitation.
   *         May be null if there is no maximum size, the maximum size has
   *          not been reached or the value eas null.
   */
  protected Decimal sample(final long value) {
    return add(Decimal.valueOf(value), DEFAULT_GROUP);
  }




  protected Decimal sample(final long value, final String group) {
    return add(Decimal.valueOf(value), group);
  }




  /**
   * Add the numeric value (double, int, or long) represented by the given
   * string to the list of samples.
   *
   * @param value the value to add to the sample list.
   *
   * @return the value removed from the sample list due to size limitation.
   *         May be null if there is no maximum size, the maximum size has
   *          not been reached or the value eas null.
   */
  protected Decimal sample(final String value) {
    return sample(value, DEFAULT_GROUP);
  }




  protected Decimal sample(final String value, final String group) {
    Decimal retval = null;
    if (StringUtil.isNotBlank(value)) {
      String str = value.replace(",", "");
      str = str.replace(" ", "");
      if (StringUtil.isEmpty(group)) {
        retval = add(Decimal.valueOf(str), DEFAULT_GROUP);
      } else {
        retval = add(Decimal.valueOf(str), group);
      }
    }
    return retval;
  }



  /**
   * @param name the name of the field being sampled
   */
  protected void setFieldName(final String name) {
    fieldName = name;
  }




  /**
   * @param groupingFieldName the groupingFieldName to set
   */
  protected void setGroupingFieldName(final String groupingFieldName) {
    this.groupingFieldName = groupingFieldName;
  }




  /**
   * @param size the maximum size of the sample array
   */
  public AbstractChangeListener setMaximumSampleSize(final int size) {
    if (size > 0) {
      maxSampleSize = size;
    } else {
      maxSampleSize = 1;
    }
    return this;
  }




  /**
   * @return
   */
  protected Decimal getLastSample() {
    return getLastSample(DEFAULT_GROUP);
  }




  /**
   * @param group
   * @return
   */
  protected Decimal getLastSample(String group) {
    Decimal retval = Decimal.NaN;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples.size() > 0) {
      retval = samples.get(samples.size() - 1);
    }
    return retval;
  }




  /**
   * Retrieve the first (oldest) sample in our list.
   * 
   * <p><strong>NOTE:</strong> If there is a maximum size set in the listener, 
   * the first sample will be the oldest, but may not be the first as samples 
   * may heve been removed to keep the size of the samples limited to the 
   * configured maximum.
   * 
   * @return the oldest sample in the cache.
   */
  protected Decimal getFirstSample() {
    return getFirstSample(DEFAULT_GROUP);
  }




  /**
   * Retrieve the first (oldest) sample in the given group list.
   * 
   * <p><strong>NOTE:</strong> If there is a maximum size set in the listener, 
   * the first sample will be the oldest, but may not be the first as samples 
   * may heve been removed to keep the size of the samples limited to the 
   * configured maximum.
   * 
   * @param group the group name of the sample list to check
   * 
   * @return the oldest sample in the cache for the given group.
   */
  protected Decimal getFirstSample(String group) {
    Decimal retval = Decimal.NaN;
    final List<Decimal> samples = getOrCreateSampleList(group);
    if (samples.size() > 0) {
      retval = samples.get(0);
    }
    return retval;
  }




  /**
   * Retrive the target field value from the frame as a decimal.
   * 
   * <p>The name of the field is determined by our {@code field} configuration
   * value.
   * 
   * @param frame the frame from which to retrieve the named field.
   * 
   * @return a Decimal representation of the configured target field.
   */
  protected Decimal getTargetFieldValue(DataFrame frame) {
    Decimal retval = Decimal.NaN;
    String value = frame.getAsString(getFieldName());
    if (StringUtil.isNotBlank(value)) {
      String str = value.replace(",", "");
      str = str.replace(" ", "");
      try {
        retval = Decimal.valueOf(str);
      } catch (Exception e) {
        Log.warn("The value could not be parsed into a decimal: '"+value+"'");
      }
    }
    return retval;
  }

}
