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
import java.util.Collection;


/**
 * Provides an implementation of the file finder.
 */
public class DefaultFileFinder implements IFileFinder {
  private File baseDir;
  private String baseAbsPath;
  private int pathStart;
  private final boolean includeDirs;




  /**
   * Constructor DefaultFileFinder
   */
  public DefaultFileFinder() {
    this(false);
  }




  /**
   * Constructor DefaultFileFinder
   * 
   * @param includeDirs flag to include subdirectories
   */
  public DefaultFileFinder(final boolean includeDirs) {
    this.includeDirs = includeDirs;
  }




  /**
   * Method accumulateFiles
   * 
   * @param src source file
   * @param fileList the collection of files to accumulate
   */
  @Override
  public void accumulateFiles(final File src, final Collection fileList) {
    accumulateFiles(src, fileList, null);
  }




  /**
   * Method accumulateFiles
   *
   * @param src source file
   * @param fileList the collection of files to accumulate
   * @param filter filename filter to select the file to accumulate
   */
  @Override
  public void accumulateFiles(final File src, final Collection fileList, final FilenameFilter filter) {
    if (src.isDirectory() && src.canRead()) {
      if (includeDirs) {
        fileList.add(getPath(src));
      }

      String[] children = null;

      if (filter != null) {
        children = src.list(filter);
      } else {
        children = src.list();
      }

      File child = null;

      for (final String element : children) {
        child = new File(src, element);

        if (includeDirs || !child.isDirectory()) {
          fileList.add(getPath(child));
        }

        accumulateFiles(child, fileList, filter);
      }
    }
  }




  /**
   * Method getPath
   * 
   * @param f the file from which the path is desired
   * 
   * @return the path to the file
   */
  protected String getPath(final File f) {
    final String absPath = f.getAbsolutePath();

    if (baseDir != null) {
      if (absPath.startsWith(baseAbsPath)) {
        return absPath.substring(pathStart);
      }
    }

    return f.getPath();
  }




  /**
   * Method setBaseDirectory
   * 
   * @param baseDir the base directory for this finder
   */
  @Override
  public void setBaseDirectory(final File baseDir) {
    this.baseDir = baseDir;
    // since these are invariant, record the absolute path of the base and the
    // start of the relative path for any file located under that path
    baseAbsPath = this.baseDir.getAbsolutePath();
    pathStart = baseAbsPath.length() + 1;
  }

}