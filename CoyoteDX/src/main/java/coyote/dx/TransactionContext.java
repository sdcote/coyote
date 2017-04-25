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
 * This is a component responsible for holding data involved with the 
 * extraction, transformation and loading of single row of data. 
 */
public class TransactionContext extends OperationalContext {
  private DataFrame sourceFrame = null;
  private DataFrame targetFrame = null;
  private DataFrame workingFrame = null;

  private boolean lastFrame = false;




  public TransactionContext( TransformContext context ) {
    this.parent = context;
  }




  /**
   * @return the sourceFrame
   */
  public DataFrame getSourceFrame() {
    return sourceFrame;
  }




  /**
   * Set the source frame.
   * 
   * <p>This also makes a copy (clone) of the source frame and sets it as the 
   * working frame. Any time the source frame is set, a new working frame 
   * should be created as well since it represents a new starting point.</p>
   * 
   * @param sourceFrame the sourceFrame to set
   */
  public void setSourceFrame( DataFrame sourceFrame ) {
    this.sourceFrame = sourceFrame;
    this.workingFrame = (DataFrame)sourceFrame.clone();
  }




  /**
   * @return the targetFrame
   */
  public DataFrame getTargetFrame() {
    return targetFrame;
  }




  /**
   * @param targetFrame the targetFrame to set
   */
  public void setTargetFrame( DataFrame targetFrame ) {
    this.targetFrame = targetFrame;
  }




  /**
   * @return the workingFrame
   */
  public DataFrame getWorkingFrame() {
    return workingFrame;
  }




  /**
   * @param workingFrame the workingFrame to set
   */
  public void setWorkingFrame( DataFrame workingFrame ) {
    this.workingFrame = workingFrame;
  }




  /**
   * @return true if this is the last frame in the stream, false if more frames are coming.
   */
  public boolean isLastFrame() {
    return lastFrame;
  }




  /**
   * @param isLast true if this is the last frame in the stream
   */
  public void setLastFrame( boolean isLast ) {
    lastFrame = isLast;
  }

}
