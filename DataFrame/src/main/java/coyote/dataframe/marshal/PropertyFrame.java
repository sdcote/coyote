/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe.marshal;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * This class marshals between Java properties and DataFrames.
 */
public class PropertyFrame {

  /**
   * Create a DataFrame from the given property file.
   * 
   * <p>If the expand argument is true, each of the property names will be 
   * expanded using the '.' (period) character as the delimiter. This will 
   * result in a hierarchical structure. Setting expand to false will result in
   * a flat structure.
   * 
   * @param source The properties to use
   * @param expand True will cause each property to be expanded based on its name
   * 
   * @return a dataframe with the contents of the properties collection.
   */
  @SuppressWarnings("rawtypes")
  public DataFrame marshal( Properties source, boolean expand ) {
    if ( expand ) {
      Map tree = buildPropertyTree( source );
      return new MapFrame().marshal( tree );
    } else {
      DataFrame retval = new DataFrame();
      if ( source != null ) {
        for ( Map.Entry<?, ?> entry : source.entrySet() ) {
          String key = entry.getKey().toString();
          Object value = entry.getValue();
          if ( value != null ) {
            if ( value instanceof Properties ) { // Could this really happen?
              value = marshal( (Properties)value, expand );
            }
            retval.add( key, value );
          }
        }
      }
      return retval;
    }
  }




  /**
   * Create a DataFrame from the given property file with each property name 
   * expanded to provide a hierarchy.
   * 
   * <p>Each of the property names will be expanded using the '.' (period) 
   * character as the delimiter. This will result in a hierarchical 
   * structure.
   * 
   * @param source The properties to use
   * 
   * @return a dataframe with the contents of the properties collection.
   */
  public DataFrame marshal( Properties source ) {
    return this.marshal( source, true );
  }




  /**
   * Create a set of properties from the given dataframe.
   * 
   * <p>The field names of child frames are concatenated using the '.' character
   * as is common practice in property file key naming.
   * 
   * @param frame The source of the data
   * 
   * @return a set of properties representing those in the given frame.
   */
  public Properties marshal( DataFrame frame ) {
    Properties properties = new Properties();
    if ( frame != null )
      recurse( frame, null, properties );
    return properties;
  }




  /**
   * Recurse into the a dataframe, building a property collection as it goes.
   * 
   * <p>The hierarchy of the dataframe is represented in the naming of the 
   * property values using the '.' to delimit each recursion into the frame.
   * 
   * @param frame The frame being recursed into, providing data for the property 
   * @param token The current build of the name of the property
   * @param properties The property into which values are placed.
   */
  private static void recurse( DataFrame frame, String token, Properties properties ) {
    for ( int x = 0; x < frame.getFieldCount(); x++ ) {
      final DataField field = frame.getField( x );
      String fname = field.getName();

      if ( fname == null )
        fname = "field" + x;

      if ( token != null )
        fname = token + "." + fname;

      if ( field.isFrame() )
        recurse( (DataFrame)field.getObjectValue(), fname, properties );
      else
        properties.setProperty( fname, field.getObjectValue().toString() );

    } // for each frame

  }




  /**
   * Build a map of maps and values representing the given properties.
   * 
   * @param p the properties to use
   * 
   * @return map containing strings and other maps representing values and nodes respectively,
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static Map buildPropertyTree( Properties p ) {
    // create the root of the tree
    Map tree = new HashMap();

    // For each of the named properties
    for ( String name : p.stringPropertyNames() ) {

      // Split the name into tokens using the '.' character as a delimiter
      String[] tokens = name.split( "\\." );

      // starting from the root
      Map currentNode = tree;

      // for each of the tokens
      for ( int i = 0, numberOfTokens = tokens.length; i < numberOfTokens; i++ ) {
        String token = tokens[i];

        // get the named object from the current node
        Object v = currentNode.get( token );

        // If there is nothing with that name in the current node
        if ( v == null ) {
          // if we still have more tokens
          if ( i < numberOfTokens - 1 ) {
            // place a new node in the tree named for this token
            Map nextNode = new HashMap();
            currentNode.put( token, nextNode );
            currentNode = nextNode;
          } else {
            // this is the last token in the name, place it in the current node
            currentNode.put( token, p.getProperty( name ) );
          }
        } else {
          // there is something here with that name... 
          // if there are more tokens in the name
          if ( i < numberOfTokens - 1 ) {
            if ( v instanceof Map ) {
              currentNode = (Map)v;
            } else {
              // there is already a value here. This means we need to create a 
              // node here but must somehow preserve the existing value. The 
              // solution is to create a new Map as the nodes value and place 
              // the current value in the new map with the name of VALUE.
              Map nextNode = new HashMap();
              currentNode.put( token, nextNode );
              currentNode = nextNode;
              currentNode.put( "VALUE", v );
            }
          } else {
            // this is the last token so place the value here
            currentNode.put( token, p.getProperty( name ) );
          }
        }
      }
    }
    return tree;
  }




  /**
   * Recursive method to traverse the given map, building JSON as it goes.
   * 
   * @param tree the tree to traverse
   * @param sb the string builder to append JSON markup
   * @param deep depth in the tree for determining indentation
   */
  @SuppressWarnings("rawtypes")
  private static void recurse( Map tree, StringBuilder sb, int deep ) {
    boolean first = true;
    for ( Object key : tree.keySet() ) {
      if ( !first )
        sb.append( ",\n" );
      else
        first = false;
      for ( int t = 0; t < deep; t++ )
        sb.append( "  " );
      sb.append( key + " : " );
      Object v = tree.get( key );
      if ( v instanceof Map ) {
        sb.append( "{\n" );
        recurse( (Map)v, sb, deep + 1 );
        for ( int t = 0; t < deep; t++ )
          sb.append( "  " );
        sb.append( "}" );
      } else {
        sb.append( v );
      }
    }
    sb.append( "\n" );
  }




  /**
   * Return the properties in JSON notation
   *  
   * @param p The properties to use
   * 
   * @return the JSON notation of the given properties
   */
  @SuppressWarnings("rawtypes")
  public static String propertiesToJson( Properties p ) {
    Map tree = buildPropertyTree( p );

    StringBuilder sb = new StringBuilder();
    sb.append( "Properties : {\n" );
    recurse( tree, sb, 1 );
    sb.append( "}" );

    return sb.toString();
  }

}
