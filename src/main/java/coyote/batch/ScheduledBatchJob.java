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
import java.util.List;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
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
 * 
 * 
 * The plan is to support a crontab style of schedule
 * "Schedule": { "Minute": "5,35", "Hour": "6-22", "Date": "*", "Month": "*", "Day": "1-6" }
 * Minute
 * Hour
 * Date = DayOfMonth
 * Month
 * Day = DayOfWeek
 */
public class ScheduledBatchJob extends ScheduledJob implements ManagedComponent {

  TransformEngine engine = null;
  Config configuration = null;




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
      } else {
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.engine_configured", engine.getName() ) );
      }
    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_job_section" ) );

    }

    // Setup the schedule - 
    List<Config> cfgs = config.getSections( "Schedule" );
    if ( cfgs.size() > 0 ) {
      Config scheduleCfg = cfgs.get( 0 );

      // Repeat according to the schedule
      setRepeatable( true );

      System.out.println( scheduleCfg.toFormattedString() );
    }

    // Set out interval for 30 minutes - for now
    super.setExecutionInterval( 30 * 60 * 1000 );

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
      Log.trace( LogMsg.createMsg( Batch.MSG, "Job.running" ) );

      // run the transformation
      // Note that depending on the configuration, this could be placed in the 
      // scheduler and run intermittently as a scheduled job or multiple 
      // transform engines could be run in the thread pool of the super-class.
      try {
        engine.run();
      } catch ( final Exception e ) {
        Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.exception_running_engine", e.getClass().getSimpleName(), e.getMessage() ) );
        Log.fatal( ExceptionUtil.toString( e ) );

        // If we blowup, set the active flag false, so the service will remove 
        // us from the scheduler. We will be reloaded if our reload flag is set
        setActiveFlag( false );
      }
      finally {
        try {
          engine.close();
        } catch ( final IOException ignore ) {}
        Log.trace( LogMsg.createMsg( Batch.MSG, "Job.completed", engine.getName() ) );
      } // try-catch-finally

    } else {
      Log.fatal( LogMsg.createMsg( Batch.MSG, "Job.no_engine" ) );
    }

  }




  /**
   * @see coyote.loader.thread.ThreadJob#terminate()
   */
  @Override
  public void terminate() {
    setActiveFlag( false );
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
