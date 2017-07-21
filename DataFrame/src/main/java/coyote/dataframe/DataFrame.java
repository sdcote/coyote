/*
 * DataFrame - a data marshaling toolkit
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.dataframe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import coyote.commons.ByteUtil;


/**
 * A hierarchical unit of data.
 * 
 * <p>This models a unit of data that can be exchanged over a variety of 
 * transports for a variety communications needs.
 * 
 * <p>This is a surprisingly efficient transmission scheme as all field values 
 * and child frames are stored in their wire format as byte arrays. They are 
 * then marshaled only when accessed and are ready for transmission.
 * 
 * <p>This class was conceived to implement the Data Transfer Object (DTO) 
 * design pattern in distributed applications. Passing a DataFrame as both 
 * argument and return values in remote service calls. Using this 
 * implementation of a DTO allows for more efficient transfer of data between
 * distributed components, reducing latency, improving throughput and 
 * decoupling not only the components of the system, but moving business logic
 * out of the data model.
 * 
 * <p>More recently, this class ha proven to be an effective implementation of 
 * the Value Object pattern and has made representing database rows and objects 
 * relatively easy to code. It has several features which make this class more 
 * feature rich than implementing VOs with Maps or other map-based structures 
 * such as properties. Most of the recent upgrades have been directly related 
 * to VO implementations. 
 */
public class DataFrame implements Cloneable {

  /** The array of fields this frame holds */
  protected ArrayList<DataField> fields = new ArrayList<DataField>();

  /** Flag indicating the top-level elements of this frame has been changed. */
  protected volatile boolean modified = false;

  /** Static flag indicating encoded fields should be checked. */
  private static boolean CHECK = false;

  /** flag indicating the data in this frame should be treated as an array; no named fields should be added, marshaling rules, etc. */
  protected volatile boolean arrayBiased = false;




  /**
   * Construct an empty frame.
   */
  public DataFrame() {}




  /**
   * Convenience constructor for a frame wrapping the given field.
   * 
   * @param field the field to place in this frame 
   */
  public DataFrame( DataField field ) {
    fields.add( field );
  }




  /**
   * Construct the frame with the given bytes.
   *
   * @param data The byte array from which to construct the frame.
   */
  public DataFrame( final byte[] data ) {
    if ( data != null ) {
      int loc = 0;
      int ploc = 0;
      try {
        final ByteArrayInputStream bais = new ByteArrayInputStream( data );
        final DataInputStream in = new DataInputStream( bais );

        while ( in.available() > 0 ) {
          ploc = loc;
          loc = data.length - in.available();
          add( new DataField( in ) );
        }
      } catch ( final EOFException eof ) {
        throw new DecodeException( "Data underflow adding field", eof, loc, ploc, ( fields.size() + 1 ), ( fields.size() > 0 ) ? fields.get( fields.size() - 1 ) : null );
      } catch ( final IOException ioe ) {
        throw new DecodeException( "Problems decoding field", ioe, loc, ploc, ( fields.size() + 1 ), ( fields.size() > 0 ) ? fields.get( fields.size() - 1 ) : null );
      } catch ( final DecodeException de ) {
        throw new DecodeException( "DF:" + de.getMessage(), de.getCause(), loc, ploc, de.getFieldIndex(), de.getField() );
      }
    }
  }




  /**
   * Return the first occurrence of a named field.
   *
   * @param name The name of the field to return.
   *
   * @return The first occurrence of the named frame field or null if a frame 
   *         Field with the given name was not found.
   *
   * @see #getFieldIgnoreCase(String)
   */
  public DataField getField( final String name ) {
    for ( int i = 0; i < fields.size(); i++ ) {
      final DataField field = fields.get( i );

      if ( ( field.getName() != null ) && field.getName().equals( name ) ) {
        return field;
      }
    }

    return null;
  }




  /**
   * Convenience method that allows for retrieving the first field with the 
   * given name ignoring differences in case.
   * 
   * @param name The name of the field for which to search.
   * 
   * @return the first field with the given name (ignoring differences in case) 
   *         or null if a field was not found with that name.
   * 
   * @see #getField(String)
   */
  public DataField getFieldIgnoreCase( final String name ) {
    for ( int i = 0; i < fields.size(); i++ ) {
      final DataField field = fields.get( i );
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( name ) ) {
        return field;
      }
    }
    return null;
  }




  /**
   * Convenience method that allows for the checking of the existence of a 
   * named field.
   * 
   * <p>This is essentially the same as calling: 
   * <pre><code>( getField("NAME") != null )</code></pre>
   * when checking for a field named "NAME", only it reads nicer.
   *  
   * @param name The name of the field for which to search.
   * 
   * @return True if the field with the exact given name exists, false 
   *         otherwise.
   * 
   * @see #containsIgnoreCase(String)
   */
  public boolean contains( final String name ) {
    for ( int i = 0; i < fields.size(); i++ ) {
      final DataField field = fields.get( i );
      if ( ( field.getName() != null ) && field.getName().equals( name ) ) {
        return true;
      }
    }
    return false;
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
  public boolean containsIgnoreCase( final String name ) {
    for ( int i = 0; i < fields.size(); i++ ) {
      final DataField field = fields.get( i );
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( name ) ) {
        return true;
      }
    }
    return false;
  }




  /**
   * Return the given occurrence of a field.
   *
   * @param indx The zero-based index of the field to return.
   *
   * @return The indexed occurrence of an frame Field or null if the index is 
   *         out of range or less than zero.
   */
  public DataField getField( final int indx ) {
    if ( ( indx < fields.size() ) && ( indx > -1 ) ) {
      return fields.get( indx );
    }

    return null;
  }




  /**
   * @return The number of fields in the frame.
   */
  public int getFieldCount() {
    return fields.size();
  }




  /**
   * Return the value of the named field as a string.
   * 
   * @param name The name of the field to retrieve.
   * 
   * @return The string value of the first field with the given name or null if 
   *         the field could not be found.
   */
  public String getAsString( final String name ) {
    final Object val = getObject( name );
    if ( val != null ) {
      return val.toString();
    }
    return null;
  }




  private Date asDate( Object val ) throws DataFrameException {
    if ( val != null ) {
      if ( val instanceof Date ) {
        return ( (Date)val );
      } else if ( val instanceof Long ) {
        try {
          return new Date( Long.parseLong( val.toString() ) );
        } catch ( Exception e ) {
          throw new DataFrameException( "Could not convert long type'" + val.getClass().getSimpleName() + "' to a date" );
        }
      } else {
        throw new DataFrameException( "Could not convert type'" + val.getClass().getSimpleName() + "' to a date" );
      }
    }
    throw new DataFrameException( "Value could not be found" );
  }




  /**
   * Convert the given object into a boolean
   *  
   * @param val the object to convert
   * 
   * @return the boolean value of the object
   * 
   * @throws DataFrameException if the object was null or not able to be parsed 
   *         into a long value
   */
  protected boolean asBoolean( Object val ) throws DataFrameException {
    if ( val != null ) {
      if ( val instanceof Boolean ) {
        return ( (Boolean)val ).booleanValue();
      } else if ( val instanceof String ) {
        String str = ( (String)val ).toLowerCase();
        if ( "true".equals( str ) || "1".equals( str ) || "yes".equals( str ) ) {
          return true;
        } else if ( "false".equals( str ) || "0".equals( str ) || "no".equals( str ) ) {
          return false;
        } else {
          try {
            return Long.parseLong( val.toString() ) > 0;
          } catch ( Exception e ) {
            return Double.parseDouble( val.toString() ) > 0;
          }
        }
      } else if ( val instanceof Number ) {
        if ( val instanceof Integer ) {
          return Integer.parseInt( val.toString() ) > 0;
        } else if ( val instanceof Long ) {
          return Long.parseLong( val.toString() ) > 0;
        } else if ( val instanceof Float ) {
          return Float.parseFloat( val.toString() ) > 0;
        } else if ( val instanceof Double ) {
          return Double.parseDouble( val.toString() ) > 0;
        } else if ( val instanceof Short ) {
          return Short.parseShort( val.toString() ) > 0;
        } else {
          throw new DataFrameException( "Could not convert numeric type'" + val.getClass().getSimpleName() + "' to a boolean" );
        }
      } else {
        throw new DataFrameException( "Could not convert type'" + val.getClass().getSimpleName() + "' to a boolean" );
      }
    }
    throw new DataFrameException( "Null Value could not be converted" );
  }




  /**
   * Convenience method to return the value of the named field as a boolean
   * value.
   * 
   * @param name name of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of 
   *         the found field could not be parsed or converted to a boolean 
   *         value.
   */
  public boolean getAsBoolean( String name ) throws DataFrameException {
    return asBoolean( getObject( name ) );
  }




  /**
   * Convenience method to return the value of the indexed field as a boolean 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of 
   *         the found field could not be parsed or converted to a boolean 
   *         value.
   */
  public boolean getAsBoolean( final int indx ) throws DataFrameException {
    return asBoolean( getObject( indx ) );
  }




  /**
   * Convenience method to return the value of the named field as a Date value.
   * 
   * @param name name of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of 
   *         the found field could not be parsed or converted to a date value.
   */
  public Date getAsDate( String name ) throws DataFrameException {
    return asDate( getObject( name ) );
  }




  /**
   * Convenience method to return the value of the indexed field as a Date 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of 
   *         the found field could not be parsed or converted to a Date value.
   */
  public Date getAsDate( final int indx ) throws DataFrameException {
    return asDate( getObject( indx ) );
  }




  /**
   * Convert the given object into a integer
   *  
   * @param val the object to convert
   * 
   * @return the integer value of the object
   * 
   * @throws DataFrameException if the object was null or not able to be parsed 
   *         into an integer value
   */
  private int asInt( Object val ) throws DataFrameException {
    if ( val != null ) {
      if ( val instanceof Integer ) {
        return ( (Integer)val ).intValue();
      } else {
        try {
          return Integer.parseInt( val.toString() );
        } catch ( Exception e ) {
          throw new DataFrameException( "Value could not be converted into an integer" );
        }
      }
    }
    throw new DataFrameException( "Value could not be found" );
  }




  /**
   * Convenience method to return the value of the named field as an integer
   * value.
   * 
   * @param name name of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to an integer value.
   */
  public int getAsInt( String name ) throws DataFrameException {
    return asInt( getObject( name ) );
  }




  /**
   * Convenience method to return the value of the indexed field as an integer 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to an integer value.
   */
  public int getAsInt( final int indx ) throws DataFrameException {
    return asInt( getObject( indx ) );
  }




  /**
   * Convert the given object into a long
   *  
   * @param val the object to convert
   * 
   * @return the long value of the object
   * 
   * @throws DataFrameException if the object was null or not able to be parsed 
   *         into a long value
   */
  private long asLong( Object val ) throws DataFrameException {
    if ( val != null ) {
      if ( val instanceof Long ) {
        return ( (Long)val ).longValue();
      } else {
        try {
          return Long.parseLong( val.toString() );
        } catch ( Exception e ) {
          throw new DataFrameException( "Value could not be converted into a long" );
        }
      }
    }
    throw new DataFrameException( "Value could not be found" );
  }




  /**
   * Convenience method to return the value of the named field as a long
   * value.
   * 
   * @param name name of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a long value.
   */
  public long getAsLong( String name ) throws DataFrameException {
    return asLong( getObject( name ) );
  }




  /**
   * Convenience method to return the value of the indexed field as a long 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a long value.
   */
  public long getAsLong( final int indx ) throws DataFrameException {
    return asLong( getObject( indx ) );
  }




  /**
   * Convert the given object into a double
   *  
   * @param val the object to convert
   * 
   * @return the long value of the object
   * 
   * @throws DataFrameException if the object was null or not able to be parsed 
   *         into a double value
   */
  private double asDouble( Object val ) throws DataFrameException {
    if ( val != null ) {
      if ( val instanceof Double ) {
        return ( (Double)val ).doubleValue();
      } else {
        try {
          return Double.parseDouble( val.toString() );
        } catch ( Exception e ) {
          throw new DataFrameException( "Value could not be converted into a double" );
        }
      }
    }
    throw new DataFrameException( "Value could not be found" );
  }




  /**
   * Convenience method to return the value of the named field as a double
   * value.
   * 
   * @param name name of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a double value.
   */
  public double getAsDouble( String name ) throws DataFrameException {
    return asDouble( getObject( name ) );
  }




  /**
   * Convenience method to return the value of the indexed field as a double 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a double value.
   */
  public double getAsDouble( final int indx ) throws DataFrameException {
    return asDouble( getObject( indx ) );
  }




  /**
   * Convert the given object into a float
   *  
   * @param val the object to convert
   * 
   * @return the long value of the object
   * 
   * @throws DataFrameException if the object was null or not able to be parsed 
   *         into a float value
   */
  private float asFloat( Object val ) throws DataFrameException {
    if ( val != null ) {
      if ( val instanceof Float ) {
        return ( (Float)val ).longValue();
      } else {
        try {
          return Float.parseFloat( val.toString() );
        } catch ( Exception e ) {
          throw new DataFrameException( "Value could not be converted into a float" );
        }
      }
    }
    throw new DataFrameException( "Value could not be found" );
  }




  /**
   * Convenience method to return the value of the named field as a float
   * value.
   * 
   * @param name name of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a float value.
   */
  public float getAsFloat( String name ) throws DataFrameException {
    return asFloat( getObject( name ) );
  }




  /**
   * Convenience method to return the value of the indexed field as a float 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a float value.
   */
  public float getAsFloat( final int indx ) throws DataFrameException {
    return asFloat( getObject( indx ) );
  }




  /**
   * Convenience method to return the value of the indexed field as a String 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field
   * 
   * @throws DataFrameException if the field does not exist or if the value of the 
   *         found field could not be parsed or converted to a String value.
   */
  public String getAsString( final int indx ) throws DataFrameException {
    final Object val = getObject( indx );
    if ( val != null ) {
      return val.toString();
    }
    throw new DataFrameException( "Indexed field does not exist" );
  }




  /**
   * Return the value of the named field as a string.
   * 
   * @param name The name of the field to retrieve.
   * 
   * @return The value of the first field with the given name or null if the 
   * field with the given name could not be found.
   * 
   * @throws DataFrameException if the type of the found field is not a DataFrame.
   */
  public DataFrame getAsFrame( final String name ) throws DataFrameException {
    final Object val = getObject( name );
    if ( val != null ) {
      if ( val instanceof DataFrame ) {
        return (DataFrame)val;
      } else {
        throw new DataFrameException( "Named field is not a frame" );
      }
    } else {
      return null;
    }
  }




  /**
   * Convenience method to return the value of the indexed field as a DataFrame 
   * value.
   * 
   * @param indx Index of the field value to return.
   * 
   * @return the value of the field or null if not found
   * 
   * @throws DataFrameException if the type of the found field is not a DataFrame.
   */
  public DataFrame getAsFrame( final int indx ) throws DataFrameException {
    final Object val = getObject( indx );
    if ( val != null ) {
      if ( val instanceof DataFrame ) {
        return (DataFrame)val;
      } else {
        throw new DataFrameException( "Indexed field is not a frame" );
      }
    } else {
      return null;
    }

  }




  /**
   * Return the object value of the named field.
   *
   * @param name The name of the field containing the object to retrieve.
   *
   * @return The object value of the first occurrence of the named field or null 
   *         if the field with the given name was not found.
   */
  public Object getObject( final String name ) {
    for ( int i = 0; i < fields.size(); i++ ) {
      final DataField field = fields.get( i );

      if ( ( field.getName() != null ) && field.getName().equals( name ) ) {
        return field.getObjectValue();
      }
    }

    return null;
  }




  /**
   * Return the object value of the indexed field.
   *
   * @param i The zero-based index of the field to return. The first element is
   *          at index zero, the second is at index 1 and so on.
   *
   * @return The object value of the field at the given index, or null if there 
   *         was no value at that index (out-of-bounds)
   */
  public Object getObject( final int i ) {
    if ( i < fields.size() ) {
      return ( fields.get( i ) ).getObjectValue();
    }

    return null;
  }




  /**
   * Create a deep-copy of this frame.
   * 
   * @return a clone of this DataFrame
   */
  public Object clone() {
    final DataFrame retval = new DataFrame();

    // Clone all the fields
    for ( int i = 0; i < fields.size(); i++ ) {
      retval.fields.add( i, (DataField)fields.get( i ).clone() );
    }
    retval.modified = false;

    return retval;
  }




  /**
   * Create a frame with a field with the given name and value.
   *
   * @param name The name of the field to populate.
   * @param value The value to place in the named field
   */
  public DataFrame( final String name, final Object value ) {
    add( name, value );
    modified = false;
  }




  /**
   * Add a new field with the given value without a name.
   * 
   * <p><strong>NOTICE:</strong> Because values are added as byte arrays and
   * not references, only complete frames can be added. This is because child 
   * frames are stored as their wire format at the time of their being added. 
   * Any fields added to the child after being added to the parent <strong>will 
   * not have their values represented in the child frame</strong>. This is 
   * partially by design as it is then possible to use one frame in the 
   * creation of all children. Also, storing the values in wire format reduces 
   * the number of times field values are marshaled, thereby improving overall
   * performance.  
   *
   * @param value The value to place in the un-named field
   *
   * @return the index of the field just added.
   */
  public int add( final Object value ) {
    modified = true;
    if ( value instanceof DataField ) {
      fields.add( (DataField)value );
    } else {
      fields.add( new DataField( value ) );
    }
    return fields.size() - 1;
  }




  /**
   * Add a frame field with the given name and value.
   * 
   * <p>The resulting frame field will be added to the frame with its type 
   * being determined by the DataField class.
   *
   * @param name The name of the field to populate.
   * @param value The value to place in the named field
   *
   * @return the index of the placed value.
   * 
   * @throws IllegalArgumentException If the name is longer than 255 characters 
   *         or the value is an unsupported type.
   */
  public int add( final String name, final Object value ) {
    modified = true;
    fields.add( new DataField( name, value ) );
    return fields.size() - 1;
  }




  /**
   * Add a frame field to the existing array of fields.
   * 
   * <p>Null fields will not be added to the list and will retun a negative 
   * index.
   * 
   * @param field The field to place in the frames array of fields
   * 
   * @return the index of the placed value or -1 if the given frame is null.
   */
  public int add( final DataField field ) {
    if ( field != null ) {
      modified = true;
      fields.add( field );
      return fields.size() - 1;
    } else {
      return -1;
    }
  }




  /**
   * Place the object in the frame under the given name, overwriting any
   * existing object with the same name.
   * 
   * <p>Note: this is different from <tt>add(String,Object)</tt> in that 
   * this method will not duplicate names.
   *
   * @param name The name of the field in which the value is to be placed. 
   * @param obj The value to place.
   * 
   * @return The index of the field the value was placed.
   */
  public int put( final String name, final Object obj ) {
    if ( ( obj != null ) || ( name != null ) ) {
      if ( name != null ) {
        for ( int i = 0; i < fields.size(); i++ ) {
          final DataField field = fields.get( i );

          if ( ( field.name != null ) && field.name.equals( name ) ) {
            if ( obj != null ) {
              field.type = DataField.getType( obj );
              field.value = DataField.encode( obj );
            } else {
              // Null object implies remove the named field
              fields.remove( i );
            }

            modified = true;

            return i;
          }
        }

        return add( name, obj );
      } else {
        return add( obj );
      }
    }

    return -1;
  }




  /**
   * Remove the first occurrence of a DataField with the given name.
   *
   * @param name name of the DataField to remove.
   * 
   * @return The DataField that was removed.
   */
  public DataField remove( final String name ) {
    DataField retval = null;
    if ( name != null ) {
      for ( int i = 0; i < fields.size(); i++ ) {
        final DataField field = fields.get( i );

        if ( ( field.name != null ) && field.name.equals( name ) ) {
          retval = fields.remove( i );

          modified = true;
        }
      }
    }
    return retval;
  }




  /**
   * Remove the first field with the given name and add a new field with the 
   * given name and new object value.
   * 
   * <p>This is equivalent to calling:<pre><code>
   * {
   *   remove( name );
   *   add( name, obj );
   * }
   * </code></pre>
   * 
   * <p><strong>NOTE:</strong> The value is not checked prior to removing the 
   * existing field which means if the object is not supported, the end state 
   * of the frame will be the named field will have been removed from the 
   * frame and no value replacing it. This is by design, allowing the invalid 
   * value to be deleted as desired and also because it saves time not having 
   * to check for existence prior to each replace.
   *  
   * @param name Name of the field to replace and then add.
   * @param obj The value of the object to set in the new field.
   */
  public void replace( final String name, final Object obj ) {
    remove( name );
    add( name, obj );
  }




  /**
   * Remove all the fields with the given name and add a single new field with 
   * the given name and new object value.
   * 
   * <p>This is equivalent to calling:<pre><code>
   * {
   *   removeAll( name );
   *   add( name, obj );
   * }
   * </code></pre>
   * 
   * <p><strong>NOTE:</strong> The value is not checked prior to removing the 
   * existing fields which means if the object is not supported, the end state 
   * of the frame will be the named fields will have been removed from the 
   * frame and no value replacing it. This is by design, allowing the invalid 
   * value to be deleted as desired and also because it saves time not having 
   * to check for existence prior to each replace.
   *  
   * @param name Name of the fields to replace and then add.
   * @param obj The value of the object to set in the new field.
   */
  public void replaceAll( final String name, final Object obj ) {
    removeAll( name );
    add( name, obj );
  }




  /**
   * Remove all occurrences of DataFields with the given name.
   *
   * @param name name of the DataField to remove.
   */
  public void removeAll( final String name ) {
    modified = true;

    if ( name != null ) {
      for ( int i = 0; i < fields.size(); i++ ) {
        final DataField field = fields.get( i );

        if ( ( field.name != null ) && field.name.equals( name ) ) {
          fields.remove( i-- );
        }
      }
    }
  }




  /**
   * Generate a digest fingerprint for this frame based solely on the wire format
   * of this and all frames contained therein.
   * 
   * <p>This performs a SHA-1 digest on the payload to help determine a unique
   * identifier for the frame. Note: the digest can be used to help determine
   * equivalence between frames.
   * 
   * <p><strong>NOTE:</strong> This is a very expensive function and should not 
   * be used without due consideration.
   *
   * @return the SHA-1 digest for this frame.
   */
  public byte[] getDigest() {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance( "SHA-1" );
    } catch ( final NoSuchAlgorithmException e ) {
      e.printStackTrace();
      return null;
    }
    digest.reset();
    digest.update( getBytes() );
    return digest.digest();
  }




  /**
   * Generate a digest fingerprint for this message based solely on the wire
   * format of the payload and returns it as a Hex string.
   *
   * <p><strong>NOTE:</strong> This is a very expensive function and should not 
   * be used without due consideration.
   *
   * @see #getDigest()
   * 
   * @return A String representation of the digest of the payload.
   */
  public String getDigestString() {
    return ByteUtil.bytesToHex( getDigest() );
  }




  /**
   * Get a copy of the frame in its wire format.
   * 
   * <p>This is a way to serialize the frame for any medium that supports binary
   * data. The resultant byte array may then be used to the 
   * <code>DataFrame(byte[])</code> constructor to reconstitute the frame.
   *
   * @return this frame represented in its wire format.
   */
  public byte[] getBytesOrig() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream( baos );

    try {
      for ( int i = 0; i < fields.size(); i++ ) {
        dos.write( fields.get( i ).getBytes() );
      }
    } catch ( final IOException e ) {
      e.printStackTrace();
    }

    return baos.toByteArray();
  }




  /**
   * Get a copy of the frame in its wire format.
   * 
   * <p>This is a way to serialize the frame for any medium that supports binary
   * data. The resultant byte array may then be used to the 
   * <code>DataFrame(byte[])</code> constructor to reconstitute the frame.
   *
   * @return this frame represented in its wire format.
   */
  public byte[] getBytes() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream( baos );
    byte[] bytes = null;

    try {
      for ( int i = 0; i < fields.size(); i++ ) {
        bytes = fields.get( i ).getBytes();
        if ( CHECK ) {
          String error = check( bytes );
          if ( error != null )
            throw new DecodeException( error, bytes );
        }

        dos.write( bytes );
      }
    } catch ( final IOException e ) {
      throw new DecodeException( "IO Error", e );
    }

    return baos.toByteArray();
  }




  /**
   * Perform a decode of the field data returning any problems in a diagnostic 
   * error string.
   * 
   * <p>During development, it is useful to check the encoding of data fields 
   * when new data types are added to the library and when new applications of 
   * the library start generating errors. This allow each encoded data field to 
   * be decoded with a byte dump performed when exceptions are thrown.
   *  
   * @param data byte array containing the encoded data field
   * 
   * @return diagnostic text if there were problems, null if the check succeeded.
   */
  public String check( final byte[] data ) {
    if ( data != null ) {
      DataField field = null;
      DataField lastfield = null;
      int offset = 0;

      try {
        final ByteArrayInputStream bais = new ByteArrayInputStream( data );
        final DataInputStream in = new DataInputStream( bais );
        while ( in.available() > 0 ) {
          offset = data.length - in.available();
          field = new DataField( in );
          if ( field != null )
            lastfield = field;
        }
      } catch ( final EOFException eof ) {
        return new String( "CHECK: Data underflow for field, offset:" + offset + " LastField:" + lastfield + "\r\n" + ByteUtil.dump( data ) );
      } catch ( final IOException ioe ) {
        return new String( "CHECK: " + ioe.getMessage() + ", offset:" + offset + " LastField:" + lastfield + "\r\n" + ByteUtil.dump( data ) );
      } catch ( final DecodeException de ) {
        return new String( "CHECK: " + de.getMessage() + ", offset:" + offset + " LastField:" + lastfield + "\r\n" + ByteUtil.dump( data ) );
      }
    }
    return null;
  }




  /**
   * Get the byte[] with this name.
   * 
   * <p>Basically, this will present the wire format of the named field.
   *
   * @param name The name of the field to query.
   *
   * @return The bytes[] value or null
   */
  public byte[] getBytes( final String name ) {
    final Object retval = getObject( name );

    if ( ( retval != null ) && ( retval instanceof byte[] ) ) {
      return (byte[])retval;
    }

    return null;
  }




  /**
   * Obtain the reference to the ordered list of DataFields making up this
   * frame.
   * 
   * <p>Changing this list changes the actual contents of the frame. <strong>Be 
   * Careful!</strong>
   *
   * @return The list of frame fields in this frame.
   */
  public List<DataField> getFields() {
    return fields;
  }




  /**
   * Set the ArrayList as the backing collection for this frame.
   * 
   * <p><strong>WARNING!</strong> all the elements MUST be DataFields or this 
   * frame will throw class cast exceptions whenever it tries to access the 
   * fields as no casting checks are performed on the backing list. Your code
   * probably should not use this method.
   * 
   * @param list An ordered list of DataFields.
   */
  public void setFields( final ArrayList<DataField> list ) {
    fields = list;
    modified = true;
  }




  /**
   * @return Returns true if this frame has been modified, false otherwise.
   */
  public boolean isModified() {
    return modified;
  }




  /**
   * Remove all the fields from this frame.
   * 
   * <p>The frame will be empty after this method is called.
   */
  public void clear() {
    fields.clear();
  }




  /**
   * @return The number of types supported/
   */
  public int getTypeCount() {
    return DataField.typeCount();
  }




  /**
   * This will return a list of unique field names in this data frame.
   * 
   * <p>Note that fields are not required to have names. They can be anonymous 
   * and accessed by their index in the frame. Therefore it is possible that 
   * some fields will be inaccessible by name and will not be represented in 
   * the returned list of names.
   * 
   * @return a list of field names in this frame.
   */
  public List<String> getNames() {
    List<String> retval = new ArrayList<String>();

    // get a list of unique field names
    Set<String> names = new HashSet<String>();
    for ( int i = 0; i < fields.size(); names.add( fields.get( i++ ).getName() ) );

    retval.addAll( names );

    return retval;
  }




  /**
   * This is a very simple string representation of this data frame.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    if ( fields.size() > 0 ) {
      boolean isArray = ( this.isEmpty() && this.isArrayBiased() ) || this.isArray();
      if ( isArray )
        b.append( "[" );
      else
        b.append( "{" );

      for ( DataField field : fields ) {
        if ( !isArray ) {
          b.append( '"' );
          b.append( field.getName() );
          b.append( "\":" );
        }

        if ( field.getType() == DataField.UDEF ) {
          b.append( "null" );
        } else if ( field.getType() == DataField.BOOLEANTYPE ) {
          b.append( field.getStringValue().toLowerCase() );
        } else if ( field.isNumeric() ) {
          b.append( field.getStringValue() );
        } else if ( field.getType() == DataField.STRING ) {
          b.append( '"' );
          b.append( field.getStringValue() );
          b.append( '"' );
        } else if ( field.getType() == DataField.DATE ) {
          b.append( '"' );
          b.append( field.getStringValue() );
          b.append( '"' );
        } else if ( field.getType() == DataField.ARRAY ) {
          b.append( field.getStringValue() ); // includes the brackets already
        } else if ( field.getType() != DataField.FRAMETYPE ) {
          if ( field.getObjectValue() != null ) {
            b.append( '"' );
            b.append( field.getObjectValue().toString() );
            b.append( '"' );
          }
        } else {
          if ( field.isNull() ) {
            b.append( "null" );
          } else {
            b.append( field.getObjectValue().toString() );
          }
        }

        b.append( "," );
      }
      b.delete( b.length() - 1, b.length() );

      if ( isArray )
        b.append( "]" );
      else
        b.append( "}" );

    } else {
      b.append( "{}" );
    }
    return b.toString();
  }




  /**
   * @return the number of fields in this frame
   * @see java.util.Map#size()
   */
  public int size() {
    return fields.size();
  }




  /**
   * @return true if there are no fields in this frame, false if this frame contains data
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return fields.size() == 0;
  }




  /**
   * Scans through all the children and if none have a name, then the frame is 
   * considered an array.
   * 
   * <p>Empty frames can be treated as an array as there are no named fields. 
   * In this case it is helpful to consider {@link #isArrayBiased()} when 
   * handling the frame. 
   * 
   * @return true if all the children have no (null) name, false otherwise.
   */
  public boolean isArray() {
    boolean retval = true;
    for ( DataField field : fields ) {
      if ( field.name != null )
        return false;
    }
    return retval;
  }




  /**
   * Indicator as to the intent of how fields should be added and represented 
   * in this frame.
   * 
   * <p>When frames are created, they have no fields, this leave them in a 
   * state which can be difficult to represent my marshalers. They may be 
   * expected to contain all unnamed fields (an array) or have no such 
   * constraint. Additionally, logic may desire to know if this collection of 
   * fields should be limited to one type (like an array) or may contain 
   * different types. This flag helps to determine the bias of this collection
   * when it was first created.
   * 
   * <p>This bias is not enforced. It may be overridden or ignored as needs 
   * change. This is only an indicator of intent when the frame was created.
   * Named fields can be added later, and the types of fields may vary.
   * 
   * @return true if this collection of fields wants to be treated as an array
   */
  public boolean isArrayBiased() {
    return arrayBiased;
  }




  /**
   * @param flag true indicates this collection of fields should be reated as an array, false otherwise
   */
  public void setArrayBias( boolean flag ) {
    this.arrayBiased = flag;
  }




  /**
   * Places all of the fields in the given frame in this frame, overwriting the 
   * values with the same name.
   * 
   * <p>This is essentially a {@code put} operation for all the fields in the 
   * given frame. Contrast this to {@link #populate(DataFrame)}.
   * 
   * @param frame The frame from which the fields are read.
   */
  public void merge( DataFrame frame ) {
    for ( DataField field : frame.fields ) {
      this.put( field.getName(), field.getObjectValue() );
    }
  }




  /**
   * Places all of the fields in the given frame in this frame.
   * 
   * <p>Overwriting does not occur. This is a straight addition of fields. It 
   * is possible that multiple fields with the same name may exist after this 
   * operation. Contrast this to {@link #merge(DataFrame)}.
   * 
   * <p>This is essentially an {@code add} operation for all the fields in the 
   * given frame.
   * 
   * @param frame The frame from which the fields are read.
   */
  public void populate( DataFrame frame ) {
    for ( DataField field : frame.getFields() ) {
      add( field );
    }
  }




  /**
   * @param key the key for which to search
   * 
   * @return true if the frame contains a field exists with the exact given key, false otherwise
   *  
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey( Object key ) {
    return key != null && key instanceof String && contains( (String)key );
  }




  /**
   * This is not supported
   * 
   * @param value not supported
   * 
   * @return true if the given value is contained in this frame - always returns false
   * 
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue( Object value ) {
    return false;
  }




  /**
   * @param key the key of the valy for which to search
   * 
   * @return the object in this frame with the given key
   * 
   * @see java.util.Map#get(java.lang.Object)
   */
  public Object get( Object key ) {
    if ( key != null && key instanceof String )
      return this.getObject( (String)key );
    else
      return null;
  }




  /**
   * @param key then key (name) of the field to be placed
   * @param value  the value to place in the field
   * 
   * @return the previous value associated with key, or null if there was no 
   *         mapping for key. (A null return can also indicate that the map 
   *         previously associated null with key, if the implementation 
   *         upports null values.)
   * 
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Object put( Object key, Object value ) {
    Object retval = null;

    if ( key != null ) {

      if ( key instanceof String ) {
        String name = (String)key;

        for ( int i = 0; i < fields.size(); i++ ) {
          final DataField field = fields.get( i );

          if ( ( field.name != null ) && field.name.equals( name ) ) {

            if ( value != null ) {
              retval = field.getObjectValue();
              field.type = DataField.getType( value );
              field.value = DataField.encode( value );
            } else {
              // Null object implies remove the named field
              retval = fields.remove( i );
            }
            modified = true;

            return retval;
          } // found
        } // for

        // not found, add the value 
        return add( name, value );
      } else {
        // key is not a string
        throw new IllegalArgumentException( "DataFrame keys must be of type String" );
      }
    } else {
      add( value );
    }
    return retval;
  }




  /**
   * @param key the key of the object to be removed
   * 
   * @return the removed object value or null if the field with the given name 
   *         was not found in this frame.
   * 
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Object remove( Object key ) {
    if ( key != null && key instanceof String ) {
      DataField field = remove( (String)key );
      if ( field != null )
        return field.getObjectValue();
    }
    return null;
  }




  /**
   * @return the set of keys in this frame (i.e. field names)
   * 
   * @see java.util.Map#keySet()
   */
  public Set keySet() {
    // get a list of unique field names
    Set<String> names = new HashSet<String>();
    for ( int i = 0; i < fields.size(); names.add( fields.get( i++ ).getName() ) );
    return names;
  }




  /**
   * @return all the field values in this frame
   *  
   * @see java.util.Map#values()
   */
  public Collection values() {
    List<Object> retval = new ArrayList<Object>();
    for ( int i = 0; i < fields.size(); retval.add( fields.get( i++ ).getObjectValue() ) );
    return retval;
  }




  /**
   * @param flag true to check encoded fields by decoding them afterwards, 
   *        false to just encode fields.
   */
  public static void setCheckFlag( boolean flag ) {
    CHECK = flag;
  }




  /**
   * Set (add) a frame field with the given name and value and return a 
   * reference to the frame to which it was added (this) allowing the chaining 
   * of set methods for more readable code and simpler coding.
   * 
   * <p>The resulting frame field will be added to the frame with its type 
   * being determined by the DataField class.
   *
   * @param name The name of the field to populate.
   * @param value The value to place in the named field
   *
   * @return the the data frame (this) to which the data was added.
   * 
   * @throws IllegalArgumentException If the name is longer than 255 characters 
   *         or the value is an unsupported type.
   */
  public DataFrame set( final String name, final Object value ) {
    add( name, value );
    return this;
  }




  /**
   * Set (add) a new field with the given value without a name and return a 
   * reference to the frame to which it was added (this) allowing the chaining 
   * of set methods for more readable code and simpler coding.
   * 
   * <p><strong>NOTICE:</strong> Because values are added as byte arrays and
   * not references, only complete frames can be added. This is because child 
   * frames are stored as their wire format at the time of their being added. 
   * Any fields added to the child after being added to the parent <strong>will 
   * not have their values represented in the child frame</strong>. This is 
   * partially by design as it is then possible to use one frame in the 
   * creation of all children. Also, storing the values in wire format reduces 
   * the number of times field values are marshaled, thereby improving overall
   * performance.  
   *
   * @param value The value to place in the un-named field
   *
   * @return the the data frame (this) to which the data was added.
   */

  public DataFrame set( final Object value ) {
    add( value );
    return this;
  }

  /**
   * @see java.util.Map#putAll(java.util.Map)
   */
  //public void putAll( Map m ) {}

  /**
   * @see java.util.Map#entrySet()
   */
  //public Set entrySet() { return null; }

}
