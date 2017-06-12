/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class CDX {

  public static final Version VERSION = new Version( 0, 9, 0, Version.EXPERIMENTAL );
  public static final String NAME = "CDX";
  public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "CdxMsg" );
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
              ( (ConfigurableComponent)object ).setConfiguration( new Config( cfg ) );
            } catch ( ConfigurationException e ) {
              Log.error( LogMsg.createMsg( CDX.MSG, "DX.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
            }
          } else {
            Log.warn( LogMsg.createMsg( CDX.MSG, "DX.instance_not_configurable", className ) );
          }
        }

        retval = object;
      } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
        Log.error( LogMsg.createMsg( CDX.MSG, "DX.instantiation_error", className, e.getClass().getName(), e.getMessage() ) );
      }
    } else {
      Log.error( LogMsg.createMsg( CDX.MSG, "DX.config_frame_did_not_contain_a_class" ) );
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
              ( (ConfigurableComponent)object ).setConfiguration( new Config( cfg ) );
            } catch ( ConfigurationException e ) {
              Log.error( LogMsg.createMsg( CDX.MSG, "DX.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
            }
          } else {
            Log.warn( LogMsg.createMsg( CDX.MSG, "DX.instance_not_configurable", className ) );
          }
          retval = object;
        } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
          Log.error( LogMsg.createMsg( CDX.MSG, "DX.instantiation_error", className, e.getClass().getName(), e.getMessage() ) );
        }
      } else {
        Log.error( LogMsg.createMsg( CDX.MSG, "DX.config_frame_did_not_contain_a_class" ) );
      }
    }

    return retval;
  }

}
