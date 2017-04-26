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

import java.nio.ByteBuffer;
import java.util.UUID;

import coyote.commons.ByteUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.mbus.network.MessageChannel;


/**
 * 
 */
public class Message extends DataFrame implements Cloneable {
  /** The name of the message identifier field. */
  public static final String IDENTIFIER = "MID";

  /** The name of the reply identifier field. */
  public static final String REPLY_ID = "RID";

  /** The name of the source address field. */
  public static final String SOURCE = "SRC";

  /** The name of the target address field. */
  public static final String TARGET = "TGT";

  /** The name of the group field. */
  public static final String GROUP = "GRP";

  /** The name of the reply-to group field. */
  public static final String REPLY = "RPY";

  /** The name of the field that contains the message flags. */
  public static final String FLAGS = "FLG";

  /** The name of the field that contains the priority field. */
  public static final String PRIORITY = "PRI";

  /** The name of the field that contains the optional message type name. */
  public static final String TYPE = "TYP";

  /** The name of the field that contains the header data. */
  public static final String HEADER = "HDR";

  public static final short LOWEST = 0;

  public static final short VERY_LOW = 1;

  public static final short LOW = 2;

  public static final short BELOW_NORMAL = 3;

  public static final short NORMAL = 4;

  public static final short ABOVE_NORMAL = 5;

  public static final short HIGH = 6;
  public static final short VERY_HIGH = 7;
  public static final short HIGHEST = 8;
  private static final String[] priorityNames = { "Lowest", "Very Low", "Low", "Below Normal", "Normal", "Above Normal", "High", "Very High", "Higest" };

  /**
   * The MessageChannel from which this message came implying where it is to be 
   * sent, rejected, returned or where responses are to be returned. May be 
   * null implying an anonymous / orphaned / newly created message.
   */
  public MessageChannel sourceChannel = null;

  /**
   * A time stamp used to expire messages. On incoming messages it represents 
   * the time the message was received, on outgoing messages it is the time 
   * sent.
   */
  volatile long timestamp = 0;

  /**
   * Save processing and memory on repeated calls to getGroup() by caching the 
   * name of the group to which this message belongs.
   */
  private String cachedGroup = null;

  /**
   * Save processing and memory on repeated calls to getType() by caching the 
   * name of the type of message this is.
   */
  private String cachedType = null;

  /**
   * 
   */
  private byte priority = Message.NORMAL;

  /**
   * 
   */
  private long flags = 0;

  private volatile boolean flagsResolved = false;




  /**
   * @param name
   * @param value
   */
  public Message( String name, boolean value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, byte value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, double value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, float value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, int value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, long value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, Object value ) {
    super( name, value );
  }




  /**
   * @param name
   * @param value
   */
  public Message( String name, short value ) {
    super( name, value );
  }




  /**
   * Create an message that is a response to the given message.
   *
   * @param request The message representing the requesting message.
   * 
   * @return An message with all header fields filled with their complementing 
   *         values from the given message or null if the argument was null.
   */
  public static Message createResponse( final Message request ) {
    if ( request != null ) {
      final Message retval = new Message();

      if ( request.getReplyGroup() != null ) {
        retval.setGroup( request.getReplyGroup() );
      } else {
        retval.setGroup( request.getGroup() );
      }

      if ( request.getId() != null ) {
        retval.setReplyId( request.getId() );
      }

      return retval;
    }
    return null;
  }




  /**
   * Get the string name of the given priority value.
   * 
   * @return A priority string for the given value or "UNKNOWN" if value is out 
   *         of range.
   */
  public static String getPriorityString( final short value ) {
    if ( ( value > -1 ) && ( value < Message.priorityNames.length ) ) {
      return Message.priorityNames[value];
    } else {
      return "UNKNOWN";
    }
  }




  /**
   * Construct an empty Message.
   */
  public Message() {}




  /**
   * Construct the message with the given bytes.
   *
   * @param data The byte array from which to construct the Message.
   */
  public Message( final byte[] data ) {
    super( data );
  }




  /**
   * Create a deep-copy of this BusMessage.
   * 
   * <p>The sequence, source and digest are NOT cloned, as they are generated 
   * as a part of the transmission process.</p>
   */
  public Object clone() {
    final Message retval = new Message();

    // Clone all the fields
    for ( int i = 0; i < fields.size(); i++ ) {
      retval.fields.add( i, (DataField)fields.get( i ).clone() );
    }
    retval.modified = false;

    return retval;
  }




  /**
   * Create an message that is a response to the given message.
   *
   * @return An message with all header fields filled with their complementing 
   *         values from the given message or null if the argument was null.
   */
  public Message createResponse() {
    return createResponse( this );
  }




  /**
   * Calculate a statistically unique message identifier for this message.
   * 
   * @return A 16 byte identifier for this message instance.
   */
  byte[] genId() {
    UUID id = UUID.randomUUID();
    ByteBuffer bb = ByteBuffer.wrap( new byte[16] );
    bb.putLong( id.getMostSignificantBits() );
    bb.putLong( id.getLeastSignificantBits() );
    byte[] retval = bb.array();
    put( Message.IDENTIFIER, retval );
    return retval;
  }




  /**
   * Access the cached value of the flags for this message. 
   * 
   * <p>If the underlying Field storing the flags was altered, this value will 
   * not be up to date. All flag operations should be performed on the  
   * Message level to ensure synchronization.</p> 
   * 
   * <p>This first call to this method will incur the cost of the retrieval and 
   * caching of the flag value. Subsequent calls will simply use the cached  
   * value.</p> 
   * 
   * <p>No attempt is made to interpret the flags in anyway. This method only  
   * gives efficient access to the values.</p>
   * 
   * @return  The current cached value of the flags for this message.
   */
  public long getFlags() {
    if ( !flagsResolved ) {
      final DataField flagField = getField( Message.FLAGS );
      if ( flagField != null ) {
        if ( flagField.isNumeric() ) {
          // Yuck!
          Object obj = flagField.getObjectValue();
          if ( obj instanceof Long )
            flags = ( (Long)obj ).longValue();
          else if ( obj instanceof Integer )
            flags = ( (Integer)obj ).longValue();
          else if ( obj instanceof Short )
            flags = ( (Short)obj ).longValue();
          else
            flags = 0;
        } else {
          flags = 0;
        }
      } else {
        flags = 0;
      }
      flagsResolved = true;
    }
    return flags;
  }




  /**
   * Return the group name of the message.
   * 
   * <p>To save processing and memory, the group name is cached after first
   * retrieval and reset if a call to setGroup is made.</p>
   *
   * @return The data in the group field, null if no group name is found.
   */
  public String getGroup() {
    if ( ( cachedGroup == null ) && ( getObject( Message.GROUP ) != null ) ) {
      cachedGroup = getObject( Message.GROUP ).toString();
    }

    return cachedGroup;
  }




  /**
   * Return the bytes representing the unique message identifier for this message.
   *
   * @return The currently set message identifier.
   */
  public byte[] getId() {
    // return the currently set message identifier
    final Object retval = getObject( Message.IDENTIFIER );

    if ( retval != null ) {
      return (byte[])retval;
    }
    return null;
  }




  /**
   * Return the string representing the unique message identifier for this
   * message.
   *
   * @return The currently set message key as a string.
   */
  public String getIdString() {
    // return the currently set message identifier
    final Object retval = getObject( Message.IDENTIFIER );

    if ( retval != null ) {
      return ByteUtil.bytesToHex( (byte[])retval );
    }
    return null;
  }




  /**
   * @return  Returns the priority of this message.
   */
  public short getPriority() {
    return priority;
  }




  /**
   * Get the string name of this messages priority.
   * 
   * @return A priority string for this message instance.
   */
  public String getPriorityString() {
    return Message.getPriorityString( priority );
  }




  /**
   * Get the reply-to group of this message.
   *
   * @return The name of the group on which replies to this message are to be
   *         published.
   */
  public String getReplyGroup() {
    if ( getObject( Message.REPLY ) != null ) {
      return getObject( Message.REPLY ).toString();
    }

    return null;
  }




  /**
   * Return the bytes representing the identifier of the request to which this 
   * message is a response.
   *
   * @return The currently set Request Identifier, or null of this message is not
   *         a response to another message or the field does not exist.
   */
  public byte[] getReplyId() {
    // return the currently set message identifier
    final Object retval = getObject( Message.REPLY_ID );

    if ( retval != null ) {
      return (byte[])retval;
    }
    return null;
  }




  /**
   * Return the string representing the identifier of the request to which this 
   * message is a response.
   *
   * @return The currently set Reply Identifier, or null of this message is not
   *         a response to another message or the field does not exist.
   */
  public String getReplyIdString() {
    // return the currently set request identifier
    final Object retval = getObject( Message.REPLY_ID );

    if ( retval != null ) {
      return ByteUtil.bytesToHex( (byte[])retval );
    }
    return null;
  }




  /**
   * Return the source of this Message.
   *
   * @return The MessageAddress representation of the source of this Message.
   */
  public MessageAddress getSource() {
    final byte[] addr = getBytes( Message.SOURCE );

    if ( addr != null ) {
      return new MessageAddress( addr );
    }

    return null;
  }




  /**
   * Obtain a reference to the message channel from which this came.
   * 
   * @return  The reference to the originating message group.
   */
  public MessageChannel getSourceChannel() {
    return sourceChannel;
  }




  /**
   * Return the destination of this Message.
   *
   * @return MessageAddress representation of the destination.
   */
  public MessageAddress getTarget() {
    final byte[] addr = getBytes( Message.TARGET );

    if ( addr != null ) {
      return new MessageAddress( addr );
    }

    return null;
  }




  /**
   * Return the epoch time in milliseconds indicating when this message was 
   * created.
   * 
   * <p>This timestamp is normally in GMT and should be safe to pass to 
   * <code>Date</code> constructors.</p>
   * 
   * @return  The epoch time in milliseconds.
   */
  public long getTimestamp() {
    return timestamp;
  }




  /**
   * Return the logical type of message this is.
   * 
   * <p>This is merely a convenience method to a totally optional and abstract 
   * typing of the message and in no way indicates the actual data types that may 
   * be included in this message.</p>
   * 
   * <p>To save processing and memory, the type name is cached after first
   * retrieval and reset if a call to setType is made.</p>
   *
   * @return The data in the type field, null if no type name is found.
   */
  public String getType() {
    if ( ( cachedType == null ) && ( getObject( Message.TYPE ) != null ) ) {
      cachedType = getObject( Message.TYPE ).toString();
    }

    return cachedType;
  }




  /**
   * Set a series of bits to represent a generic bit field.
   *  
   * <p>The bits are stored in a Field through this operation and calls to this 
   * method are guaranteed to keep the underlying Field up to date. Alterations 
   * to the underlying Field are NOT cached and may be out of sync. All updates 
   * to the messages flags should be made through this method call and not 
   * directly to the underlying Field.</p>
   * 
   * @param bits  The bit field value to set.
   */
  public void setFlags( final long bits ) {
    flags = bits;

    final DataField flagField = getField( Message.FLAGS );
    if ( flagField == null ) {
      if ( flags != 0 ) {
        put( Message.FLAGS, flags );
      }
    } else {
      if ( flags != 0 ) {
        put( Message.FLAGS, flags );
      } else {
        remove( Message.FLAGS );
      }
    }
  }




  /**
   * Set the name of this messages group.
   * 
   * <p>This method also caches the group so repeated calls to getGroup doesn't
   * have to parse the message and create a new string each time.</p>
   *
   * @param name The name of the group to set; if null, the group will be
   *        removed from the message.
   */
  public void setGroup( final String name ) {
    put( Message.GROUP, name );

    cachedGroup = name;
  }




  /**
   * Set the priority of this message.
   * 
   * <p>Priorities are a local construct used in the local processing framework
   * and will not be encoded as part of the message in one of the message
   * Fields.</p>
   * 
   * @param priority The priority to set.
   */
  public void setPriority( final short priority ) {
    if ( ( priority > -1 ) && ( priority < Message.priorityNames.length ) ) {
      this.priority = (byte)priority;
    } else {
      throw new IllegalArgumentException( "Invalid value priority argument" );
    }
  }




  /**
   * Set the reply-to group of this message.
   *
   * @param name The name of the group on which replies to this message are to be
   *        published.
   */
  public void setReplyGroup( final String name ) {
    put( Message.REPLY, name );
  }




  /**
   * Set the identifier of the message to which this message is a response.
   * 
   * <p>This allows receivers to correlate response messages to the request 
   * which caused them.</p>
   * 
   * <p>If this field exists, then this message is considered a response.</p>
   * 
   * @param id the bytes representing the identifier of the message
   */
  public void setReplyId( final byte[] id ) {
    put( Message.REPLY_ID, id );
  }




  /**
   * Set the source of this Message.
   *
   * @param addr The MessageAddress representation of the source of this Message.
   */
  public void setSource( final MessageAddress addr ) {
    if ( addr != null ) {
      put( Message.SOURCE, addr.getBytes() );
    } else {
      remove( Message.SOURCE );
    }
  }




  /**
   * Set the source channel of this Message. 
   * 
   * <p>This reference implies the MessageChannel from which this message was 
   * received. If null, then no implications can be made.</p> 
   * 
   * <p>This is normally accessed by framework components and not business  
   * logic so consider why this method would be accessed by your code.</p>
   * 
   * @param channel  The MessageChannel to set as this messages source.
   */
  public void setSourceChannel( final MessageChannel channel ) {
    sourceChannel = channel;
  }




  /**
   * Sets the intended destination of this Message.
   *
   * @param addr The MessageAddress representation of the target destination.
   */
  public void setTarget( final MessageAddress addr ) {
    if ( addr != null ) {
      put( Message.TARGET, addr.getBytes() );
    } else {
      remove( Message.TARGET );
    }
  }




  /**
   * Set the timestamp in this message. 
   * 
   * <p>This value SHOULD BE the GMT epoch time in milliseconds for the sake of 
   * consistency. Normally it is safe to use the 
   * <code>java.lang.System.currentTimeMillis()</code> or the  
   * <code>Date().getTime()</code> methods as those currently return the GMT 
   * value of the epoch time in milliseconds.</p>
   * 
   * @param l  The epoch time in milliseconds to set.
   */
  public void setTimestamp( final long l ) {
    timestamp = l;
    //modified = true;
  }




  /**
   * Set the name this messages type.
   * 
   * <p>This field is purely optional but provides a convenient way to classify
   * the contents of the message further within the group.</p>
   *
   * @param name The name of the type to set; if null, the type will be removed 
   *             from the message.
   */
  public void setType( final String name ) {
    put( Message.TYPE, name );

    cachedType = name;
  }




  /**
   * Method dump
   */
  public String dump() {
    return MessageUtil.dump( this );
  }




  public String toString() {
    return MessageUtil.prettyPrint( this, 0 );
  }




  /**
   * This dumps the message in a form of XML.
   *
   * @return an XML representation of the message.
   */
  public String toXml() {
    return MessageUtil.toXML( this, null );
  }

}
