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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.dataframe.DataFrame;
import coyote.loader.Loader;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class Batch {

  public static final Version VERSION = new Version( 0, 2, 0, Version.EXPERIMENTAL );
  public static final String NAME = "Batch";
  public static final DateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
  public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
  public static final DateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat( "HH:mm:ss" );

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "BatchMsg" );
  }




 


  /**
   * Create an instance of the given named class and configure it with the 
   * given dataframe if it is a configurable component.
   * 
   * @param className Fully qualified name of the class to instantiate
   * 
   * @param cfg the configuration to apply to the instance if it is configurable
   * 
   * @return a configured component
   */
  public static Object createComponent( String className, DataFrame cfg ) {
    Object retval = null;
    if ( StringUtil.isNotBlank( className ) ) {

      try {
        Class<?> clazz = Class.forName( className );
        Constructor<?> ctor = clazz.getConstructor();
        Object object = ctor.newInstance();

        if ( cfg != null ) {
          if ( object instanceof ConfigurableComponent ) {
            try {
              ( (ConfigurableComponent)object ).setConfiguration( cfg );
            } catch ( ConfigurationException e ) {
              Log.error( LogMsg.createMsg( Batch.MSG, "Batch.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
            }
          } else {
            Log.warn( LogMsg.createMsg( Batch.MSG, "Batch.instance_not_configurable", className ) );
          }
        }

        retval = object;
      } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
        Log.error( LogMsg.createMsg( Batch.MSG, "Batch.instantiation_error", className, e.getClass().getName(), e.getMessage() ) );
      }
    } else {
      Log.error( LogMsg.createMsg( Batch.MSG, "Batch.config_frame_did_not_contain_a_class" ) );
    }

    return retval;
  }




  /**
   * Create an instance of a class as specified in the CLASS attribute of 
   * the given dataframe and configure it with the given dataframe if it is a 
   * configurable component.
   * 
   * @param cfg the configuration to apply to the instance if it is configurable
   * 
   * @return a configured component
   */
  public static Object createComponent( DataFrame cfg ) {
    Object retval = null;
    if ( cfg != null ) {
      if ( cfg.contains( ConfigTag.CLASS ) ) {
        String className = cfg.getAsString( ConfigTag.CLASS );

        try {
          Class<?> clazz = Class.forName( className );
          Constructor<?> ctor = clazz.getConstructor();
          Object object = ctor.newInstance();

          if ( object instanceof ConfigurableComponent ) {
            try {
              ( (ConfigurableComponent)object ).setConfiguration( cfg );
            } catch ( ConfigurationException e ) {
              Log.error( LogMsg.createMsg( Batch.MSG, "Batch.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
            }
          } else {
            Log.warn( LogMsg.createMsg( Batch.MSG, "Batch.instance_not_configurable", className ) );
          }
          retval = object;
        } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
          Log.error( LogMsg.createMsg( Batch.MSG, "Batch.instantiation_error", className, e.getClass().getName(), e.getMessage() ) );
        }
      } else {
        Log.error( LogMsg.createMsg( Batch.MSG, "Batch.config_frame_did_not_contain_a_class" ) );
      }
    }

    return retval;
  }

}
