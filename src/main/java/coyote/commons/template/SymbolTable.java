/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.template;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import coyote.commons.ArrayUtil;
import coyote.commons.DateUtil;
import coyote.commons.StringUtil;


/**
 * Simply a table of named string values.
 * 
 * <p>This table has some utility functions to manage the data in the table 
 * such as placing system properties in and removing them from the table.</p>
 * 
 * <p>The following variables are ignored for security reasons:<ul>
 * <li>{@code http.proxyPassword} - potential password exposure</li>
 * </ul></p>
 */
public class SymbolTable extends HashMap {

  private static final long serialVersionUID = -3448311765253950903L;




  /**
   * Constructor SymbolTable
   */
  public SymbolTable() {}




  /**
   * Read all the System properties into the SymbolTable.
   */
  public synchronized void readSystemProperties() {
    readProperties( System.getProperties() );
    cleanse();
  }




  private void cleanse() {
    remove( "http.proxyPassword" );
  }




  /**
   * Remove all the System properties from the SymbolTable.
   */
  public synchronized void removeSystemProperties() {
    removeProperties( System.getProperties() );
  }




  /**
   * Remove the given set of properties from the symbol table.
   * 
   * @param props The properties to remove
   */
  public synchronized void removeProperties( Properties props ) {
    if ( props != null ) {
      for ( Enumeration en = props.propertyNames(); en.hasMoreElements(); ) {
        String name = (String)en.nextElement();
        remove( name );
      }
    }
  }




  /**
   * Read the given set of properties into the symbol table.
   * 
   * @param props the properties to add/replace in this symbol table
   */
  public synchronized void readProperties( Properties props ) {
    if ( props != null ) {
      for ( Enumeration en = props.propertyNames(); en.hasMoreElements(); ) {
        String name = (String)en.nextElement();
        put( name, System.getProperty( name ) );
      }
    }
  }




  /**
   * Return the String value of the named symbol from the table.
   *
   * @param symbol the symbol to lookup in the table
   *
   * @return the value of the symbol or an empty string if the value was not found.
   */
  public synchronized String getString( String symbol ) {
    if ( symbol != null ) {
      if ( containsKey( symbol ) ) {
        return get( symbol ).toString();
      } else if ( symbol.equals( "time" ) ) {
        return DateUtil.toExtendedTime( new Date() );
      } else if ( symbol.equals( "currentMilliseconds" ) ) {
        return Long.toString( System.currentTimeMillis() );
      } else if ( symbol.equals( "currentSeconds" ) ) {
        return Long.toString( System.currentTimeMillis() / 1000 );
      } else if ( symbol.equals( "epocTime" ) ) {
        return Long.toString( System.currentTimeMillis() / 1000 );
      } else if ( symbol.equals( "rfc822date" ) ) {
        return DateUtil.RFC822Format( new Date() );
      } else if ( symbol.equals( "iso8601date" ) ) {
        return DateUtil.ISO8601Format( new Date() );
      } else if ( symbol.equals( "iso8601GMT" ) ) {
        return DateUtil.ISO8601GMT( new Date() );
      } else if ( symbol.equals( "CR" ) ) {
        return StringUtil.CR;
      } else if ( symbol.equals( "NL" ) ) {
        return StringUtil.NL;
      } else if ( symbol.equals( "CRLF" ) ) {
        return StringUtil.CRLF;
      } else if ( symbol.equals( "FS" ) ) {
        return StringUtil.FILE_SEPARATOR;
      } else if ( symbol.equals( "PS" ) ) {
        return StringUtil.PATH_SEPARATOR;
      } else if ( symbol.equals( "HT" ) ) {
        return StringUtil.HT;
      } else if ( symbol.equals( "NL" ) ) {
        return StringUtil.NL;
      } else if ( symbol.equals( "symbolDump" ) ) {
        return dump();
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
  public synchronized void merge( HashMap symbols ) {
    for ( Iterator it = symbols.keySet().iterator(); it.hasNext(); ) {
      try {
        String key = (String)it.next();
        put( key, symbols.get( key ) );
      } catch ( Exception ex ) {
        // key was not a String?
        // value was null?
      }
    }
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
  public synchronized String[] getChildNames( String prefix ) {
    String[] retval = new String[0];

    for ( Iterator it = keySet().iterator(); it.hasNext(); ) {
      String key = (String)it.next();

      if ( key.startsWith( prefix ) ) {
        retval = (String[])ArrayUtil.addElement( (String[])retval, (String)key );
      }
    }

    return retval;
  }




  /**
   * More of a debugging tool, this creates a string with the entire contents 
   * of the symbol table.
   *
   * @return the entire contents of the table as a string.
   */
  public synchronized String dump() {
    StringBuffer retval = new StringBuffer();

    for ( Iterator it = keySet().iterator(); it.hasNext(); ) {
      retval.append( "'" );
      String key = (String)it.next();
      retval.append( key );
      retval.append( "' = " );
      Object value = get( key );
      if ( value != null )
        retval.append( value.toString() );
      retval.append( StringUtil.LINE_FEED );
    }

    return retval.toString();
  }

}