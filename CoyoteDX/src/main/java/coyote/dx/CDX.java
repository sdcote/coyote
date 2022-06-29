/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.commons.Version;
import coyote.dataframe.DataFrame;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * 
 */
public class CDX {

  public static final Version VERSION = new Version(0, 9, 0, Version.DEVELOPMENT);
  public static final String NAME = "CDX";
  public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSX";
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
  public static final String DEFAULT_FRAMEPATH_NAME = "framePath";
  public static final String JSON_EXT = ".json";

  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName("CdxMsg");
  }


  public static enum Sort {
    ASCEND, DESCEND, NONE, ASCEND_CI, DESCEND_CI
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
  public static Object createComponent(String className, DataFrame cfg) {
    Object retval = null;
    if (StringUtil.isNotBlank(className)) {

      try {
        Class<?> clazz = Class.forName(className);
        Constructor<?> ctor = clazz.getConstructor();
        Object object = ctor.newInstance();

        if (cfg != null) {
          if (object instanceof ConfigurableComponent) {
            try {
              ((ConfigurableComponent)object).setConfiguration(new Config(cfg));
            } catch (ConfigurationException e) {
              Log.error(LogMsg.createMsg(CDX.MSG, "DX.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
          } else {
            Log.warn(LogMsg.createMsg(CDX.MSG, "DX.instance_not_configurable", className));
          }
        }

        retval = object;
      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        Log.error(LogMsg.createMsg(CDX.MSG, "DX.instantiation_error", className, e.getClass().getName(), e.getMessage()));
      }
    } else {
      Log.error(LogMsg.createMsg(CDX.MSG, "DX.config_frame_did_not_contain_a_class"));
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
  public static Object createComponent(DataFrame cfg) {
    Object retval = null;
    if (cfg != null) {
      if (cfg.contains(ConfigTag.CLASS)) {
        String className = cfg.getAsString(ConfigTag.CLASS);

        try {
          Class<?> clazz = Class.forName(className);
          Constructor<?> ctor = clazz.getConstructor();
          Object object = ctor.newInstance();

          if (object instanceof ConfigurableComponent) {
            try {
              ((ConfigurableComponent)object).setConfiguration(new Config(cfg));
            } catch (ConfigurationException e) {
              Log.error(LogMsg.createMsg(CDX.MSG, "DX.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
          } else {
            Log.warn(LogMsg.createMsg(CDX.MSG, "DX.instance_not_configurable", className));
          }
          retval = object;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "DX.instantiation_error", className, e.getClass().getName(), e.getMessage()));
        }
      } else {
        Log.error(LogMsg.createMsg(CDX.MSG, "DX.config_frame_did_not_contain_a_class"));
      }
    }

    return retval;
  }




  /**
   * Called by other classes to get our version number.
   * 
   * @return a string representing our version.
   */
  public String getVersion() {
    return VERSION.toString();
  }




  /**
   * Take a relative file, and try to resolve it by searching in a number of 
   * locations.
   * 
   * <p>Search for the file in the following locations:<ol>
   * <li>Users current working directory
   * <li>Configuration location, if it is a file system directory
   * <li>Work directory (from system properties)
   * <li>Job directory</ol>
   * 
   * @param file the file to resolve
   * @param context the transform context containing the different file 
   *        locations for the transform job.
   * 
   * @return a file which may or may not have been resolved.
   */
  public static File resolveFile(File file, TransformContext context) {
    File retval = file;
    if (file != null && !file.isAbsolute()) {
      retval = new File(System.getProperty(System.getProperty("user.dir"), file.getPath()));
      if (!retval.exists()) {
        String cfgUri = System.getProperty(coyote.loader.ConfigTag.CONFIG_URI);
        if (StringUtil.isNotBlank(cfgUri)) {
          try {
            URI uri = new URI(cfgUri);
            if (UriUtil.isFile(uri)) {
              File cfgFile = UriUtil.getFile(uri);
              if (cfgFile != null) {
                retval = new File(cfgFile.getParent(), file.getPath());
              }
            }
          } catch (URISyntaxException ignore) {
            // The configuration may have come from the network
          }
        }
        if (!retval.exists()) {
          retval = new File(context.getSymbols().getString(Symbols.WORK_DIRECTORY), file.getPath());
          if (!retval.exists()) {
            retval = new File(context.getSymbols().getString(Symbols.JOB_DIRECTORY), file.getPath());
          }
        }
      }
    }

    return retval;
  }




  public static File resolveFile(String token, TransformContext context) {
    File retval = null;
    URI uri = UriUtil.parse(token);
    if (uri != null) {
      retval = UriUtil.getFile(uri);
      if (retval == null) {
        if (uri.getScheme() == null) {
          // Assume a file if there is no scheme
          Log.debug("Source URI did not contain a scheme, assuming a filename");
          retval = new File(token);
        } else {
          Log.warn(LogMsg.createMsg(CDX.MSG, "Reader.source_is_not_file", token));
        }
      }
    } else {
      Log.debug("Source could not be parsed into a URI, assuming a filename");
      retval = new File(token);
    }
    if (retval != null) {
      Log.debug("Using a source file of " + retval.getAbsolutePath());
    } else {
      Log.error("Using a source file of NULL_REF");
    }
    // if not absolute, use the CDX fixture to attempt to resolve the relative file
    try{
      if (retval != null && !retval.isAbsolute()) {
        retval = CDX.resolveFile(retval, context);
      }
    Log.debug("Using an absolute source file of " + retval.getAbsolutePath());
    } catch (NullPointerException npe) {
      Log.notice("Could not get absolute path for source file of " + retval);
    }
    return retval;
  }

}
