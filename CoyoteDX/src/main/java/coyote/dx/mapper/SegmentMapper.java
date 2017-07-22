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
package coyote.dx.mapper;

import java.util.HashMap;
import java.util.Map;

import coyote.commons.SegmentFilter;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.ConfigTag;
import coyote.dx.FrameMapper;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * This is a mapper which scans the working frame for matches and finds 
 * matching data to place in the target frame. This will append data to any 
 * existing data in the target frame delimited with a specific string.
 */
public class SegmentMapper extends AbstractFrameMapper implements FrameMapper {
  private static final String DEFAULT_DELIMITER = ", ";
  private static final String IGNORE_BLANKS = "IgnoreBlanks";
  private String delimiter = DEFAULT_DELIMITER;
  private boolean ignoreBlanks = true;

  private Map<SegmentFilter, String> filters = new HashMap<SegmentFilter, String>();




  /**
   * @see coyote.dx.mapper.AbstractFrameMapper#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( Config cfg ) throws ConfigurationException {
    super.setConfiguration( cfg );

    for ( SourceToTarget mapping : fields ) {
      if ( StringUtil.isBlank( mapping.getSourceName() ) || StringUtil.isBlank( mapping.getTargetName() ) ) {
        throw new ConfigurationException( "Invalid Field '" + mapping + "'" );
      }
      try {
        filters.put( new SegmentFilter( mapping.getSourceName() ), mapping.getTargetName() );
      } catch ( Exception e ) {
        throw new ConfigurationException( "Cannot configure segment filter mapping: " + e.getMessage() );
      }
    }

    // Check if we are to ignore blank values in the source frame when appending data to the target
    if ( cfg.containsIgnoreCase( IGNORE_BLANKS ) ) {
      try {
        ignoreBlanks = cfg.getAsBoolean( IGNORE_BLANKS );
      } catch ( DataFrameException e ) {
        Log.info( "Ignore Blanks setting not valid " + e.getMessage() );
        ignoreBlanks = true;
      }
    }
    if ( this.isIgnoringBlankSourceData() )
      Log.debug( "Ignoring blank source values when appending data to target" );

    // configure the delimiter
    if ( cfg.containsIgnoreCase( ConfigTag.DELIMITER ) ) {
      delimiter = cfg.getString( ConfigTag.DELIMITER );
      Log.debug( "Using a target delimiter of '" + delimiter + "'" );
    }

  }




  /**
   * @see coyote.dx.FrameMapper#process(coyote.dx.context.TransactionContext)
   */
  @Override
  public void process( TransactionContext context ) throws MappingException {

    if ( filters.size() > 0 ) {
      DataField targetField;
      SegmentFilter filter;
      String target;

      for ( Map.Entry<SegmentFilter, String> entry : filters.entrySet() ) {
        filter = entry.getKey();
        target = entry.getValue();

        for ( DataField sourceField : context.getWorkingFrame().getFields() ) {
          if ( filter.matches( sourceField.getName() ) ) {
            targetField = (DataField)context.getTargetFrame().getField( target );
            if ( targetField == null ) {
              targetField = (DataField)context.getWorkingFrame().getField( sourceField.getName() ).clone();
              targetField.setName( target );
              context.getTargetFrame().getFields().add( targetField );
            } else {
              // append the data, causing the target type to be String
              String targetFieldData = targetField.getStringValue();
              String sourceFieldData = sourceField.getStringValue();
              if ( sourceFieldData != null ) {
                if ( isIgnoringBlankSourceData() && StringUtil.isBlank( sourceFieldData ) ) {
                  continue;
                } else {
                  targetFieldData = targetFieldData.concat( delimiter ).concat( sourceFieldData );
                }
              }
              context.getTargetFrame().replace( target, targetFieldData );
            }
          } // match
        } // for each source field
      } // for each filter

    } else {
      // if no field map, just perform a straight clone of the working frame
      context.setTargetFrame( (DataFrame)context.getWorkingFrame().clone() );
    }

  }




  /**
   * @return
   */
  private boolean isIgnoringBlankSourceData() {
    return ignoreBlanks;
  }

}
