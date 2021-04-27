package coyote.dx.listener;

import coyote.commons.Decimal;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * This is a listener which compares the current frame against the last frame
 * cached from a subsequent read/mapping event or the average of all previous
 * cached frames.
 * 
 * <p>The main use case is for this listener to observe a series of numeric 
 * field values and perform some action when the values differ. The transform 
 * may be a long-running job in a service or handling batch transfers.
 * 
 * <p>The default mode is to compare the current reading against the last. 
 * 
 * <p>Averaging mode averages all prior samples and compares the current 
 * reading against the average. By setting the {@code Average} configuration 
 * attribute to true, the percent increase will be based on the average of all 
 * prior cached samples. The number of prior samples can be limited with the 
 * {@code Limit} configuration parameter with any integer greater than zero 
 * allowed.
 * 
 * <p>The {@code Exceeds} config parameter is required and defines the 
 * percentage of change to check. This is a positive, non-zero value with 1.0 
 * representing 100% and 0.5 representing 50%.
 * 
 * <p>The {@code Condition} expression determines if the listener samples the 
 * packet. Normal value for this expression relate to checkig the existence of 
 * fields.
 * 
 * <p>The {@code Enabled} flag determine if the listener runs at all.
 * 
 * <p>The {@code Field} configuration parameter is the required minimum
 * configuration attribute as it directs the listener to which file to sample.
 * This field must always contain a numeric value.
 * 
 * <p>This listener supports grouping in that separate lists of samples will 
 * be tracked mased on the value of a spacific field. By specifyinf a field 
 * name in the {@code Group} configuration attribute, the listener will track
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
public class PercentChange extends AbstractChangeListener implements ContextListener {

  private static final String EXCEEDS_TAG = "Exceeds";
  private static final String GROUP_TAG = "Group";
  private static final String AVERAGE_TAG = "Average";
  private static final String DECAY_TAG = "Decay";
  
  private static final String CURRENT_SAMPLE = "CurrentSample";
  private static final String BASE_SAMPLE = "BaseSample";
  private static final String CURRENT_SAMPLE_WHOLE = "CurrentSampleWhole";
  private static final String BASE_SAMPLE_WHOLE = "BaseSampleWhole";
  private static final String DIFFERENCE = "Difference";
  private static final String DIFFERENCE_WHOLE = "DifferenceWhole";
  private static final String PERCENTAGE = "Percentage";
  private static final String ABSOLUTE_PERCENTAGE = "AbsolutePercentage";
  private static final String ABSOLUTE_DIFFERENCE = "AbsoluteDifference";
  private static final String ACTION = "Action";
  private static final Object DROPPED = "dropped";
  private static final Object INCREASED = "increased";
  private static final String PERCENT = "Percent";
  private static final String ABSOLUTE_PERCENT = "AbsolutePercent";
  private Decimal sentinel = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(final Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (getConfiguration().containsIgnoreCase(EXCEEDS_TAG)) {
      String percentChange = getString(EXCEEDS_TAG);
      try {
        sentinel = Decimal.valueOf(percentChange);
      } catch (Exception e) {
        throw new ConfigurationException(getClass().getSimpleName() + ": The '" + EXCEEDS_TAG + "' configuration parameter must be a numeric value");
      }
    } else {
      throw new ConfigurationException(getClass().getSimpleName() + " requires the '" + EXCEEDS_TAG + "' configuration parameter");
    }
  }




  /**
   * @see coyote.dx.listener.AbstractChangeListener#sample(coyote.dx.context.TransactionContext)
   */
  @Override
  public void sample(TransactionContext txnContext) {
    DataFrame frame = txnContext.getTargetFrame();
    if (frame != null) {
      if (getFieldName() != null) {
        try {
          Decimal baseSample = null;
          String group = null;
          // if there is a grouping fieldname        
          if (StringUtil.isNotEmpty(getGroupingFieldName())) {
            group = frame.getAsString(getGroupingFieldName());
            if (group != null) {
              if (isAveraging()) {
                Decimal decayFactor = getDecay();
                if (decayFactor != null && !decayFactor.isNaN()) {
                  baseSample = super.getExponentialAverage(decayFactor, group);
                } else {
                  baseSample = getSimpleAverage(group);
                }
              } else {
                baseSample = getLastSample(group);
              }
            } else {
              Log.warn("Could not determine group from field value - skipping");
            }
          } else {
            if (isAveraging()) {
              Decimal decayFactor = getDecay();
              if (decayFactor != null && !decayFactor.isNaN()) {
                baseSample = super.getExponentialAverage(decayFactor, group);
              } else {
                baseSample = getSimpleAverage();
              }
            } else {
              baseSample = getLastSample();
            }
          }

          if (baseSample != null) {
            Decimal currentSample = getTargetFieldValue(frame);
            Decimal difference = currentSample.minus(baseSample);

            Decimal percentage;
            if (difference.isEqual(Decimal.ZERO)) {
              percentage = Decimal.ZERO;
            } else {
              percentage = difference.dividedBy(baseSample);
            }
            Log.debug(group + "  Current: " + currentSample + "  Difference: " + difference + "  Percentage: " + percentage);

            if (percentage.abs().isGreaterThan(sentinel) && (getDirection() == Direction.BOTH || (getDirection() == Direction.UP && difference.isGreaterThan(Decimal.ZERO)) || (getDirection() == Direction.DOWN && difference.isLessThan(Decimal.ZERO)))) {
              DataFrame taskframe = (DataFrame)frame.clone();
              taskframe.put(GROUP_TAG, group);
              taskframe.put(CURRENT_SAMPLE, currentSample.toDouble());
              taskframe.put(BASE_SAMPLE, baseSample.toDouble());
              taskframe.put(CURRENT_SAMPLE_WHOLE, currentSample.getWholePart().toDouble());
              taskframe.put(BASE_SAMPLE_WHOLE, baseSample.getWholePart().toDouble());
              taskframe.put(DIFFERENCE, difference.toDouble());
              taskframe.put(DIFFERENCE_WHOLE, difference.getWholePart().toDouble());
              taskframe.put(PERCENTAGE, percentage.toDouble());
              taskframe.put(PERCENT, percentage.multipliedBy(Decimal.HUNDRED).getWholePart().toDouble());
              taskframe.put(ABSOLUTE_PERCENT, percentage.multipliedBy(Decimal.HUNDRED).getWholePart().abs().toDouble());
              taskframe.put(ABSOLUTE_PERCENTAGE, percentage.abs().toDouble());
              taskframe.put(ABSOLUTE_DIFFERENCE, difference.abs().toDouble());
              if (difference.isLessThan(Decimal.ZERO)) {
                taskframe.put(ACTION, DROPPED);
              } else {
                taskframe.put(ACTION, INCREASED);
              }
              executeTask(taskframe);
            }

          } else {
            Log.error("No last sample");
          }

        } catch (final Throwable ball) {
          Log.error(ExceptionUtil.stackTrace(ball));
        }
      }

    } else {
      Log.error("No frame to sample");
    }
  }




  /**
   * @return
   */
  private Decimal getDecay() {
    Decimal retval = null;
    if (getConfiguration().containsIgnoreCase(DECAY_TAG)) {
      retval = Decimal.valueOf(getString(DECAY_TAG));
    }
    return retval;
  }




  private boolean isAveraging() {
    boolean retval = false;
    if (getConfiguration().containsIgnoreCase(AVERAGE_TAG)) {
      retval = getBoolean(AVERAGE_TAG);
    }
    return retval;
  }

}
