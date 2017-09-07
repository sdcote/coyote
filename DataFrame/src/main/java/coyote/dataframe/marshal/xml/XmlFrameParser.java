/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe.marshal.xml;

import java.io.IOException;
import java.io.Reader;

import coyote.commons.SimpleReader;
import coyote.commons.StringParser;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FieldType;
import coyote.dataframe.marshal.ParseException;


/**
 * This is a SAX style parser which reads from a stream and builds a DOM style
 * model of the XML data with DataFrames.
 * 
 * <p>Attributes are largely ignored as they do not map to the fields concept. 
 * The notable exception is the use of the {@code type} attribute to indicate 
 * the type the field value contains.
 */
public class XmlFrameParser extends StringParser {
  private static final String XML_DELIMS = " \t\n><";

  private static final int OPEN = '<';
  private static final int CLOSE = '>';
  public static final String TYPE_ATTRIBUTE_NAME = "type";




  /**
   * Create a parser using the given reader from which to read characters.
   * 
   * @param reader the stream reader to use
   */
  public XmlFrameParser( final Reader reader ) {
    super( reader, XML_DELIMS );
  }




  /**
   * The most common use case constructor which wraps the given string in a 
   * reader.
   * 
   * @param string the string data to parse
   */
  public XmlFrameParser( final String string ) {
    super( new SimpleReader( string ), XML_DELIMS );
  }




  /**
   * Generate a parse exception with the given message.
   * 
   * <p>All the position information is populated in the exception based on the 
   * readers current counters.
   * 
   * @param message The text message to include in the exception 
   * 
   * @return a parse exception with the given message.
   */
  private ParseException error( final String message ) {
    return new ParseException( message, getOffset(), getCurrentLineNumber(), getColumnNumber(), getLastCharacterRead() );
  }




  /**
   * Parse the reader into a dataframe
   * 
   * @return a dataframe parsed from the data set in this parser.
   * 
   * @throws ParseException if there are problems parsing the XML
   */
  public DataFrame parse() throws ParseException {

    DataFrame retval = null;

    Tag tag = null;

    // Start reading tags until we pass the preamble and comments
    do {
      tag = readTag();
      // TODO: check for preamble to get version and encoding to instruct how to parse the rest
      if ( tag == null ) {
        break;
      }
    }
    while ( tag.isComment() || tag.isPreamble() );

    // We have a tag which is not a preamble or comment, if it is an open 
    // tag then we have data that goes into a data frame  
    if ( ( tag != null ) && tag.isOpenTag() ) {
      retval = readFrame( tag );
    }

    return retval;
  }




  /**
   * Read in the value of a tag creating a data field containing the value as 
   * of the requested data type.
   * 
   * <p>The position of the reader will be immediately behind the closing tag 
   * of the read-in field.
   * 
   * @param openTag The opening tag read in for this field
   * 
   * @return a data field constructed from the XML value at the current 
   *         position in the reader's stream
   */
  private DataField readField( final Tag openTag ) {
    DataField retval = null;

    // This will be the name of the field
    final String name = openTag.getName();

    // This will be the type into which the string data is converted   
    final String type = openTag.getAttribute( TYPE_ATTRIBUTE_NAME );

    final FieldType fieldType = DataField.getFieldType( type );

    String value = readValue();

    // read what should be the closing tag
    final Tag closeTag = readTag();

    if ( closeTag != null ) {

      if ( closeTag.isCloseTag() ) {
        if ( !closeTag.getName().equals( openTag.getName() ) ) {
          throw error( "Malformed XML: expected closing tag for '" + openTag.getName() + "' not '" + closeTag.getName() + "'" );
        }
      } else {
        // We read in an opening tag indicating a nested field, get a frame for 
        // the tag we just read in
        final DataFrame frame = readFrame( closeTag );
        retval = new DataField( name, frame );
        value = null; // forget about the value
      } // close tag check

    } else {
      throw error( "Malformed XML: unexpected end of data reading close tag for '" + name + "'" );
    }

    // If we don't have a nested data field as a return value, try to use the 
    // value we read in  
    if ( retval == null ) {

      // TODO try to convert the string data of "value" into an object of the requested type
      if ( fieldType != null ) {
        // TODO all manner of data type parsing goes here
        retval = new DataField( name, value ); // string for now
      } else {
        // not valid field type, just use string
        retval = new DataField( name, value );
      }
    }
    return retval;
  }




  /**
   * Return the data as a frame.
   * 
   * <p>This method is called when the value of a field is another open tag 
   * which indicates a new field. These nested fields are placed in a frame and 
   * returned to the caller.
   * 
   * @param openTag The open tag read in signaling a nested field.
   * 
   * @return a data frame containing the nested field and any other peer field 
   *         encountered
   */
  private DataFrame readFrame( final Tag openTag ) {

    if ( openTag == null ) {
      throw new IllegalArgumentException( "ReadFrame called will null OpenTag" );
    }

    Tag currentTag = openTag;

    DataFrame retval = null;
    DataField field = null;

    // We have a tag which is not a preamble or comment; start looping through the tags 
    while ( currentTag != null ) {

      // Skip preamble and comments
      if ( !currentTag.isComment() && !currentTag.isPreamble() ) {
        if ( currentTag.isOpenTag() ) {

          if ( currentTag.isEmptyTag() ) {
            // empty tag, empty field
            field = new DataField( currentTag.getName(), null );
          } else {
            // read the field from the data stream this will consume the close tag for this field
            field = readField( currentTag );
          }

          // add the parsed field into the dataframe
          if ( field != null ) {
            // create the dataframe if this is the first field
            if ( retval == null ) {
              retval = new DataFrame();
            }

            // add the new field to the dataframe we will return
            retval.add( field );

          } else {
            throw error( "Problems reading field: null value" );
          }

          // read the next opening tag
          currentTag = readTag();

        } else {
          break;
        }

      } else {
        // read past the comment or preamble
        currentTag = readTag();
      } // not comment or preample

    } // while we have tags

    return retval;
  }




  /**
   * Read in a tag from the current position in the buffer.
   * 
   * <p>The reader is positioned immediately after the closing ('&gt;') 
   * character.
   * 
   * @return the next tag in the buffer or null if there are no tags left
   */
  private Tag readTag() {
    Tag retval = null;
    String token = null;

    // read to the next open character
    try {
      readTo( OPEN );
    } catch ( final Exception e ) {
      // assume there are no more tags
      return null;
    }

    try {
      // read everything up to the closing character into the token
      token = readTo( CLOSE );

      if ( token != null ) {

        // TODO: check for <![CDATA[ ]]>
        // check if the token starts with <![CDATA[
        // if it does, check to see if it ends with ]]> (it probably wont)
        // if it does, then we might be a complete tag, problem is <![CDATA[ can be nested so...
        // if not then read to the next ]]>, but nesting means I could have read in another <![CDATA[ so...
        // ...so maybe we should treat <![CDATA[  as its own open tag and ]]> as a close tag...

        token = token.trim();

        if ( token.length() > 0 ) {
          retval = new Tag( token );
        }
      }
    } catch ( final IOException e ) {
      throw error( "Could not read a complete tag: IO error" );
    }

    return retval;
  }




  /**
   * Read up to and return everything prior to the next the opening ('&lt;') 
   * character.
   * 
   * <p>The reader is positioned just before the opening character of the next 
   * tag.
   *  
   * @return everything up to the next opening ('&lt;') character or an empty 
   *         string ("") if there are no characters read. Will never be null. 
   */
  private String readValue() {
    final StringBuffer b = new StringBuffer();
    try {
      if ( OPEN == peek() ) {
        return null;
      }

      // keep reading characters until the next character is an open marker
      while ( OPEN != peek() ) {
        b.append( (char)read() );
      }

    } catch ( final IOException e ) {
      e.printStackTrace();
    }
    return b.toString();
  }

}
