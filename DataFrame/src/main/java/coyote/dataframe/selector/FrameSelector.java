/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dataframe.selector;

import java.util.ArrayList;
import java.util.List;

import coyote.commons.SegmentFilter;
import coyote.dataframe.DataFrame;


/**
 * Field Selectors allow for the quick retrieval of data from the hierarchy of
 * DataFrames.
 */
public class FrameSelector extends AbstractSelector {

  /**
   * Create a selector with the given expression.
   * 
   * @param expression The segment filter expression to use for all selections
   */
  public FrameSelector( final String expression ) {
    filter = new SegmentFilter( expression );
  }




  /**
   * Count how many frame matches there are.
   * 
   * @param frame The dataframe containing the source of the data
   * 
   * @return the number of frames matching the current expression
   */
  public int count( final DataFrame frame ) {
    final List<DataFrame> retval = new ArrayList<DataFrame>();
    if ( frame != null ) {
      recurseFrames( frame, null, retval );
    }
    return retval.size();
  }




  /**
   * Return a list of DataFrames from the given DataFrame matching the 
   * currently set expression.
   * 
   * @param frame The dataframe containing the source of the data
   * 
   * @return a non-null list of DataFrames which match the currently set expression
   */
  public List<DataFrame> select( final DataFrame frame ) {
    final List<DataFrame> retval = new ArrayList<DataFrame>();
    if ( frame != null ) {
      recurseFrames( frame, null, retval );
    }
    return retval;
  }

}
