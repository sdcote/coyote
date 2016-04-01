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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.UUID;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.Logger;


/**
 * This class sets up logging only after the engine is run and the context is
 * opened so as to enable use of the context values and templates in logging
 * configuration values.
 * 
 * TODO This code has been build incrementally and is in need of refactoring.
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

    if ( getConfiguration() != null && getConfiguration().getFieldCount() > 0 ) {
      Log.debug( LogMsg.createMsg( Batch.MSG, "LogManager.initializing" ) );
      // Remove all the loggers since we will be adding our own and possibly 
      // duplicating loggers resulting in duplicate messages (e.g. the same 
      // message to the console multiple times) 
      Log.removeAllLoggers();

      // Find the loggers
      for ( DataField field : getConfiguration().getFields() ) {

        if ( field.isFrame() ) {
          DataFrame cfgFrame = (DataFrame)field.getObjectValue();
          if ( StringUtil.isNotBlank( field.getName() ) ) {
            if ( field.isFrame() ) {
              configLogger( field.getName(), cfgFrame );
            } else {
              System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.invalid_logger_configuration_section" ) );
            }
          } else {
            System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.no_logger_classname", cfgFrame.toString() ) );
          }
        } else {
          System.err.println( LogMsg.createMsg( Batch.MSG, "EngineFactory.invalid_logger_configuration_section" ) );
        }

      }

    } else {
      Log.debug( LogMsg.createMsg( Batch.MSG, "LogManager.no_config" ) );
    }

  }




  @Override
  public void close() throws IOException {}




  private void configLogger( String className, DataFrame frame ) {

    // Make sure the class is fully qualified 
    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = LOGGER_PKG + "." + className;
      frame.put( ConfigTag.CLASS, className );
    } else {
      System.err.println( "No logger Class: " + frame.toString() );
    }

    String loggerName = frame.getAsString( ConfigTag.NAME );

    // It is necessary to name loggers, but if not configured, generate a name
    if ( StringUtil.isBlank( loggerName ) ) {
      loggerName = UUID.randomUUID().toString();
    }

    // All loggers must have a name
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

            // make sure the URI is valid
            URI testUri = UriUtil.parse( cval );
            if ( testUri != null ) {
              if ( UriUtil.isFile( testUri ) ) {
                File logfile = UriUtil.getFile( testUri );

                // make it absolute to our job directory
                if ( !logfile.isAbsolute() ) {
                  if ( getContext() != null && getContext().getSymbols() != null ) {
                    File jobdir = new File( getContext().getSymbols().getString( Symbols.JOB_DIRECTORY ) );
                    logfile = new File( jobdir, logfile.getPath() );
                    testUri = FileUtil.getFileURI( logfile );
                    cval = testUri.toString();
                  }
                }
              }

            } else {
              System.out.println( "Bad target URI '" + cval + "'" );
            }

          } else if ( ConfigTag.CATEGORIES.equalsIgnoreCase( field.getName() ) ) {
            // Categories should be normalized to upper case
            cval = cval.toUpperCase();
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
