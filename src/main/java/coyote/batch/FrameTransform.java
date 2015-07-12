package coyote.batch;

import coyote.dataframe.DataFrame;


/**
 * Frame transforms allow the modification of DataField names and values in a 
 * given DataFrame. It also allows new fields to be created resulting in an 
 * enrichment of data. Alternately, Frame Transforms can remove data to protect
 * sensitive information.
 * 
 * <p>One of the earliest uses was for the encryption of sensitive fields prior 
 * to storage or transmission.</p>
 */
public interface FrameTransform extends ConfigurableComponent {

  /**
   * Transform the given frame.
   * 
   * @param frame The frame to transform
   * 
   * @return the transformed frame
   * 
   * @throws TransformException if any problems occurred during transformation.
   */
  public DataFrame process( DataFrame frame ) throws TransformException;

}
