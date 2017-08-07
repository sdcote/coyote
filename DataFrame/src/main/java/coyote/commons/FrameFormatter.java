/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


public class FrameFormatter {

  /** Platform specific line separator (default = CRLF) */
  private static final String LINE_FEED = System.getProperty( "line.separator", "\r\n" );

  /** Name of a frame used in XML if it has no field name, also used as the root */
  private static final String NODENAME = "frame";

  /** Prefix of a field name when no name if found */
  private static final String FIELDNAME = "field";
  
  private static final String EMPTY_PADDING = "";

  // These dictate how characters are escaped into XML @see toEscaped(String)
  private final static String ESCAPE_CHARS = "<>&\"\'";
  private final static List<String> ESCAPE_STRINGS = Collections.unmodifiableList( Arrays.asList( new String[] { "&lt;", "&gt;", "&amp;", "&quot;", "&apos;" } ) );
  private static String UNICODE_LOW = "" + ( (char)0x20 ); //space
  private static String UNICODE_HIGH = "" + ( (char)0x7f );




  /**
   * Return an XML string representing the given data frame.
   * 
   * @param frame The frame to represent in XML
   * 
   * @return and XML string
   */
  public static String toXML( DataFrame frame ) {
    return toIndentedXML( frame, NODENAME, -1, -1 );
  }




  /**
   * Return a multi-line XML string representing the given data frame.
   * 
   * @param frame The frame to represent in XML
   * 
   * @return and XML string
   */
  public static String toIndentedXML( DataFrame frame ) {
    return toIndentedXML( frame, NODENAME, 0, 2 );
  }




  /**
   * Display the given frame in human readable, multi-line text with an 
   * indentation of 2 characters.
   * 
   * @param frame The frame to format
   * 
   * @return an easy to read test representation of the given frame.
   */
  public static String prettyPrint( DataFrame frame ) {
    return prettyPrint( frame, 2 );
  }




  /**
   * format the frame in a readable format of the wire format of the frame.
   * 
   * @param frame The frame to dump.
   * 
   * @return a string representing the binary dump of the frame.
   */
  public static String dump( DataFrame frame ) {
    final int length = frame.getBytes().length;
    final StringBuffer buffer = new StringBuffer();
    buffer.append( "DataFrame of " );
    buffer.append( length );
    buffer.append( " bytes" + LINE_FEED );
    buffer.append( ByteUtil.dump( frame.getBytes(), length ) );
    buffer.append( LINE_FEED );

    return buffer.toString();
  }




  /**
   * Display the given frame in human readable, multi-line text a given indentation.
   * 
   * @param frame The frame to format
   * @param the number of columns to indent each successive level
   * 
   * @return an easy to read test representation of the given frame.
   */
  private static String prettyPrint( final DataFrame frame, final int indent ) {
    String padding = null;
    int nextindent = -1;

    if ( indent > -1 ) {
      final char[] pad = new char[indent];
      for ( int i = 0; i < indent; pad[i++] = ' ' ) {}

      padding = new String( pad );
      nextindent = indent + 2;
    } else {
      padding = EMPTY_PADDING;
    }

    final StringBuffer buffer = new StringBuffer();

    for ( int x = 0; x < frame.getFieldCount(); x++ ) {
      final DataField field = frame.getField( x );
      if ( indent > -1 ) {
        buffer.append( padding );
      }
      buffer.append( x );
      buffer.append( ": " );

      buffer.append( "'" );
      buffer.append( field.getName() );
      buffer.append( "' " );
      buffer.append( field.getTypeName() );
      buffer.append( "(" );
      buffer.append( field.getType() );
      buffer.append( ") " );

      if ( field.getType() == DataField.FRAMETYPE ) {
        buffer.append( LINE_FEED );
        buffer.append( prettyPrint( (DataFrame)field.getObjectValue(), nextindent ) );
      } else {
        buffer.append( field.getObjectValue().toString() );
      }

      if ( x + 1 < frame.getFieldCount() ) {
        buffer.append( LINE_FEED );
      }

    }

    return buffer.toString();
  }




  /**
   * Recursive call into generating XML from a given dataframe.
   * 
   * @param frame The frame to represent as XML
   * @param name The name of the current XML node
   * @param indent -1 means no indentation or padding
   * @param increment number of columns to increment each indentation. 
   * 
   * @return XML string representing the given dataframe
   */
  private static String toIndentedXML( final DataFrame frame, String name, final int indent, int increment ) {

    String padding = EMPTY_PADDING; // padding(indent) for our markup
    String fieldpadding = EMPTY_PADDING; // padding for our fields
    int nextindent = -1; // what the next, recursive indent value should be

    // if we are indenting
    if ( indent > -1 ) {
      // Generate our padding
      final char[] pad = new char[indent];
      for ( int i = 0; i < indent; pad[i++] = ' ' ) {}
      padding = new String( pad );
      // determine our next indent
      nextindent = indent + increment;
    }

    final StringBuffer xml = new StringBuffer();
    xml.append( padding );
    xml.append( "<" );

    // Name the XML node
    if ( name == null || name.trim().length() == 0 )
      xml.append( NODENAME );
    else
      xml.append( name );

    // If we have fields, close the opening XML tag
    if ( ( frame.getFieldCount() > 0 ) ) {
      xml.append( ">" );

      // if this is not a flat XML, move to the next line
      if ( indent > -1 ) {
        xml.append( LINE_FEED );
      }

      // Generate our field padding
      if ( indent > -1 ) {
        final char[] pad = new char[increment];
        for ( int i = 0; i < increment; pad[i++] = ' ' ) {}
        fieldpadding = new String( pad );
      }

      // get each field in this frame
      for ( int x = 0; x < frame.getFieldCount(); x++ ) {
        final DataField field = frame.getField( x );

        String fname = field.getName();

        if ( field.isFrame() ) {
          xml.append( toIndentedXML( (DataFrame)field.getObjectValue(), fname, nextindent, increment ) );
        } else {
          xml.append( padding );
          xml.append( fieldpadding ); // indent the field

          // Add the field
          xml.append( "<" );
          if ( fname == null ) {
            fname = FIELDNAME + x;
          } else {
            xml.append( fname );
          }
          xml.append( " type='" );
          xml.append( field.getTypeName() );
          xml.append( "'>" );
          xml.append( toEscaped( field.getObjectValue().toString() ) );
          xml.append( "</" );
          xml.append( fname );
          xml.append( ">" );
        }

        // if this is not a flat XML, move to the next line
        if ( indent > -1 ) {
          xml.append( LINE_FEED );
        }

      } // for each field

      xml.append( padding );
      xml.append( "</" );
      if ( name == null || name.trim().length() == 0 ) {
        xml.append( NODENAME );
      } else {
        xml.append( name );
      }
      xml.append( ">" );
    } else {
      xml.append( "/>" );
    }

    return xml.toString();
  }




  /**
   * This is a quick & dirty method to escape some common characters. Should 
   * only use for the content of an attribute or tag.
   *       
   * @param content The string to transform
   * 
   * @return the new string with XML safe content.
   */
  private static String toEscaped( String content ) {
    String result = content;

    if ( ( content != null ) && ( content.length() > 0 ) ) {
      boolean modified = false;
      StringBuilder stringBuilder = new StringBuilder( content.length() );
      for ( int i = 0, count = content.length(); i < count; ++i ) {
        String character = content.substring( i, i + 1 );
        int pos = ESCAPE_CHARS.indexOf( character );
        if ( pos > -1 ) {
          stringBuilder.append( ESCAPE_STRINGS.get( pos ) );
          modified = true;
        } else {
          if ( ( character.compareTo( UNICODE_LOW ) > -1 ) && ( character.compareTo( UNICODE_HIGH ) < 1 ) ) {
            stringBuilder.append( character );
          } else {
            stringBuilder.append( "&#" + ( (int)character.charAt( 0 ) ) + ";" );
            modified = true;
          }
        }
      }
      if ( modified ) {
        result = stringBuilder.toString();
      }
    }

    return result;
  }

}
