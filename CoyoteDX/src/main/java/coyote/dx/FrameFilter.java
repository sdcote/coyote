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

import coyote.dx.context.TransactionContext;


/**
 * Filter as in filter out.  If the frame matches the filter, it will be 
 * removed from the transformation stream.
 * 
 * <p>Filters actively modify (transform) the read-in data, removing either 
 * individual fields or the entire record from the transaction context.
 */
public interface FrameFilter extends ConfigurableComponent {

  /**
   * Process the given transaction context, removing any data as directed by 
   * this components configuration and logic.
   * 
   * <p>Some filters will sanitize data, removing sensitive fields from the 
   * working record.
   * 
   * <p>Other filters may remove the entire working frame if the record is not 
   * to be processed. In such cases, the rest of the processing is skipped and 
   * the next record is read in by the reader.
   * 
   * <p>The method has a "continue" flag that is returned based on the result 
   * of processing. If true is returned, the remaining filters will be called. 
   * If false is returned, the filter check will not continue. The most common 
   * reason why false is returned is when the entire working frame has been 
   * removed and further filtering is unnecessary. Another, less common reason 
   * is when the filter determines that the working record should be accepted 
   * unconditionally, regardless of what other filters may indicate.  
   * 
   * @param context the transaction context to process.
   * 
   * @return true to continue processing remaining filters, false to skip any remaining filters (early exit). 
   */
  public boolean process( TransactionContext context );
  


  /**
   * @return true if this filter is enabled to run, false if the filter is not 
   *         to be processed
   */
  public boolean isEnabled();




  /**
   * @param flag true to enable this task, false to prevent it from being
   *        processed.
   */
  public void setEnabled( boolean flag );


}
