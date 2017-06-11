/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.loader.cfg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.MarshalException;


/**
 * The Config class models a component that is used to make file-based
 * configuration of components easier than using property files.
 *
 * <p>The primary goal of this class is to allow hierarchical configurations to
 * be specified using different notations (such as JSON) as a formatting
 * strategy. Basic File and network protocol I/O is supported in a simple
 * interface.</p>
 */
public class Config extends DataFrame implements Cloneable, Serializable {

  public static final String CLASS = "Config";

  // Common configuration tags
  public static final String CLASS_TAG = "Class";
  public static final String NAME_TAG = "Name";
  public static final String ID_TAG = "ID";

  /** Serialization identifier */
  private static final long serialVersionUID = -6020161245846637528L;




  /**
   * Read a configuration from the given file.
   *
   * <p>This assumes the file contains a valid UTF-8 JSON format.
   *
   * @param file the file to read
   *
   * @return A usable Config reference.
   *
   * @throws IOException if there were problems reading the file
   * @throws ConfigurationException if there were issues creating a configuration object from the data read in from the file.
   */
  public static Config read( final File file ) throws IOException, ConfigurationException {
    return Config.read( new FileInputStream( file ) );
  }




  /**
   * Read the data from the given input stream and create an object from the
   * data read.
   *
   * <p>This assumes a UTF-8 JSON formatted stream of bytes.</p>
   *
   * @param configStream the input stream from which to read
   *
   * @return a configuration filled with the
   *
   * @throws ConfigurationException if there were issues creating a configuration object from the data read in from the stream.
   */
  public static Config read( final InputStream configStream ) throws ConfigurationException {
    final Config retval = new Config();

    final byte[] buffer = new byte[8192];
    int bytesRead;
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      while ( ( bytesRead = configStream.read( buffer ) ) != -1 ) {
        output.write( buffer, 0, bytesRead );
      }
    } catch ( final IOException e ) {
      throw new ConfigurationException( "Could not read configuration stream", e );
    }

    String data = null;
    try {
      data = new String( output.toByteArray(), "UTF-8" );
    } catch ( final UnsupportedEncodingException e ) {
      throw new ConfigurationException( "Could not read UTF-8", e );
    }

    if ( data != null ) {
      final List<DataFrame> config = JSONMarshaler.marshal( data );
      if ( ( config.size() > 0 ) && ( config.get( 0 ) != null ) ) {
        retval.populate( config.get( 0 ) );
      }
    }

    return retval;
  }




  /**
   * Read a configuration from the given named file.
   *
   * <p>This assumes the file contains a valid UTF-8 JSON format.
   *
   * @param filename the name of the file to read
   *
   * @return A usable Config reference.
   *
   * @throws IOException if there were problems reading the file
   * @throws ConfigurationException if there were issues creating a configuration object from the data read in from the file.
   */
  public static Config read( final String filename ) throws IOException, ConfigurationException {
    return Config.read( new FileInputStream( filename ) );
  }




  /**
   * Read a configuration from the given URI.
   *
   * <p>This assumes the URI represents a stream of valid UTF-8 JSON formattted
   * data.
   *
   * @param uri the URI of the stream to read
   *
   * @return A usable Config reference.
   *
   * @throws IOException if there were problems reading the file
   * @throws ConfigurationException if there were issues creating a configuration object from the data read in from the URI.
   */
  public static Config read( final URI uri ) throws IOException, ConfigurationException {
    if ( StringUtil.isNotBlank( uri.getScheme() ) ) {
      if ( uri.getScheme().toLowerCase().startsWith( "file" ) ) {
        return Config.read( new FileInputStream( UriUtil.getFile( uri ) ) );
      } else {
        return Config.read( uri.toURL().openStream() );
      }
    } else {
      // Assume this is a file path
      return Config.read( new FileInputStream( uri.toString() ) );
    }
  }

  /**
   * A collection of ConfigSlots we use to optionally validate the completeness
   * of the Config object or to provide default configurations.
   */
  private HashMap<String, ConfigSlot> slots = null;




  /**
   * Default constructor
   */
  public Config() {}




  /**
   * Create a new Config from a DataFrame.
   *
   * <p>This essentially wraps a clone of the frame with the Config accessor
   * methods.
   *
   * @param frame the frame to use as a source of data.
   */
  public Config( final DataFrame frame ) {
    populate( frame );
  }




  /**
   * Read a configuration from the given string.
   *
   * <p>This assumes the string contains a valid UTF-8 JSON format.
   *
   * @param data the string to read
   *
   * @throws ConfigurationException if there were issues creating a configuration object from the data read in from the file.
   */
  public Config( final String data ) throws ConfigurationException {
    try {
      if ( data != null ) {
        final List<DataFrame> config = JSONMarshaler.marshal( data );
        if ( ( config.size() > 0 ) && ( config.get( 0 ) != null ) ) {
          populate( config.get( 0 ) );
        }
      }
    } catch ( final MarshalException e ) {
      throw new ConfigurationException( "Could not read UTF-8", e );
    }
  }




  /**
   * Add the referenced ConfigSlot.
   *
   * @param slot the reference to the ConfigSlot to add.
   */
  public void addConfigSlot( final ConfigSlot slot ) {
    if ( slots == null ) {
      slots = new HashMap();
    }

    if ( slot != null ) {
      slots.put( slot.getName(), slot );
    }
  }




  /**
   * Return an Iterator over all the ConfigSlots
   *
   * @return an Iterator over all the ConfigSlot, never returns null;
   */
  public Iterator<ConfigSlot> configSlotIterator() {
    if ( slots != null ) {
      return slots.values().iterator();
    } else {
      return new Vector().iterator();
    }
  }




  /**
   * Perform a case insensitive search for the first value with the given name
   * and returns it as a boolean.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as a boolean
   *
   * @throws NumberFormatException if the field could not be found or if the value
   *          could not be parsed into a boolean.
   * @throws IllegalArgumentException if tag is null or empty.
   */
  public boolean getBoolean( final String tag ) throws NumberFormatException {
    if ( StringUtil.isNotEmpty( tag ) ) {
      for ( final DataField field : getFields() ) {
        if ( tag.equalsIgnoreCase( field.getName() ) ) {
          try {
            return asBoolean( field.getObjectValue() );
          } catch ( final DataFrameException e ) {
            throw new NumberFormatException( e.getMessage() );
          }
        }
      }
      throw new NumberFormatException( "Tag not found, cannot convert null to boolean" );
    }
    throw new IllegalArgumentException( "Tag argument is null or empty" );
  }




  /**
   * @return the value of the class tag, if present
   */
  public String getClassName() {
    return getString( CLASS_TAG );
  }




  /**
   * Retrieve a named ConfigSlot from the configuration
   *
   * @param name String which represents the name of the slot to retrieve
   *
   * @return value ConfigSlot object with the given name or null if it does
   *         not exist
   */
  public ConfigSlot getConfigSlot( final String name ) {
    if ( slots != null ) {
      synchronized( slots ) {
        return slots.get( name );
      }
    } else {
      return null;
    }
  }




  /**
   * Perform a case insensitive search for the first value with the given name
   * and returns it as a double.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as a double
   *
   * @throws NumberFormatException if the field could not be found or if the value
   *          could not be parsed into a double.
   */
  public double getDouble( final String tag ) throws NumberFormatException {
    return Double.parseDouble( getString( tag ) );
  }




  /**
   * Access the current number of elements set in this configuration.
   *
   * @return number of named values in this configuration
   */
  public int getElementCount() {
    return fields.size();
  }




  /**
   * Perform a case insensitive search for the first value with the given name
   * and returns it as a float.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as a float
   *
   * @throws NumberFormatException if the field could not be found or if the value
   *          could not be parsed into a float.
   */
  public float getFloat( final String tag ) throws NumberFormatException {
    return Float.parseFloat( getString( tag ) );
  }




  /**
   * @return the id of this config
   */
  public String getId() {
    return getString( ID_TAG );
  }




  /**
   * Perform a case insensitive search for the first value with the given name
   * and returns it as an integer.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as an integer
   *
   * @throws NumberFormatException if the field could not be found or if the value
   *          could not be parsed into an integer.
   */
  public int getInt( final String tag ) throws NumberFormatException {
    return Integer.parseInt( getString( tag ) );
  }




  /**
   * Perform a case insensitive search for the first value with the given name
   * and returns it as a long.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as a long
   *
   * @throws NumberFormatException if the field could not be found or if the value
   *          could not be parsed into a long.
   */
  public long getLong( final String tag ) throws NumberFormatException {
    return Long.parseLong( getString( tag ) );
  }




  /**
   * @return the name of this config
   */
  public String getName() {
    return getString( NAME_TAG );
  }




  /**
   * Return the first section with the given name.
   *
   * <p>This performs a case-insensitive search for the section.
   *
   * @param tag The name of the section for which to search
   *
   * @return The first section with a matching name or null if no section with that name exists
   */
  public Config getSection( final String tag ) {

    // If we have a tag for which to search...
    if ( StringUtil.isNotBlank( tag ) ) {
      // Look for the class to load
      for ( final DataField field : getFields() ) {
        if ( tag.equalsIgnoreCase( field.getName() ) && field.isFrame() ) {
          final Config cfg = new Config();
          cfg.populate( (DataFrame)field.getObjectValue() );
          return cfg;
        } // name match && a frame
      } // for
    } // tag != null

    return null;
  }




  /**
   * Return all the configuration sections within this section
   *
   * <p>This will not return scalar attributes, just the embedded sections.
   *
   * @return The list of sections. May be empty, but never null;
   */
  public List<Config> getSections() {
    final List<Config> retval = new ArrayList<Config>();

    // Look for the class to load
    for ( final DataField field : getFields() ) {
      if ( field.isFrame() ) {
        final Config cfg = new Config();
        if ( field.isNotNull() ) {
          cfg.populate( (DataFrame)field.getObjectValue() );
        }
        retval.add( cfg );
      } // name match && a frame
    } // for

    // return what we have found
    return retval;
  }




  /**
   * Return all the configuration sections with the given name.
   *
   * <p>This performs a case-insensitive search for the sections.
   *
   * @param tag The name of the section for which to search
   *
   * @return The list of sections with a matching name. May be empty, but never null;
   */
  public List<Config> getSections( final String tag ) {
    final List<Config> retval = new ArrayList<Config>();

    // If we have a tag for which to search...
    if ( StringUtil.isNotBlank( tag ) ) {
      // Look for the class to load
      for ( final DataField field : getFields() ) {
        if ( tag.equalsIgnoreCase( field.getName() ) && field.isFrame() ) {
          final Config cfg = new Config();
          cfg.populate( (DataFrame)field.getObjectValue() );
          retval.add( cfg );
        } // name match && a frame
      } // for
    } // tag != null

    // return what we have found
    return retval;
  }




  /**
   * Perform a case insensitive search for the first value with the given name
   * and returns it as a short.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as a short
   *
   * @throws NumberFormatException if the field could not be found or if the value
   *          could not be parsed into a short.
   */
  public short getShort( final String tag ) throws NumberFormatException {
    return Short.parseShort( getString( tag ) );
  }




  /**
   * Perform a case insensitive search for the first value with the given name.
   *
   * @param tag the name of the configuration attribute for which to search
   *
   * @return the first value with the given name as a string or null if not
   *         configuration field with that name was found, or if the found
   *         field contained a null value.
   */
  public String getString( final String tag ) {
    return getString( tag, true );
  }




  /**
   * Perform a search for the first value with the given name.
   *
   * @param tag the name of the configuration attribute for which to search
   * @param ignoreCase true to ignore the case of the tag, false for a strict, case sensitive match
   *
   * @return the first value with the given name as a string or null if not
   *         configuration field with that name was found, or if the found
   *         field contained a null value.
   */
  public String getString( final String tag, final boolean ignoreCase ) {
    if ( StringUtil.isNotBlank( tag ) ) {
      for ( final DataField field : getFields() ) {
        if ( tag.equals( field.getName() ) || ( ignoreCase && tag.equalsIgnoreCase( field.getName() ) ) ) {
          return field.getStringValue();
        }
      }
    }
    return null;
  }




  /**
   * Remove the referenced ConfigSlot
   *
   * @param slot The reference to the ConfigSlot to remove.
   */
  public void removeConfigSlot( final ConfigSlot slot ) {
    if ( slots == null ) {
      return;
    } else {
      synchronized( slots ) {
        slots.remove( slot );
      }
    }
  }




  /**
   * @param name the class name to set in this config
   */
  public void setClassName( final String name ) {
    put( Config.CLASS_TAG, name );
  }




  /**
   * Use the set configuration slots and prime the configuration with those
   * defaults.
   *
   * <p>This allows the caller to set a configuration object to the defaults.
   * This is useful a a starting point for configurable components when a
   * configuration has not been provided.</p>
   */
  public void setDefaults() {
    final Iterator<ConfigSlot> it = configSlotIterator();

    while ( it.hasNext() ) {
      final ConfigSlot slot = it.next();

      if ( slot != null ) {
        final Object defaultValue = slot.getDefaultValue();

        if ( defaultValue != null ) {
          put( slot.getName(), defaultValue );
        }
      }
    }

  }




  /**
   * @param id the id of the config to set
   */
  public void setId( final String id ) {
    this.put( ID_TAG, id );
  }




  /**
   * @param name the name of the config to set
   */
  public void setName( final String name ) {
    this.put( NAME_TAG, name );
  }




  /**
   * @return Formatted, multi-line JSON string representing the record.
   */
  public String toFormattedString() {
    return JSONMarshaler.toFormattedString( this );
  }

}
