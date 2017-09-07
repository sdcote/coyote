/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class DataFrameUtil {

  /**
   * Flatten any hierarchy into a single level.
   * 
   * <p>The field names of child frames are concatenated using the '.' character
   * as is common practice in property file key naming.
   * 
   * @param frame The source of the data
   * 
   * @return a dataframe with its hierarchy flattened to a single level.
   */
  public static DataFrame flatten( DataFrame frame ) {
    DataFrame retval = new DataFrame();
    if ( frame != null )
      recurse( frame, null, retval );
    return retval;
  }




  /**
   * Recurse into the a dataframe, building a target frame as it goes.
   * 
   * <p>The hierarchy of the dataframe is represented in the naming of the 
   * property values using the '.' to delimit each recursion into the frame.
   * 
   * @param source The frame being recursed into, providing data for the target 
   * @param token The current build of the name of the property
   * @param target The frame into which values are placed.
   */
  private static void recurse( DataFrame source, String token, DataFrame target ) {
    for ( int x = 0; x < source.getFieldCount(); x++ ) {
      final DataField field = source.getField( x );
      String fname = field.getName();

      if ( fname == null )
        fname = Integer.toString( x );

      if ( token != null )
        fname = token + "." + fname;

      if ( field.isFrame() ){
        DataFrame childFrame = (DataFrame)field.getObjectValue();
        if( childFrame!= null){
        recurse( childFrame, fname, target );
        }
      } else {
        target.set( fname, field.getObjectValue() );
      }
    } // for each frame
  }




  
}
