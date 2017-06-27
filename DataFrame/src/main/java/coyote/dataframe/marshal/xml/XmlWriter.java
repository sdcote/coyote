package coyote.dataframe.marshal.xml;

import java.io.IOException;
import java.io.Writer;

import coyote.dataframe.DataField;


public class XmlWriter {
  private static final String FIELD = "field";

  private static final int CONTROL_CHARACTERS_END = 0x001f;

  private static final char[] QUOT_CHARS = { '\\', '"' };
  private static final char[] BS_CHARS = { '\\', '\\' };
  private static final char[] LF_CHARS = { '\\', 'n' };
  private static final char[] CR_CHARS = { '\\', 'r' };
  private static final char[] TAB_CHARS = { '\\', 't' };
  private static final char[] UNICODE_2028_CHARS = { '\\', 'u', '2', '0', '2', '8' };
  private static final char[] UNICODE_2029_CHARS = { '\\', 'u', '2', '0', '2', '9' };
  private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };




  private static char[] getReplacementChars( final char ch ) {
    if ( ch > '\\' ) {
      if ( ( ch < '\u2028' ) || ( ch > '\u2029' ) ) {
        // The lower range contains 'a' .. 'z'. Only 2 checks required.
        return null;
      }
      return ch == '\u2028' ? UNICODE_2028_CHARS : UNICODE_2029_CHARS;
    }
    if ( ch == '\\' ) {
      return BS_CHARS;
    }
    if ( ch > '"' ) {
      // This range contains '0' .. '9' and 'A' .. 'Z'. Need 3 checks to
      // get here.
      return null;
    }
    if ( ch == '"' ) {
      return QUOT_CHARS;
    }
    if ( ch > CONTROL_CHARACTERS_END ) {
      return null;
    }
    if ( ch == '\n' ) {
      return LF_CHARS;
    }
    if ( ch == '\r' ) {
      return CR_CHARS;
    }
    if ( ch == '\t' ) {
      return TAB_CHARS;
    }
    return new char[] { '\\', 'u', '0', '0', HEX_DIGITS[( ch >> 4 ) & 0x000f], HEX_DIGITS[ch & 0x000f] };
  }

  public final Writer writer;




  XmlWriter( final Writer writer ) {
    this.writer = writer;
  }




  public void writeEquals() throws IOException {
    writer.write( '=' );
  }




  public void writeFieldClose() throws IOException {}




  public void writeFieldName( final DataField field ) throws IOException {
    if ( field.getName() != null ) {
      writeLiteral( field.getName() );
    } else {
      writeLiteral( FIELD );
    }
  }




  public void writeFieldOpen() throws IOException {}




  public void writeFieldType( final DataField field ) throws IOException {}




  public void writeForwardSlash() throws IOException {
    writer.write( '/' );
  }




  public void writeFrameClose() throws IOException {
    writer.write( "</frame>" );
  }




  public void writeFrameOpen() throws IOException {
    writer.write( "<frame>" );
  }




  /**
   * Writes the string to the underlying writer with no additional formatting.
   * 
   * @param value the value to write
   * 
   * @throws IOException if writing encountered an error
   */
  public void writeLiteral( final String value ) throws IOException {
    writer.write( value );
  }




  public void writeSpace() throws IOException {
    writer.write( ' ' );
  }




  public void writeString( final String string ) throws IOException {
    writer.write( '"' );
    writeXmlString( string );
    writer.write( '"' );
  }




  public void writeTagClose() throws IOException {
    writer.write( '>' );
  }




  public void writeTagOpen() throws IOException {
    writer.write( '<' );
  }




  /**
   * Write the string replacing any non-standard characters as necessary.
   * 
   * @param string
   * 
   * @throws IOException
   */
  public void writeXmlString( final String string ) throws IOException {
    final int length = string.length();
    int start = 0;
    for ( int index = 0; index < length; index++ ) {
      final char[] replacement = getReplacementChars( string.charAt( index ) );
      if ( replacement != null ) {
        writer.write( string, start, index - start );
        writer.write( replacement );
        start = index + 1;
      }
    }
    writer.write( string, start, length - start );
  }




  public void writeEmptyFrame() throws IOException {
    writer.write( "<frame/>" );    
  }

}
