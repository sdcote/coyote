/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader.log;

import java.io.OutputStreamWriter;


/**
 * SysErrAppender is an implementation of Logger that extends LoggerBase and
 * defines event() to write the event to a Writer.
 */
public class SysErrAppender extends ConsoleAppender {

  /**
   * Constructor SysErrAppender
   */
  public SysErrAppender() {
    super( new OutputStreamWriter( System.err ), 0 );
  }




  /**
   * Construct a WriterLogger that writes to System.out with an initial mask
   * value.
   *
   * @param mask The initial mask value.
   */
  public SysErrAppender( final long mask ) {
    super( new OutputStreamWriter( System.err ), mask );
  }
}