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
package coyote.batch;

/**
 * 
 */
public interface FrameReader extends ConfigurableComponent {

  /**
   * Read a frame into the given transaction context.
   * 
   * <p>The reader should also set the row number in the context as well.</p>
   * 
   * @param context the context into which the source frame should be set.
   */
  public void read( TransactionContext context );




  public boolean eof();

}
