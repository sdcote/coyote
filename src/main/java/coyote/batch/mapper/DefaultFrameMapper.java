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
package coyote.batch.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.ConfigurationException;
import coyote.batch.FrameMapper;
import coyote.batch.TransactionContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;


/**
 * This is the default mapper which simply copies fields from the source frame 
 * with one name to the target frame with another.
 */
public class DefaultFrameMapper extends AbstractFrameMapper implements FrameMapper {

  /** The logger for the class */
  final Logger log = LoggerFactory.getLogger( getClass() );




  /**
   * Expects a configuration in the form of "Fields" : { "SourceField" : "TargetField" }
   *  
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // Retrieve the "fields" section from the configuration frame
    DataFrame mapFrame = null;
    try {
      mapFrame = frame.getAsFrame( ConfigTag.FIELDS );
    } catch ( DataFrameException e ) {
      mapFrame = null;
    }

    // If we found the "fields" frame...
    if ( mapFrame != null ) {
      // For each name:value pair, setup a soure:target mapping
      for ( DataField field : mapFrame.getFields() ) {
        if ( StringUtil.isNotBlank( field.getName() ) && field.getValue().length > 0 ) {
          fieldMap.put( field.getName(), field.getObjectValue().toString() );
        }
      }
    } else {
      log.warn( "No {} section in Mapper configuration", ConfigTag.FIELDS );
    }

  }




  /**
   * @see coyote.batch.FrameMapper#process(coyote.batch.TransactionContext)
   */
  @Override
  public void process( TransactionContext context ) throws MappingException {

    if ( fieldMap.size() > 0 ) {
      // for each frame in the map (insertion order)
      for ( String workingName : fieldMap.keySet() ) {
        DataField targetField;
        if ( context.getWorkingFrame().contains( workingName ) ) {
          // clone the named field from the working frame
          targetField = (DataField)context.getWorkingFrame().getField( workingName ).clone();
          // re-name the file to that of the target frame
          targetField.setName( fieldMap.get( workingName ) );
        } else {
          // This is normal, the value could just be missing for this record 
          // only. create a new null frame with the desired name
          targetField = new DataField( workingName, null );
        }
        // place the mapped field in the target data frame for writing 
        context.getTargetFrame().getFields().add( targetField );
      }
    } else {
      // if no field map, just perform a straight clone of the working frame
      context.setTargetFrame( (DataFrame)context.getWorkingFrame().clone() );
    }

  }

}
