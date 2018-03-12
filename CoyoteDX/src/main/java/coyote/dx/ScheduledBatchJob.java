/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import coyote.commons.CronEntry;
import coyote.commons.ExceptionUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.Context;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.thread.ScheduledJob;


/**
 * This is a wrapper around a (Job) component which will be called repeatedly 
 * on a schedule.
 */
public class ScheduledBatchJob extends ScheduledJob implements ManagedComponent {

  TransformEngine engine = null;
  Config configuration = null;
  CronEntry cronentry = null;
  Context context = null;
  private Loader loader = null;




  /**
   * Set the configuration of this component.
   * 
   * @see coyote.loader.component.ManagedComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config config) {
    configuration = config;

    if (configuration != null) {
      Log.debug(config.toFormattedString());

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance(config);

      engine.setLoader(getLoader());

      if (StringUtil.isBlank(engine.getName())) {
        Log.trace(LogMsg.createMsg(CDX.MSG, "Job.unnamed_engine_configured"));
        name = GUID.randomGUID().toString();
      } else {
        Log.trace(LogMsg.createMsg(CDX.MSG, "Job.engine_configured", engine.getName()));
        super.setName(engine.getName());
      }
    } else {
      Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.no_job_section"));
    }

    // Setup the schedule - 
    List<Config> cfgs = config.getSections(ConfigTag.SCHEDULE);
    if (cfgs.size() > 0) {
      Config scheduleCfg = cfgs.get(0);
      cronentry = new CronEntry();

      // go through each in order, this allows the user to determine how 
      // attributes are applied by processing them in order they appear and 
      // overwriting previous attributes.
      for (DataField field : scheduleCfg.getFields()) {
        if (StringUtil.equalsIgnoreCase(ConfigTag.PATTERN, field.getName())) {
          try {
            cronentry = CronEntry.parse(field.getStringValue());
          } catch (ParseException e) {
            Log.error(LogMsg.createMsg(CDX.MSG, "Job.schedule_patterm_parse_error", e.getMessage()));
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.MINUTES, field.getName())) {
          cronentry.setMinutePattern(field.getStringValue());
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.HOURS, field.getName())) {
          cronentry.setHourPattern(field.getStringValue());
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.MONTHS, field.getName())) {
          cronentry.setMonthPattern(field.getStringValue());
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.DAYS, field.getName())) {
          cronentry.setDayPattern(field.getStringValue());
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.DAYS_OF_WEEK, field.getName())) {
          cronentry.setDayOfWeekPattern(field.getStringValue());
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.MILLIS, field.getName())) {
          long millis = 0;
          try {
            millis = Long.parseLong(field.getStringValue());
            setExecutionInterval(millis);
          } catch (NumberFormatException e) {
            Log.error(LogMsg.createMsg(CDX.MSG, "Job.schedule_interval_parse_error", e.getMessage()));
          }
        }
      }
    }

    if (cronentry != null) {
      setRepeatable(true);
      setExecutionTime(cronentry.getNextTime());
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug(cronentry.toString());
      }
    } else {
      // no cron entry implies the job runs continually in its own thread - e.g. HTTP listeners
      Log.debug(LogMsg.createMsg(CDX.MSG, "Job.schedule_no_cron_entry", getExecutionInterval()));
      setRepeatable(false);
      setExecutionTime(System.currentTimeMillis() + 1000);
    }

  }




  /**
   * @see coyote.loader.thread.ScheduledJob#getExecutionInterval()
   */
  @Override
  public long getExecutionInterval() {
    if (cronentry != null) {
      return cronentry.getNextInterval();
    } else {
      return super.getExecutionInterval();
    }
  }




  @Override
  public Config getConfiguration() {
    return configuration;
  }




  public TransformEngine getEngine() {
    return engine;
  }




  /**
   * @see coyote.loader.thread.ThreadJob#initialize()
   */
  @Override
  public void initialize() {
    setActiveFlag(true);
    Log.trace("Initializing job " + getName() + " engine:" + engine.getName());
  }




  /**
   * @see coyote.loader.thread.ThreadJob#doWork()
   */
  @Override
  public void doWork() {

    if (engine != null) {
      Log.trace(LogMsg.createMsg(CDX.MSG, "Job.running", getName(), engine.getName()));

      // this sets our execution time to the exact millisecond based on when 
      // this job ACTUALLY ran. This is to ensure slow running jobs don't cause
      // execution delays
      if (cronentry != null) {
        setExecutionTime(cronentry.getNextTime());
      }

      // run the transformation
      // Note that depending on the configuration, this could be placed in the 
      // scheduler and run intermittently as a scheduled job or multiple 
      // transform engines could be run in the thread pool of the super-class.
      try {
        engine.run();
      } catch (final Exception e) {
        Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.exception_running_engine", e.getClass().getSimpleName(), e.getMessage(), getName(), engine.getName()));
        Log.fatal(ExceptionUtil.stackTrace(e));
        if (Log.isLogging(Log.DEBUG_EVENTS)) {
          Log.debug(ExceptionUtil.stackTrace(e));
        }

        // If we blowup, set the active flag false, so the service will remove 
        // us from the scheduler. We will be reloaded if our reload flag is set
        setActiveFlag(false);
      } finally {
        try {
          engine.close();
        } catch (final IOException ignore) {}
        Log.trace(LogMsg.createMsg(CDX.MSG, "Job.completed", getName(), engine.getName()));
      } // try-catch-finally

    } else {
      Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.no_engine"));
    }

    // break out of our doWork loop and go inactive
    shutdown();
  }




  @Override
  public String getApplicationId() {
    return null;
  }




  @Override
  public String getCategory() {
    return null;
  }




  @Override
  public String getId() {
    return null;
  }




  @Override
  public DataFrame getProfile() {
    return null;
  }




  @Override
  public long getStartTime() {
    return 0;
  }




  @Override
  public DataFrame getStatus() {
    return null;
  }




  @Override
  public String getSystemId() {
    return null;
  }




  @Override
  public Config getTemplate() {
    return null;
  }




  @Override
  public boolean isLicensed() {
    return false;
  }




  @Override
  public void quiesce() {}




  @Override
  public void setId(String id) {}




  @Override
  public void setStartTime(long millis) {}




  @Override
  public void shutdown(DataFrame params) {
    if (engine != null) {
      engine.shutdown();
    }
  }




  @Override
  public Context getContext() {
    return context;
  }




  @Override
  public void setContext(Context context) {
    this.context = context;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getLoader()
   */
  @Override
  public Loader getLoader() {
    return loader;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setLoader(coyote.loader.Loader)
   */
  @Override
  public void setLoader(Loader loader) {
    this.loader = loader;
  }




  /**
   * @return the cron entry for this job or null of this is not a repeatable job.
   */
  public CronEntry getCronEntry() {
    return cronentry;
  }

}
