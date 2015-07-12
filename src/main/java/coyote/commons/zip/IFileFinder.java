/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.commons.zip;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;


/**
 * Instances of classes that implement this interface provide various ways of
 * recursively finding all files in a directory.
 * 
 * @version $Revision:$
 */
public interface IFileFinder {

  /**
   * Method accumulateFiles
   * 
   * @param src
   * @param fileList
   */
  void accumulateFiles( File src, Collection fileList );




  /**
   * Method accumulateFiles
   * 
   * @param src
   * @param fileList
   * @param filter
   */
  void accumulateFiles( File src, Collection fileList, FilenameFilter filter );




  /**
   * Method setBaseDirectory
   * 
   * @param baseDir
   */
  void setBaseDirectory( File baseDir );
}