package coyote.commons.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A CSV reader which uses the CSVParser to split lines into fields.
 */
public class CSVReader implements Closeable {

  private final BufferedReader bufferedReader;

  private boolean hasNext = true;

  private final CSVParser csvParser;

  private final int lineToSkip;

  private boolean linesSkipped;

  /** The default line to start reading. */
  public static final int LINES_TO_SKIP = 0;




  /**
   * Constructs CSVReader.
   * 
   * @param reader the reader to an underlying CSV source.
   */
  public CSVReader( final Reader reader ) {
    this( reader, CSVParser.SEPARATOR, CSVParser.QUOTE_CHARACTER, CSVParser.ESCAPE_CHARACTER );
  }




  /**
   * Constructs CSVReader.
   * 
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries.
   */
  public CSVReader( final Reader reader, final char separator ) {
    this( reader, separator, CSVParser.QUOTE_CHARACTER, CSVParser.ESCAPE_CHARACTER );
  }




  /**
   * Constructs CSVReader.
   * 
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   */
  public CSVReader( final Reader reader, final char separator, final char quotechar ) {
    this( reader, separator, quotechar, CSVParser.ESCAPE_CHARACTER, LINES_TO_SKIP, CSVParser.STRICT_QUOTES );
  }




  /**
   * Constructs CSVReader.
   *
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param strictQuotes sets if characters outside the quotes are ignored
   */
  public CSVReader( final Reader reader, final char separator, final char quotechar, final boolean strictQuotes ) {
    this( reader, separator, quotechar, CSVParser.ESCAPE_CHARACTER, LINES_TO_SKIP, strictQuotes );
  }




  /**
   * Constructs CSVReader.
    *
    * @param reader the reader to an underlying CSV source.
    * @param separator the delimiter to use for separating entries
    * @param quotechar the character to use for quoted elements
    * @param escape the character to use for escaping a separator or quote
    */

  public CSVReader( final Reader reader, final char separator, final char quotechar, final char escape ) {
    this( reader, separator, quotechar, escape, LINES_TO_SKIP, CSVParser.STRICT_QUOTES );
  }




  /**
   * Constructs CSVReader.
   * 
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param line the line number to skip for start reading 
   */
  public CSVReader( final Reader reader, final char separator, final char quotechar, final int line ) {
    this( reader, separator, quotechar, CSVParser.ESCAPE_CHARACTER, line, CSVParser.STRICT_QUOTES );
  }




  /**
   * Constructs CSVReader.
   *
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   * @param line the line number to skip for start reading
   */
  public CSVReader( final Reader reader, final char separator, final char quotechar, final char escape, final int line ) {
    this( reader, separator, quotechar, escape, line, CSVParser.STRICT_QUOTES );
  }




  /**
   * Constructs CSVReader.
   * 
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   * @param line the line number to skip for start reading
   * @param strictQuotes sets if characters outside the quotes are ignored
   */
  public CSVReader( final Reader reader, final char separator, final char quotechar, final char escape, final int line, final boolean strictQuotes ) {
    this( reader, separator, quotechar, escape, line, strictQuotes, CSVParser.IGNORE_LEADING_WHITESPACE );
  }




  /**
   * Constructs CSVReader.
   * 
   * @param reader the reader to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   * @param line the line number to skip for start reading
   * @param strictQuotes sets if characters outside the quotes are ignored
   * @param ignoreLeadingWhiteSpace it true, parser should ignore white space before a quote in a field
   */
  public CSVReader( final Reader reader, final char separator, final char quotechar, final char escape, final int line, final boolean strictQuotes, final boolean ignoreLeadingWhiteSpace ) {
    this.bufferedReader = new BufferedReader( reader );
    this.csvParser = new CSVParser( separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace );
    this.lineToSkip = line;
  }




  /**
   * Reads the entire file into a List with each element being a String[] of 
   * tokens.
   * 
   * @return a List of String[], with each String[] representing a line of the file.
   * 
   * @throws IOException if the next line could not be read
   * @throws ParseException if the line could not be parsed
   */
  public List<String[]> readAll() throws IOException, ParseException {

    final List<String[]> allElements = new ArrayList<String[]>();
    while ( hasNext ) {
      final String[] nextLineAsTokens = readNext();
      if ( nextLineAsTokens != null )
        allElements.add( nextLineAsTokens );
    }
    return allElements;

  }




  /**
   * Reads the next line from the buffer and converts to a string array.
   * 
   * @return a string array with each comma-separated element as a separate entry.
   * 
   * @throws IOException if the next line could not be read
   * @throws ParseException if the read line could not be parsed
   */
  public String[] readNext() throws IOException, ParseException {

    String[] result = null;
    do {
      final String nextLine = getNextLine();
      if ( !hasNext ) {
        return result; // should throw if still pending?
      }
      final String[] r = csvParser.parseLineMulti( nextLine );
      if ( r.length > 0 ) {
        if ( result == null ) {
          result = r;
        } else {
          final String[] t = new String[result.length + r.length];
          System.arraycopy( result, 0, t, 0, result.length );
          System.arraycopy( r, 0, t, result.length, r.length );
          result = t;
        }
      }
    }
    while ( csvParser.isPending() );
    return result;
  }




  /**
   * Reads the next line from the file.
   * 
   * @return the next line from the file without trailing newline
   * 
   * @throws IOException if the next line could not be read
   */
  private String getNextLine() throws IOException {
    if ( !this.linesSkipped ) {
      for ( int i = 0; i < lineToSkip; i++ ) {
        bufferedReader.readLine();
      }
      this.linesSkipped = true;
    }
    final String nextLine = bufferedReader.readLine();
    if ( nextLine == null ) {
      hasNext = false;
    }

    return hasNext ? nextLine : null;
  }




  /**
   * This reads ahead and skips past any CR or LF characters.
   */
  public void consumeEmptyLines() {
    if ( bufferedReader.markSupported() ) {
      int character = 0;
      do {
        try {
          bufferedReader.mark( 2 );
          character = bufferedReader.read();
          if ( character != 10 && character != 13 ) {
            bufferedReader.reset();
            break;
          }
        } catch ( IOException e ) {
          e.printStackTrace();
        }
      }
      while ( character > 0 );
    }
  }




  /**
   * Closes the underlying reader.
   * 
   * @throws IOException if the close fails
   */
  @Override
  public void close() throws IOException {
    bufferedReader.close();
  }




  /**
   * End Of File check
   * 
   * @return true if the are no more records to read, false otherwise
   */
  public boolean eof() {
    return !hasNext;
  }

}
