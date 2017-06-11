/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader.component;

import java.text.ParseException;
import java.util.List;

import coyote.commons.CronEntry;
import coyote.commons.GUID;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.AbstractLoader;
import coyote.loader.ConfigTag;
import coyote.loader.Context;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.thread.ScheduledJob;


/**
 * This is a component which is run intermittently in the scheduler and not 
 * continually in the thread pool like a thread job.
 */
public class AbstractScheduledComponent extends ScheduledJob implements ManagedComponent {

  private static final String UNKNOWN = "Unknown";

  protected Config configuration = null;
  protected volatile boolean logging = false;
  protected volatile boolean enabled = true;
  protected volatile boolean licensed = false;
  protected long startTime;
  protected String identifier = new GUID().toString();
  protected String componentName = AbstractScheduledComponent.CLASS;
  protected Loader loader = null;
  protected CronEntry cronentry = null;
  protected Context context = null;




  /**
   * 
   */
  public AbstractScheduledComponent() {
    super.setDoWorkOnce( true );
  }




  /**
   * Configure the component with the given configuration object.
   * 
   * @param config The object containing the configuration attributes.
   */
  @Override
  public void setConfiguration( final Config config ) {
    configuration = config;
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
            Log.error( LogMsg.createMsg( AbstractLoader.MSG, "Component.schedule_patterm_parse_error", e.getMessage() ) );
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
            Log.error( LogMsg.createMsg( AbstractLoader.MSG, "Component.schedule_interval_parse_error", e.getMessage() ) );
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
        Log.error( LogMsg.createMsg( AbstractLoader.MSG, "Component.schedule_no_cron_entry", getExecutionInterval() ) );

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




  /**
   * @see coyote.loader.component.Component#getApplicationId()
   */
  @Override
  public String getApplicationId() {
    return UNKNOWN;
  }




  /**
   * @see coyote.loader.component.Component#getCategory()
   */
  @Override
  public String getCategory() {
    return UNKNOWN;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getConfiguration()
   */
  @Override
  public Config getConfiguration() {
    return configuration;
  }




  /**
   * @see coyote.loader.component.Component#getDescription()
   */
  @Override
  public String getDescription() {
    return null;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getId()
   */
  @Override
  public String getId() {
    return identifier;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getName()
   */
  @Override
  public String getName() {
    return componentName;
  }




  /**
   * @see coyote.loader.component.Component#getProfile()
   */
  @Override
  public DataFrame getProfile() {
    final DataFrame retval = new DataFrame();
    retval.put( CLASS, CLASS );
    retval.put( "ID", identifier );

    return retval;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getStartTime()
   */
  @Override
  public long getStartTime() {
    return startTime;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getStatus()
   */
  @Override
  public DataFrame getStatus() {
    return getProfile();
  }




  /**
   * @see coyote.loader.component.Component#getSystemId()
   */
  @Override
  public String getSystemId() {
    return ManagedComponent.CLASS;
  }




  /**
   * Return a Config that can be used as a template for defining instances
   * of this component.
   *
   * @return a configuration that can be used as a template
   * 
   * @see coyote.loader.component.ManagedComponent#getTemplate()
   */
  @Override
  public Config getTemplate() {
    final Config template = new Config();

    try {
      template.setName( ManagedComponent.CLASS );

      // define the slots
      // template.addConfigSlot( new ConfigSlot( LogicComponent.ENABLED_TAG, "Flag indicating the component is enabled to run.", new Boolean( true ) ).toString() );
    } catch ( final Exception ex ) {
      // should always work
    }

    return template;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    return enabled;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled( final boolean flag ) {
    enabled = flag;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setId(java.lang.String)
   */
  @Override
  public void setId( final String id ) {
    if ( ( id != null ) && ( id.length() > 0 ) ) {
      identifier = id;
    }
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setStartTime(long)
   */
  @Override
  public void setStartTime( final long millis ) {
    startTime = millis;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#isLicensed()
   */
  @Override
  public boolean isLicensed() {
    return false;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#quiesce()
   */
  @Override
  public void quiesce() {}




  /**
   * @see coyote.loader.component.ManagedComponent#shutdown(coyote.dataframe.DataFrame)
   */
  @Override
  public void shutdown( DataFrame params ) {}




  /**
   * @see coyote.loader.component.ManagedComponent#setLoader(coyote.loader.Loader)
   */
  @Override
  public void setLoader( Loader loader ) {
    this.loader = loader;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setContext(coyote.loader.Context)
   */
  @Override
  public void setContext( Context context ) {
    this.context = context;
  }




  /**
   * @see coyote.loader.component.Component#getContext()
   */
  @Override
  public Context getContext() {
    return context;
  }

}
