/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx;

import coyote.dataframe.DataFrame;


/**
 * 
 */
public interface FrameReader extends ConfigurableComponent {

  /**
   * Read and return a frame.
   * 
   * @param context the context containing data related to the current transaction.
   */
  public DataFrame read( TransactionContext context );




  public boolean eof();

}
