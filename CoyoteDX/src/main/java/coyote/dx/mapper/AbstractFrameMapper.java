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
package coyote.dx.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.Component;
import coyote.dx.TransformContext;


/**
 * Represents the base call for all the mappers.
 */
public abstract class AbstractFrameMapper extends AbstractConfigurableComponent implements Component {
  /** An insertion ordered list of target fields to be written to the target frame */
  List<SourceToTarget> fields = new ArrayList<SourceToTarget>();




  /**
   * @see coyote.dx.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    this.context = context;

  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

  }

  //

  //

  /**
   * 
   */
  protected class SourceToTarget {
    private final String sourceName;
    private final String targetName;




    public SourceToTarget( String source, String target ) {
      sourceName = source;
      targetName = target;
    }




    /**
     * @return the target field name
     */
    public String getTargetName() {
      return targetName;
    }




    /**
     * @return the source field name
     */
    public String getSourceName() {
      return sourceName;
    }

  }

}
