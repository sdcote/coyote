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
import java.net.URI;

import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.commons.template.Template;
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

    // Remove all currently set loggers
    Log.removeAllLoggers();

    // Find the loggers
    for ( DataField field : getConfiguration().getFields() ) {

      if ( ConfigTag.LOGGERS.equalsIgnoreCase( field.getName() ) ) {

        if ( field.isFrame() ) {
          DataFrame cfgFrame = (DataFrame)field.getObjectValue();

          if ( cfgFrame.isArray() ) {

            for ( DataField cfgfield : cfgFrame.getFields() ) {
              if ( cfgfield.isFrame() ) {
                configLogger( (DataFrame)cfgfield.getObjectValue() );
              } else {
                System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.invalid_logger_configuration_section" ) );
              }
            }
          } else {
            configLogger( cfgFrame );
          }
        } else {
          System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.invalid_logger_configuration_section" ) );
        }

      }

    }

  }




  @Override
  public void close() throws IOException {}




  //

  //

  //

  //

  //

  //

  private void configLogger( DataFrame frame ) {

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

      Config cfg = new Config();

      // populate the logger config, replacing strings with template values
      for ( DataField field : frame.getFields() ) {
        if ( field.getType() == DataField.STRING ) {
          String cval = getString( field.getStringValue() );
          if ( StringUtil.isBlank( cval ) ) {
            cval = Template.resolve( field.getStringValue(), getContext().getSymbols() );
          }

          // treat targets a little differently
          if ( ConfigTag.TARGET.equalsIgnoreCase( field.getName() ) ) {

            // the targets for loggers MUST be a URI
            if ( !( "stdout".equalsIgnoreCase( cval ) || "stderr".equalsIgnoreCase( cval ) ) ) {
              URI testTarget = UriUtil.parse( cval );

              if ( testTarget != null ) {
                if ( testTarget.getScheme() == null ) {
                  cval = "file://" + cval;
                }
              }
            }
          }

          cfg.add( field.getName(), cval );
        } else {
          cfg.add( field );
        }

      }

      Logger logger = createLogger( cfg );

      if ( logger != null ) {
        try {
          Log.addLogger( loggerName, logger );
        } catch ( Exception e ) {
          System.out.println( LogMsg.createMsg( Batch.MSG, "LogManager.Could not add configured logger", loggerName, logger.getClass(), e.getMessage() ) );
        }
      } else {
        System.err.println( LogMsg.createMsg( Batch.MSG, "LogManager.Could not create an instance of the specified logger" ) );
      }
    }
  }




  private static Logger createLogger( Config cfg ) {

    System.out.println( cfg.toFormattedString() );
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
