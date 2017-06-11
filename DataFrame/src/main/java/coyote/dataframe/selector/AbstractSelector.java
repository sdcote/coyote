/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe.selector;

import java.util.List;

import coyote.commons.SegmentFilter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * Base class for field and frame selectors
 */
public abstract class AbstractSelector {

  /** The segment filter used to search for fields. */
  protected SegmentFilter filter = null;




  /**
   * Recurse onto the frame concatenating the field names according to their 
   * hierarchy and performing a check on the name to see if it matches the set 
   * filter.
   * 
   * @param frame The current frame to check
   * @param token the current value of the concatenated field name
   * 
   * @param results The current set of fields found to have matched the filter
   */
  protected void recurseFields( final DataFrame frame, final String token, final List<DataField> results ) {
    if ( frame != null ) {
      for ( int x = 0; x < frame.getFieldCount(); x++ ) {
        final DataField field = frame.getField( x );
        String fname = field.getName();

        if ( fname == null ) {
          fname = "field" + x;
        }

        if ( token != null ) {
          fname = token + "." + fname;
        }

        if ( field.isFrame() ) {
          recurseFields( (DataFrame)field.getObjectValue(), fname, results );
        } else {
          if ( filter.matches( fname ) ) {
            results.add( field );
          }
        }

      } // for each frame

    } // frame !null

  }




  /**
   * Recurse onto the frame concatenating the field names according to their 
   * hierarchy and performing a check on the name to see if it matches the set 
   * filter.
   * 
   * @param frame The current frame to check
   * @param token the current value of the concatenated field name
   * 
   * @param results The current set of frames found to have matched the filter
   */
  protected void recurseFrames( final DataFrame frame, final String token, final List<DataFrame> results ) {
    if ( frame != null ) {
      for ( int x = 0; x < frame.getFieldCount(); x++ ) {
        final DataField field = frame.getField( x );
        String fname = field.getName();

        if ( fname == null ) {
          fname = "frame" + x;
        }

        if ( token != null ) {
          fname = token + "." + fname;
        }

        if ( field.isFrame() ) {
          if ( filter.matches( fname ) ) {
            results.add( (DataFrame)field.getObjectValue() );
          }
          recurseFrames( (DataFrame)field.getObjectValue(), fname, results );

        } // if frame

      } // for each frame

    } // frame !null

  }

}
