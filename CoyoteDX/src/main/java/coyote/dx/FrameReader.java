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
package coyote.dx;

import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;


/**
 * All readers must implement this interface to be used in the transform engine.
 */
public interface FrameReader extends ConfigurableComponent {

  /**
   * Read and return a frame.
   * 
   * <p>It is possible to return a null frame. This may be caused by a timeout
   * waiting for data to arrive. In such cases, the engine will continue the 
   * loop, skipping any frame processing. The loop will only be exited if the 
   * transform context is in error or if the reader returns true on the call 
   * to check {@code eof()}.
   * 
   * @param context the context containing data related to the current transaction.
   * 
   * @return the dataframe containing the data record, null if no record was read 
   */
  public DataFrame read( TransactionContext context );




  /**
   * @return true if there are no more records to be read, false to keep 
   *         reading.
   */
  public boolean eof();

}
