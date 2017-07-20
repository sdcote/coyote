/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.dx.task;

import java.io.File;

import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.Symbols;


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
    try {
      return getContext().getSymbols().get( Symbols.JOB_DIRECTORY ).toString();
    } catch ( Throwable t ) {}
    return "";
  }




  /**
   * @return the string for the SOURCE configuration attribute, otherwise use 
   *         FILENAME. May be null
   */
  protected String getSourceOrFile() {
    final String source = getString( ConfigTag.SOURCE );
    if ( StringUtil.isNotBlank( source ) ) {
      return source;
    } else {
      return getString( ConfigTag.FILE );
    }
  }




  /**
   * @param source
   * @return
   */
  protected File getFile( String source ) {
    final File file = new File( source );
    File retval = null;
    if ( !file.exists() ) {
      if ( !file.isAbsolute() ) {
        retval = new File( getJobDir(), source );
      }
    } else {
      retval = file;
    }
    return retval;
  }
  
}
