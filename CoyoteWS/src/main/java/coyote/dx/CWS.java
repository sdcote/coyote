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

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.web.ExchangeType;
import coyote.dx.web.Method;
import coyote.dx.web.Parameters;
import coyote.dx.web.Proxy;
import coyote.dx.web.Resource;
import coyote.dx.web.auth.AbstractAuthenticator;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.decorator.AbstractDecorator;
import coyote.dx.web.decorator.RequestDecorator;
import coyote.loader.Loader;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * 
 */
public class CWS {
  public static final Version VERSION = new Version( 0, 0, 1, Version.EXPERIMENTAL );
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

  // Message bundle for i18n
  public static final BundleBaseName MSG;
  static {
    MSG = new BundleBaseName( "BatchWSMsg" );
  }




  /**
   * This handles Proxy configuration in a uniform manner across BatchWS 
   * components.
   * 
   * @param cfg
   * 
   * @return a configured proxy object through which components can use to navigate request calls
   * 
   * @throws ConfigurationException if there are problems with the configuration data
   */
  public static Proxy configProxy( DataFrame cfg ) throws ConfigurationException {
    Proxy retval = null;
    if ( cfg != null ) {
      retval = new Proxy();

      for ( DataField field : cfg.getFields() ) {
        if ( ConfigTag.HOST.equalsIgnoreCase( field.getName() ) ) {
          retval.setHost( field.getStringValue() );

        } else if ( ConfigTag.PORT.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isNumeric() ) {
            try {
              retval.setPort( cfg.getAsInt( field.getName() ) );
            } catch ( DataFrameException e ) {
              Log.error( LogMsg.createMsg( MSG, "Proxy port '{}' is not a valid integer", field.getStringValue() ) );
            }
          } else {
            // Try to parse the string into an integer
            try {
              retval.setPort( Integer.parseInt( field.getStringValue() ) );
            } catch ( NumberFormatException e ) {
              throw new ConfigurationException( "Could not parse proxy port '" + field.getStringValue() + "' into an integer" );
            }
          }

        } else if ( ConfigTag.DOMAIN.equalsIgnoreCase( field.getName() ) ) {
          retval.setDomain( field.getStringValue() );

        } else if ( ConfigTag.USERNAME.equalsIgnoreCase( field.getName() ) ) {
          retval.setUsername( field.getStringValue() );

        } else if ( ConfigTag.PASSWORD.equalsIgnoreCase( field.getName() ) ) {
          retval.setPassword( field.getStringValue() );

        } else if ( (Loader.ENCRYPT_PREFIX+ConfigTag.USERNAME).equalsIgnoreCase( field.getName() ) ) {
          retval.setUsername( CipherUtil.decryptString( field.getStringValue() ) );

        } else if ( (Loader.ENCRYPT_PREFIX+ConfigTag.PASSWORD).equalsIgnoreCase( field.getName() ) ) {
          retval.setPassword( CipherUtil.decryptString( field.getStringValue() ) );
        }
      } //for fields
      Log.debug( LogMsg.createMsg( MSG, "Instance proxy {}", retval ) );
    } else {
      Log.debug( LogMsg.createMsg( MSG, "Null proxy config" ) );
    }
    return retval;
  }




  /**
   * 
   * @param className
   * @param cfg
   * @param resource
   */
  public static void configDecorator( String className, DataFrame cfg, Resource resource ) {
    Log.debug( LogMsg.createMsg( CWS.MSG, "Decorator.configuring_decorator", className, cfg ) );

    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = AbstractDecorator.class.getPackage().getName() + "." + className;
    } else {
      Log.error( LogMsg.createMsg( CWS.MSG, "Decorator.no_decorator_class", cfg.toString() ) );
    }

    try {
      Class<?> clazz = Class.forName( className );
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();

      if ( object instanceof RequestDecorator ) {
        if ( cfg != null ) {
          try {
            ( (RequestDecorator)object ).setConfiguration( cfg );
          } catch ( Exception e ) {
            Log.error( LogMsg.createMsg( CWS.MSG, "Decorator.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
          }
        }

        resource.addRequestDecorator( (RequestDecorator)object );
        Log.debug( LogMsg.createMsg( CWS.MSG, "Decorator.created_decorator", object.getClass().getName() ) );
      } else {
        Log.warn( LogMsg.createMsg( CWS.MSG, "Decorator.class_is_not_decorator", className ) );
      }
    } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
      Log.error( LogMsg.createMsg( CWS.MSG, "Decorator.could_not_instantiate", className, e.getClass().getName(), e.getMessage() ) );
    }

  }




  /**
   * Return a configured set of parameters from the given dataframe
   * 
   * TODO: Support setting request headers, although decorators can also do this.
   * 
   * @param cfg configuration data
   * 
   * @return a configured protocol parameters object, or null if configuration frame was null
   */
  public static Parameters configParameters( DataFrame cfg ) throws ConfigurationException {
    Parameters retval = null;
    if ( cfg != null ) {
      retval = new Parameters();
      String prefix = null;
      String namespace = null;

      for ( DataField field : cfg.getFields() ) {
        if ( METHOD.equalsIgnoreCase( field.getName() ) ) {
          Method method = Method.getMethodByName( field.getStringValue() );
          if ( method != null ) {
            retval.setMethod( method );
          } else {
            throw new ConfigurationException( "Invalid method name of '" + field.getStringValue() + "'" );
          }
        } else if ( EXCHANGE_TYPE.equalsIgnoreCase( field.getName() ) ) {
          ExchangeType type = ExchangeType.getTypeByName( field.getStringValue() );
          if ( type != null ) {
            retval.setExchangeType( type );
          } else {
            throw new ConfigurationException( "Invalid exchange type of '" + field.getStringValue() + "'" );
          }
        } else if ( OPERATION.equalsIgnoreCase( field.getName() ) ) {
          retval.setSoapOperation( field.getStringValue() );
        } else if ( PREFIX.equalsIgnoreCase( field.getName() ) ) {
          prefix = field.getStringValue();
        } else if ( NAMESPACE.equalsIgnoreCase( field.getName() ) ) {
          namespace = field.getStringValue();
        }
      } //for fields

      // if SOAP prefix and namespace were set,
      if ( StringUtil.isNotBlank( prefix ) && StringUtil.isNotBlank( namespace ) ) {
        retval.setSoapNamespace( prefix, namespace );
        retval.setExchangeType( ExchangeType.SOAP );
      } else if ( StringUtil.isNotBlank( prefix ) || StringUtil.isNotBlank( namespace ) ) {
        if ( StringUtil.isNotBlank( prefix ) ) {
          throw new ConfigurationException( "Namespace URL is required when specifying prefix" );
        } else {
          throw new ConfigurationException( "XML prefic is required when specifying a Namespace URL" );
        }
      }

      // If SOAP operation is set, default to SOAP exchange type
      if ( StringUtil.isNotBlank( retval.getSoapOperation() ) ) {
        retval.setExchangeType( ExchangeType.SOAP );
      }

      Log.debug( LogMsg.createMsg( MSG, "BatchWS.resource_protocol", retval ) );
    } else {
      Log.debug( LogMsg.createMsg( MSG, "BatchWS.null_protocol_config" ) );
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
  public static Authenticator configAuthenticator( DataFrame cfg ) throws ConfigurationException {
    Authenticator retval = null;

    // Make sure the class is fully qualified 
    String className = cfg.getAsString( ConfigTag.CLASS );
    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = AUTH_PKG + "." + className;
      cfg.put( ConfigTag.CLASS, className );
    }

    try {
      Class<?> clazz = Class.forName( className );
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();

      if ( object instanceof Authenticator ) {
        try {
          retval = (Authenticator)object;
          retval.setConfiguration( cfg );
        } catch ( ConfigurationException e ) {
          Log.error( LogMsg.createMsg( CDX.MSG, "Batch.configuration_error", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() ) );
        }
      } else {
        Log.warn( LogMsg.createMsg( CDX.MSG, "BatchWS.instance_not_authenticator", className ) );
      }

    } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
      Log.error( LogMsg.createMsg( CDX.MSG, "Batch.instantiation_error", className, e.getClass().getName(), e.getMessage() ) );
    }

    return retval;
  }

}
