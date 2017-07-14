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

}
