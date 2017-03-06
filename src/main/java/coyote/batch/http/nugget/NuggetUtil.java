/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http.nugget;

import java.util.ArrayList;
import java.util.Map;

import coyote.commons.network.http.IHTTPSession;


/**
 * Utilities for web nuggets
 */
public class NuggetUtil {

  /**
   * Return each portion of the path as and array of tokens.
   * 
   * @param path the path to split
   * 
   * @return the array of tokens composing the path, may be empty but never null.
   */
  public static String[] getPathArray( final String path ) {
    final String array[] = path.split( "/" );
    final ArrayList<String> pathArray = new ArrayList<String>();

    for ( final String s : array ) {
      if ( s.length() > 0 ) {
        pathArray.add( s );
      }
    }

    return pathArray.toArray( new String[] {} );
  }




  /**
   * Get debug text for a set of params and a session
   * 
   * @param urlParams
   * @param session
   * 
   * @return text suitable for inclusion on an HTML page.
   */
  public static String getText( Map<String, String> urlParams, IHTTPSession session ) {
    String text = "<html><body>Debug handler. Method: " + session.getMethod().toString() + "<br>";
    text += "<h1>Uri parameters:</h1>";
    for ( Map.Entry<String, String> entry : urlParams.entrySet() ) {
      String key = entry.getKey();
      String value = entry.getValue();
      text += "<div> Param: " + key + "&nbsp;Value: " + value + "</div>";
    }
    text += "<h1>Query parameters:</h1>";
    for ( Map.Entry<String, String> entry : session.getParms().entrySet() ) {
      String key = entry.getKey();
      String value = entry.getValue();
      text += "<div> Query Param: " + key + "&nbsp;Value: " + value + "</div>";
    }
    text += "</body></html>";

    return text;
  }

}
