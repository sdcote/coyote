/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.commons.network.http.Method;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.context.TransformContext;
import coyote.dx.web.ExchangeType;
import coyote.dx.web.Parameters;
import coyote.dx.web.Proxy;
import coyote.dx.web.Resource;
import coyote.dx.web.auth.AbstractAuthenticator;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.decorator.AbstractDecorator;
import coyote.dx.web.decorator.RequestDecorator;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class CWS {
  public static final Version VERSION = new Version(0, 1, 0, Version.DEVELOPMENT);
  public static final String NAME = "CoyoteWS";

  /** Constant to assist in determining the full class name of authenticators */
  public static final String AUTH_PKG = AbstractAuthenticator.class.getPackage().getName();

  // Configuration tags
  public static final String BODY = "Body";
  public static final String PROXY = "Proxy";
  public static final String AUTHENTICATOR = "Authenticator";
  public static final String PREEMTIVE_AUTH = "preemptive";

  // Protocol tags
  public static final String PROTOCOL = "Protocol";
  public static final String METHOD = "method";
  public static final String EXCHANGE_TYPE = "ExchangeType";
  public static final String OPERATION = "operation";
  public static final String PREFIX = "prefix";
  public static final String NAMESPACE = "namespace";
  public static final String DECORATOR = "Decorator";

  // for the reader
  public static final String SELECTOR = "Selector";
  public static final String PAGINATION = "Pagination";
  private static final String STEP = "Step";
  private static final String START = "Start";

  // For the WebServiceWriter
  public static final String RESPONSE_WRITER = "ResponseWriter";

  // Message bundle for i18n
  public static final BundleBaseName MSG;

  static {
    MSG = new BundleBaseName("CWSMsg");
  }




  /**
   * This handles Proxy configuration in a uniform manner across BatchWS 
   * components.
   * 
   * @param cfg the configuration to use for creating the proxy
   * 
   * @return a configured proxy object through which components can use to navigate request calls
   * 
   * @throws ConfigurationException if there are problems with the configuration data
   */
  public static Proxy configProxy(DataFrame cfg) throws ConfigurationException {
    Proxy retval = null;
    if (cfg != null) {
      retval = new Proxy();

      for (DataField field : cfg.getFields()) {
        if (ConfigTag.HOST.equalsIgnoreCase(field.getName())) {
          retval.setHost(field.getStringValue());

        } else if (ConfigTag.PORT.equalsIgnoreCase(field.getName())) {
          if (field.isNumeric()) {
            try {
              retval.setPort(cfg.getAsInt(field.getName()));
            } catch (DataFrameException e) {
              Log.error(LogMsg.createMsg(MSG, "Proxy port '{}' is not a valid integer", field.getStringValue()));
            }
          } else {
            // Try to parse the string into an integer
            try {
              retval.setPort(Integer.parseInt(field.getStringValue()));
            } catch (NumberFormatException e) {
              throw new ConfigurationException("Could not parse proxy port '" + field.getStringValue() + "' into an integer");
            }
          }

        } else if (ConfigTag.DOMAIN.equalsIgnoreCase(field.getName())) {
          retval.setDomain(field.getStringValue());

        } else if (ConfigTag.USERNAME.equalsIgnoreCase(field.getName())) {
          retval.setUsername(field.getStringValue());

        } else if (ConfigTag.PASSWORD.equalsIgnoreCase(field.getName())) {
          retval.setPassword(field.getStringValue());

        } else if ((Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME).equalsIgnoreCase(field.getName())) {
          retval.setUsername(CipherUtil.decryptString(field.getStringValue()));

        } else if ((Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD).equalsIgnoreCase(field.getName())) {
          retval.setPassword(CipherUtil.decryptString(field.getStringValue()));
        }
      } //for fields
      Log.debug(LogMsg.createMsg(MSG, "Instance proxy {}", retval));
    } else {
      Log.debug(LogMsg.createMsg(MSG, "Null proxy config"));
    }
    return retval;
  }




  /**
   * Add a request decorator to the resource.
   *  
   * @param className the class name of the decorator to create
   * @param cfg the configuration of the decorator
   * @param resource the resource to which the decorator will be added
   * @param context the transform context which contains variables and symbols
   *                used to resolve variable values in the configuration
   */
  public static void configDecorator(String className, DataFrame cfg, Resource resource, TransformContext context) {
    Log.debug(LogMsg.createMsg(CWS.MSG, "Decorator.configuring_decorator", className, cfg));

    if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
      className = AbstractDecorator.class.getPackage().getName() + "." + className;
    } else {
      Log.error(LogMsg.createMsg(CWS.MSG, "Decorator.no_decorator_class", cfg.toString()));
    }

    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();

      if (object instanceof RequestDecorator) {
        if (cfg != null) {
          DataFrame resolvedFrame = resolveDataFrame(cfg, context);
          try {
            ((RequestDecorator)object).setConfiguration(resolvedFrame);
          } catch (Exception e) {
            Log.error(LogMsg.createMsg(CWS.MSG, "Decorator.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
          }
        }

        resource.addRequestDecorator((RequestDecorator)object);
        Log.debug(LogMsg.createMsg(CWS.MSG, "Decorator.created_decorator", object.getClass().getName()));
      } else {
        Log.warn(LogMsg.createMsg(CWS.MSG, "Decorator.class_is_not_decorator", className));
      }
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      Log.error(LogMsg.createMsg(CWS.MSG, "Decorator.could_not_instantiate", className, e.getClass().getName(), e.getMessage()));
    }

  }




  /**
   * Return a configured set of parameters from the given dataframe
   * 
   * @param cfg configuration data
   * @param context the transform context which contains variables and symbols
   *                used to resolve variable values in the configuration
   * 
   * @return a configured protocol parameters object, or null if configuration frame was null
   * @throws ConfigurationException if there were problems configuring the parameters
   */
  public static Parameters configParameters(DataFrame cfg, TransformContext context) throws ConfigurationException {
    Parameters retval = null;
    if (cfg != null) {
      retval = new Parameters();
      String prefix = null;
      String namespace = null;

      DataFrame resolvedFrame = resolveDataFrame(cfg, context);
      for (DataField field : resolvedFrame.getFields()) {
        if (METHOD.equalsIgnoreCase(field.getName())) {
          Method method = Method.getMethodByName(field.getStringValue());
          if (method != null) {
            retval.setMethod(method);
          } else {
            throw new ConfigurationException("Invalid method name of '" + field.getStringValue() + "'");
          }
        } else if (EXCHANGE_TYPE.equalsIgnoreCase(field.getName())) {
          ExchangeType type = ExchangeType.getTypeByName(field.getStringValue());
          if (type != null) {
            retval.setExchangeType(type);
          } else {
            throw new ConfigurationException("Invalid exchange type of '" + field.getStringValue() + "'");
          }
        } else if (OPERATION.equalsIgnoreCase(field.getName())) {
          retval.setSoapOperation(field.getStringValue());
        } else if (PREFIX.equalsIgnoreCase(field.getName())) {
          prefix = field.getStringValue();
        } else if (NAMESPACE.equalsIgnoreCase(field.getName())) {
          namespace = field.getStringValue();
        }
      } //for fields

      // if SOAP prefix and namespace were set,
      if (StringUtil.isNotBlank(prefix) && StringUtil.isNotBlank(namespace)) {
        retval.setSoapNamespace(prefix, namespace);
        retval.setExchangeType(ExchangeType.SOAP);
      } else if (StringUtil.isNotBlank(prefix) || StringUtil.isNotBlank(namespace)) {
        if (StringUtil.isNotBlank(prefix)) {
          throw new ConfigurationException("Namespace URL is required when specifying prefix");
        } else {
          throw new ConfigurationException("XML prefic is required when specifying a Namespace URL");
        }
      }

      // If SOAP operation is set, default to SOAP exchange type
      if (StringUtil.isNotBlank(retval.getSoapOperation())) {
        retval.setExchangeType(ExchangeType.SOAP);
      }

      Log.debug(LogMsg.createMsg(MSG, "CoyoteWS.resource_protocol", retval));
    } else {
      Log.debug(LogMsg.createMsg(MSG, "CoyoteWS.null_protocol_config"));
    }
    return retval;
  }




  /**
   * Resolve all the string fields in the given data frame against the given 
   * context.
   * 
   * <p>This performs a two stage resolution. First, the value of the string 
   * field is used to perform a case sensitive search of the context for a 
   * variable with the given name. If a variable with that name is found, the 
   * value of that context variable is used. Next, the value is resolved 
   * against the symbol table in the given context. The resolved values are 
   * pre-processed against the template, meaning that any unresolved variables 
   * are left in the value for easier debugging.
   * 
   * <p>Only string values are processed. All other data types are simply 
   * cloned resulting in a deep copy of the given frame.
   * 
   * <p>The fields of the given DataFrame are processed breadth first, with 
   * any nested frames being processed recursively. The final result is a 
   * separate, mutable copy of the given frame.
   * 
   * @param cfg the frame to resolve
   * @param context the transform context which contains variables and symbols
   *                used to resolve variable values in the configuration
   * 
   * @return a copy of the dataframe with the string fields resolved.
   */
  public static DataFrame resolveDataFrame(DataFrame cfg, TransformContext context) {
    DataFrame retval = null;
    if (cfg != null) {
      retval = new DataFrame();
      if (context != null) {
        for (DataField field : cfg.getFields()) {
          if (field.isFrame()) {
            retval.add(field.getName(), resolveDataFrame((DataFrame)field.getObjectValue(), context));
          } else if (field.getType() == DataField.STRING) {
            String value = field.getStringValue();
            if (StringUtil.isNotBlank(value)) {
              String cval = context.getAsString(value, true);
              if (cval != null) {
                value = cval;
              }
              value = Template.preProcess(value, context.getSymbols());
            }
            retval.add(field.getName(), value);
            // Only log if the value changed
            if (Log.isLogging(Log.DEBUG_EVENTS) && value != null && !value.equals(field.getStringValue())) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Component.resolved_value", value, retval));
            }
          } else {
            retval.add((DataField)field.clone());
          }
        }
      } else {
        Log.warn("Cannot resolve dataframe: null context");
        retval = (DataFrame)cfg.clone();
      }
    }
    return retval;
  }




  /**
   * Create, configure and return an authenticator using the given 
   * configuration frame.
   * 
   * <p>The configuration must contain a class attribute to construct the 
   * appropriate authenticator. If the class is not fully qualified, the 
   * default namespace is assumed.</p>
   * 
   * @param cfg the configuration frame (must contain a class attribute)
   * 
   * @return A configured authenticator
   * 
   * @throws ConfigurationException if the configuration was invalid
   */
  public static Authenticator configAuthenticator(DataFrame cfg) throws ConfigurationException {
    Authenticator retval = null;

    // Make sure the class is fully qualified 
    String className = cfg.getAsString(ConfigTag.CLASS);
    if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
      className = AUTH_PKG + "." + className;
      cfg.put(ConfigTag.CLASS, className);
    }

    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();

      if (object instanceof Authenticator) {
        try {
          retval = (Authenticator)object;
          retval.setConfiguration(cfg);
        } catch (ConfigurationException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Batch.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
        }
      } else {
        Log.warn(LogMsg.createMsg(CDX.MSG, "BatchWS.instance_not_authenticator", className));
      }

    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      Log.error(LogMsg.createMsg(CDX.MSG, "DX.instantiation_error", className, e.getClass().getName(), e.getMessage()));
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
   * Create a Pagination object out of the given configuration data frame.
   * 
   * @param cfg configuration data frame
   * 
   * @return the Pagination object created from the config data or null if the 
   *         conf dataframe was null
   * 
   * @throws ConfigurationException if the step value is missing or if the 
   *         step or start could not be parsed into a valid long value.
   */
  public static Pagination configPagination(DataFrame cfg) throws ConfigurationException {
    Pagination retval = null;
    if (cfg != null) {
      Config config = new Config(cfg);

      long step = 0;

      // Case in-sensitive search for field value
      DataField field = cfg.getFieldIgnoreCase(STEP);
      if (field != null) {
        try {
          // get the name of the field exactly as it appears in the frame
          step = config.getAsLong(field.getName());
        } catch (DataFrameException e) {
          throw new ConfigurationException("Configuration value '" + field.getName() + "' could not be parsed into a numeric value");
        }
      } else {
        throw new ConfigurationException("Pagination must contain a '" + STEP + "' value");
      }

      long start = 0;
      field = cfg.getFieldIgnoreCase(START);
      if (field != null) {
        try {
          start = config.getAsLong(field.getName());
        } catch (DataFrameException e) {
          throw new ConfigurationException("Configuration value '" + field.getName() + "' could not be parsed into a numeric value");
        }
      }

      String name = config.getName();
      retval = new Pagination(name, start, step);

    }
    return retval;
  }

}
