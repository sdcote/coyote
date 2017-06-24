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
import java.lang.reflect.Method;
import java.util.Hashtable;

import coyote.commons.StringParser;
import coyote.commons.StringUtil;


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
 * <p>If the token is preceded with a &quot;$&quot;, then the token is treated
 * as a key to be used to lookup an object in a symbol table. That object's
 * <code>toString</code> method is called and the returning value placed in the
 * position where the token was found. This is analogous to a variable lookup.
 * If the token is not found in the table or the symbols object returns a null
 * string, then an empty string is returned.</p>
 *
 * <p>Tokens that are not preceded with a &quot;$&quot; are treated as object
 * tokens. When encountered, the parser attempts to lookup the object in the 
 * static cache of objects. If found, the object's {@code toString()} method is 
 * called and that value is used at that location in the template. If the class 
 * is not found, a string value of &quot;null&quot; is returned.</p>
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
  private static Hashtable<String, Object> staticCache = new Hashtable<String, Object>();
  private static final String[] EMPTY_ARGS = new String[0];

  private static final String OPEN = "[#";
  private static final String CLOSE = "#]";
  private static final char DOT = '.';
  private static final char OP = '(';
  private static final char CP = ')';
  private static final char VAR = '$';
  private static final char PIPE = '|';
  private static final String VAR_PREFIX = String.valueOf( VAR );




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
   * Put an object in the class cache for use in template resolution
   *
   * @param obj the object to cache
   */
  public static void put( Object obj ) {
    if ( obj != null ) {
      staticCache.put( obj.getClass().getName(), obj );
    }
  }




  /**
   * Put an object in the class cache for use in template resolution
   *
   * @param obj the object to cache
   * @param name name of the class to place in the template
   */
  public static void put( String name, Object obj ) {
    if ( obj != null && name != null && name.length() > 0 ) {
      staticCache.put( name, obj );
    }
  }




  /**
   * Get the object with the given name from the cache.
   *
   * @param name The name of the object to retrieve.
   *
   * @return the object with the given name or null if not found.
   */
  public static Object get( String name ) {
    if ( ( name != null ) && ( name.length() > 0 ) ) {
      return staticCache.get( name );
    }
    return null;
  }




  /**
   * Lookup the given tag in the given symbol table and object cache.
   *
   * @param tag the tag this method is to resolve
   * @param symbols The hash of scalar typed data mapped by name
   * @param cache the hash table of object instances that may provide dynamic 
   *        data
   * @param preprocess true indicates unresolved symbols should remain in the 
   *        result, false means they should be replaced with an empty string.
   *
   * @return a string representing the data behind the given tag.
   */
  public static String resolve( String tag, SymbolTable symbols, Hashtable cache, boolean preprocess ) {
    StringBuffer retval = new StringBuffer();

    StringParser parser = new StringParser( tag );

    try {
      while ( !parser.eof() ) {
        String token = parser.readToken();

        if ( ( token == null ) || ( token.length() < 1 ) ) {
          break;
        }

        if ( token.startsWith( VAR_PREFIX ) ) {
          // if the token contains a vertical pipe character, split the token into the variable key and the format string.
          int boundry = token.indexOf( PIPE );
          if ( boundry > 0 ) {
            String key = token.substring( 1, boundry );
            String format = token.substring( boundry + 1 );
            if ( preprocess ) {
              if ( symbols.containsKey( key ) ) {
                retval.append( symbols.getString( key, format ) );
              } else {
                retval.append( OPEN );
                retval.append( VAR );
                retval.append( key );
                retval.append( PIPE );
                retval.append( format );
                retval.append( CLOSE );
              }
            } else {
              retval.append( symbols.getString( key, format ) );
            }
          } else {
            if ( preprocess ) {
              if ( symbols.containsKey( token.substring( 1 ) ) ) {
                retval.append( symbols.getString( token.substring( 1 ) ) );
              } else {
                retval.append( OPEN );
                retval.append( VAR );
                retval.append( token.substring( 1 ) );
                retval.append( CLOSE );
              }
            } else {
              retval.append( symbols.getString( token.substring( 1 ) ) );
            }
          }
        } else {
          // Must be a class; use the entire tag when parsing

          // the last dotted token is always assumed to be a method name
          int indx = tag.lastIndexOf( DOT );
          if ( indx != -1 ) {
            // we have an object key and a method
            String objectKey = tag.substring( 0, indx );
            String methodToken = tag.substring( indx + 1 );
            String methodName = null;
            String[] arguments = EMPTY_ARGS;

            // get the object by the key
            Object obj = Template.get( objectKey );

            if ( obj != null ) {
              // parse out the method to call - It should be within parentheses
              indx = methodToken.indexOf( OP );
              if ( indx != -1 ) {
                methodName = methodToken.substring( 0, indx );
                String args = methodToken.substring( indx + 1 );

                // parse to the closing parentheses
                indx = args.indexOf( CP );
                if ( indx != -1 ) {
                  args = args.substring( 0, indx );
                }

                if ( StringUtil.isNotBlank( args ) ) {
                  // split the argument portion into separate strings by commas
                  // ignoring any spaces
                  arguments = args.split( ",\\s*" );
                }
              }

              // if the arguments start with a $, resolve them to their values
              for ( int x = 0; x < arguments.length; x++ ) {
                if ( StringUtil.isNotBlank( arguments[x] ) && arguments[x].charAt( 0 ) == VAR ) {
                  arguments[x] = symbols.getString( arguments[x].substring( 1 ) );
                }

                // Handle quoted values especially empty string arguments: ""
                if ( arguments[x].length() > 1 && arguments[x].indexOf( '"' ) > -1 ) {
                  String qval = StringUtil.getQuotedValue( arguments[x] );
                  if ( qval != null ) {
                    arguments[x] = qval;
                  }
                }

              }
              if ( StringUtil.isNotBlank( methodName ) ) {
                // reflect into the object to see if there is the named method 
                // with the correct number of string arguments

                Method method = null;
                Object returned = null;

                // setup the argument signature
                Class<?>[] cArg = new Class[arguments.length];
                for ( int x = 0; x < arguments.length; x++ ) {
                  cArg[x] = arguments[x].getClass();;
                }

                // find the method with the argument signature
                try {
                  method = obj.getClass().getMethod( methodName, cArg );
                } catch ( Exception e1 ) {
                  // silently ignore e1.printStackTrace();
                }

                // if we found a method matching the signature
                if ( method != null ) {
                  // invoke the method
                  try {
                    returned = method.invoke( obj, (Object[])arguments );

                    // If we received a return value, append it
                    if ( returned != null ) {
                      retval.append( returned.toString() );
                    }
                  } catch ( Exception e ) {
                    System.out.println( "Template Class Error:" + e.getClass().getSimpleName() + ":" + e.getMessage() );
                    //e.printStackTrace();
                  }

                } // if we found a method with the signature

              } // if we have a method name to call

            } else {
              if ( preprocess ) {
                retval.append( OPEN );
                retval.append( token );
                retval.append( CLOSE );
              }
            } // if object with the name is found

          } else {
            // we just have an object

            // get the object by the key
            Object obj = Template.get( token );

            // If we have an object with that name call its toString method
            if ( obj != null ) {
              retval.append( obj.toString() );
            }

          } // object key 

        } // variable or object token?

      } // while parser not EOF
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
      return toString( this, symbols, classCache, false );
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
   * @param preprocess to to leave unresolved variables in place, false 
   *        replaces unresolved variables with an empty string.
   *
   * @return a string representing the fully-resolved template.
   *
   * @throws TemplateException
   */
  public static String toString( Template template, SymbolTable symbols, Hashtable cache, boolean preprocess ) throws TemplateException {
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
              buffer.append( resolve( tag, symbols, cache, preprocess ) );
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




  /**
   * Resolve the template string with the given symbol table, but leave the 
   * unresolved variables in the template.
   * 
   * <p>This enables several passes through the template with different symbol 
   * tables, each contributing its own symbols to the result. This implies 
   * that the final pass through the symbol table should be with the resolve 
   * method to ensure all the template markup is removed.
   * 
   * @param text the template text
   * @param symbols the symbol table to use in resolving the variables
   * 
   * @return the resulting string with only the variables existing in the 
   *         given table resolved and the rest left unchanged.
   */
  public static String preProcess( String text, SymbolTable symbols ) {
    if ( symbols != null && text != null ) {
      return new Template( text, symbols ).preProcess();
    } else {
      return text;
    }
  }




  /**
   * Resolve this template with the given symbol table leaving the unresolved 
   * variables in the template.
   * 
   * @return the resulting string with only the variables existing in the 
   *         given table resolved and the rest left unchanged.
   */
  private String preProcess() {
    try {
      // call our static method with our instance attributes
      return toString( this, symbols, classCache, true );
    } catch ( TemplateException te ) {
      System.err.println( te.getMessage() );
      System.err.println( te.getContext() );

      throw new IllegalArgumentException( "Parser error" );
    }
  }

}