package coyote.commons.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;


/**
 * A CSV writer
 */
public class CSVWriter implements Closeable {

  private final Writer writer;
  private final PrintWriter printWriter;
  private final char separator;
  private final char quoteChar;
  private final char escapeChar;
  private final String lineDelimiter;
  
  /** Initial allocated size of a string builder */
  public static final int INITIAL_STRING_SIZE = 128;

  /** The character used for escaping quotes. */
  public static final char ESCAPE_CHARACTER = '"';

  /** The default separator to use if none is supplied to the constructor. */
  public static final char SEPARATOR = ',';

  /**
   * The default quote character to use if none is supplied to the
   * constructor.
   */
  public static final char QUOTE_CHARACTER = '"';

  /** The quote constant to use when you wish to suppress all quoting. */
  public static final char NO_QUOTE_CHARACTER = '\u0000';

  /** The escape constant to use when you wish to suppress all escaping. */
  public static final char NO_ESCAPE_CHARACTER = '\u0000';

  /** Default line terminator uses RFC4180 defined terminator. */
  public static final String LINE_DELIMITER = "\r\n";




  /**
   * Constructs CSVWriter using a comma for the separator.
   *
   * @param writer the writer to an underlying CSV source.
   */
  public CSVWriter(final Writer writer) {
    this(writer, SEPARATOR);
  }




  /**
   * Constructs CSVWriter with supplied separator.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries.
   */
  public CSVWriter(final Writer writer, final char separator) {
    this(writer, separator, QUOTE_CHARACTER);
  }




  /**
   * Constructs CSVWriter with supplied separator and quote char.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   */
  public CSVWriter(final Writer writer, final char separator, final char quotechar) {
    this(writer, separator, quotechar, ESCAPE_CHARACTER);
  }




  /**
   * Constructs CSVWriter with supplied separator and quote char.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escapechar the character to use for escaping quotechars or escapechars
   */
  public CSVWriter(final Writer writer, final char separator, final char quotechar, final char escapechar) {
    this(writer, separator, quotechar, escapechar, LINE_DELIMITER);
  }




  /**
   * Constructs CSVWriter with supplied separator and quote char.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param lineEnd the line feed terminator to use
   */
  public CSVWriter(final Writer writer, final char separator, final char quotechar, final String lineEnd) {
    this(writer, separator, quotechar, ESCAPE_CHARACTER, lineEnd);
  }




  /**
   * Constructs CSVWriter with supplied separator, quote char, escape char and line ending.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escapechar the character to use for escaping quotechars or escapechars
   * @param lineEnd the line feed terminator to use
   */
  public CSVWriter(final Writer writer, final char separator, final char quotechar, final char escapechar, final String lineEnd) {
    this.writer = writer;
    this.printWriter = new PrintWriter(writer);
    this.separator = separator;
    this.quoteChar = quotechar;
    this.escapeChar = escapechar;
    this.lineDelimiter = lineEnd;
  }




  /**
   * Writes the entire list to a CSV file. The list is assumed to be a
   * String[]
   *
   * @param allLines a List of String[], with each String[] representing a line of the file.
   */
  public void writeAll(final List<String[]> allLines) {
    for (final String[] line : allLines) {
      writeNext(line);
    }
  }




  /**
   * Writes the next line to the file.
   *
   * @param nextLine a string array with each comma-separated element as a 
   *        separate entry.
   */
  public void writeNext(final String[] nextLine) {
    if (nextLine == null)
      return;

    final StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
    for (int i = 0; i < nextLine.length; i++) {
      if (i != 0) {
        sb.append(separator);
      }

      final String nextElement = nextLine[i];
      if (nextElement == null) {
        continue;
      }
      if (quoteChar != NO_QUOTE_CHARACTER) {
        sb.append(quoteChar);
      }

      sb.append(stringContainsSpecialCharacters(nextElement) ? processLine(nextElement) : nextElement);

      if (quoteChar != NO_QUOTE_CHARACTER) {
        sb.append(quoteChar);
      }
    }

    sb.append(lineDelimiter);
    printWriter.write(sb.toString());

  }




  /**
   * Flush underlying stream to writer.
   */
  public void flush() {
    printWriter.flush();
  }




  /**
   * Close the underlying stream writer flushing any buffered content.
   *
   * @throws IOException if the underlying writer could not be closed
   */
  @Override
  public void close() throws IOException {
    flush();
    printWriter.close();
    writer.close();
  }




  /**
   *  Checks to see if the there has been an error in the printstream. 
   */
  public boolean checkError() {
    return printWriter.checkError();
  }




  private boolean stringContainsSpecialCharacters(final String token) {
    boolean retval = false;
    char character;
    for (int x = 0; x < token.length(); x++) {
      character = token.charAt(x);
      if (character < 32 || character == QUOTE_CHARACTER || character == separator || character == ESCAPE_CHARACTER) {
        retval = true;
        break;
      }
    }
    return retval;
  }




  protected StringBuilder processLine(final String nextElement) {
    final StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
    for (int x = 0; x < nextElement.length(); x++) {
      final char nextChar = nextElement.charAt(x);
      if ((escapeChar != NO_ESCAPE_CHARACTER) && (nextChar == quoteChar)) {
        sb.append(escapeChar).append(nextChar);
      } else if ((escapeChar != NO_ESCAPE_CHARACTER) && (nextChar == escapeChar)) {
        sb.append(escapeChar).append(nextChar);
      } else {
        sb.append(nextChar);
      }
    }
    return sb;
  }

}
