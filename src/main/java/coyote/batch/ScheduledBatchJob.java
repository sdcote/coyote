/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import coyote.commons.CronEntry;
import coyote.commons.ExceptionUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.thread.ScheduledJob;


/**
 * This is a wrapper around an engine which will be called repeatedly on a 
 * schedule.
 */
public class ScheduledBatchJob extends ScheduledJob implements ManagedComponent {

  TransformEngine engine = null;
  Config configuration = null;
  CronEntry cronentry = null;




  /**
   * Set the configuration of this component.
   * 
   * @see coyote.loader.component.ManagedComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( Config config ) {
    configuration = config;

    if ( configuration != null ) {
      Log.debug( config.toFormattedString() );

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance( config );

      if ( StringUtil.isBlank( engine.getName() ) ) {
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.unnamed_engine_configured" ) );
        name = GUID.randomGUID().toString();
      } else {
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.engine_configured", engine.getName() ) );
        super.setName( engine.getName() );
      }
    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_job_section" ) );
    }

    // Setup the schedule - 
    List<Config> cfgs = config.getSections( ConfigTag.SCHEDULE );
    if ( cfgs.size() > 0 ) {
      Config scheduleCfg = cfgs.get( 0 );
      cronentry = new CronEntry();

      // go through each in order, this allows the user to determine how 
      // attributes are applied by processing them in order they appear and 
      // overwriting previous attributes.
      for ( DataField field : scheduleCfg.getFields() ) {
        if ( ConfigTag.PATTERN.equalsIgnoreCase( field.getName() ) ) {
          try {
            cronentry = CronEntry.parse( field.getStringValue() );
          } catch ( ParseException e ) {
            Log.error( LogMsg.createMsg( Batch.MSG, "Job.schedule_patterm_parse_error", e.getMessage() ) );
          }
        } else if ( ConfigTag.MINUTES.equalsIgnoreCase( field.getName() ) ) {
          cronentry.setMinutePattern( field.getStringValue() );
        } else if ( ConfigTag.HOURS.equalsIgnoreCase( field.getName() ) ) {
          cronentry.setHourPattern( field.getStringValue() );
        } else if ( ConfigTag.MONTHS.equalsIgnoreCase( field.getName() ) ) {
          cronentry.setMonthPattern( field.getStringValue() );
        } else if ( ConfigTag.DAYS.equalsIgnoreCase( field.getName() ) ) {
          cronentry.setDayPattern( field.getStringValue() );
        } else if ( ConfigTag.DAYS_OF_WEEK.equalsIgnoreCase( field.getName() ) ) {
          cronentry.setDayOfWeekPattern( field.getStringValue() );
        } else if ( ConfigTag.MILLIS.equalsIgnoreCase( field.getName() ) ) {
          long millis = 0;
          try {
            millis = Long.parseLong( field.getStringValue() );
            setExecutionInterval( millis );
          } catch ( NumberFormatException e ) {
            Log.error( LogMsg.createMsg( Batch.MSG, "Job.schedule_interval_parse_error", e.getMessage() ) );
          }
        }
      }

      if ( cronentry != null ) {

        // Repeat according to the schedule
        setRepeatable( true );
        setExecutionTime( cronentry.getNextTime() );

        if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
          Log.debug( cronentry.toString() );
        }
      } else {
        Log.error( LogMsg.createMsg( Batch.MSG, "Job.schedule_no_cron_entry", getExecutionInterval() ) );

        // No schedule, no repeat
        setRepeatable( false );

        // run one second in the future to give initialization time to settle
        setExecutionTime( System.currentTimeMillis() + 1000 );
      }

    }

  }




  /**
   * @see coyote.loader.thread.ScheduledJob#getExecutionInterval()
   */
  @Override
  public long getExecutionInterval() {
    if ( cronentry != null ) {
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
    setActiveFlag( true );
  }




  /**
   * @see coyote.loader.thread.ThreadJob#doWork()
   */
  @Override
  public void doWork() {

    if ( engine != null ) {
      Log.trace( LogMsg.createMsg( Batch.MSG, "Job.running", getName(), engine.getName() ) );

      // this sets our execution time to the exact millisecond based on when 
      // this job ACTUALLY ran. This is to ensure slow running jobs don't cause
      // execution delays 
      setExecutionTime( cronentry.getNextTime() );

      // run the transformation
      // Note that depending on the configuration, this could be placed in the 
      // scheduler and run intermittently as a scheduled job or multiple 
      // transform engines could be run in the thread pool of the super-class.
      try {
        engine.run();
      } catch ( final Exception e ) {
        Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.exception_running_engine", e.getClass().getSimpleName(), e.getMessage(), getName(), engine.getName() ) );
        Log.fatal( ExceptionUtil.toString( e ) );

        // If we blowup, set the active flag false, so the service will remove 
        // us from the scheduler. We will be reloaded if our reload flag is set
        setActiveFlag( false );
      }
      finally {
        try {
          engine.close();
        } catch ( final IOException ignore ) {}
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.completed", getName(), engine.getName() ) );
      } // try-catch-finally

    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_engine" ) );
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
  public void setLoader( Loader loader ) {}




  @Override
  public void quiesce() {}




  @Override
  public void setId( String id ) {}




  @Override
  public void setStartTime( long millis ) {}




  @Override
  public void shutdown( DataFrame params ) {}

}
