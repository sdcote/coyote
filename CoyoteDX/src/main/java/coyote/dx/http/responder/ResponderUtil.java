/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.util.ArrayList;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPSession;


/**
 * Utilities for HTTP responders
 */
public class ResponderUtil {

  /**
   * Return each portion of the path as and array of tokens.
   * 
   * @param path the path to split
   * 
   * @return the array of tokens composing the path, may be empty but never null.
   */
  public static String[] getPathArray(final String path) {
    final String array[] = path.split("/");
    final ArrayList<String> pathArray = new ArrayList<String>();

    for (final String s : array) {
      if (s.length() > 0) {
        pathArray.add(s);
      }
    }

    return pathArray.toArray(new String[]{});
  }




  /**
   * Get debug text for a set of params and a session
   * 
   * @param urlParams
   * @param session
   * 
   * @return text suitable for inclusion on an HTML page.
   */
  public static String getDebugText(Map<String, String> urlParams, HTTPSession session) {

    final StringBuilder text = new StringBuilder("<html><body>");

    text.append("<h2>");
    text.append(session.getMethod().toString().toUpperCase());
    text.append(": ");
    text.append(session.getUri());
    text.append("</h2>\r\n");

    if (urlParams.size() > 0) {
      text.append("<h3>Uri Parameters:</h3>\r\n<ul>");
      for (final Map.Entry<String, String> entry : urlParams.entrySet()) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        text.append("<li>URI Param '");
        text.append(key);
        text.append("' = '");
        text.append(value);
        text.append("'</li>");
      }
      text.append("</ul>\r\n");
    } else {
      text.append("<p>No parameters parsed from URI</p>\r\n");
    }

    final Map<String, String> queryParams = session.getParms();
    if (queryParams.size() > 0) {
      text.append("<h3>Query Parameters:</h3>\r\n<ul>");
      for (final Map.Entry<String, String> entry : queryParams.entrySet()) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        text.append("<li>Query String Param '");
        text.append(key);
        text.append("' = ");
        text.append(value);
        text.append("</li>");
      }
      text.append("</ul>\r\n");
    } else {
      text.append("<p>No query params in URL</p>\r\n");
    }

    if (StringUtil.isNotEmpty(session.getUserName())) {
      text.append("<h3>Authentication Details</h3>\r\n");
      text.append("<p>Username: ");
      text.append(session.getUserName());
      text.append("<br/>Groups: ");
      text.append(session.getUserGroups());
      text.append("</p>");
    } else {
      text.append("<p>No authenticated user associated with session.</p>");
    }
    text.append("</body></html>");

    return text.toString();
  }

}
