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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;


/**
 * This is used by components needing to read and write fixed length field 
 * values in a text file.
 */
public class FieldDefinition {

  private short type = 0;
  private int start = 0;
  private int length = 0;
  private int alignment = 0;
  private String name = null;
  private boolean trimFlag = false;
  private String formatText = null;
  private DateFormat dateFormat = null;
  private DecimalFormat decimalFormat = null;




  public FieldDefinition( String name, String format, boolean trim ) {
    this( name, 0, 0, null, format, trim, 0 );
  }




  public FieldDefinition( String name, String type, String format, boolean trim ) {
    this( name, 0, 0, type, format, trim, 0 );
  }




  public FieldDefinition( String name, int start, int length, String type, String format, boolean trim ) {
    this( name, start, length, type, format, trim, 0 );
  }




  public FieldDefinition( String name, int start, int length, String type, String format, boolean trim, int align ) {
    if ( StringUtil.isBlank( name ) ) {
      throw new IllegalArgumentException( "Name is required for field definition" );
    } else {
      this.name = name;
    }
    if ( start < 0 ) {
      throw new IllegalArgumentException( "Start must be a positive value" );
    } else {
      this.start = start;
    }

    if ( length < 1 ) {
      throw new IllegalArgumentException( "Length must be greater than zero" );
    } else {
      this.length = length;
    }

    if ( trim ) {
      setTrimming( true );
    } else {
      setTrimming( false );
    }

    // normalize and set the alignment value
    if ( align < 1 ) {
      alignment = 0;
    } else if ( align > 1 ) {
      alignment = 2;
    } else {
      alignment = align;
    }

    if ( StringUtil.isBlank( type ) ) {
      this.type = DataField.STRING;
    } else {
      if ( type.equalsIgnoreCase( "STRING" ) || type.equalsIgnoreCase( "STR" ) ) {
        this.type = DataField.STRING;
      } else if ( type.equalsIgnoreCase( "S8" ) ) {
        this.type = DataField.S8;
      } else if ( type.equalsIgnoreCase( "U8" ) ) {
        this.type = DataField.U8;
      } else if ( type.equalsIgnoreCase( "SHORT" ) || type.equalsIgnoreCase( "S16" ) ) {
        this.type = DataField.S16;
      } else if ( type.equalsIgnoreCase( "U16" ) ) {
        this.type = DataField.U16;
      } else if ( type.equalsIgnoreCase( "INTEGER" ) || type.equalsIgnoreCase( "INT" ) || type.equalsIgnoreCase( "S32" ) ) {
        this.type = DataField.S32;
      } else if ( type.equalsIgnoreCase( "U32" ) ) {
        this.type = DataField.U32;
      } else if ( type.equalsIgnoreCase( "LONG" ) || type.equalsIgnoreCase( "S64" ) ) {
        this.type = DataField.S64;
      } else if ( type.equalsIgnoreCase( "U64" ) ) {
        this.type = DataField.U64;
      } else if ( type.equalsIgnoreCase( "FLOAT" ) || type.equalsIgnoreCase( "FLT" ) ) {
        this.type = DataField.FLOAT;
      } else if ( type.equalsIgnoreCase( "DOUBLE" ) || type.equalsIgnoreCase( "DBL" ) ) {
        this.type = DataField.DOUBLE;
      } else if ( type.equalsIgnoreCase( "BOOLEAN" ) || type.equalsIgnoreCase( "BOL" ) ) {
        this.type = DataField.BOOLEANTYPE;
      } else if ( type.equalsIgnoreCase( "DATE" ) || type.equalsIgnoreCase( "DAT" ) ) {
        this.type = DataField.DATE;
      } else if ( type.equalsIgnoreCase( "URI" ) ) {
        this.type = DataField.URI;
      } else {
        throw new IllegalArgumentException( "unsupported type specification" );
      }
    }

    if ( StringUtil.isNotBlank( format ) ) {
      formatText = format;
      if ( this.type == DataField.DATE ) {
        dateFormat = new SimpleDateFormat( format );
      } else {
        decimalFormat = new DecimalFormat( format );
      }
    }

  }




  /**
   * @return the type
   */
  public short getType() {
    return type;
  }




  /**
   * @param type the type to set
   */
  public void setType( short type ) {
    this.type = type;
  }




  /**
   * @return the start
   */
  public int getStart() {
    return start;
  }




  /**
   * @param start the start to set
   */
  public void setStart( int start ) {
    this.start = start;
  }




  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }




  /**
   * @param length the length to set
   */
  public void setLength( int length ) {
    this.length = length;
  }




  /**
   * @return the name
   */
  public String getName() {
    return name;
  }




  /**
   * @param name the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }




  /**
   * @return the end position (i.e. start+length).
   */
  public int getEnd() {
    return start + length;
  }




  /**
   * Convert the given string into the data type defined by this object.
   * 
   * <p>If the trimming flag is set, the returned values will be based on the 
   * trimmed version of the value, e.g. a String will have its leading and 
   * trailing whitespace trimmed before being returned.</p>
   * 
   * @param value the string value to parse
   * 
   * @return object representation of the type
   */
  public Object convert( String value ) {
    switch ( type ) {
      case DataField.STRING:
        if ( trimFlag ) {
          return value.trim();
        } else {
          return value;
        }
      case DataField.DATE:
        // if all whitespace, return a null value
        if ( StringUtil.isBlank( value ) ) {
          return null;
        }

        if ( dateFormat != null ) {
          try {
            if ( trimFlag ) {
              return dateFormat.parse( value.trim() );
            } else {
              return dateFormat.parse( value );
            }
          } catch ( ParseException e ) {
            System.err.println( "'" + name + "' Date Parse Exception: " + e.getMessage() );
            return null;
          }
        } else {
          System.err.println( "No date format for field '" + name + "'" );
        }

        return value;
      case DataField.FLOAT:
        // if all whitespace, return 0.0
        if ( StringUtil.isBlank( value ) ) {
          return 0F;
        }
        if ( decimalFormat != null ) {
          try {
            if ( trimFlag ) {
              return (Float)decimalFormat.parse( value.trim() );
            } else {
              return (Float)decimalFormat.parse( value );
            }
          } catch ( ParseException e ) {
            System.err.println( "'" + name + "' Float Parse Exception: " + e.getMessage() );
            return 0F;
          }
        } else {
          try {
            if ( trimFlag ) {
              return Float.parseFloat( value.trim() );
            } else {
              return Float.parseFloat( value );
            }
          } catch ( Exception e ) {
            System.err.println( "'" + name + "' Float Parse Exception: " + e.getMessage() );
            return 0F;
          }
        }
      case DataField.DOUBLE:
        // if all whitespace, return 0.0
        if ( StringUtil.isBlank( value ) ) {
          return 0F;
        }
        if ( decimalFormat != null ) {
          try {
            if ( trimFlag ) {
              return (Double)decimalFormat.parse( value.trim() );
            } else {
              return (Double)decimalFormat.parse( value );
            }
          } catch ( ParseException e ) {
            System.err.println( "'" + name + "' Double Parse Exception: " + e.getMessage() );
            return 0D;
          }
        } else {
          try {
            if ( trimFlag ) {
              return Double.parseDouble( value.trim() );
            } else {
              return Double.parseDouble( value );
            }
          } catch ( Exception e ) {
            System.err.println( "'" + name + "' Double Parse Exception: " + e.getMessage() );
            return 0F;
          }
        }
      case DataField.S32:
      case DataField.U32:
      case DataField.S16:
      case DataField.U16:
      case DataField.S8:
      case DataField.U8:
        // if all whitespace, return 0
        if ( StringUtil.isBlank( value ) ) {
          return 0;
        }
        if ( decimalFormat != null ) {
          try {
            if ( trimFlag ) {
              return (Integer)decimalFormat.parse( value.trim() );
            } else {
              return (Integer)decimalFormat.parse( value );
            }
          } catch ( ParseException e ) {
            System.err.println( "'" + name + "' Integer Parse Exception: " + e.getMessage() );
            return 0;
          }
        } else {
          try {
            return Integer.parseInt( value.trim() );
          } catch ( Exception e ) {
            System.err.println( "'" + name + "' Integer Parse Exception: " + e.getMessage() );
            return 0;
          }
        }
      case DataField.S64:
      case DataField.U64:
        // if all whitespace, return 0
        if ( StringUtil.isBlank( value ) ) {
          return 0L;
        }
        if ( decimalFormat != null ) {
          try {
            if ( trimFlag ) {
              return (Long)decimalFormat.parse( value.trim() );
            } else {
              return (Long)decimalFormat.parse( value );
            }
          } catch ( ParseException e ) {
            System.err.println( "'" + name + "' Long Parse Exception: " + e.getMessage() );
            return 0;
          }
        } else {
          try {
            if ( trimFlag ) {
              return Long.parseLong( value.trim() );
            } else {
              return Long.parseLong( value );
            }
          } catch ( Exception e ) {
            System.err.println( "'" + name + "' Long Parse Exception: " + e.getMessage() );
            return 0L;
          }
        }
      default:
        System.err.println( "Can't handle a type of " + type + " for field '" + name + "'" );
        break;
    }

    return null;
  }




  /**
   * @return true values are to be trimmed, false leave whitespace
   */
  public boolean isTrimming() {
    return trimFlag;
  }




  /**
   * @param flag true to trim values, false to leave them as they are
   */
  public void setTrimming( boolean flag ) {
    trimFlag = flag;
  }




  /**
   * @return 0 for left alignment (default), 1 for center, 2 for right alignment
   */
  public int getAlignment() {
    return alignment;
  }




  /**
   * @return true if this definition has a format pattern to format the values, false otherwise. 
   */
  public boolean hasFormatter() {
    return StringUtil.isNotBlank( formatText );
  }




  /**
   * Return the field as a formatted string if there is an appropriate 
   * formatter defined for this field definition.
   * 
   * @param field The field to format
   * 
   * @return the string formatted using the currently set format string or 
   * simply the default {@code toString()} for the data in the field.
   */
  public String getFormattedValue( DataField field ) {

    String retval = null;

    // If we have format text to apply to this fields value...
    if ( this.hasFormatter() ) {

      // based on the data type, determine which formatter to use
      if ( field.isNumeric() ) {
        // see if we have a formatter cached
        if ( decimalFormat == null ) {
          // create one for later use
          decimalFormat = new DecimalFormat( formatText );
        }
        // format the value using the DecimalFormat
        if ( field.isNotNull() ) {
          retval = decimalFormat.format( field.getObjectValue() );
          //log.trace( "Formatting {}({}) with '{}' -- Result: '{}'", field, field.getStringValue(), formatText, retval );
        }
      } else if ( DataField.DATE == field.getType() ) {
        // Use the date format...same approach as above
        if ( dateFormat == null ) {
          dateFormat = new SimpleDateFormat( formatText );
        }
        if ( field.isNotNull() ) {
          retval = dateFormat.format( field.getObjectValue() );
          //log.trace( "Formatting {}({}) with '{}' -- Result: '{}'", field, field.getStringValue(), formatText, retval );
        }
      }
    }

    // if no formatting occurred, just return the toString result
    if ( retval == null ) {
      retval = field.getStringValue();
    }

    return retval;
  }

}
