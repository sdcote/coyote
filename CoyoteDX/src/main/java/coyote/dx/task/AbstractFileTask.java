/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;

import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;


/**
 * This contains access to utility functions any file-based operation may find
 * useful.
 */
public abstract class AbstractFileTask extends AbstractTransformTask {

  protected static final int STREAM_BUFFER_LENGTH = 1024;




  /**
   * @return the absolute path to the job directory or "" if not set.
   */
  protected String getJobDir() {
    String retval = "";
    try {
      retval= getJobDirectory().getAbsolutePath();
    } catch (Throwable t) {}
    return retval;
  }




  /**
   * @return the string for the SOURCE configuration attribute, otherwise use 
   *         FILENAME. May be null
   */
  protected String getSourceOrFile() {
    return getTagOrFile(ConfigTag.SOURCE);
  }




  /**
   * @return the string for the TARGET configuration attribute, otherwise use 
   *         FILENAME. May be null
   */
  protected String getTargetOrFile() {
    return getTagOrFile(ConfigTag.TARGET);
  }




  private String getTagOrFile(String tag) {
    final String retval = getString(tag);
    if (StringUtil.isNotBlank(retval)) {
      return retval;
    } else {
      return getString(ConfigTag.FILE);
    }
  }




  /**
   * Get a file reference which should exist.
   * 
   * @param source the name of the file to create
   * @return a fully-qualified file, possibly created in the job directory if it is not fully-qualified
   */
  protected File getExistingFile(String source) {
    final File file = new File(source);
    File retval = null;
    if (!file.exists()) {
      if (!file.isAbsolute()) {
        retval = new File(getJobDir(), source);
      }
    } else {
      retval = file;
    }
    return retval;
  }




  /**
   * Get a file reference and resolve it to the job directory if it is not 
   * absolute.
   * 
   * @param name the file name to create
   * @return a fully-qualified file, possibly created in the job directory if it is not fully-qualified
   */
  protected File getAbsoluteFile(String name) {
    final File file = new File(name);
    File retval = null;
    if (!file.isAbsolute()) {
      retval = new File(getJobDir(), name);
    } else {
      retval = file;
    }
    return retval;
  }

}
