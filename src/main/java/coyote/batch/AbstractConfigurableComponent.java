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

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.security.BlowfishCipher;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractConfigurableComponent implements ConfigurableComponent {

  protected DataFrame configuration = new DataFrame();
  protected TransformContext context = null;




  /**
   * @see coyote.batch.ConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    configuration = frame;
  }




  /**
   * @see coyote.batch.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see coyote.batch.ConfigurableComponent#getConfiguration()
   */
  @Override
  public DataFrame getConfiguration() {
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
   * Perform a case insensitive search for a configuration value with the given 
   * name.
   * 
   * @param key than name of the configuration parameter to find
   * 
   * @return the string value of the configuration parameter or null if it is
   *         not found.
   * 
   * @see #getString(String) for a fully resolved search including the context
   */
  public String findString( String key ) {
    String retval = null;
    // Normally we would just call getAsString(key) on the config, but that is 
    // case sensitive so we will check each frame ourselves
    for ( DataField field : getConfiguration().getFields() ) {
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( key ) ) {
        retval = field.getStringValue();
        break;
      }
    }
    return retval;
  }




  /**
   * Return the configuration value with the given (case insensitive) key.
   * 
   * <p>Check the configuration first and return that value, but if there is no 
   * value set (null) then check the transform context for a value with the 
   * given key.</p>
   * 
   * <p>The value is treated as a template and will be resolved against the 
   * symbol table currently set in the context.</p>
   * 
   * <p>This is a case insensitive search, for usability.</p>
   * 
   * <p>NOTE: if you are looking for a value to later use to look up a value in 
   * the context, what will happen is the context lookup portion of this method 
   * will probably return the object value in the context and performing a 
   * {@code toString()} on that object which is probably not what you want. For 
   * this use case, use the {@code findString()} method for roughly equivalent 
   * functionality without the context lookup or template resolution.</p>
   * 
   * @param key the name of the configuration parameter to return
   * 
   * @return the value with that name in the configuration or null if the given 
   *         key is null, or the configuration value with that name could not 
   *         be found in either the configuration of transform context.
   * 
   * @see #findString(String) to just locate the raw value in the configuration 
   *      without context or template resolution.
   */
  public String getString( String key ) {
    String value = null;

    // Perform a case insensitive search for the value with the given key
    value = this.findString( key );
    
    if ( context != null ) {
      // See if there is a match in the context for reference resolution
      if ( value != null ) {
        // perform a case sensitive search for the value in the context
        String cval = context.getAsString( value, true );

        // if an exact match was found in the context...
        if ( cval != null ) {
          // ...resolve the value as a template
          return Template.resolve( cval, context.getSymbols() );
        }
      }

      if ( value == null ) {
        // perform a case insensitive search in the context
        value = context.getAsString( key, false );
      }
      String retval = Template.resolve( value, context.getSymbols() );

      // Only log if the value changed
      if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
        if ( retval != null && !retval.equals( value ) ) {
          Log.debug( LogMsg.createMsg( Batch.MSG, "Component.resolved_value", value, retval ) );
        }
      }

      return retval;
    } else {
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
