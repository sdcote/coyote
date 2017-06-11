/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.dataframe.selector;

import java.util.ArrayList;
import java.util.List;

import coyote.commons.SegmentFilter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * Field Selectors allow for the quick retrieval of data from the hierarchy of
 * DataFrames.
 */
public class FieldSelector extends AbstractSelector {

  /**
   * Create a selector with the given expression.
   * 
   * @param expression The segment filter expression to use for all selections
   */
  public FieldSelector( final String expression ) {
    filter = new SegmentFilter( expression );
  }




  /**
   * Count how many field matches there are.
   * 
   * @param frame The dataframe containing the source of the data
   * 
   * @return the number of fields matching the current expression
   */
  public int count( final DataFrame frame ) {
    final List<DataField> retval = new ArrayList<DataField>();
    if ( frame != null ) {
      recurseFields( frame, null, retval );
    }
    return retval.size();
  }




  /**
   * Return a list of DataFields  from the given DataFrame matching the 
   * currently set expression.
   * 
   * @param frame The dataframe containing the source of the data
   * 
   * @return a non-null list of DataFields which match the currently set expression
   */
  public List<DataField> select( final DataFrame frame ) {
    final List<DataField> retval = new ArrayList<DataField>();
    if ( frame != null ) {
      recurseFields( frame, null, retval );
    }
    return retval;
  }

}
