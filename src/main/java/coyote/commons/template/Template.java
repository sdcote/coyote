/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
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

import java.io.IOException;
import java.util.Hashtable;

import coyote.commons.StringParser;


/**
 * Parses a String and replaces variables with the values represented in a
 * table of symbols.
 * 
 * <p>Templates are designed to be created once then evaluated multiple times 
 * using different or updated {@code SymbolTable} objects so different values 
 * can be used. The original use case for this class was creating an email body
 * for a monitoring system which would send an email with the details of an 
 * event in consistent manner. The details of the event were different (e.g. 
 * time, error message, component in error, severity addressee, etc.) and 
 * concatenating strings was just to cumbersome. This class made it easy to 
 * create a template of the message and populate the symbol table with the 
 * details of event and let the template fill in the variables.</p>
 *
 * <p>This utility searches for tags delimited by the opening &quot;[#&quot;
 * and the closing &quot;#]&quot; string sequences. This parser then replaces
 * the tokens within the delimiters with the string values those tokens
 * represent.</p>
 *
 * <p> If the token is preceded with a &quot;$&quot;, then the token is treated
 * as a key to be used to lookup an object in a symbol table. That object's
 * <code>toString</code> method is called and the returning value placed in the
 * position where the token was found. This is analogous to a variable lookup.
 * If the token is not found in the table or the symbols object returns a null
 * string, then the string &quot;null&quot; is returned.</p>
 *
 * <p>Tokens that are not preceded with a &quot;$&quot; are treated as class
 * tokens when encountered, the parser attempts to create a new instance of
 * that class, calling the new instances <code>toString</code> method after
 * it's constructor. If the class is not found, a string value of
 * &quot;null&quot; is returned.</p>
 *
 * <p>If a class token contains the &quot;.&quot; character, then special
 * processing will take place. Specifically, if the last token delimited by the
 * &quot;.&quot; character in the class token starts with a lowercase alpha
 * character, then that sub-token will be treated as a method name, and the
 * remaining symbols in that tag will be resolved and passed to that method
 * after that method's class is instantiated.</p>
 *
 * <p>Classes that are created during the parsing of a template string are
 * cached and re-used by the instance of the Template class. This means the
 * template can be effectively initialized by one template string, loading the
 * classes needed for later template parsing operations.</p>
 *
 * <p>Pre-initialized class references may be placed into the class cache in an
 * effort to give the template visibility into component frameworks. This
 * allows templates to call into generic, sharable facilities such as Data
 * Access Objects (DAO), data repositories, and any other specialized object
 * that is designed to present data in a string format.</p>
 */
public class Template extends StringParser {
  private static SymbolTable symbols = new SymbolTable();
  private Hashtable<String, Object> classCache = new Hashtable<String, Object>();

  private static final String OPEN = "[#";
  private static final String CLOSE = "#]";




  /**
   * Constructor Template
   *
   * @param string
   */
  public Template( String string ) {
    super( string );
  }




  /**
   * Constructor Template
   *
   * @param string
   * @param symbols
   */
  public Template( String string, SymbolTable symbols ) {
    super( string );

    Template.symbols = symbols;
  }




  /**
   * Method put
   *
   * @param obj
   */
  public void put( Object obj ) {
    if ( obj != null ) {
      classCache.put( obj.getClass().getName(), obj );
    }
  }




  /**
   * Get the object with the given name.
   *
   * @param name The name of the object to retrieve.
   *
   * @return the object with the given name or null if not found.
   */
  public Object get( String name ) {
    if ( ( name != null ) && ( name.length() > 0 ) ) {
      return classCache.get( name );
    }

    return null;
  }




  /**
   * Lookup the given tag in the given symbol table and object cache.
   *
   * @param tag the tag this method is to resolve
   * @param symbols The hash of scalar typed data mapped by name
   * @param cache the hash table of object instances that may provide dynamic data
   *
   * @return a string representing the data behind the given tag.
   */
  public static String resolve( String tag, SymbolTable symbols, Hashtable cache ) {
    StringBuffer retval = new StringBuffer();

    StringParser parser = new StringParser( tag );

    try {
      while ( !parser.eof() ) {
        String token = parser.readToken();

        if ( ( token == null ) || ( token.length() < 1 ) ) {
          break;
        }

        if ( token.startsWith( "$" ) ) {
          String key = token.substring( 1 );
          retval.append( symbols.getString( key ) );
        } else {
          // Must be a class; see if it is a method or constructor reference

          //
          // TODO: More goes here
          //

        }
      }
    } catch ( Exception ex ) {

    }

    return retval.toString();
  }




  /**
   * Return this template as a string, resolving all the tags.
   *
   * @return the String representing the resolved template.
   */
  public String toString() {
    try {
      // call our static method with our instance attributes
      return toString( this, symbols, classCache );
    } catch ( TemplateException te ) {
      System.err.println( te.getMessage() );
      System.err.println( te.getContext() );

      throw new IllegalArgumentException( "Parser error" );
    }
  }




  /**
   * Convert the template into a string using the given symbol map.
   *
   * <p>This is where all the work takes place.
   *
   * @param template The string representing the template data
   * @param symbols the SymbolTable to us when resolving tokens
   * @param cache the class cache to use when looking up class references
   *
   * @return a string representing the fully-resolved template.
   *
   * @throws TemplateException
   */
  public static String toString( Template template, SymbolTable symbols, Hashtable cache ) throws TemplateException {
    if ( template != null ) {
      if ( symbols == null ) {
        symbols = new SymbolTable();
      }

      StringBuffer buffer = new StringBuffer();

      try {
        // Keep looping
        while ( !template.eof() ) {
          String userText = template.readToPattern( OPEN );

          if ( userText != null ) {
            buffer.append( userText );
          }

          // if we are at the End Of the File, then we are done
          if ( template.eof() ) {
            break;
          } else {
            // Skip past the opening tag delimiter
            template.skip( OPEN.length() );

            // Start reading the contents of the tag
            String tag = template.readToPattern( CLOSE );

            // If we are at EOF then the read terminated before the closing tag
            // was encountered. This means the template is not complete.
            if ( template.eof() ) {
              buffer.append( "TEMPLATE ERROR: reached EOF before finding closing delimiter '" + CLOSE + "' at " + template.getPosition() );

              return buffer.toString();
            }

            // read past the closing delimiter
            template.skip( CLOSE.length() );

            // OK, now perform our replacement magik
            if ( ( tag != null ) && ( tag.length() > 0 ) ) {
              // resolve the tag using the given symbol table and class cache
              buffer.append( resolve( tag, symbols, cache ) );
            }

          }
        }
      } catch ( IOException ioe ) {
        throw new TemplateException( "IOE", ioe );
      }

      return buffer.toString();
    }

    return null;
  }




  /**
   * Method mergeSymbols
   *
   * @param table
   */
  public void mergeSymbols( SymbolTable table ) {
    symbols.merge( table );
  }




  /**
   * Method getSymbols
   *
   * @return the symbol table
   */
  public SymbolTable getSymbols() {
    return symbols;
  }




  /**
   * Method setSymbols
   *
   * @param symbols
   */
  public void setSymbols( SymbolTable symbols ) {
    Template.symbols = symbols;
  }




  /**
   * Method addSymbol
   *
   * @param name
   * @param value
   */
  public void addSymbol( String name, Object value ) {
    if ( ( name != null ) && ( value != null ) ) {
      symbols.put( name, value );
    }
  }




  /**
   * Resolve the template string with the given symbol table.
   * 
   * <p>This is a utility method to make using templates easier and to improve 
   * the readability of code.</p>
   * 
   * <p>If the text or the symbol reference is null, the reference to the text 
   * is returned unchanged. This prevents null reference exceptions from being 
   * thrown.</p> 
   * 
   * @param text the template text
   * @param symbols the symbol table to use in resolving the variables
   * 
   * @return the resulting string with all the variable resolved.
   */
  public static String resolve( String text, SymbolTable symbols ) {
    if ( symbols != null && text != null ) {
      return new Template( text, symbols ).toString();
    } else {
      return text;
    }

  }

}