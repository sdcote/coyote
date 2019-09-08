/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import coyote.commons.StringParseException;
import coyote.commons.StringParser;

import java.io.IOException;

/**
 * <p>This is a very forgiving parser and supports more than just the query string syntax. It will accept a variety of
 * commonly worded filter queries. For example, it will accept  the '=' (equals) and the text 'is' to mean the same
 * thing.</p>
 *
 * <p>The basic syntax for a filter is one ore more clauses chained together with ^ or AND or a ^OR or OR meaning and
 * OR. Each clause is maid up with a field, predicate and a value each separated by a space. Some predicates (such as
 * ISEMPTY) does not require a value and the clause terminates with the predicate.</p>
 */
public class FilterParser extends StringParser {

  /**
   * @param string
   */
  public FilterParser(String string) {
    super(string);
  }


  public static SnowFilter parse(String text) throws StringParseException {
    FilterParser parser = new FilterParser(text);
    SnowFilter retval = new SnowFilter();

    // This keeps track of whether the currently built clause is to be placed in the filter with an AND. If false, the
    // clause will be placed in the filter with an OR.
    boolean isAnd = true;

    String field = null;
    Predicate predicate = null;
    String value = null;
    String token = null;
    char next = 0;
    try {
      // Keep looping
      while (!parser.eof()) {
        field = null;
        predicate = null;
        value = null;
        token = null;
        next = 0;

        // skip to some text
        parser.skipWhitespace();

        // read to a space, ! or =
        field = parser.readToDelimiter(" !=><");
        parser.skipWhitespace();

        // start to read the predicate
        next = (char) parser.peek();

        switch (next) {
          case '=':
            predicate = Predicate.IS;
            parser.read();
            break;
          case '>':
            next = (char) parser.readAndPeek();
            if ('=' == next) {
              predicate = Predicate.GREATER_THAN_EQUALS;
              parser.read();
            } else {
              predicate = Predicate.GREATER_THAN;
            }
            break;
          case '<':
            next = (char) parser.readAndPeek();
            if ('=' == next) {
              predicate = Predicate.LESS_THAN_EQUALS;
              parser.read();
            } else {
              predicate = Predicate.LESS_THAN;
            }
            break;
          case '!':
            parser.read();
            next = (char) parser.peek();
            if ('=' == next) {
              predicate = Predicate.IS_NOT;
              parser.read();
              break;
            } else {
              //Maybe !empty, !like, !sameas
              token = parser.readToken();
              if (token.equalsIgnoreCase("empty")) {
                predicate = Predicate.IS_NOT_EMPTY;
              } else if (token.equalsIgnoreCase("like")) {
                predicate = Predicate.DOES_NOT_CONTAIN;
              } else if (token.equalsIgnoreCase("sameas")) {
                predicate = Predicate.DIFFERENT_FROM;
              } else {
                throw new StringParseException("Negation predicate not recognized at " + parser.getPosition());
              }
            }
          default:
            token = parser.readToken();
            if ("LIKE".equalsIgnoreCase(token)) {
              predicate = Predicate.LIKE;
            } else if ("IS".equalsIgnoreCase(token)) {
              predicate = Predicate.IS;
            } else if ("CONTAINS".equalsIgnoreCase(token)) {
              predicate = Predicate.CONTAINS;
            } else if ("NSAMEAS".equalsIgnoreCase(token)) {
              predicate = Predicate.DIFFERENT_FROM;
            } else if ("NOT%20LIKE".equalsIgnoreCase(token)) {
              predicate = Predicate.DOES_NOT_CONTAIN;
            } else if ("NOTLIKE".equalsIgnoreCase(token)) {
              predicate = Predicate.NOT_LIKE;
            } else if ("NOT".equalsIgnoreCase(token)) {
              token = parser.readAndPeekToken();
              if ("LIKE".equalsIgnoreCase(token)) {
                predicate = Predicate.NOT_LIKE;
              } else if ("EMPTY".equalsIgnoreCase(token)) {
                predicate = Predicate.IS_NOT_EMPTY;
              }
              predicate = Predicate.DOES_NOT_CONTAIN;
              parser.readToken();
              break;
            } else if ("ENDSWITH".equalsIgnoreCase(token)) {
              predicate = Predicate.ENDS_WITH;
            } else if ("IN".equalsIgnoreCase(token)) {
              predicate = Predicate.IN;
            } else if ("ANYTHING".equalsIgnoreCase(token)) {
              predicate = Predicate.IS_ANYTHING;
            } else if ("ISEMPTY".equalsIgnoreCase(token)) {
              predicate = Predicate.IS_EMPTY;
            } else if ("EMPTYSTRING".equalsIgnoreCase(token)) {
              predicate = Predicate.IS_EMPTY_STRING;
            } else if ("ISNOTEMPTY".equalsIgnoreCase(token)) {
              predicate = Predicate.IS_NOT_EMPTY;
            } else if ("NOTEMPTY".equalsIgnoreCase(token)) {
              predicate = Predicate.IS_NOT_EMPTY;
            } else if ("123TEXTQUERY321".equalsIgnoreCase(token)) {
              predicate = Predicate.KEYWORDS;
            } else if ("SAMEAS".equalsIgnoreCase(token)) {
              predicate = Predicate.SAME_AS;
            } else if ("STARTSWITH".equalsIgnoreCase(token)) {
              predicate = Predicate.STARTS_WITH;
            }
            break;
        }

        if (predicate == null) {
          throw new StringParseException("Could not determine predicate - last token '" + token + "' at " + parser.getPosition());
        }

        // if the predicate requires a value for comparison
        if (predicate.requiresValue()) {

          // This will read up to the next clause connector (^ ^OR || && AND OR)
          value = readValue(parser).trim();

        }

        // create a clause
        if (isAnd) {
          retval.and(field.trim(), predicate, value);
        } else {
          retval.or(field.trim(), predicate, value);
        }
        parser.skipWhitespace();

        // check for AND or OR ( maybe ^ and ^OR....&& and || as well?)
        if (!parser.eof()) {
          next = (char) parser.peek();
          switch (next) {
            case '^':
              next = (char) parser.readAndPeek();

              if (' ' == next) {
                isAnd = true;
                parser.read();
              }

              // peek the next 2 characters to see if it is an OR
              token = parser.peek(2);
              if ("or".equalsIgnoreCase(token)) {
                isAnd = false;
                parser.read();
                parser.read();
              } else {
                // looks like the start of the next field
                isAnd = true;
              }
              break;
            case '&':
              next = (char) parser.readAndPeek();
              if ('&' == next) {
                isAnd = true;
                parser.read();
              } else {
                throw new StringParseException("Invalid AND at " + parser.getPosition());
              }
              break;
            case '|':
              next = (char) parser.readAndPeek();
              if ('|' == next) {
                isAnd = false;
                parser.read();
              } else {
                throw new StringParseException("Invalid OR at " + parser.getPosition());
              }
              break;
            default:
              // looking for AND or OR; if not either, then default to ANDing and treat this as the name of the next clause
              token = parser.readToken();
              if ("and".equalsIgnoreCase(token)) {
                isAnd = true;
              }
              if ("or".equalsIgnoreCase(token)) {
                isAnd = false;
                parser.readToken();
              } else {
                // must be part of next name, default to AND
                isAnd = true;
              }
              break;

          }// switch

        } // if !eof

      } // while !eof

    } catch (IOException ioe) {
      StringBuffer b = new StringBuffer();
      b.append("IOE: ");
      b.append(parser.getPosition());
      b.append(" field = '");
      b.append(field);
      b.append("' predicate = '");
      b.append(predicate);
      b.append("' value = '");
      b.append(value);
      b.append("' token = '");
      b.append(token);
      b.append("' next = ");
      b.append(next);
      throw new StringParseException(b.toString(), ioe);
    }

    return retval;
  }


  private static String readValue(FilterParser parser) throws IOException {
    StringBuffer b = new StringBuffer();

    char next = 0;

    while (!parser.eof()) {

      // peek at the next character
      next = (char) parser.peek();
      switch (next) {
        case '^':
        case '&':
        case '|':
          // it appears to be one of the clause connectors
          return b.toString();

        case ' ':
          b.append(' ');
          parser.skipWhitespace();
          break;

        default:
          // looking for AND or OR; if not either, then continue
          if ("and".equalsIgnoreCase(parser.peek(3)) || "or".equalsIgnoreCase(parser.peek(2))) {
            return b.toString();
          } else {
            // Add the next token to our return value
            b.append(parser.readToDelimiter(" \t\n^"));
          }
          break;

      } // switch

    } // while !eof

    return b.toString();
  }

}
