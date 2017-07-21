package coyote.dataframe.marshal.json;

import java.io.IOException;
import java.io.Writer;


public class JsonWriter {

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




  JsonWriter( final Writer writer ) {
    this.writer = writer;
  }




  public void writeArrayClose() throws IOException {
    writer.write( ']' );
  }




  public void writeArrayOpen() throws IOException {
    writer.write( '[' );
  }




  public void writeArraySeparator() throws IOException {
    writer.write( ',' );
  }




  /**
   * Write the string replacing any non-standard characters as necessary.
   * 
   * @param string
   * 
   * @throws IOException
   */
  public void writeJsonString( final String string ) throws IOException {
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




  public void writeMemberName( final String name ) throws IOException {
    writer.write( '"' );
    writeJsonString( name );
    writer.write( '"' );
  }




  public void writeMemberSeparator() throws IOException {
    writer.write( ':' );
  }




  /**
   * Writes the string to the underlying writer with no additional formatting.
   * 
   * <p>Same function as {@link #writeLiteral(String)} but named differently 
   * for more readable code.
   * 
   * @param value the value to write
   * 
   * @throws IOException if writing encountered an error
   */
  public void writeNumber( final String value ) throws IOException {
    writer.write( value );
  }




  public void writeObjectClose() throws IOException {
    writer.write( '}' );
  }




  public void writeObjectOpen() throws IOException {
    writer.write( '{' );
  }




  public void writeObjectSeparator() throws IOException {
    writer.write( ',' );
  }




  public void writeString( final String string ) throws IOException {
    writer.write( '"' );
    writeJsonString( string );
    writer.write( '"' );
  }




  public void writeArray( Object value ) throws IOException {
    writeArrayOpen();
    if ( value != null ) {
      if ( value instanceof Object[] ) {
        Object[] array = (Object[])value;
        for ( int x = 0; x < array.length; x++ ) {
          if ( array[x] != null ) {
            if ( array[x] instanceof Number ) {
              writeNumber( array[x].toString() );
            } else if ( array[x] instanceof Boolean ) {
              writeLiteral( array[x].toString() );
            } else {
              writeString( array[x].toString() );
            }
          }
          if ( x + 1 < array.length ) {
            writeObjectSeparator();
          }
        }
      } else {
        System.err.println( "JsonWriter.writeArray cannot handle " + value.getClass().getSimpleName() );
      }
    }
    writeArrayClose();
  }

}
