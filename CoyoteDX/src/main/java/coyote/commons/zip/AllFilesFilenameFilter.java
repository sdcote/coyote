/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.zip;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Accepts all files.
 */
public class AllFilesFilenameFilter implements FilenameFilter {

  /**
   * Check if the filter should accept the file.
   * 
   * @param dir the directory 
   * @param name the name of the file
   * @return true always
   */
  @Override
  public boolean accept(final File dir, final String name) {
    return true;
  }
}