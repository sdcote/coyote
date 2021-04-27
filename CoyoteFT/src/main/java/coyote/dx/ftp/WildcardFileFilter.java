/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.ftp;

import java.util.ArrayList;
import java.util.Stack;


/**
 * 
 */
public class WildcardFileFilter implements FileFilter {

  private static final int NOT_FOUND = -1;
  private static final boolean IGNORECASE = true;
  private String pattern = "*";




  protected static int checkIndexOf(final String str, final int strStartIndex, final String search) {
    final int endIndex = str.length() - search.length();
    if (endIndex >= strStartIndex) {
      for (int i = strStartIndex; i <= endIndex; i++) {
        if (checkRegionMatches(str, i, search)) {
          return i;
        }
      }
    }
    return -1;
  }




  protected static boolean checkRegionMatches(final String str, final int strStartIndex, final String search) {
    return str.regionMatches(IGNORECASE, strStartIndex, search, 0, search.length());
  }




  protected static String[] splitOnTokens(final String text) {

    if ((text.indexOf('?') == NOT_FOUND) && (text.indexOf('*') == NOT_FOUND)) {
      return new String[]{text};
    }

    final char[] array = text.toCharArray();
    final ArrayList<String> list = new ArrayList<String>();
    final StringBuilder buffer = new StringBuilder();
    char prevChar = 0;
    for (final char ch : array) {
      if ((ch == '?') || (ch == '*')) {
        if (buffer.length() != 0) {
          list.add(buffer.toString());
          buffer.setLength(0);
        }
        if (ch == '?') {
          list.add("?");
        } else if (prevChar != '*') {
          list.add("*");
        }
      } else {
        buffer.append(ch);
      }
      prevChar = ch;
    }
    if (buffer.length() != 0) {
      list.add(buffer.toString());
    }

    return list.toArray(new String[list.size()]);
  }




  /**
  * Checks a filename to see if it matches the specified wildcard matcher.
  * 
  * <p>The wildcard matcher uses the characters '?' and '*' to represent a
  * single or multiple (zero or more) wildcard characters.</p>
  *
  * @param filename  the filename to match on
  * @param wildcardMatcher  the wildcard string to match against
  * 
  * @return true if the filename matches the wildcard string
  */
  public static boolean wildcardMatch(final String filename, final String wildcardMatcher) {
    if ((filename == null) && (wildcardMatcher == null)) {
      return true;
    }
    if ((filename == null) || (wildcardMatcher == null)) {
      return false;
    }

    final String[] wcs = splitOnTokens(wildcardMatcher);
    boolean anyChars = false;
    int textIdx = 0;
    int wcsIdx = 0;
    final Stack<int[]> backtrack = new Stack<int[]>();

    // loop around a backtrack stack, to handle complex * matching
    do {
      if (backtrack.size() > 0) {
        final int[] array = backtrack.pop();
        wcsIdx = array[0];
        textIdx = array[1];
        anyChars = true;
      }

      // loop whilst tokens and text left to process
      while (wcsIdx < wcs.length) {

        if (wcs[wcsIdx].equals("?")) {
          // ? so move to next text char
          textIdx++;
          if (textIdx > filename.length()) {
            break;
          }
          anyChars = false;

        } else if (wcs[wcsIdx].equals("*")) {
          // set any chars status
          anyChars = true;
          if (wcsIdx == (wcs.length - 1)) {
            textIdx = filename.length();
          }

        } else {
          // matching text token
          if (anyChars) {
            // any chars then try to locate text token
            textIdx = checkIndexOf(filename, textIdx, wcs[wcsIdx]);
            if (textIdx == NOT_FOUND) {
              // token not found
              break;
            }
            final int repeat = checkIndexOf(filename, textIdx + 1, wcs[wcsIdx]);
            if (repeat >= 0) {
              backtrack.push(new int[]{wcsIdx, repeat});
            }
          } else {
            // matching from current position
            if (!checkRegionMatches(filename, textIdx, wcs[wcsIdx])) {
              break; // didn't match
            }
          }

          // matched text token, move text index to end of matched token
          textIdx += wcs[wcsIdx].length();
          anyChars = false;
        }

        wcsIdx++;
      }

      // full match
      if ((wcsIdx == wcs.length) && (textIdx == filename.length())) {
        return true;
      }

    }
    while (backtrack.size() > 0);

    return false;
  }




  /**
   * @see coyote.dx.ftp.FileFilter#accept(coyote.dx.ftp.RemoteFile)
   */
  @Override
  public boolean accept(final RemoteFile file) {
    return wildcardMatch(file.getName(), getPattern());
  }




  /**
   * @return the pattern used to match file names
   */
  public String getPattern() {
    return pattern;
  }




  /**
   * @param pattern the pattern to be used in matching file names
   */
  public void setPattern(final String pattern) {
    this.pattern = pattern;
  }
}
