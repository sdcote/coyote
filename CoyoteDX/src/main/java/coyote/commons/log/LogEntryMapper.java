package coyote.commons.log;

import coyote.commons.DateUtil;
import coyote.commons.StringParser;
import coyote.dataframe.DataFrame;
import coyote.loader.log.Log;
import coyote.vault.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * This takes a log entry and parses it into a set of name-value pairs
 */
public class LogEntryMapper {

  private static final int OPEN_BRACKET = '[';
  private static final int CLOSE_BRACKET = ']';
  private static final int QUOTE = '"';
  private static final int PERCENT = '%';
  private static final int OPEN_BRACE = '{';
  private static final int CLOSE_BRACE = '}';
  private static final String DASH = "-";

  private static final Map<ParsingMode, Map<String, String>> NAMES = new HashMap<ParsingMode, Map<String, String>>();
  private static final Map<ParsingMode, Map<String, LogFieldType>> TYPES = new HashMap<ParsingMode, Map<String, LogFieldType>>();
  private static final String APACHE_DATE_FORMAT = "d/MMM/y:H:m:s Z";

  static {
    // Apache mappings see: https://httpd.apache.org/docs/2.4/mod/mod_log_config.html
    Map<String, String> nameMap = new HashMap<String, String>();
    NAMES.put(ParsingMode.APACHE, nameMap);
    nameMap.put("A", "LocalIP");
    nameMap.put("B", "ResponseSize");
    nameMap.put("C", "Cookie");
    nameMap.put("D", "ServerTime");
    nameMap.put("H", "RequestProtocol");
    nameMap.put("I", "BytesReceived");
    nameMap.put("L", "RequestLogID");
    nameMap.put("O", "BytesSent");
    nameMap.put("P", "ProcessID");
    nameMap.put("R", "ResponseHandler");
    nameMap.put("S", "BytesTransferred");
    nameMap.put("T", "ResponseTime");
    nameMap.put("U", "RequestedURL");
    nameMap.put("V", "ServerName");
    nameMap.put("X", "ConnectionStatus");
    nameMap.put("a", "ClientIP");
    nameMap.put("b", "ResponseSizeCLF");
    nameMap.put("e", "ENV");
    nameMap.put("f", "Filename");
    nameMap.put("h", "RemoteHostname");
    nameMap.put("i", "VAR");
    nameMap.put("k", "KeepaliveCount");
    nameMap.put("l", "RemoteLogName");
    nameMap.put("m", "RequestMethod");
    nameMap.put("n", "NoteVAR");
    nameMap.put("o", "ReplyVAR");
    nameMap.put("p", "ServerPort");
    nameMap.put("q", "QueryString");
    nameMap.put("r", "RequestLine");
    nameMap.put("s", "Status");
    nameMap.put("t", "Time");
    nameMap.put("ti", "RequestTrailer");
    nameMap.put("to", "ResponseTrailer");
    nameMap.put("u", "RemoteUser");
    nameMap.put("v", "CanonicalServerName");
    Map<String, LogFieldType> typeMap = new HashMap<String, LogFieldType>();
    TYPES.put(ParsingMode.APACHE, typeMap);
    typeMap.put("A", LogFieldType.TEXT);
    typeMap.put("B", LogFieldType.NUMBER);
    typeMap.put("C", LogFieldType.TEXT);
    typeMap.put("D", LogFieldType.NUMBER);
    typeMap.put("H", LogFieldType.TEXT);
    typeMap.put("I", LogFieldType.NUMBER);
    typeMap.put("L", LogFieldType.TEXT);
    typeMap.put("O", LogFieldType.NUMBER);
    typeMap.put("P", LogFieldType.NUMBER);
    typeMap.put("R", LogFieldType.TEXT);
    typeMap.put("S", LogFieldType.NUMBER);
    typeMap.put("T", LogFieldType.NUMBER);
    typeMap.put("U", LogFieldType.TEXT);
    typeMap.put("V", LogFieldType.TEXT);
    typeMap.put("X", LogFieldType.TEXT);
    typeMap.put("a", LogFieldType.TEXT);
    typeMap.put("b", LogFieldType.NUMBER);
    typeMap.put("e", LogFieldType.TEXT);
    typeMap.put("f", LogFieldType.TEXT);
    typeMap.put("h", LogFieldType.TEXT);
    typeMap.put("i", LogFieldType.TEXT);
    typeMap.put("k", LogFieldType.NUMBER);
    typeMap.put("l", LogFieldType.TEXT);
    typeMap.put("m", LogFieldType.TEXT);
    typeMap.put("n", LogFieldType.TEXT);
    typeMap.put("o", LogFieldType.TEXT);
    typeMap.put("p", LogFieldType.NUMBER);
    typeMap.put("q", LogFieldType.TEXT);
    typeMap.put("r", LogFieldType.TEXT);
    typeMap.put("s", LogFieldType.NUMBER);
    typeMap.put("t", LogFieldType.DATE);
    typeMap.put("ti", LogFieldType.TEXT);
    typeMap.put("to", LogFieldType.TEXT);
    typeMap.put("u", LogFieldType.TEXT);
    typeMap.put("v", LogFieldType.TEXT);

  }


  private final LogFormat logFormat;


  LogEntryMapper(ParsingMode mode, String format) {
    logFormat = calculateFormat(mode, format);
  }

  public LogEntryMapper(String name, String format) {
    logFormat = calculateFormat(ParsingMode.getModeByName(name), format);
  }


  private LogFormat calculateFormat(ParsingMode mode, String format) {
    LogFormat retval = new LogFormat(mode);
    final StringParser parser = new StringParser(format);
    switch (mode) {
      case APACHE:
        parseApacheFormat(format, retval);
        break;
      default:
        parseCommonFormat(format, retval);
        break;
    }

    return retval;
  }

  /**
   * This just creates a set of fields of type "text". THe format is assumed to be a set of space-delimited names.
   *
   * @param format    the format of the string
   * @param logFormat the LogFormat object to update.
   */
  private void parseCommonFormat(String format, LogFormat logFormat) {
    final StringParser parser = new StringParser(format);
    try {
      while (!parser.eof()) {
        parser.skipWhitespace();
        String token = parser.readToken();
        logFormat.add(new LogFieldFormat(token, LogFieldType.TEXT));
      }
    } catch (final Exception ex) {
      System.err.println("Error at position:" + parser.getOffset() + " line:" + parser.getCurrentLineNumber() + " column:" + parser.getColumnNumber() + " last:" + new Character((char) parser.getLastCharacterRead()));
    }
  }

  /**
   * Parse the Apache log format string into a set of LogFields to be placed in the given log format object.
   *
   * @param format
   * @param logFormat
   */
  private void parseApacheFormat(String format, LogFormat logFormat) {
    final StringParser parser = new StringParser(format);
    Map<String, String> nameMap = NAMES.get(ParsingMode.APACHE);
    Map<String, LogFieldType> typeMap = TYPES.get(ParsingMode.APACHE);

    String token = null;
    int columnNumber = 0;
    try {
      while (!parser.eof()) {

        parser.skipWhitespace();
        parser.readTo(PERCENT);
        int nextChar = parser.peek();

        if (OPEN_BRACE == nextChar) {
          parser.read();
          token = parser.readTo(CLOSE_BRACE);
        } else if (PERCENT == nextChar) {
          parser.read();
          parser.readTo(PERCENT);
        }
        // all that's left is the directive
        String directive = parser.readToken();

        //return only the directive and not trailing delimiters
        directive = directive.replaceAll("[^a-zA-Z]", "");

        // lookup the name of the directive
        String name = nameMap.get(directive);
        if (StringUtil.isBlank(name)) {
          name = "column." + columnNumber + "(" + directive + ")";
        }
        if (StringUtil.isNotBlank(token)) {
          name = name + "-" + token;
        }

        LogFieldType type = typeMap.get(directive);
        if (type == null) {
          type = LogFieldType.TEXT;
        }

        logFormat.add(new LogFieldFormat(name, type));

        columnNumber++;
      } // while parser not EOF
    } catch (final Exception ex) {
      System.err.println("Error at position:" + parser.getOffset() + " line:" + parser.getCurrentLineNumber() + " column:" + parser.getColumnNumber() + " last:" + new Character((char) parser.getLastCharacterRead()));
    }
  }


  /**
   * Convert the given log entry into a named and typed data frame.
   *
   * @param logEntry the log entry
   * @return a dataframe representing the log entry.
   */
  public DataFrame mapToFrame(String logEntry) {
    DataFrame retval = new DataFrame();
    final StringParser parser = new StringParser(logEntry);

    String token = null;
    int index = 0;
    try {
      while (!parser.eof()) {
        parser.skipWhitespace();
        int nextChar = parser.peek();

        switch (nextChar) {
          case OPEN_BRACKET:
            parser.read(); // consume open bracket
            token = parser.readTo(CLOSE_BRACKET);
            break;
          case QUOTE:
            parser.read(); // consume open quote
            token = parser.readTo(QUOTE);
            break;
          default:
            token = parser.readToken();
            break;
        }
        if (index < logFormat.size()) {
          Object value = token;
          LogFieldFormat fieldFormat = logFormat.get(index++);

          if (LogFieldType.NUMBER.equals(fieldFormat.getType())) {
            if (DASH.equalsIgnoreCase(token.trim())) {
              value = 0L;
            } else {
              try {
                value = Long.parseLong(token);
              } catch (NumberFormatException e) {
                Log.warn("Could not convert log entry column " + index + " (" + fieldFormat.getName() + ") to a number: " + e.getMessage());
                value = 0L;

              }
            }
          } else if (LogFieldType.DATE.equals(fieldFormat.getType())) {
            try {
              value = DateUtil.parse(token);
              if (value == null) {
                try {
                  value = new SimpleDateFormat(APACHE_DATE_FORMAT).parse(token);
                } catch (ParseException e) {
                  Log.error("Cannot parse date: '" + token + "'");
                }
              }
            } catch (NumberFormatException e) {
              Log.warn("Could not convert log entry column " + index + " (" + fieldFormat.getName() + ") to a date: " + e.getMessage());
            }
          }
          retval.add(fieldFormat.getName(), value);
        } else {
          Log.warn("There are more log entry fields than log format directives");
          retval.add(Integer.toString(index++), token);
        }

      } // while parser not EOF
    } catch (final Exception ex) {
      System.err.println("Error at position:" + parser.getOffset() + " line:" + parser.getCurrentLineNumber() + " column:" + parser.getColumnNumber() + " last:" + new Character((char) parser.getLastCharacterRead()));
    }

    return retval;
  }

  public LogFormat getLogFormat() {
    return logFormat;
  }
}
