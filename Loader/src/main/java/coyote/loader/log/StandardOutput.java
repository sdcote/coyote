/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.io.OutputStreamWriter;


/**
 * StandardOutput is an implementation of Logger that extends ConsoleAppender 
 * and specifically writes to STDOUT.
 * 
 * <p>It can be configured thusly:<pre>
 * "StandardOutput": { "categories": "info, notice" }</pre>
 */
public class StandardOutput extends ConsoleAppender implements Logger {

  /**
   * Default constructor.
   */
  public StandardOutput() {
    super(new OutputStreamWriter(System.out), 0);
  }




  /**
   * Construct a WriterLogger that writes to STDOUT with an initial mask
   * value.
   *
   * @param mask The initial mask value.
   */
  public StandardOutput(final long mask) {
    super(new OutputStreamWriter(System.out), mask);
  }
}
