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
package coyote.mbus.message;

import java.util.Calendar;
import java.util.Date;

import coyote.commons.ByteUtil;
import coyote.dataframe.DataField;


/**
 * The MessageUtil class contains utilities for dealing with messages.
 */
public class MessageUtil {

  /** Platform specific line separator (default = CRLF) */
  public static final String LINE_FEED = System.getProperty( "line.separator", "\r\n" );




  /**
   * Method dump
   */
  public static String dump( final Message packet ) {
    final int length = packet.getBytes().length;
    final StringBuffer buffer = new StringBuffer();
    buffer.append( "packet message of " );
    buffer.append( length );
    buffer.append( " bytes\r\n" );
    if ( packet.getTimestamp() > 0 ) {
      buffer.append( "Timestamp: " );
      buffer.append( new Date( packet.getTimestamp() ) );
    }
    buffer.append( "\r\n" );
    buffer.append( ByteUtil.dump( packet.getBytes(), length ) );
    buffer.append( "\r\n" );

    return buffer.toString();
  }




  public static Message parseXML( final String xml ) {
    return null;
  }




  public static String prettyPrint( final Message packet, final int indent ) {
    String padding = null;
    int nextindent = -1;

    if ( indent > -1 ) {
      final char[] pad = new char[indent];
      for ( int i = 0; i < indent; pad[i++] = ' ' ) {
        ;
      }

      padding = new String( pad );
      nextindent = indent + 2;
    } else {
      padding = new String( "" );
    }

    final StringBuffer buffer = new StringBuffer();

    for ( int x = 0; x < packet.getFieldCount(); x++ ) {
      final DataField field = packet.getField( x );
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

      if ( field.getType() == 66 ) {
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( MessageUtil.prettyPrint( (Message)field.getObjectValue(), nextindent ) );
      } else {
        buffer.append( field.getObjectValue() );
      }

      if ( x + 1 < packet.getFieldCount() ) {
        buffer.append( System.getProperty( "line.separator" ) );
      }

    }

    return buffer.toString();
  }




  /**
   * Returns the given date as a basic ISO8601 formatted date string.
   *
   * @param date The date to convert.
   *
   * @return A string in thr format of yyyymmddThhmmss.zzz ZZZZ
   */
  private static String toBasic( final Date date ) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime( date );

    final StringBuffer retval = new StringBuffer();
    retval.append( MessageUtil.zeropad( cal.get( Calendar.YEAR ), 4 ) );
    retval.append( MessageUtil.zeropad( cal.get( Calendar.MONTH ) + 1, 2 ) );
    retval.append( MessageUtil.zeropad( cal.get( Calendar.DATE ), 2 ) );
    retval.append( "T" );
    retval.append( MessageUtil.zeropad( cal.get( Calendar.HOUR_OF_DAY ), 2 ) );
    retval.append( MessageUtil.zeropad( cal.get( Calendar.MINUTE ), 2 ) );
    retval.append( MessageUtil.zeropad( cal.get( Calendar.SECOND ), 2 ) );
    retval.append( MessageUtil.zeropad( cal.get( Calendar.MILLISECOND ), 3 ) );

    final int offset = ( cal.get( Calendar.ZONE_OFFSET ) / 1000 );
    int hours = offset / ( 60 * 60 );
    final int minutes = offset - ( hours * ( 60 * 60 ) );

    if ( offset == 0 ) {
      retval.append( "Z" );
    } else {
      if ( offset < 0 ) {
        retval.append( "-" );

        hours *= -1;
      } else {
        retval.append( "+" );
      }

      retval.append( MessageUtil.zeropad( hours, 2 ) );
      retval.append( MessageUtil.zeropad( minutes, 2 ) );
    }

    return retval.toString();
  }




  private static String toBasic( final long millis ) {
    return MessageUtil.toBasic( new Date( millis ) );
  }




  public static String toXML( final Message packet, final int indent ) {
    String padding = null;
    String nextPadding = null;

    int nextindent = -1;

    if ( indent > -1 ) {
      final char[] pad = new char[indent];
      for ( int i = 0; i < indent; pad[i++] = ' ' ) {
        ;
      }

      padding = new String( pad );
      nextindent = indent + 2;
    } else {
      padding = new String( "" );
    }

    final StringBuffer xml = new StringBuffer( padding + "<" );

    if ( packet.getType() != null ) {
      xml.append( packet.getType() );
    } else {
      xml.append( "Message" );
    }

    if ( packet.getTimestamp() > 0 ) {
      // It would be nice to specify a timezone into which this timestamp would
      // be converted; ie. if this is represented in MDT it would be nice to
      // get it converted to EDT at will
      xml.append( " stamp=\"" + MessageUtil.toBasic( packet.getTimestamp() ) + "\"" );
    }

    if ( packet.getId() != null ) {
      xml.append( " id=\"" + packet.getIdString() + "\"" );
    }

    if ( ( packet.getFieldCount() > 0 ) ) {

      xml.append( ">" );

      if ( indent >= 0 ) {
        xml.append( LINE_FEED );
      }

      if ( nextindent > -1 ) {
        final char[] pad = new char[nextindent];
        for ( int i = 0; i < nextindent; pad[i++] = ' ' ) {
          ;
        }

        nextPadding = new String( pad );
      } else {
        nextPadding = new String( "" );
      }

      for ( int x = 0; x < packet.getFieldCount(); x++ ) {
        final DataField field = packet.getField( x );

        if ( Message.TYPE.equals( field.getName() ) ) {} else if ( Message.IDENTIFIER.equals( field.getName() ) ) {} else {
          xml.append( nextPadding );

          String fname = field.getName();
          xml.append( padding );
          if ( fname == null ) {
            fname = "field" + x;
          }

          xml.append( "<Field " );

          if ( field.getName() != null ) {
            xml.append( "name='" );
            xml.append( field.getName() );
            xml.append( "' " );
          }
          xml.append( "type='" );
          xml.append( field.getTypeName() );
          xml.append( "'>" );
          xml.append( field.getObjectValue() );
          xml.append( "</Field>" );

        }

        if ( indent >= 0 ) {
          xml.append( LINE_FEED );
        }
      }

      if ( packet.getType() != null ) {
        xml.append( padding + "</" + packet.getType() + ">" );
      } else {
        xml.append( "</Message>" );
      }

    } else {
      xml.append( "/>" );
    }

    return xml.toString();
  }




  /**
   * This dumps the packet in a form of XML adding a name attribute to the packet 
   * for additional identification.
   * 
   * <p>This method is called to represent an embedded or child packet with the
   * name of the Field being used as the name of the XML node.</p>
   *
   * @return an XML representation of the packet.
   */
  public static String toXML( final Message packet, final String name ) {
    final StringBuffer buffer = new StringBuffer();
    buffer.append( "<Message" );
    if ( name != null ) {
      buffer.append( " name='" );
      buffer.append( name );
      buffer.append( "'" );
    }

    if ( packet.getPriority() != Message.NORMAL ) {
      buffer.append( " priority='" );
      buffer.append( packet.getPriorityString() );
      buffer.append( "'" );
    }

    if ( packet.getFieldCount() > 0 ) {
      buffer.append( ">" );
      for ( int i = 0; i < packet.getFieldCount(); i++ ) {
        final DataField field = packet.getField( i );

        if ( field.getType() == 66 ) {
          buffer.append( MessageUtil.toXML( (Message)field.getObjectValue(), field.getName() ) );
        } else {
          String fname = field.getName();

          if ( fname == null ) {
            fname = "field" + i;
          }

          buffer.append( "<Field " );

          if ( field.getName() != null ) {
            buffer.append( "name='" );
            buffer.append( field.getName() );
            buffer.append( "' " );
          }
          buffer.append( "type='" );
          buffer.append( field.getTypeName() );
          buffer.append( "'>" );
          buffer.append( field.getObjectValue() );
          buffer.append( "</Field>" );
        }
      } // foreach field

      buffer.append( "</Message>" );
    } else {
      buffer.append( "/>" );
    }

    return buffer.toString();
  }




  /**
   * Method zeropad
   *
   * @param num
   * @param size
   */
  private static String zeropad( final int num, final int size ) {
    final String value = Integer.toString( num );

    if ( value.length() >= size ) {
      return value;
    }

    final StringBuffer buf = new StringBuffer( size );
    for ( int i = 0; i++ < ( size - value.length() ); buf.append( '0' ) ) {
      ;
    }

    buf.append( value );

    return buf.toString();
  }




  /**
   * 
   */
  private MessageUtil() {}

}
