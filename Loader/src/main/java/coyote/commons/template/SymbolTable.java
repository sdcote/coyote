/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.template;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import coyote.commons.ArrayUtil;
import coyote.commons.CipherUtil;
import coyote.commons.DateUtil;
import coyote.commons.NumberUtil;
import coyote.commons.StringUtil;
import coyote.loader.Loader;


/**
 * Simply a table of named string values.
 *
 * <p>This table has some utility functions to manage the data in the table
 * such as placing system properties in and removing them from the table.</p>
 *
 * <p>The following variables are ignored for security reasons:<ul>
 * <li>{@code http.proxyPassword} - potential password exposure</li>
 * </ul>
 */
public class SymbolTable extends HashMap {

  private static final long serialVersionUID = -3448311765253950903L;

  /** keys with this prefix are assumed to be encrypted and should be decrypted before being returned */
  static final String ENCRYPT_PREFIX = Loader.ENCRYPT_PREFIX;

  // Hash Maps of formatters under the (possibly mistaken) assumption that construction and garbage collection may be more expensive than caching and searching
  private final HashMap<String, DateFormat> dateFormatMap = new HashMap<String, DateFormat>();
  private final HashMap<String, NumberFormat> numberFormatMap = new HashMap<String, NumberFormat>();




  /**
   * Constructor SymbolTable
   */
  public SymbolTable() {}




  /**
   * Determine if the given symbol is a supported literal.
   *
   * @param symbol the literal symbol to check
   *
   * @return true if it is a supported literal, false if the symbol is not a literal
   */
  public boolean containsLiteral(final String symbol) {
    return (StringUtil.isNotEmpty(getStaticValue(symbol)));
  }




  /**
   * More of a debugging tool, this creates a string with the entire contents
   * of the symbol table.
   *
   * @return the entire contents of the table as a string.
   */
  public synchronized String dump() {
    final StringBuffer retval = new StringBuffer();

    for (final Iterator it = keySet().iterator(); it.hasNext();) {
      retval.append("'");
      final String key = (String)it.next();
      retval.append(key);
      retval.append("' = ");
      final Object value = get(key);
      if (value != null) {
        retval.append(value.toString());
      }
      retval.append(StringUtil.LINE_FEED);
    }

    return retval.toString();
  }




  /**
   * Return all the symbols which start with the given prefix.
   *
   * <p>This method is used when a symbol naming convention is used which
   * involves segmented names in the form of tokens representing levels and
   * some token delimiter. This models a hierarchy of values common in complex
   * property files. Using this method, it is possible to retrieve all the keys
   * for a specific level in the tree.</p>
   *
   * @param prefix The prefix of the symbol for which to search (e.g. "log.config.file.")
   *
   * @return a list of symbols which start with that parental prefix
   */
  public synchronized String[] getChildNames(final String prefix) {
    String[] retval = new String[0];

    for (final Iterator it = keySet().iterator(); it.hasNext();) {
      final String key = (String)it.next();

      if (key.startsWith(prefix)) {
        retval = (String[])ArrayUtil.addElement(retval, key);
      }
    }

    return retval;
  }




  /**
   * Return the String value of the named symbol from the table.
   *
   * <p>If the symbol starts with the "encoded" prefix, use the CipherUtil to
   * decrypt the value before returning it. This way, the the symbol table
   * contains the encrypted value and only gets decrypted when referenced.
   * This should reduce the exposure of the protected value.
   *
   * @param symbol the symbol to lookup in the table
   *
   * @return the value of the symbol or an empty string if the value was not
   *         found.
   */
  public synchronized String getString(final String symbol) {
    if (symbol != null) {
      if (containsKey(symbol)) {
        final Object obj = get(symbol);
        if (obj != null) {
          if (symbol.startsWith(ENCRYPT_PREFIX)) {
            final String retval = CipherUtil.decryptString(obj.toString());
            return retval;
          } else {
            return obj.toString();
          }
        }
      }
      return getStaticValue(symbol);
    }
    return "";
  }




  /**
   * Return the String value of the named symbol from the table applying the
   * given formatting.
   *
   * @param symbol the symbol to lookup in the table
   * @param format the format pattern to use
   *
   * @return the value of the symbol or an empty string if the value was not found.
   */
  public synchronized String getString(final String symbol, final String format) {
    if (symbol != null) {
      if (containsKey(symbol)) {
        final Object retval = get(symbol);

        // check to see if there is formatting to be applied to the value
        if (StringUtil.isNotBlank(format)) {

          // apply formatting based on type type of object it is
          if (retval instanceof Number) {
            // If retval is numeric, then use a number format
            return formatNumber((Number)retval, format);
          } else if (retval instanceof Date) {
            // if retval is a date, then use a date format
            return formatDate((Date)retval, format);
          } else if (retval instanceof String) {
            // maybe it is a string representation of a number
            try {
              return formatNumber(NumberUtil.parse((String)retval), format);
            } catch (NumberFormatException e) {
              // whoops, guess not
            }
          }
        }

        // either the format string was empty or returned value is not format-able
        return retval.toString();

      } else {
        // we do not have the symbol in our table, so try a static value
        return getStaticValue(symbol);
      }
    }

    return "";
  }




  /**
   * Go through all the symbols in the given table and add/replace them to our
   * table.
   *
   * @param symbols the Hashtable of name value pairs to merge.
   */
  public synchronized void merge(final HashMap symbols) {
    for (final Iterator it = symbols.keySet().iterator(); it.hasNext();) {
      try {
        final String key = (String)it.next();
        put(key, symbols.get(key));
      } catch (final Exception ex) {
        // key was not a String?
        // value was null?
      }
    }
  }




  /**
   * Read the given set of properties into the symbol table.
   *
   * @param props the properties to add/replace in this symbol table
   */
  public synchronized void readProperties(final Properties props) {
    if (props != null) {
      for (final Enumeration en = props.propertyNames(); en.hasMoreElements();) {
        final String name = (String)en.nextElement();
        put(name, System.getProperty(name));
      }
    }
  }




  /**
   * Read all the System properties into the SymbolTable.
   */
  public synchronized void readSystemProperties() {
    readProperties(System.getProperties());
    cleanse();
  }




  /**
   * Remove the given set of properties from the symbol table.
   *
   * @param props The properties to remove
   */
  public synchronized void removeProperties(final Properties props) {
    if (props != null) {
      for (final Enumeration en = props.propertyNames(); en.hasMoreElements();) {
        final String name = (String)en.nextElement();
        remove(name);
      }
    }
  }




  /**
   * Remove all the System properties from the SymbolTable.
   */
  public synchronized void removeSystemProperties() {
    removeProperties(System.getProperties());
  }




  /**
   * Remove properties which are known to contain sensitive data
   */
  private void cleanse() {
    remove("http.proxyPassword");
  }




  /**
   * Format the given date with the given format string.
   *
   * @param date the date to format
   * @param format the format string
   *
   * @return the formatted date string or an empty string if the date is null
   */
  private String formatDate(final Date date, final String format) {
    if (date != null) {

      // retrieve an existing formatter
      DateFormat formatter = dateFormatMap.get(format);

      // if one was not found, create one and cache it for later
      if (formatter == null) {
        formatter = new SimpleDateFormat(format);
        dateFormatMap.put(format, formatter);
      }

      // return the formatted date
      return formatter.format(date);
    } else {
      return "";
    }
  }




  /**
   * Format the given number with the given format string.
   *
   * @param number the date to format
   * @param format the format string
   *
   * @return the formatted number string or an empty string if the number is null
   */
  private String formatNumber(final Number number, final String format) {
    if (number != null) {
      NumberFormat formatter = numberFormatMap.get(format);

      // if one was not found, create one and cache it for later
      if (formatter == null) {
        formatter = new DecimalFormat(format);
        numberFormatMap.put(format, formatter);
      }

      // return the formatted number
      return formatter.format(number);
    } else {
      return "";
    }
  }




  private String getStaticValue(final String symbol) {
    if (symbol != null) {
      if (symbol.equals("time")) {
        return DateUtil.toExtendedTime(new Date());
      } else if (symbol.equals("currentMilliseconds")) {
        return Long.toString(System.currentTimeMillis());
      } else if (symbol.equals("currentSeconds")) {
        return Long.toString(System.currentTimeMillis() / 1000);
      } else if (symbol.equals("epocTime")) {
        return Long.toString(System.currentTimeMillis() / 1000);
      } else if (symbol.equals("rfc822date")) {
        return DateUtil.RFC822Format(new Date());
      } else if (symbol.equals("iso8601date")) {
        return DateUtil.ISO8601Format(new Date());
      } else if (symbol.equals("iso8601GMT")) {
        return DateUtil.ISO8601GMT(new Date());
      } else if (symbol.equals("CR")) {
        return StringUtil.CR;
      } else if (symbol.equals("NL")) {
        return StringUtil.NL;
      } else if (symbol.equals("CRLF")) {
        return StringUtil.CRLF;
      } else if (symbol.equals("FS")) {
        return StringUtil.FILE_SEPARATOR;
      } else if (symbol.equals("PS")) {
        return StringUtil.PATH_SEPARATOR;
      } else if (symbol.equals("HT")) {
        return StringUtil.HT;
      } else if (symbol.equals("NL")) {
        return StringUtil.NL;
      } else if (symbol.equals("symbolDump")) {
        return dump();
      }
    }

    return "";
  }

}