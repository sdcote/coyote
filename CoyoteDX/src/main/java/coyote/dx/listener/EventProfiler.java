/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;

import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * This listener keeps track of the data read in to and out of the engine and
 * reports on the characteristics of the data observed.
 *
 * <p>This tracks the occurrences of a tracked field providing a variety of information about the tracked field.</p>
 *
 * <pre>
 * "Listener": {
 *   "EventProfiler": { "timestamp": "Time", "track": "Resource", "target": "Resource.txt" },
 *   "EventProfiler": { "timestamp": "Time", "track": "RemoteHostname", "target": "Remote.txt" }
 * }
 * </pre>
 * <p>
 * Target will support "stdout" and 'stderr' as valid location. The result will be the reports will be sent to the console.
 */
public class EventProfiler extends AbstractFileRecorder implements ContextListener {
  private static final int DEFAULT_LIMIT = 25;

  private String timestampFieldName = null;
  private String trackedFieldName = null;
  private EventTracker tracker = null;

  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    timestampFieldName = getString(ConfigTag.TIMESTAMP);
    if (StringUtil.isBlank(timestampFieldName)) {
      throw new ConfigurationException("Null, empty or blank argument for " + ConfigTag.TIMESTAMP + " configuration parameter");
    }

    trackedFieldName = getString(ConfigTag.TRACK);
    if (StringUtil.isBlank(timestampFieldName)) {
      throw new ConfigurationException("Null, empty or blank argument for " + ConfigTag.TRACK + " configuration parameter");
    }

    if (!cfg.contains(ConfigTag.LIMIT)) {
      cfg.set(ConfigTag.LIMIT, DEFAULT_LIMIT);
    }

    // We need UTF-8 encoding.
    super.setCharacterSet(StandardCharsets.UTF_8);
  }


  /**
   * @see AbstractFileRecorder#open(TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    tracker = new EventTracker(trackedFieldName);
    tracker.setLimit(getInteger(ConfigTag.LIMIT));

    // We are expecting an array of strings
    Config section = getConfiguration().getSection(ConfigTag.INCLUDE);
    if (section != null) {
      for (int x = 0; x < section.getFieldCount(); x++) {
        tracker.addIncludePattern(section.getField(x).getStringValue());
      }
    }

    section = getConfiguration().getSection(ConfigTag.EXCLUDE);
    if (section != null) {
      for (int x = 0; x < section.getFieldCount(); x++) {
        tracker.addExcludePattern(section.getField(x).getStringValue());
      }
    }

  }


  /**
   * Process the frame after all other processing has been performed.
   *
   * @see coyote.dx.context.ContextListener#onMap(coyote.dx.context.TransactionContext)
   */
  @Override
  public void onMap(TransactionContext txnContext) {
    DataFrame frame = txnContext.getTargetFrame();
    if( frame != null) {
      Date date = null;

      try {
        date = frame.getAsDate(timestampFieldName);
      } catch (DataFrameException e) {
        Log.error("invalid data received: " + timestampFieldName + " returned " + frame.getField(timestampFieldName).toString());
      }

      if (date != null) {
        try {
          DataField field = frame.getField(trackedFieldName);
          if (field != null) {
            if (field.isNumeric()) {
              tracker.sample(date, frame.getAsDouble(trackedFieldName));
            } else {
              tracker.sample(date, frame.getAsString(trackedFieldName));
            }
          }
        } catch (Throwable e) {
          Log.error("Exception onMap:" + e.getMessage() + "\n" + ExceptionUtil.stackTrace(e));
        }
      } else {
        Log.warn("Could not find tracked field'"+trackedFieldName+"' in "+frame.toString());
      }
    } else{
      Log.debug("onMap: No target frame");
    }
  }


  /**
   * @see AbstractListener#onEnd(OperationalContext)
   */
  @Override
  public void onEnd(OperationalContext opContext) {
    if (opContext instanceof TransformContext) {
      write(tracker.toString());
    }
  }


}