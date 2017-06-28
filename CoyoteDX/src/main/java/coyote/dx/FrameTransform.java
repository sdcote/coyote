package coyote.dx;

import coyote.dataframe.DataFrame;


/**
 * Frame transforms allow the modification of DataField names and values in a 
 * given DataFrame. It also allows new fields to be created resulting in an 
 * enrichment of data. Alternately, Frame Transforms can remove data to protect
 * sensitive information.
 * 
 * <p>One of the earliest uses was for the encryption of sensitive fields 
 * prior to storage or transmission.
 * 
 * <p>The transform has free-reign on what it does in this method. It can be 
 * used to perform complex business logic processing and may represent a 
 * "black box" in which the entire integration is performed.
 */
public interface FrameTransform extends ConfigurableComponent {

  /**
   * Transform the given frame and return the transform frame.
   * 
   * <p>It is acceptable to perform an in-place transformation and not create a 
   * new data frame on each call. Just know that the return value of this 
   * method will unconditionally be set in the transaction context as the new 
   * working frame and the next operation on the working frame will use this
   * methods return value. In other words, the returned frame will become the
   * the new working frame for the transaction context.
   * 
   * <p>If this method throws an exception, the transaction context will be 
   * placed in error and the dataframe will not be written. The transform will 
   * just read the next record.
   * 
   * @param frame The frame to transform
   * 
   * @return the transformed frame which will be placed in the working frame of 
   *         the transaction context.
   * 
   * @throws TransformException if any problems occurred during transformation
   *         causing the transaction to be aborted.
   */
  public DataFrame process( DataFrame frame ) throws TransformException;

}
