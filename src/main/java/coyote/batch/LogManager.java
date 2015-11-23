/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.Logger;


/**
 * This class sets up logging only after the engine is run and the context is
 * opened so as to enable use of the context values and templates in logging
 * configuration values.
 */
public class LogManager extends AbstractConfigurableComponent implements ConfigurableComponent {

  /** Constant to assist in determining the full class name of loggers */
  private static final String LOGGER_PKG = Log.class.getPackage().getName();

  TransformContext context = null;




  /**
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // TODO we should probably perform some validation checks here to ensure logging operates as expected when the engine runs.
  }




  @Override
  public void open( TransformContext context ) {
    setContext( context );
System.out.println("SETTING UP LOGGING WITH "+getConfiguration());
  }




  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }




  //

  //

  //

  //

  //

  //

  /**
   * Configure the logging for the job
   * 
   * @param cfg the logging configuration section
   * @param retval the engine being configured.
   */
  private static void configLogging( DataFrame cfg, TransformEngine retval ) {
    // preserve existing categories
    boolean isDebug = Log.isLogging( Log.DEBUG );
    boolean isInfo = Log.isLogging( Log.INFO );

    // Remove all currently set loggers
    Log.removeAllLoggers();

    // Find the loggers
    for ( DataField field : cfg.getFields() ) {

      if ( ConfigTag.LOGGERS.equalsIgnoreCase( field.getName() ) ) {

        if ( field.isFrame() ) {
          DataFrame cfgFrame = (DataFrame)field.getObjectValue();

          // there can be many loggers
          if ( cfgFrame.isArray() ) {

            for ( DataField cfgfield : cfgFrame.getFields() ) {
              if ( cfgfield.isFrame() ) {
                configLogger( (DataFrame)cfgfield.getObjectValue() );
              } else {
                System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.invalid_logger_configuration_section" ) );
              }
            }
          } else {
            // ... or just one
            configLogger( cfgFrame );
          }
        } else {
          System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.invalid_logger_configuration_section" ) );
        }

      }

    }

    // Find the categories we will be logging
    for ( DataField field : cfg.getFields() ) {

      if ( ConfigTag.CATEGORIES.equalsIgnoreCase( field.getName() ) ) {
        if ( StringUtil.isNotBlank( field.getStringValue() ) ) {

          // Split each of the categories and enable each one
          String[] tokens = field.getStringValue().split( "," );

          if ( tokens.length > 0 ) {
            // Turn off all categories
            Log.setMask( 0 );

            // start logging all specified categories
            for ( int x = 0; x < tokens.length; x++ ) {
              Log.startLogging( tokens[x].trim().toUpperCase() );
              System.out.println( tokens[x].trim() );
            }
          }
        }
        break;
      }
    }

    // restore the command line overrides
    if ( isDebug )
      Log.startLogging( Log.DEBUG );
    if ( isInfo )
      Log.startLogging( Log.INFO );

  }




  private static void configLogger( DataFrame frame ) {

    // Make sure the class is fully qualified 
    String className = frame.getAsString( ConfigTag.CLASS );
    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = LOGGER_PKG + "." + className;
      frame.put( ConfigTag.CLASS, className );
    } else {
      System.err.println( "No logger Class: " + frame.toString() );
    }

    String loggerName = frame.getAsString( ConfigTag.NAME );
    if ( StringUtil.isNotBlank( loggerName ) ) {

      // look for a target name and ... bummer
      Config cfg = new Config();
      cfg.merge( frame );

      Logger logger = createLogger( cfg );

      if ( logger != null ) {
        try {
          Log.addLogger( loggerName, logger );
        } catch ( Exception e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.Could not create an instance of the specified logger" ) );
      }
    }
  }




  private static Logger createLogger( Config cfg ) {
    Logger retval = null;
    if ( cfg != null ) {
      if ( cfg.contains( ConfigTag.CLASS ) ) {
        String className = cfg.getAsString( ConfigTag.CLASS );

        try {
          Class<?> clazz = Class.forName( className );
          Constructor<?> ctor = clazz.getConstructor();
          Object object = ctor.newInstance();

          if ( object instanceof Logger ) {
            retval = (Logger)object;
            try {
              retval.setConfig( cfg );

            } catch ( Exception e ) {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_configure_logger {} - {} : {}", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
            }
          } else {
            Log.warn( LogMsg.createMsg( Batch.MSG, "EngineFactory.instance_is_not_a_logger of {} is not configurable", className ) );
          }
        } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Could not instantiate {} reason: {} - {}", className, e.getClass().getName(), e.getMessage() ) );
        }
      } else {
        Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Configuration frame did not contain a class name" ) );
      }
    }

    return retval;
  }

}
