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
package coyote.commons.zip;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Accepts all files.
 */
public class AllFilesFilenameFilter implements FilenameFilter {

  /**
   * Method accept
   * 
   * @param dir
   * @param name
   * @return true always
   */
  @Override
  public boolean accept( final File dir, final String name ) {
    return true;
  }
}