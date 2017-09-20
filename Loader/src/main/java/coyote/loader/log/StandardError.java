/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.io.OutputStreamWriter;


/**
 * StandardError is an implementation of Logger that extends ConsoleAppender 
 * and specifically writes to STDERR.
 * 
 * <p>It can be configured thusly:<pre>
 * "StandardError": { "categories": "warn, error, fatal" }</pre>
 */
public class StandardError extends ConsoleAppender {

  /**
   * Constructor StandardError
   */
  public StandardError() {
    super( new OutputStreamWriter( System.err ), 0 );
  }




  /**
   * Construct a WriterLogger that writes to STDERR with an initial mask
   * value.
   *
   * @param mask The initial mask value.
   */
  public StandardError( final long mask ) {
    super( new OutputStreamWriter( System.err ), mask );
  }
}