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
 * Filter as in filter out.  If the frame matches the filter, it will be 
 * removed from the transformation stream.
 */
public interface FrameFilter extends ConfigurableComponent {

  /**
   * Process the given transaction context, removing any data as directed by 
   * this components configuration and logic.
   * 
   * @param context the transaction context to process.
   * 
   *  @return true to continue processing remaining filters, false to skip any remaining filters (early exit) 
   */
  public boolean process( TransactionContext context );

}
