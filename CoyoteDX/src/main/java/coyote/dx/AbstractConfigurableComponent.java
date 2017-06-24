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

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractConfigurableComponent implements ConfigurableComponent {

  protected Config configuration = new Config();
  protected TransformContext context = null;




  /**
   * @see coyote.dx.ConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( Config cfg ) throws ConfigurationException {
    configuration = cfg;
  }




  /**
   * @see coyote.dx.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see coyote.dx.ConfigurableComponent#getConfiguration()
   */
  @Override
  public Config getConfiguration() {
    return configuration;
  }




  /**
   * Return the configuration value with the given key as a DataFrame.
   * 
   * <p>Check the configuration first and return that value, but if there is no 
   * value set (null) or the value is not a DataFrame then check the transform 
   * context for a value with the given key. If it exists AND is a DataFrame, 
   * return that value.</p>
   * 
   * <p>This is a convenience method to return a child configuration object 
   * from the main configuration frame, and secondarily a way to retrieve 
   * DataFrames from the transform context.</p>
   * 
   * @param key the name of the configuration parameter to return.
   * 
   * @return the value with that name in the configuration or null if the 
   * configuration value with that name could not be found in either the 
   * configuration of transform context or if either instance was not a 
   * DataFrame.
   */
  protected DataFrame getFrame( String key ) {
    DataFrame value = null;
    try {
      value = getConfiguration().getAsFrame( key );
    } catch ( DataFrameException ignore ) {}
    if ( value == null ) {
      Object obj = context.get( key );
      if ( obj != null && obj instanceof DataFrame ) {
        value = (DataFrame)obj;
      }
    }
    return value;
  }








  /**
   * Return the configuration or context value with the given (case 
   * in-sensitive) key.
   * 
   * <p>This checks the configuration for the value. Normally this is the 
   * value returned, but the value may point to a value in the context. For 
   * example, all components are to share the same server port. Instead of 
   * defining the server port in multiple locations, making changing the port 
   * more difficult later, the configuration value may point to a single value 
   * in the context.
   * 
   * <p>After the value is returned from the configuration, the value is used 
   * as a key to search for a context value. For example, the component may 
   * look for a configuration value of "port" which is configured with a value 
   * of "SmtpPort". This method will then look in the context for a value with 
   * the key of "SmtpPort" and if a value is found in the context, that value 
   * will be used. Note: this context lookup is case sensitive.
   * 
   * <p>If there is no value with that key defined in the configuration, then
   * this method will search the context with that key. In this case, the 
   * context serves as an extension of the configuration. For example, a 
   * component may look for a value of "port" which does not exist in its 
   * configuration. This methed will then search the context for a value 
   * mapped to the "port" key and return it if it exists. As an extension of 
   * the configuration look-up, this search is case in-sensitive.
   * 
   * <p>If any value is found in the context, it is treated as a template and 
   * pre-processed to resolve any variable in the value. Note: the template is 
   * not fully resolved, but pre-processed meaning any unresolved variables 
   * are left in the returned value for later resolution and to aid in 
   * debugging what symbols are needed or if variables are misspelt.
   * 
   * <p>Values pulled directly from the configuration (not retrieved from the 
   * context) are not treated as a template and passed as they appear in the 
   * configuration. It is up to the component to resolve any variable in 
   * configuration values. 
   * 
   * @param key the name of the configuration parameter to return
   * 
   * @return the value with that name in the configuration or null if the given 
   *         key is null, or the configuration value with that name could not 
   *         be found in either the configuration of transform context.
   */
  public String getString( String key ) {
    String value = null;

    // Perform a case insensitive search for the value with the given key
    value = getConfiguration().getString( key );
    
    if ( context != null ) {
      // See if there is a match in the context for reference resolution
      if ( value != null ) {
        // perform a case sensitive search for the value in the context
        String cval = context.getAsString( value, true );

        // if an exact match was found in the context...
        if ( cval != null ) {
          // ...resolve the value as a template
          return Template.preProcess( cval, context.getSymbols() );
        }
      }

      if ( value == null ) {
        // perform a case insensitive search in the context
        value = context.getAsString( key, false );
      }
      String retval = Template.preProcess( value, context.getSymbols() );

      // Only log if the value changed
      if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
        if ( retval != null && !retval.equals( value ) ) {
          Log.debug( LogMsg.createMsg( CDX.MSG, "Component.resolved_value", value, retval ) );
        }
      }

      return retval;
    } else {
      // no context, return the raw value
      return value;
    }
  }




  /**
   * Get the configuration value with the given key and decipher it.
   * 
   * <p>The name of the encryption algorithm is assumed to be Blowfish unless 
   * otherwise specified in the {@code cipher.name} system property.</p>
   * 
   * <p>Similarly, the decryption key is assumed to be the toolkit default 
   * unless otherwise specified in the {@code cipher.key} system property.</p>
   * 
   * @param key the name of the configuration parameter to decipher return
   * 
   * @return the decrypted value of the named configuration attribute or null 
   *         if it does not exist in the configuration or the context.
   */
  public String getEncryptedString( String key ) {
    String retval = null;

    // retrieve what we will assume is cipher text, base64 encoded bytes
    String cipherText = getString( key );
    if ( StringUtil.isNotBlank( cipherText ) ) {
      retval = CipherUtil.decryptString( cipherText );
    }

    // return either null or the results of our decryption
    return retval;
  }




  /**
   * Retrieves the named configuration property as a boolean; defaults to FALSE.
   * 
   * <p>This performs a case insensitive search in much the same manner as 
   * {@code #getString(String)} and uses the value to determine the boolean value.</p>
   * 
   * <p>A case insensitive value of {@code true}, {@code yes}, {@code 1}, 
   * {@code t},or {@code y} results in true being returned. All other values 
   * result in false including the case where the named attribute is missing.</p>
   *  
   * @param name the name of the property to retrieve
   * 
   * @return the value of the named property as a boolean or false if it was not found.
   */
  public boolean getBoolean( String name ) {
    String value = getString( name );
    if ( StringUtil.isNotBlank( value ) ) {
      return ( "true".equalsIgnoreCase( value ) || "yes".equalsIgnoreCase( value ) || "1".equalsIgnoreCase( value ) || "y".equalsIgnoreCase( value ) || "t".equalsIgnoreCase( value ) );
    }
    return false;
  }




  /**
   * @param name the name of the property to retrieve
   * 
   * @return the value of the named property as an integer or 0 if it was not found.
   */
  public int getInteger( String name ) {
    String value = getString( name );
    if ( StringUtil.isNotBlank( value ) ) {
      try {
        return Integer.parseInt( value );
      } catch ( NumberFormatException ignore ) {}
      return 0;
    }

    return 0;
  }




  /**
   * Convenience method that allows for the checking of the existence of a 
   * named field.
   * 
   * @param name The name of the field for which to search.
   * 
   * @return True if the field with the exact given name exists, false 
   *         otherwise.
   * 
   * @see #containsIgnoreCase(String)
   */
  public boolean contains( String name ) {
    return configuration.contains( name );
  }




  /**
   * Convenience method that allows for the checking of the existence of a 
   * named field ignoring differences in case.
   * 
   * @param name The name of the field for which to search.
   * 
   * @return True if the field with the given name exists (ignoring differences 
   *         in case), false otherwise.
   * 
   * @see #contains(String)
   */

  public boolean containsIgnoreCase( String name ) {
    return configuration.containsIgnoreCase( name );
  }




  /**
   * @param context the transform context to set
   */
  public void setContext( TransformContext context ) {
    this.context = context;
  }

}
